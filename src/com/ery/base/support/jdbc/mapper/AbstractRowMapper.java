
package com.ery.base.support.jdbc.mapper;

import com.ery.base.support.jdbc.IRowMapper;
import com.ery.base.support.jdbc.JdbcException;
import com.ery.base.support.utils.Utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;


public abstract class AbstractRowMapper<T> implements IRowMapper<T> {
	protected Class<?> clazz = null;

    public AbstractRowMapper(Class<?> clazz){
        this.clazz = clazz;
    }
    
    protected CellDataCall cellCvt = new CellDataCall() {
        @Override
        public Object cvt(Object rs, String columnLabel, int columnIdx) {
            try{
                if(rs instanceof Clob){
                    Clob clob = (Clob)rs;
                    return clob.getSubString(1, (int)clob.length());
                } else if(rs instanceof Blob){
                    Blob blob = (Blob) rs;
                    InputStream in = blob.getBinaryStream();
                    DataContentCode code = blobColumnCode.get(columnLabel);
                    if(DataContentCode.BINARY.equals(code)){
                        byte[] data = new byte[4096];
                        int r = -1;
                        int idx = 0;
                        byte[] tmp = new byte[1024];
                        while ((r=in.read(tmp))!=-1){
                            if(data.length-idx<r){
                                //需要扩容
                                data = Utils.expandVolume(data,r);
                            }
                            System.arraycopy(tmp,0,data,idx,r);
                            idx += r;
                        }
                        in.close();
                        if(idx<data.length){
                            byte[] res = new byte[idx];
                            System.arraycopy(data,0,res,0,idx);
                            return res;
                        }
                        return data;
                    }else{
                        InputStreamReader ins = new InputStreamReader(in,code!=null?code.getCharset():"utf-8");
                        BufferedReader bin = new BufferedReader(ins);
                        StringBuilder res = new StringBuilder();
                        String tmp = null;
                        while ((tmp = bin.readLine())!=null) {
                            res.append(tmp).append("\n");
                        }
                        bin.close();
                        return res.toString();
                    }
                }
            }catch (Exception e){
                throw new JdbcException(e);
            }
            return rs;
        }
    };

    protected String[] columnHeaders = null;//存放列头信息

    //初始列元数据
    protected void initColMeta(ResultSet rs) throws SQLException {
        if(columnHeaders==null){
            ResultSetMetaData metaData = rs.getMetaData();
            int colCount = metaData.getColumnCount();
            columnHeaders = new String[colCount];
            for(int i=1;i<=colCount;i++){
                String columnName = metaData.getColumnLabel(i);
                columnHeaders[i-1] = columnName;
            }
        }
    }

    private Map<String,CellDataCall.DataContentCode> blobColumnCode = new HashMap<String, CellDataCall.DataContentCode>();

    
    public void setBlobColumnCode(String columnLabel,CellDataCall.DataContentCode code){
        blobColumnCode.put(columnLabel,code);
    }

    public String[] getColumnHeaders() {
        return columnHeaders;
    }

    public void setColumnHeaders(String[] columnHeaders) {
        this.columnHeaders = columnHeaders;
    }

    public void setCellCvt(CellDataCall cellCvt) {
        this.cellCvt = cellCvt;
    }
}
