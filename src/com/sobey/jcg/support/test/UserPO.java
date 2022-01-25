package com.sobey.jcg.support.test;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

/**
 * META_MAG_USER表的实体类
 *
 */
public class UserPO {
	// attributes
	private Integer id;
	private String email;

	/**
	 * 必须使用MD5加密
	 */
	private String passwd;
	private String nameCN;
	private String mobile;

	/**
	 * 用户状态：0，禁用(不可登录);1，可用(可登录);2，待核(不可登录)
	 */
	private Integer state;
	// private int deptID;
	private Integer stationId;

	// 以下字段是对USER的扩充，在USER表中不存在这些字段。
	private Integer deptId;// 该用户所属部门
	private Integer zoneId;
	private String stationName;

	/**
	 * 是否具有管理子部门用户的权限，或运算后存入库中的值
	 */
	private int magFlag;
	/**
	 * 是否具有管理子部门用户的权限，默认为0，不具备管理权限。枚举：1，本部门；2，部门；4，本岗位；8，子岗位
	 */
	private List<Integer> magFlags = new ArrayList<Integer>();

	// setter and getter

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getStationName() {
		return stationName;
	}

	public void setStationName(String stationName) {
		this.stationName = stationName;
	}

	public Integer getMagFlag() {
		return magFlag;
	}

	/**
	 * 设置管理权限，用&运算计算出该用户拥有的权限。
	 * 
	 * @param magFlag
	 */
	public void setMagFlag(Integer magFlag) {
		this.magFlag = magFlag;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPasswd() {
		return passwd;
	}

	public void setPasswd(String passwd) {
		this.passwd = passwd;
	}

	public String getNameCN() {
		return nameCN;
	}

	public void setNameCN(String nameCN) {
		try {
			// nameCN="中午";
			this.nameCN = URLDecoder.decode(nameCN, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			this.nameCN = null;
		}
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
	}

	public List<Integer> getMagFlags() {
		return magFlags;
	}

	public void setMagFlags(List<Integer> magFlags) {
		this.magFlags = magFlags;
	}

	public Integer getStationId() {
		return stationId;
	}

	public void setStationId(Integer stationId) {
		this.stationId = stationId;
	}

	public Integer getDeptId() {
		return deptId;
	}

	public void setDeptId(Integer deptId) {
		this.deptId = deptId;
	}

	public Integer getZoneId() {
		return zoneId;
	}

	public void setZoneId(Integer zoneId) {
		this.zoneId = zoneId;
	}
}