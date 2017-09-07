package com.ytsp.entrance.util;

import java.util.Date;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import com.ibm.icu.util.Calendar;
import com.ytsp.common.util.StringUtil;

public class ValidateUtil {
	
	public static  String[] chars = new String[] { "a", "b", "c", "d", "e", "f",
		"g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s",
		"t", "u", "v", "w", "x", "y", "z", "0", "1", "2", "3", "4", "5",
		"6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "G", "H", "I",
		"J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
		"W", "X", "Y", "Z" };
	
	//验证是都是手机格式
	public static boolean isMoblie(String moblieNum){
		if(StringUtil.isNullOrEmpty(moblieNum)){
			return false;
		}
		Pattern p = Pattern.compile("^1\\d{10}");  //以1开头的11为手机号码
		Matcher m = p.matcher(moblieNum); 
		return m.matches();
	}
	
	//验证是否是邮箱格式
	public static boolean isEmail(String email){
		if(StringUtil.isNullOrEmpty(email)){
			return false;
		}
		Pattern p = Pattern.compile("^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$");  //以1开头的11为手机号码
		Matcher m = p.matcher(email); 
		return m.matches();
	}
	
	//验证账号只能是数字字母
	public static boolean validateAccount(String account){
		if(StringUtil.isNullOrEmpty(account)){
			return false;
		}
		Pattern p = Pattern.compile("^(?![0-9]+$)[0-9A-Za-z]{6,29}$");
		Matcher m = p.matcher(account);
		return m.matches();
	}
	
	//生成手机短信验证码
	public static String createRandom(int length) {
		String retNum = "";
		String validateNum = "1234567890";
		int len = validateNum.length();
		for (int i = 0; i < length; i++) {
			double randomNum = Math.random() * len;
			int num = (int) Math.floor(randomNum);
			retNum += validateNum.charAt(num);
		}
		
		return retNum;
	}
	
	//获取当前时间10分钟后的时间
	public static Date getEndTime(){
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, 10);
		return cal.getTime();
	}
	
	public static  boolean isValidateNumValid(Date validatTime,int time){
		Calendar cal = Calendar.getInstance();
		cal.setTime(validatTime);
		cal.add(Calendar.MINUTE, time);
		Calendar now = Calendar.getInstance();
		now.setTime(new Date());
		return now.before(cal);
	}
	
	//获取http真实请求的ip地址
	 public static String getIpAddress(HttpServletRequest request) {  
	        String ip = request.getHeader("x-forwarded-for");  
	        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
	            ip = request.getHeader("Proxy-Client-IP");  
	        }  
	        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
	            ip = request.getHeader("WL-Proxy-Client-IP");  
	        }  
	        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
	            ip = request.getHeader("HTTP_CLIENT_IP");  
	        }  
	        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
	            ip = request.getHeader("HTTP_X_FORWARDED_FOR");  
	        }  
	        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
	            ip = request.getRemoteAddr();  
	        }  
	        return ip;  
	    }  
	 
		
	public static String generateShortUuid() {
		StringBuffer shortBuffer = new StringBuffer();
		String uuid = UUID.randomUUID().toString().replace("-", "");
		for (int i = 0; i < 8; i++) {
			String str = uuid.substring(i * 4, i * 4 + 4);
			int x = Integer.parseInt(str, 16);
			shortBuffer.append(chars[x % 0x3E]);
		}
		return shortBuffer.toString();
	}
	 
}
