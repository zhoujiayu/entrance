package com.statistics.entity;

import java.io.Serializable;
import java.util.Date;


/**
 *统计实体类
 */
public class Statistics implements Serializable{
	private static final long serialVersionUID = 6266621931569463005L;
	//请求头
	private Header head;
	//请求号
	private String commandCode = "";
	//业务参数
	private Object bizParam = "";
	//统计数据实体类
	private Object entity;
	//页面类型
	private String pageType = "";
	//页面描述
	private String pageDesc = "";
	//请求返回数据
	private String responseData = "";
//	//时间戳：毫秒级
//	private String timestamp = "";
	//校验码：生成规则：platform+appVersion+userId+commandCode+timestamp+"imagemedia"的MD5值
	private String secretKey = "";
	//统计日期
	private Date time;
	//是否有多个统计entity
	private boolean isMult = false;
	//位置字段
	private String location = "";
	//位置名称
	private String locationName = "";
	
	public Date getTime() {
		return time;
	}
	public void setTime(Date time) {
		this.time = time;
	}
	private int _hashkey;
	
	public int get_hashkey() {
		return _hashkey;
	}
	public void set_hashkey(int _hashkey) {
		this._hashkey = _hashkey;
	}
	public String getLocationName() {
		return locationName;
	}
	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public boolean isMult() {
		return isMult;
	}
	public void setMult(boolean isMult) {
		this.isMult = isMult;
	}
	
	public String getPageDesc() {
		return pageDesc;
	}
	public void setPageDesc(String pageDesc) {
		this.pageDesc = pageDesc;
	}
	public String getSecretKey() {
		return secretKey;
	}
	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}
	public String getPageType() {
		return pageType;
	}
	public void setPageType(String pageType) {
		this.pageType = pageType;
	}
	public Object getEntity() {
		return entity;
	}
	public void setEntity(Object entity) {
		this.entity = entity;
	}
	public Header getHead() {
		return head;
	}
	public void setHead(Header head) {
		this.head = head;
	}
	
	public String getResponseData() {
		return responseData;
	}
	public void setResponseData(String responseData) {
		this.responseData = responseData;
	}
	public String getCommandCode() {
		return commandCode;
	}
	public void setCommandCode(String commandCode) {
		this.commandCode = commandCode;
	}
	public Object getBizParam() {
		return bizParam;
	}
	public void setBizParam(Object bizParam) {
		this.bizParam = bizParam;
	}
	
}
