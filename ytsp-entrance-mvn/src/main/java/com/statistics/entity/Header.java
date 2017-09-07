package com.statistics.entity;

import java.io.Serializable;

public class Header implements Serializable{
	private static final long serialVersionUID = -5365204584286411608L;
	//平台:安卓，iphone,ipad
	private String platform = "";
	//app当前使用的版本
	private String appVersion = "";
	//手机系统版本
	private String systemVersion = "";
	//h5版本
	private String H5Version = "";
	//用户创建日期
	private String userCreateTime = "";
	//用户id
	private String userId = "";
	//用户帐号
	private String userAccount = "";
	//若为登录状态，用户是否为vip标识。0为非vip,1为vip
	private String isVip = "";
	//ip地址
	private String ip = "";
	//地理位置
	private String area;
	//屏幕分辨率
	private String screenResolution = "";
	//设置厂商（三星，小米等）
	private String deviceManufacturer = "";
	//设备型号：安卓（三星s6，小米note3等）苹果(iphone4,iphone6s等)
	private String deviceModel = "";
	//渠道标识
	private String channelSign = "";
	//省
	private String province;
	//城市
	private String city;
	//手机sim卡
	private String imsi = "";
	
	public String getImsi() {
		return imsi;
	}
	public void setImsi(String imsi) {
		this.imsi = imsi;
	}
	public String getIsVip() {
		return isVip;
	}
	public void setIsVip(String isVip) {
		this.isVip = isVip;
	}
	public String getProvince() {
		return province;
	}
	public void setProvince(String province) {
		this.province = province;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getDeviceManufacturer() {
		return deviceManufacturer;
	}
	public void setDeviceManufacturer(String deviceManufacturer) {
		this.deviceManufacturer = deviceManufacturer;
	}
	public String getDeviceModel() {
		return deviceModel;
	}
	public void setDeviceModel(String deviceModel) {
		this.deviceModel = deviceModel;
	}
	public String getChannelSign() {
		return channelSign;
	}
	public void setChannelSign(String channelSign) {
		this.channelSign = channelSign;
	}
	public String getPlatform() {
		return platform;
	}
	public void setPlatform(String platform) {
		this.platform = platform;
	}
	public String getAppVersion() {
		return appVersion;
	}
	public void setAppVersion(String appVersion) {
		this.appVersion = appVersion;
	}
	public String getSystemVersion() {
		return systemVersion;
	}
	public void setSystemVersion(String systemVersion) {
		this.systemVersion = systemVersion;
	}
	public String getH5Version() {
		return H5Version;
	}
	public void setH5Version(String h5Version) {
		H5Version = h5Version;
	}
	public String getUserCreateTime() {
		return userCreateTime;
	}
	public void setUserCreateTime(String userCreateTime) {
		this.userCreateTime = userCreateTime;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getUserAccount() {
		return userAccount;
	}
	public void setUserAccount(String userAccount) {
		this.userAccount = userAccount;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getArea() {
		return area;
	}
	public void setArea(String area) {
		this.area = area;
	}
	public String getScreenResolution() {
		return screenResolution;
	}
	public void setScreenResolution(String screenResolution) {
		this.screenResolution = screenResolution;
	}
	
}
