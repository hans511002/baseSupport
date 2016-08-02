package com.ery.base.support.utils;

import com.ery.base.support.log4j.LogUtils;
import org.apache.commons.IFileTransfer;
import org.apache.commons.ftp.edtftpj.FTPTransfer;

import java.io.*;
import java.util.HashMap;
import java.util.Map;


public class FileReadUtils {

    
    public static InputStream getRemoteFileStream(String host,String port,String user,String pass,String filePath){
        IFileTransfer ftp = new FTPTransfer();
        Map<String, Object> mapInit = new HashMap<String, Object>();
        mapInit.put(FTPTransfer.REMOTE_HOST, host);
        mapInit.put(FTPTransfer.REMOTE_POST, port);
        mapInit.put(FTPTransfer.USERNAME, user);
        mapInit.put(FTPTransfer.PASSWORD, pass);
        ftp.init(mapInit);
        try {
            ftp.connectServer();
            return ftp.getInputStream(filePath);
        } catch (Exception e) {
            LogUtils.error("ftp init error:" + user + "@" + host + ":" + port + "");
            throw new RuntimeException("远程文件流构建失败!"+e.getMessage(),e);
        }
    }

    
    public static InputStream getLocalFileStream(File file){
        try {
            return new BufferedInputStream(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            LogUtils.error("local file init error:"+file.getPath());
            throw new RuntimeException("本地文件流构建失败!"+e.getMessage(),e);
        }
    }

    
    public static InputStream getLocalFileStream(String filePath){
        return getLocalFileStream(getFile(filePath));
    }

    
    public static File getFile(String filePath){
        String osName = System.getProperty("os.name").toLowerCase();//操作系统
        String usrDir = System.getProperty("user.dir");//当前程序工作目录
        File file = null;
        if(osName.contains("win")){
            if(filePath.length()>1 && ":".equals(filePath.substring(1, 1))){
                //绝对路径
                file = new File(filePath);
            }else{
                //相对
                file = new File(usrDir,filePath);
            }
        }else{
            if("/".equals(filePath.substring(0,1))){
                //绝对路径
                file = new File(filePath);
            }else{
                //相对
                file = new File(usrDir,filePath);
            }
        }
        return file;
    }

}
