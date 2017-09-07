package com.ytsp.entrance.sms;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.ytsp.common.util.StringUtil;
import com.ytsp.entrance.util.DateFormatter;

public class SmsUtil {
	
	/**
	* <p>功能描述:构建发送短信请求</p>
	* <p>参数：@param msg
	* <p>参数：@param phone
	* <p>参数：@return</p>
	* <p>返回类型：String</p>
	 */
	public static String buildSendMessageRequest(String msg,String phone){
		if(StringUtil.isNullOrEmpty(msg) || StringUtil.isNullOrEmpty(phone)){
			return "";
		}
		StringBuffer retURL = new StringBuffer(SmsConfig.SEND_MSG_URL);
		retURL.append("username=").append(SmsConfig.USERNAME).append("&")
			  .append("password=").append(SmsConfig.PASSWORD).append("&")
			  .append("phone=").append(phone).append("&")
			  .append("message=").append(msg).append("&")
			  .append("epid=").append(SmsConfig.EPID).append("&")
			  .append("linkid=").append("&")
			  .append("subcode=").append("01");
		return retURL.toString();
	}
	
	/**
	* <p>功能描述:获取短信验证码参数</p>
	* <p>参数：@param msg
	* <p>参数：@param phone
	* <p>参数：@return</p>
	* <p>返回类型：Map<String,String></p>
	 */
	public static Map<String,String> getSendMessageParam(String msg,String phone){
		Map<String,String> param = new HashMap<String, String>();
		param.put("username", SmsConfig.USERNAME);
		param.put("password", SmsConfig.PASSWORD);
		param.put("phone", phone);
		param.put("message", msg);
		param.put("epid", SmsConfig.EPID);
		param.put("linkid", "");
		//尾号，00表示：物联商通 ，01表示：爱看儿童世界
		param.put("subcode", "01");
		return param;
	}
	
	
	/**
	* <p>功能描述:构建东方网润发送短信参数</p>
	* <p>参数：@param msg 短信内容
	* <p>参数：@param phone 手机号
	* <p>参数：@return</p>
	* <p>返回类型：Map<String,String></p>
	 */
	public static Map<String,String> getDFWRSendMessageParam(String msg,String phone){
		Map<String,String> param = new HashMap<String, String>();
		param.put("ua", SmsConfig.DFWR_USERNAME);
		param.put("pw", SmsConfig.DFWR_PASSWORD);
		param.put("mb", phone);
		param.put("ms", msg);
		String tm = DateFormatter.date2String(new Date(), "yyyy-MM-dd hh:mm:ss");
		param.put("tm", tm);
		return param;
	}
}
