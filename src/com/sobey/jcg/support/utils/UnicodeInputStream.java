package com.sobey.jcg.support.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.nio.charset.Charset;


public class UnicodeInputStream extends InputStream {
    private static final int BOM_SIZE = 4;

    PushbackInputStream internalIn;
    boolean isInited = false;
    String defaultEnc;
    String encoding;
    int headSize;//编码头占位

    public UnicodeInputStream(InputStream in){
        this(in, Charset.defaultCharset().name());
    }

    public UnicodeInputStream(InputStream in, String defaultEnc) {
        internalIn = new PushbackInputStream(in, BOM_SIZE);
        this.defaultEnc = defaultEnc;
    }

    public String getDefaultEncoding() {
        return defaultEnc;
    }

    public String getEncoding() {
        if (!isInited) {
            try {
                init();
            } catch (IOException ex) {
                throw new IllegalStateException("Init method failed.",ex);
            }
        }
        return encoding;
    }
    
    public int headSize(){
        if(!isInited){
            try {
                init();
            } catch (IOException ex) {
                throw new IllegalStateException("Init method failed.",ex);
            }
        }
        return headSize;
    }

    
    protected void init() throws IOException {
        if (isInited)
            return;

        byte bom[] = new byte[BOM_SIZE];
        int n, unread;
        n = internalIn.read(bom, 0, bom.length);
        if ((bom[0] == (byte) 0x00) && (bom[1] == (byte) 0x00)
                && (bom[2] == (byte) 0xFE) && (bom[3] == (byte) 0xFF)) {
            encoding = "UTF-32BE";
            headSize = 4;
        } else if ((bom[0] == (byte) 0xFF) && (bom[1] == (byte) 0xFE)
                && (bom[2] == (byte) 0x00) && (bom[3] == (byte) 0x00)) {
            encoding = "UTF-32LE";
            headSize = 4;
        } else if ((bom[0] == (byte) 0xEF) && (bom[1] == (byte) 0xBB)
                && (bom[2] == (byte) 0xBF)) {
            encoding = "UTF-8";
            headSize = 3;
        } else if ((bom[0] == (byte) 0xFE) && (bom[1] == (byte) 0xFF)) {
            encoding = "UTF-16BE";
            headSize = 2;
        } else if ((bom[0] == (byte) 0xFF) && (bom[1] == (byte) 0xFE)) {
            encoding = "UTF-16LE";
            headSize = 2;
        } else {
            // Unicode BOM mark not found, unread all bytes
            encoding = defaultEnc;
            headSize = 0;
        }
        unread = n - headSize;
        // System.out.println("read=" + n + ", unread=" + unread);

        if (unread > 0)
            internalIn.unread(bom, (n - unread), unread);

        isInited = true;
    }

    public void close() throws IOException {
        // init();
        isInited = true;
        internalIn.close();
    }

    public int read() throws IOException {
        // init();
        isInited = true;
        return internalIn.read();
    }
}
