package com.ytsp.entrance.sms;

public class SmsConfig {
	//鸿联95发送短信请求地址
	public static final String SEND_MSG_URL = "http://114.255.71.158:8061/";
	//鸿联95用户名
	public static final String USERNAME = "wxyt";
	//鸿联95密码
	public static final String PASSWORD = "wxyt123";
	//鸿联95企业id
	public static final String EPID = "121309";
	//鸿联95备用 唯一ID，可为空
	public static final String LINKID = "";
	//鸿联95扩展小号:可为空
	public static final String SUBCODE = "";
	//鸿联95余额查询接口
	public static final String GET_FEE_URL = "http://114.255.71.158:8061/getfee/";
	
	public static final String SMS_MSG_CONTENT = "【爱看儿童世界】您的验证码是：CODE，10分钟内有效。如非您本人操作，可忽略本消息。";
	
	//东方网润短信发送请求地址
	public static final String DFWR_SEND_MSG_URL = "http://sms.qisu100.com:18002/send.do";
	//东方网润用户名
	public static final String DFWR_USERNAME = "xkong";
	//东方网润密码
	public static final String DFWR_PASSWORD = "752444";
	
}
