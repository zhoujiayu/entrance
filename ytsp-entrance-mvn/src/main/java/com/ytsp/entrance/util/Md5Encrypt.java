package com.ytsp.entrance.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import com.ytsp.db.domain.EbCoupon;

public class Md5Encrypt {
	/**
	 * @param str
	 * @return
	 */
	public static String md5(String str) {
		if (str == null) {
			return null;
		}
		MessageDigest messageDigest = null;
		try {
			messageDigest = MessageDigest.getInstance("MD5");
			messageDigest.reset();
			messageDigest.update(str.getBytes("utf-8"));
		} catch (NoSuchAlgorithmException e) {
			return str;
		} catch (UnsupportedEncodingException e) {
			return str;
		}
		byte[] byteArray = messageDigest.digest();
		StringBuffer md5StrBuff = new StringBuffer();
		for (int i = 0; i < byteArray.length; i++) {
			if (Integer.toHexString(0xFF & byteArray[i]).length() == 1)
				md5StrBuff.append("0").append(
						Integer.toHexString(0xFF & byteArray[i]));
			else
				md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
		}
		return md5StrBuff.toString();
	}
	
	public static void main(String[] args) {
		EbCoupon ebCoupon=new EbCoupon();
		ebCoupon.setId(438);
		
		byte[] head4 = Md5Encrypt.int2byte(ebCoupon.hashCode());
		byte[] tail4 = Md5Encrypt.int2byte(ebCoupon.getId());
		char timeChar=(char) ((new Date().getTime())%0xffff);
		char a=(char) ((ebCoupon.hashCode()+ebCoupon.getId())%0xffff);
		byte[] last2 = Md5Encrypt.char2byte((char)((timeChar+a)%0xffff));
		byte[] input = new byte[10];
		for (int i = 0; i < 8; i++) {
			if(i%2==0)
				input[i]=tail4[i/2];
			else
				input[i]=head4[(i+1)/2-1];
		}
		for (int j = 0; j < 2; j++) {
			input[8 + j] = last2[j];
		}
		System.err.print(Base32.encode(input));
		
//		byte[] head4 = Md5Encrypt.int2byte(22342);
//		byte[] tail4 = Md5Encrypt.int2byte(111);
//		byte[] input = new byte[8];
//		System.arraycopy(head4, 0, input, 0, 4);
//		System.arraycopy(tail4, 0, input, 4, 4);
//		for (byte head : head4) {
//			System.err.print(head+",");
//		}
//		System.err.print("\n");
//		for (byte head : tail4) {
//			System.err.print(head+",");
//		}
//		System.err.print("\n");
//		for (byte head : input) {
//			System.err.print(head+",");
//		}
	}
	
	public static byte[] int2byte(int res) {  
		byte[] targets = new byte[4]; 
		targets[0] = (byte) (res & 0xff);// 最低位   
		targets[1] = (byte) ((res >> 8) & 0xff);// 次低位   
		targets[2] = (byte) ((res >> 16) & 0xff);// 次高位   
		targets[3] = (byte) (res >>> 24);// 最高位,无符号右移。   
		return targets;   
	}
	public static byte[] char2byte(char res) {  
		byte[] targets = new byte[2]; 
		targets[0] = (byte) (res & 0xff);// 最低位   
		targets[1] = (byte) ((res >> 8) & 0xff);// 高位   
		return targets;   
	}
	
	public static String code(String input) {
		try {
			MessageDigest md = MessageDigest.getInstance(System.getProperty("MD5.algorithm", "MD5"));
			return Base32.encode(md.digest(input.getBytes("utf-8")));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	public static String code(byte[] input) {
		try {
			MessageDigest md = MessageDigest.getInstance(System.getProperty("MD5.algorithm", "MD5"));
			return Base32.encode(md.digest(input));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
}
