package com.ery.base.support.sys.podo;

import java.io.Serializable;


public class DataSrcPO implements Serializable{

    
    public final static int DS_TYPE_ORACLE = 1;
    public final static int DS_TYPE_MYSQL = 2;
    public final static int DS_TYPE_HBASE = 3;
    public final static int DS_TYPE_FTP = 4;
    public final static int DS_TYPE_HDFS = 5;


    private long DATA_SOURCE_ID;//数据源ID
    private String DATA_SOURCE_NAME;//数据源名称
    private int DATA_SOURCE_TYPE;//数据源类型,对应【POConstant.DS_TYPE_?】

    
    private String DATA_SOURCE_URL;
    private String DATA_SOURCE_USER;//用户【JSON数组】
    private String DATA_SOURCE_PASS;//密码
    private String DATA_SOURCE_DESC;//数据源描述
    
    private String DATA_SOURCE_CFG;
    private int STATE;//状态

    public long getDATA_SOURCE_ID() {
        return DATA_SOURCE_ID;
    }

    public void setDATA_SOURCE_ID(long DATA_SOURCE_ID) {
        this.DATA_SOURCE_ID = DATA_SOURCE_ID;
    }

    public String getDATA_SOURCE_NAME() {
        return DATA_SOURCE_NAME;
    }

    public void setDATA_SOURCE_NAME(String DATA_SOURCE_NAME) {
        this.DATA_SOURCE_NAME = DATA_SOURCE_NAME;
    }

    public int getDATA_SOURCE_TYPE() {
        return DATA_SOURCE_TYPE;
    }

    public void setDATA_SOURCE_TYPE(int DATA_SOURCE_TYPE) {
        this.DATA_SOURCE_TYPE = DATA_SOURCE_TYPE;
    }

    public String getDATA_SOURCE_URL() {
        return DATA_SOURCE_URL;
    }

    public void setDATA_SOURCE_URL(String DATA_SOURCE_URL) {
        this.DATA_SOURCE_URL = DATA_SOURCE_URL;
    }

    public String getDATA_SOURCE_USER() {
        return DATA_SOURCE_USER;
    }

    public void setDATA_SOURCE_USER(String DATA_SOURCE_USER) {
        this.DATA_SOURCE_USER = DATA_SOURCE_USER;
    }

    public String getDATA_SOURCE_PASS() {
        return DATA_SOURCE_PASS;
    }

    public void setDATA_SOURCE_PASS(String DATA_SOURCE_PASS) {
        this.DATA_SOURCE_PASS = DATA_SOURCE_PASS;
    }

    public String getDATA_SOURCE_DESC() {
        return DATA_SOURCE_DESC;
    }

    public void setDATA_SOURCE_DESC(String DATA_SOURCE_DESC) {
        this.DATA_SOURCE_DESC = DATA_SOURCE_DESC;
    }

    public String getDATA_SOURCE_CFG() {
        return DATA_SOURCE_CFG;
    }

    public void setDATA_SOURCE_CFG(String DATA_SOURCE_CFG) {
        this.DATA_SOURCE_CFG = DATA_SOURCE_CFG;
    }

    public int getSTATE() {
        return STATE;
    }

    public void setSTATE(int STATE) {
        this.STATE = STATE;
    }
}
