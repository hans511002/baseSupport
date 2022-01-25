package com.sobey.jcg.support.jdbc;

import com.sobey.jcg.support.log4j.LogUtils;
import com.sobey.jcg.support.utils.ArrayUtils;
import com.sobey.jcg.support.utils.Convert;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Hashtable;



public class DataTable {

    
    public Object[][] rows = null;

    
    public String[] colTypesName;

    
    public String[] colsName;

    
    public int[] colTypes;

    
    public int colsCount = 0;

    
    public int rowsCount = 0;

    
    private Hashtable<String, Integer> colRel = null;


    
    private int posStart = -1;

    private int count = -1;

    
    private boolean isCalTotal = true;

    
    public DataTable() {

    }

    
    public DataTable(ResultSet rs) throws Exception {
        fill(rs);
    }

    
    public DataTable(ResultSet rs, int posStart, int count) throws Exception {
        this.posStart = posStart;
        this.count = count;
        fill(rs);
    }

    public DataTable(ResultSet rs, int posStart, int count, int totalCount) throws Exception {
        this.posStart = posStart;
        this.count = count;
        this.rowsCount = totalCount;
        fill(rs);
    }

    
    public DataTable(ResultSet rs, int posStart, int count
            , boolean isCalTotal) throws Exception {
        this.posStart = posStart;
        this.count = count;
        this.isCalTotal = isCalTotal;
        fill(rs);
    }

    
    public DataTable(int rowsCount, int colsCount) {
        this.colsCount = colsCount;
        this.rowsCount = rowsCount;
        this.rows = new Object[rowsCount][colsCount];
        this.colsName = new String[colsCount];
        this.colTypesName = new String[colsCount];
        this.colTypes = new int[colsCount];
    }

    
    public DataTable(String[] colsName, Object[][] rows) {
        this.colsName = colsName;
        this.rows = rows;
        this.colsCount = colsName.length;
        this.rowsCount = rows.length;
        this.colTypesName = new String[colsName.length];
        this.colTypes = new int[colsName.length];
    }

    
    public int getColIndex(String name) {
        if (colRel == null || colRel.size() == 0) {
            if (colRel == null) {
                colRel = new Hashtable<String, Integer>();
            }
            for (int i = 0; i < colsName.length; i++) {
                colRel.put(colsName[i], i);
            }
        }
        name = name.toUpperCase();
        Integer index = -1;
        index = colRel.get(name);
        if (index == null) {
            index = -1;
        }
        return index;
    }

    
    public void clearColRel() {
        colRel.clear();
        colRel = null;
    }

    
    public boolean fill(ResultSet rs) throws Exception {
        boolean result = false;
        if (this.rows != null) {
            this.clear();
        }
        if (rs != null) {
            try {
                ResultSetMetaData resultSetMetaData = rs.getMetaData();
                colsCount = resultSetMetaData.getColumnCount(); // 取得结果集列数
                if (isCalTotal || count < 0) {
                    rs.last();
                    rowsCount = rs.getRow(); // 取得结果集行数
                    rs.beforeFirst();
                }
                if (count > 0) {
                    rows = new Object[count][colsCount];
                } else {
                    rows = new Object[rowsCount][colsCount];
                }
                int rowindex = 0;
                colTypes = new int[colsCount]; // 保存结果集列类型
                colTypesName = new String[colsCount]; // 保存结果集列类型名
                colsName = new String[colsCount]; // 保存结果集列名
                for (int i = 0; i < colsCount; i++) {
                    colTypes[i] = resultSetMetaData.getColumnType(i + 1);
                    colTypesName[i] = resultSetMetaData.getColumnTypeName(i + 1);
                    colsName[i] = resultSetMetaData.getColumnName(i + 1).toUpperCase();
                }
                Object[] row = null; // 结果集某一行数据
                // 将结果集数据保存到二位数组
                if (count > 0 && posStart > 0) {
                    rs.absolute(posStart);
                }
                int nowCount = 0;
                while (rs.next()) {
                    if (nowCount++ == 0 && count > 0) {
                        row = new Object[colsCount + 1];//最后一格为总数
                        row[row.length - 1] = rowsCount;
                    } else {
                        row = new Object[colsCount];
                    }
                    for (int i = 0; i < colsCount; i++) {
                        row[i] = rs.getObject(i + 1);
                    }
                    rows[rowindex++] = row;
                    if (nowCount == count) {
                        break;
                    }
                }
                if (rows.length > nowCount) {
                    rows= (Object[][]) ArrayUtils.subarray(rows, 0, nowCount);
                }
                result = true;
            } catch (Exception ex) {
                LogUtils.error("RS记录集转换为表格式" + ex.toString(), ex);
                this.clear();
                throw ex;
            }
        }
        return result;
    }

    
    public void clear() {
        this.rows = null;
        this.colTypesName = null;
        this.colsName = null;
        this.colTypes = null;
        this.colsCount = 0;
        this.rowsCount = 0;
        colRel = null;
    }

    
    public Object getObj(int i, int j) {
        if (rows[i][j] == null) {
            return "";
        } else {
            return rows[i][j];
        }
    }

    
    public String getColName(int i) {
        try {
            return colsName[i];
        } catch (Exception ex) {
            return "";
        }
    }

    
    public String getColTypeName(int i) {
        try {
            return colTypesName[i];
        } catch (Exception ex) {
            return "";
        }
    }

    
    public int getColType(int i) {
        try {
            return colTypes[i];
        } catch (Exception ex) {
            return -1;
        }
    }

    
    public Object[] getColAsObj(int col) {
        Object[] result = new Object[this.rowsCount];
        for (int i = 0; i < this.rowsCount; i++) {
            result[i] = this.rows[i][col];
        }
        return result;
    }

    
    public double[] getColAsDbl(int col) {
        double[] result = new double[this.rowsCount];
        for (int i = 0; i < this.rowsCount; i++) {
            result[i] = Double.parseDouble(Convert.toString(this.rows[i][col]));
        }
        return result;
    }

    
    public int[] getColAsInt(int col) {
        int[] result = new int[this.rowsCount];
        for (int i = 0; i < this.rowsCount; i++) {
            result[i] = Integer.parseInt(Convert.toString(this.rows[i][col]));
        }
        return result;
    }

    
    public String[] getColAsString(int col) {
        String[] result = new String[this.rowsCount];
        for (int i = 0; i < this.rowsCount; i++) {
            result[i] = this.CheckNull(this.rows[i][col].toString());
        }
        return result;
    }

    
    public void delCol(int col) {
        if (this.colsCount < col) {
            return;
        }
        String[] tempName;
        this.colsCount--;
        int[] tempColTypes = this.colTypes;
        this.colTypes = new int[this.colsCount];
        this.copy(tempColTypes, this.colTypes, 0, col);
        this.copyPart(tempColTypes, this.colTypes, col + 1, col);
        tempName = this.colsName;
        this.colsName = new String[this.colsCount];
        this.copy(tempName, this.colsName, 0, col);
        this.copyPart(tempName, this.colsName, col + 1, col);
        tempName = this.colTypesName;
        this.colTypesName = new String[this.colsCount];
        this.copy(tempName, this.colTypesName, 0, col);
        this.copyPart(tempName, this.colTypesName, col + 1, col);
        Object[] row = null; // 结果集某一行数据
        for (int i = 0; i < this.rowsCount; i++) {
            row = this.rows[i];
            this.rows[i] = new Object[this.colsCount];
            this.copy(row, this.rows[i], 0, col);
            this.copyPart(row, this.rows[i], col + 1, col);
        }
    }

    
    public void insertCol(int colNum) {
        if (this.colsCount < colNum) {
            return;
        }
        String[] tempName;
        this.colsCount++;
        int[] tempColTypes = this.colTypes;
        this.colTypes = new int[this.colsCount];
        System.arraycopy(tempColTypes, 0, colTypes, 0, colNum);
        System.arraycopy(tempColTypes, colNum, colTypes, colNum + 1,
                tempColTypes.length - colNum);
        // common.copy(tempColTypes, this.ColTypes, 0, colNum);
        // common.copyPart(tempColTypes, this.ColTypes, colNum, colNum);
        tempName = this.colsName;
        this.colsName = new String[this.colsCount];
        System.arraycopy(tempName, 0, colsName, 0, colNum);
        System.arraycopy(tempName, colNum, colsName, colNum + 1,
                tempName.length - colNum);
        tempName = this.colTypesName;
        this.colTypesName = new String[this.colsCount];
        System.arraycopy(tempName, 0, colTypesName, 0, colNum);
        System.arraycopy(tempName, colNum, colTypesName, colNum + 1,
                tempName.length - colNum);
        Object[] row = null; // 结果集某一行数据
        for (int i = 0; i < this.rowsCount; i++) {
            row = this.rows[i];
            this.rows[i] = new Object[this.colsCount];
            System.arraycopy(row, 0, rows[i], 0, colNum);
            System.arraycopy(row, colNum, rows[i], colNum + 1, row.length
                    - colNum);
        }
    }

    
    public void insertRow(int rowNum) {
        Object[][] tempRows = new Object[this.rowsCount + 1][];
        for (int i = 0; i < rowNum - 1; i++) {
            tempRows[i] = rows[i];
        }
        Object temp[] = new Object[this.colsCount];
        for (int i = 0; i < temp.length; i++) {
            temp[i] = "-";
        }
        tempRows[rowNum - 1] = temp;
        for (int i = rowNum; i < rowsCount; i++) {
            tempRows[rowNum + i] = rows[i];
        }
        this.rows = tempRows;
        this.rowsCount++;
    }

    
    public void copyColData(int colNum, DataTable srcTable, int srcColIndex) {
        if (colNum >= this.colsCount) {
            insertCol(colNum);
        }
        this.colsName[colNum] = srcTable.colsName[srcColIndex];
        for (int i = 0; i < srcTable.rowsCount; i++) {
            this.rows[i][colNum] = srcTable.rows[i][srcColIndex];
        }
    }

    
    public Object[][] copyColsData(int[] cols) {
        Object[][] objs = new Object[rowsCount][cols.length];
        for (int i = 0; i < rowsCount; i++) {
            for (int r = 0; r < cols.length; r++) {
                objs[i][r] = rows[i][cols[r]];
            }
        }
        return objs;
    }

    
    public void changeTwoColsData(int firstCol, int secondCol) {
        if (firstCol > colsCount || secondCol > colsCount) {
            return;
        }
        if (firstCol == secondCol) {
            return;
        }
        // 更换列名
        String tmpName = colsName[firstCol];
        colsName[firstCol] = colsName[secondCol];
        colsName[secondCol] = tmpName;
        // 更换列数据
        for (int i = 0; i < rowsCount; i++) {
            Object tmp = rows[i][firstCol];
            rows[i][firstCol] = rows[i][secondCol];
            rows[i][secondCol] = tmp;
        }
    }

    
    public void reverse(String name) {
        int tempNum = this.colsCount;
        this.colsCount = this.rowsCount + 1;
        this.rowsCount = tempNum - 1;
        String[] tempName;
        this.colTypes = null;
        this.colTypesName = null;

        tempName = this.colsName;
        this.colsName = new String[this.colsCount];
        this.colsName[0] = name;
        for (int i = 1; i < this.colsCount; i++) {
            this.colsName[i] = this.getObj(i - 1, 0).toString();
        }
        Object[][] tempRows = this.rows;
        this.rows = new Object[this.rowsCount][this.colsCount];
        for (int i = 0; i < this.rowsCount; i++) {
            for (int j = 0; j < this.colsCount; j++) {
                if (j == 0) {
                    this.rows[i][0] = tempName[i + 1];
                } else {
                    this.rows[i][j] = tempRows[j - 1][i + 1];
                }
            }
        }
    }

    
    public DataTable copyFrame() {
        DataTable tmp = new DataTable();
        tmp.colsCount = this.colsCount;
        tmp.colsName = this.colsName;
        tmp.colTypes = this.colTypes;
        tmp.colTypesName = this.colTypesName;
        tmp.rowsCount = this.rowsCount;
        tmp.rows = new Object[tmp.rowsCount][tmp.colsCount];
        return tmp;
    }

    
    public void sortByIndex(int index) {
        sortByIndex(index, 0, 0);
    }

    
    public void sortByIndex(int index, int type) {
        sortByIndex(index, type, 0);
    }

    
    public void sortByIndex(int index, int type, int order) {
        if (index >= this.colsCount) {
            return;
        }
        double intV = 0, intTempV = 0;
        String strV = "", strTempV = "";
        Object[] row = null;
        for (int i = 0; i < this.rowsCount - 1; i++) {
            for (int j = i + 1; j < this.rowsCount; j++) {
                if (type == 0) {
                    intV = Double.parseDouble(Convert.toString(this.rows[i][index]));
                    intTempV = Double.parseDouble(Convert.toString(this.rows[j][index]));
                    if (order == 0) {
                        if (intV > intTempV) {
                            row = this.rows[i].clone();
                            this.rows[i] = this.rows[j].clone();
                            this.rows[j] = row.clone();
                        }
                    } else {
                        if (intV < intTempV) {
                            row = this.rows[i].clone();
                            this.rows[i] = this.rows[j].clone();
                            this.rows[j] = row.clone();
                        }
                    }
                } else {
                    strV = Convert.toString(this.rows[i][index]);
                    strTempV = Convert.toString(this.rows[j][index]);
                    if (order == 0) {
                        if (strV.compareTo(strTempV) > 0) {
                            row = this.rows[i].clone();
                            this.rows[i] = this.rows[j].clone();
                            this.rows[j] = row.clone();
                        }
                    } else {
                        if (strV.compareTo(strTempV) < 0) {
                            row = this.rows[i].clone();
                            this.rows[i] = this.rows[j].clone();
                            this.rows[j] = row.clone();
                        }
                    }
                }
            }
        }
    }

    
    private int[] copy(int[] src, int[] dest, int start, int length) {
        if (src.length < length + start) length = src.length - start;
        for (int i = start; i < length + start; i++) {
            dest[i - start] = src[i];
        }
        return dest;
    }

    
    private Object[] copy(Object[] src, Object[] dest, int start, int length) {
        if (src.length < length + start) length = src.length - start;
        for (int i = start; i < length + start; i++) {
            dest[i - start] = src[i];
        }
        return dest;
    }

    
    private Object[] copyPart(Object[] src, Object[] dest, int srcStart, int desStart) {
        for (int i = srcStart; i < src.length; i++) {
            dest[desStart + i - srcStart] = src[i];
        }
        return dest;
    }

    
    private String CheckNull(String s) {
        return (s == null || s.toLowerCase().equals("null")) ? "" : s;
    }

    
    private int[] copyPart(int[] src, int[] dest, int srcStart, int desStart) {
        for (int i = srcStart; i < src.length; i++) {
            dest[desStart + i - srcStart] = src[i];
        }
        return dest;
    }

    
    public String[] getColTypesName() {
        return colTypesName;
    }

    
    public void setColTypesName(String[] colTypesName) {
        this.colTypesName = colTypesName;
    }

    
    public Object[][] getRows() {
        return rows;
    }

    
    public void setRows(Object[][] rows) {
        this.rows = rows;
    }

    
    public int getRowsCount() {
        return rowsCount;
    }

    
    public void setRowsCount(int rowsCount) {
        this.rowsCount = rowsCount;
    }

    
    public int getColsCount() {
        return colsCount;
    }

    
    public void setColsCount(int colsCount) {
        this.colsCount = colsCount;
    }

    
    public String[] getColsName() {
        return colsName;
    }

    
    public void setColsName(String[] colsName) {
        this.colsName = colsName;
    }
}
