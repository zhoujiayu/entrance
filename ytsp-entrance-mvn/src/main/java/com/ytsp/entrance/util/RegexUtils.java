package com.ytsp.entrance.util;

import java.util.regex.Pattern;

public class RegexUtils {
	private static final Pattern standarChar = Pattern.compile("^[a-zA-Z0-9_@\\.\u4e00-\u9fa5]+$");
	private static final Pattern account = Pattern.compile("^[a-zA-Z0-9_-\u4e00-\u9fa5]+$");
	private static final Pattern email = Pattern.compile("^[\\w-]+(\\.[\\w-]+)*@[\\w-]+(\\.[\\w-]+)+$");
	private static final Pattern var = Pattern.compile("^[a-zA-Z]+\\w+$");
	private static final Pattern password = Pattern.compile("^[A-Za-z0-9]{4,20}$");
	private static final Pattern ipAddress = Pattern
			.compile("(((2[0-4]\\d)|(25[0-5]))|(1\\d{2})|([1-9]\\d)|(\\d))[.](((2[0-4]\\d)|(25[0-5]))|(1\\d{2})|([1-9]\\d)|(\\d))[.](((2[0-4]\\d)|(25[0-5]))|(1\\d{2})|([1-9]\\d)|(\\d))[.](((2[0-4]\\d)|(25[0-5]))|(1\\d{2})|([1-9]\\d)|(\\d))");
	private static final Pattern mobilephone = Pattern.compile("^(0|86|17951)?(13[0-9]|15[012356789]|17[678]|18[0-9]|14[57])[0-9]{8}$");
	
	public static boolean matchStandarCharV5_0(String str) {
		return account.matcher(str).matches();
	}
	
	public static boolean matchMobilephone(String str) {
		return mobilephone.matcher(str).matches();
	}
	
	public static boolean matchStandarChar(String str) {
		return standarChar.matcher(str).matches();
	}

	public static boolean matchEmail(String str) {
		return email.matcher(str).matches();
	}

	public static boolean matchVar(String str) {
		return var.matcher(str).matches();
	}
	
	public static boolean matchPassword(String str) {
		return password.matcher(str).matches();
	}

	public static boolean matchIPAddress(String str) {
		return ipAddress.matcher(str).matches();
	}

	public static void main(String args[]) {
//		String emailStr = "xsupercooler@qq.com";
//		System.out.println(email.matcher(emailStr).matches());
		
//		String name="a2你好-";
//		System.out.println(standarChar.matcher(name).matches());
		String phone = "891721212__saf3";
		System.out.println(matchStandarCharV5_0(phone));
//		String pwd="@@@imagem.cn";
//		System.out.println(standarChar.matcher(pwd).matches());
		Pattern var = Pattern.compile("^\\w+[岁][-]\\w+[岁]$");
		System.out.println(var.matcher("12岁岁-2岁").matches());
	}
}
