package com.sobey.jcg.support.sys.podo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sobey.jcg.support.sys.SystemVariable;
import com.sobey.jcg.support.utils.Convert;

public class DataSrcDAO extends BaseDAO {

	public List<DataSrcPO> getAllDataSrc() {
		String sql = SystemVariable.getDataSourceQuerySql();
		List<Map<String, Object>> mapList = getDataAccess().queryForList(sql);
		List<DataSrcPO> dataSrcPOs = new ArrayList<DataSrcPO>();
		for (Map<String, Object> map : mapList) {
			DataSrcPO dataSrcPO = new DataSrcPO();
			dataSrcPO.setDATA_SOURCE_ID(Convert.toLong(map.get("DATA_SOURCE_ID")));
			dataSrcPO.setDATA_SOURCE_NAME(Convert.toString(map.get("DATA_SOURCE_NAME")));
			dataSrcPO.setDATA_SOURCE_TYPE(Convert.toInt(map.get("DATA_SOURCE_TYPE")));
			dataSrcPO.setDATA_SOURCE_URL(Convert.toString(map.get("DATA_SOURCE_URL")));
			dataSrcPO.setDATA_SOURCE_USER(Convert.toString(map.get("DATA_SOURCE_USER")));
			dataSrcPO.setDATA_SOURCE_PASS(Convert.toString(map.get("DATA_SOURCE_PASS")));
			dataSrcPO.setDATA_SOURCE_DESC(Convert.toString(map.get("DATA_SOURCE_DESC")));
			dataSrcPO.setDATA_SOURCE_CFG(Convert.toString(map.get("DATA_SOURCE_CFG")));
			dataSrcPOs.add(dataSrcPO);
		}
		return dataSrcPOs;
	}

}
