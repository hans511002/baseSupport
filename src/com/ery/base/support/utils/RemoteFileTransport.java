package com.ery.base.support.utils;

import com.enterprisedt.net.ftp.*;
import com.jcraft.jsch.*;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class RemoteFileTransport {
    
    public static final int CONTENT_TYPE_BINARY = 1;
    public static final int CONTENT_TYPE_ASCII = 2;
    
    public static final int TRANSPORT_TYPE_FTP = 1;
    public static final int TRANSPORT_TYPE_SFTP = 2;
    
    public static final int SORT_NONE = 0;
    public static final int SORT_TIME_ASC = 1;
    public static final int SORT_TIME_DESC = -1;

    //基本属性
    private String host;
    private int port;
    private String user;
    private String pass;
    //参数
    private int timeout;
    private String charset;
    private int contentType;
    private int transportType;
    

    //辅助参数
    private boolean inited;
    private FileTransferClient ftp;
    private ChannelSftp sftp;
    private String currentPath;

    public RemoteFileTransport(String host, int port, String user, String pass) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.pass = pass;
        this.timeout = 30000;
        this.charset = "utf-8";
        this.contentType = CONTENT_TYPE_BINARY;
        if(port==21){
            this.transportType = TRANSPORT_TYPE_FTP;
        }else if(port==22){
            this.transportType = TRANSPORT_TYPE_SFTP;
        }else{
            this.transportType = TRANSPORT_TYPE_FTP;
        }
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public int getContentType() {
        return contentType;
    }

    public void setContentType(int contentType) {
        this.contentType = contentType;
    }

    public int getTransportType() {
        return transportType;
    }

    public void setTransportType(int transportType) {
        this.transportType = transportType;
    }

    public synchronized void init() throws Exception{
        if(!inited){
            switch (transportType){
                case TRANSPORT_TYPE_FTP:
                    ftp = new FileTransferClient();
                    ftp.setRemoteHost(host);
                    ftp.setRemotePort(port);
                    ftp.setUserName(user);
                    ftp.setPassword(pass);
                    ftp.setContentType(contentType == CONTENT_TYPE_BINARY ? FTPTransferType.BINARY : FTPTransferType.ASCII);
                    ftp.getAdvancedSettings().setControlEncoding(charset);
                    ftp.setTimeout(timeout);
                    ftp.connect();
                    currentPath = ftp.executeCommand("pwd");
                    break;
                case TRANSPORT_TYPE_SFTP:
                    JSch jSch = new JSch();
                    Session session = jSch.getSession(user,host,port);
                    Properties config = new Properties();
                    config.put("StrictHostKeyChecking", "no");
                    session.setConfig(config);
                    session.setPassword(pass);
                    session.setTimeout(timeout);
                    session.connect();
                    Channel channel = session.openChannel("sftp");
                    sftp = (ChannelSftp)channel;
                    sftp.connect();
                    currentPath = sftp.pwd();
                    break;
            }
            inited = true;
        }
    }

    public void connect() throws Exception {
        if(!isConnected()){
            switch (transportType){
                case TRANSPORT_TYPE_FTP:
                    ftp.connect();
                    break;
                case TRANSPORT_TYPE_SFTP:
                    if(sftp.isClosed()){
                        JSch jSch = new JSch();
                        Session session = jSch.getSession(user,host,port);
                        Properties config = new Properties();
                        config.put("StrictHostKeyChecking", "no");
                        session.setConfig(config);
                        session.setPassword(pass);
                        session.setTimeout(timeout);
                        session.connect();
                        Channel channel = session.openChannel("sftp");
                        sftp = (ChannelSftp)channel;
                    }
                    sftp.connect();
                    break;
            }
        }
    }

    public void disconnect() throws Exception {
        switch (transportType){
            case TRANSPORT_TYPE_FTP:
                ftp.disconnect();
                break;
            case TRANSPORT_TYPE_SFTP:
                sftp.exit();
                sftp.getSession().disconnect();
                break;
        }
    }

    public boolean isConnected(){
        switch (transportType){
            case TRANSPORT_TYPE_FTP:
                return ftp.isConnected();
            case TRANSPORT_TYPE_SFTP:
                return sftp.isConnected();
        }
        return false;
    }

    public void changeDir(String path) throws Exception {
        switch (transportType){
            case TRANSPORT_TYPE_FTP:
                ftp.changeDirectory(path);
                currentPath = ftp.executeCommand("pwd");
                break;
            case TRANSPORT_TYPE_SFTP:
                sftp.cd(path);
                currentPath = sftp.pwd();
                break;
        }
    }
    
    public RemoteFile getFile(String path) throws Exception{
        String dir ;
        String name;
        int i;
        switch (transportType){
            case TRANSPORT_TYPE_FTP:
                i = path.lastIndexOf("/");
                dir = path.substring(0,i);
                name = path.substring(i+1);
                FTPFile[] fs = ftp.directoryList(dir);
                for(FTPFile f : fs){
                    if(f.getName().equals(name)){
                        return new RemoteFile(dir,name,f);
                    }
                }
                break;
            case TRANSPORT_TYPE_SFTP:
                i = path.lastIndexOf("/");
                dir = path.substring(0,i);
                name = path.substring(i+1);
                SftpATTRS att = sftp.stat(path);
                if(att!=null){
                    return new RemoteFile(dir,name,att);
                }
                break;
        }
        return null;
    }

    
    public List<RemoteFile> listFiles(final int mode,String nameFilterRegex) throws Exception{
        List<RemoteFile> arr = null;
        int i;
        switch (transportType){
            case TRANSPORT_TYPE_FTP:
                FTPFile[] fs = ftp.directoryList();
                arr = new ArrayList<RemoteFile>();
                //从第3个开始读，因为第一个是【.】，第二个是【..】
                for(i=2;i<fs.length;i++){
                    FTPFile f = fs[i];
                    if(nameFilterRegex!=null && !"".equals(nameFilterRegex)){
                        Matcher matcher = Pattern.compile(nameFilterRegex).matcher(f.getName());
                        if(!matcher.matches()){
                            continue;
                        }
                    }
                    arr.add(new RemoteFile(currentPath,f.getName(),f));
                }
                break;
            case TRANSPORT_TYPE_SFTP:
                Vector list = sftp.ls(currentPath);
                arr = new ArrayList<RemoteFile>();
                for(i=2;i<list.size();i++){
                    ChannelSftp.LsEntry ls = (ChannelSftp.LsEntry)list.get(i);
                    if(nameFilterRegex!=null && !"".equals(nameFilterRegex)){
                        Matcher matcher = Pattern.compile(nameFilterRegex).matcher(ls.getFilename());
                        if(!matcher.matches()){
                            continue;
                        }
                    }
                    arr.add(new RemoteFile(currentPath,ls.getFilename(),ls.getAttrs()));
                }
                break;
        }
        if(arr!=null && mode!=SORT_NONE && arr.size()>1){
            Collections.sort(arr,new Comparator<RemoteFile>() {
                @Override
                public int compare(RemoteFile o1, RemoteFile o2) {
                    return (int)(o1.getLastModified().getTime()-o2.getLastModified().getTime()) * mode;
                }
            });
        }
        return arr;
    }

    public InputStream getInputStream(String fileName,long skip) throws Exception{
        InputStream inputStream = null;
        switch (transportType){
            case TRANSPORT_TYPE_FTP:
                inputStream = ftp.downloadStream(fileName);
                inputStream.skip(skip);
                break;
            case TRANSPORT_TYPE_SFTP:
                inputStream =  sftp.get(fileName,null,skip);
                break;
        }
        return inputStream;
    }

    public void upload(String dir,String uploadFile) throws Exception {
        File file = new File(uploadFile);
        switch (transportType){
            case TRANSPORT_TYPE_FTP:
                changeDir(dir);
                ftp.uploadFile(uploadFile,file.getName());
                break;
            case TRANSPORT_TYPE_SFTP:
                changeDir(dir);
                sftp.put(new FileInputStream(file),file.getName());
                break;
        }
    }

    public void rename(String oldname,String newname) throws Exception{
        switch (transportType){
            case TRANSPORT_TYPE_FTP:
                ftp.rename(oldname,newname);
                break;
            case TRANSPORT_TYPE_SFTP:
                sftp.rename(oldname,newname);
                break;
        }
    }

    public void deleteFile(String file) throws Exception{
        switch (transportType){
            case TRANSPORT_TYPE_FTP:
                ftp.deleteFile(file);
                break;
            case TRANSPORT_TYPE_SFTP:
                sftp.rm(file);
                break;
        }
    }

    public void mkDir(String path) throws Exception{
        switch (transportType){
            case TRANSPORT_TYPE_FTP:
                ftp.createDirectory(path);
                break;
            case TRANSPORT_TYPE_SFTP:
                sftp.mkdir(path);
                break;
        }
    }
    
    public void deleteDir(String path) throws Exception{
        switch (transportType){
            case TRANSPORT_TYPE_FTP:
                ftp.deleteDirectory(path);
                break;
            case TRANSPORT_TYPE_SFTP:
                sftp.rmdir(path);
                break;
        }
    }
    
    public boolean exists(String path) throws Exception{
        switch (transportType){
            case TRANSPORT_TYPE_FTP:
                return ftp.exists(path);
            case TRANSPORT_TYPE_SFTP:
                try{
                    return sftp.stat(path)!=null;
                }catch (SftpException e){
                    return false;
                }
        }
        return false;
    }

    public class RemoteFile{
        boolean isLink;
        boolean isFile;
        boolean isDir;
        long size;
        Date lastModified;
        String dir;
        String name;
        String path;

        public RemoteFile(String dir,String name,FTPFile f) {
            isLink = f.isLink();
            isDir = f.isDir();
            isFile = f.isFile();
            size = f.size();
            lastModified = f.lastModified();
            this.dir = dir;
            this.name = name;
            this.path = dir+"/"+name;
        }

        public RemoteFile(String dir,String name,SftpATTRS att) {
            isLink = att.isLink();
            isDir = att.isDir();
            isFile = !att.isDir() && !att.isLink();
            size = att.getSize();
            lastModified = Convert.toTime(att.getMtimeString(),"EEE MMM dd HH:mm:ss Z yyyy");
            this.dir = dir;
            this.name = name;
            this.path = dir+"/"+name;
        }

        public String getDir() {
            return dir;
        }

        public void setDir(String dir) {
            this.dir = dir;
        }

        public boolean isLink() {
            return isLink;
        }

        public void setLink(boolean link) {
            isLink = link;
        }

        public boolean isFile() {
            return isFile;
        }

        public void setFile(boolean file) {
            isFile = file;
        }

        public boolean isDir() {
            return isDir;
        }

        public void setDir(boolean dir) {
            isDir = dir;
        }

        public long getSize() {
            return size;
        }

        public void setSize(long size) {
            this.size = size;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Date getLastModified() {
            return lastModified;
        }

        public void setLastModified(Date lastModified) {
            this.lastModified = lastModified;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }
}


