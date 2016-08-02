package com.ery.base.support.jdbc.mapper;

import java.sql.SQLException;


public interface CellDataCall {

    
    Object cvt(Object rs, String columnLabel,int columnIdx);

    //数据内容编码
    public enum DataContentCode{

        //字符数据
        UTF_8("utf-8"),
        GBK("gbk"),
        GB2312("gb2312"),
        ISO_8859_1("iso8859-1"),
        //二进制数据
        BINARY("binary");

        private String charset;
        DataContentCode(String charset) {
            this.charset = charset;
        }

        public String getCharset() {
            return charset;
        }
    }
    
}
