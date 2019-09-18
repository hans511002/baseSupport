package com.ery.base.support.test;

import java.util.Calendar;
import java.util.Date;

public class Test extends BaseTestCase {
	short b;
	char c;
	private TestAction action = new TestAction();

	/**
	 * @param
	 * @param
	 * @param
	 * @param
	 */
	public Test() {
		super("meta", "meta", "jdbc:oracle:thin:@133.37.251.241:1521:ora10", "oracle.jdbc.driver.OracleDriver");
		action.setTestDAO(new TestDAO());
	}

	// public void testLog(){
	// System.out.println("......................................");
	// Log.debug("测试......");
	// }
	// public void testQueryForArrary(){
	// Object[][] objects = action.queryForArrary(true);
	// Assert.assertEquals(objects.length,34);
	// for (Object[] object : objects){
	// for (int i=0 ;i<object.length;i++){
	// System.out.print(object[i]+"///////////");
	//
	// }
	//
	// }
	// System.out.println("测试方法QueryForArrary结束");
	// }
	// public void testQueryForPrimitiveArray(){
	// String[] strings = action.queryForPrimitiveArray(2688);
	// Assert.assertEquals(strings.length,1);
	// for (String string : strings){
	// System.out.print(string+"//////////////////");
	// }
	// System.out.println("测试方法QueryForPrimitiveArray结束");
	// }
	// public void testQueryForBeanArray(){
	// MeteTestPO[] meteTestPOs = action.queryForBeanArray(2688);
	// for (MeteTestPO meteTestPO : meteTestPOs){
	// System.out.print(meteTestPO.getLoginId()+"//////////////////");
	// Assert.assertEquals(meteTestPO.getLoginId(),2321);
	// }
	// System.out.println("测试方法QueryForBeanArray结束");
	// }
	// public void testQueryForArray1(){
	// Map<String,Object>[] maps = action.queryForArray(1);
	//
	// for (Map<String, Object> map: maps){
	// Assert.assertEquals(MapUtils.getIntValue(map, "USER_ID"),0);
	// }
	// System.out.println("测试方法QueryForArray1结束");
	// }
	// public void testQueryForList1(){
	// List<Map<String,Object>> lists = action.queryForList1();
	// Assert.assertEquals(lists.size(),2309);
	// for (int i=0; i<lists.size(); i++){
	// System.out.print(lists.get(i).get("LOG_ID"));
	// }
	// System.out.println("测试方法QueryForList1结束");
	// }
	// public void testQueryForPrimitiveList(){
	// List<String> lists = action.queryForPrimitiveList();
	// Assert.assertEquals(lists.size(),2309);
	// for (int i=0 ;i<lists.size() ;i++){
	// System.out.print(lists.get(i));
	// }
	// System.out.println("测试方法testQueryForPrimitiveList结束");
	// }
	// public void testQueryForBeanList(){
	// List<MeteTestPO> lists = action.queryForBeanList();
	// Assert.assertEquals(lists.size(),2309);
	// for (int i=0 ;i<lists.size() ;i++){
	// System.out.print(lists.get(i).getLoginId());
	// }
	// System.out.println("测试方法testQueryForBeanList结束");
	// }
	// public void testQueryForLong(){
	// System.out.print(action.queryForLong(2688));
	// Assert.assertEquals(action.queryForLong(2688),2321);
	// System.out.println("测试方法testQueryForLong结束");
	// }
	// public void testQueryForLongByNvl(){
	// Assert.assertEquals(action.queryForLongByNvl(0,8888),0);
	// System.out.println(action.queryForLongByNvl(0,2688)+"测试方法testQueryForLongByNvl结束");
	// }
	// public void testQueryForInt(){
	// Assert.assertEquals(action.queryForInt(2688),2321);
	// System.out.println(action.queryForInt(2688)+"测试方法testQueryForInt结束");
	// }
	// public void testQueryForIntByNvl(){
	// Assert.assertEquals(action.queryForIntByNvl(0,8888),0);
	// System.out.println("测试方法testQueryForIntByNvl结束");
	// }
	// public void testQueryForString(){
	// Assert.assertEquals(action.queryForString(2688),"2321");
	// System.out.println(action.queryForString(2688)+"测试方法testQueryForString结束");
	// }
	// public void testQueryForObject(){
	// int i = action.queryForObject(2688);
	// Assert.assertEquals(i,2688);
	// System.out.println("测试方法testQueryForObject结束");
	// }
	// public void testQueryForObjectByNvl(){
	// int i = action.queryForObjectByNvl(8888,8888);
	// Assert.assertEquals(i,8888);
	// System.out.println("测试方法testQueryForObjectByNvl结束");
	// }
	// public void testQueryForMap(){
	// Map<String,Object> map = action.queryForMap(2688);
	// Assert.assertEquals(map.size(),6);
	// System.out.println(map.get("LOG_ID")+"测试方法testQueryForMap结束");
	// }
	// public void testQueryForBean(){
	// MeteTestPO meteTestPO = action.queryForBean(2688);
	// Assert.assertEquals(meteTestPO.getUserId(),2688);
	// System.out.println(meteTestPO.getLoginId()+"测试方法testQueryForBean结束");
	// }
	// public void testQueryByRowMapper(){
	// MeteTestPO meteTestPO = action.queryByRowMapper(2688);
	// Assert.assertEquals(meteTestPO.getLoginId(),2321);
	// System.out.println(meteTestPO.getLoginId()+"测试方法testQueryByRowMapper结束");
	// }
	// public void testQueryForDataTable(){
	// DataTable dataTable = action.queryForDataTable(2688);
	// Assert.assertEquals(dataTable.rowsCount,1);
	// System.out.println(dataTable.colsName+"--列名；"+dataTable.colsCount+"---列数量");
	// }

	public void testExeUpdate(String a) {
		action.exeUpdate();
	}

	public static final void main(String[] args) throws Exception {
		Date date = new Date(System.currentTimeMillis());
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DATE, 1);
		calendar.set(Calendar.HOUR, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		System.out.println(calendar.getTime());
	}
}
