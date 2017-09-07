package com.ytsp.entrance.util;

import java.security.Key;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class DESUtils {
	private static Key key;
	private static String KEY_STR = "tangguodianying";
	static {
		try{
			KeyGenerator generator = KeyGenerator.getInstance("DES");
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
		    random.setSeed(KEY_STR.getBytes());
			generator.init(random);
			key = generator.generateKey();
			generator = null;
		}catch(Exception e){
			
		}
	}
	
	public static String getEncryptString(String str){
		BASE64Encoder base64en = new BASE64Encoder();
		String ret = null;
		try{
			byte[] strBytes = str.getBytes("UTF8");
			Cipher cipher = Cipher.getInstance("DES");
			cipher.init(Cipher.ENCRYPT_MODE, key);
			byte[] encryptStrBytes = cipher.doFinal(strBytes);
			ret = base64en.encode(encryptStrBytes);
			ret = toBase64StringForUrl(ret);
		}catch(Exception e){
			throw new RuntimeException(e);
		}
		return ret;
	}
	
	public static String getDecryptString(String str){
		str = fromBase64StringForUrl(str);
 		BASE64Decoder decoder = new BASE64Decoder();
		try{
			byte[] strBytes = decoder.decodeBuffer(new String(str.getBytes(),"UTF8"));
			Cipher cipher = Cipher.getInstance("DES");
			cipher.init(Cipher.DECRYPT_MODE, key);
			byte[] encryptStrBytes = cipher.doFinal(strBytes);
			return new String(encryptStrBytes,"UTF8");
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
//	public static void main(String[] orgs){
//		System.out.println(toBase64StringForUrl(DESUtils.getEncryptString("root")));
//		System.out.println(DESUtils.getDecryptString(fromBase64StringForUrl("N8HXwV1C-qs.")));
//	}
	
	private static String fromBase64StringForUrl(String base64String)
	{
	   return base64String.replace('.', '=').replace('*', '+').replace('-', '/');
	}

	private static String toBase64StringForUrl(String normalString)
	{
	   return normalString.replace('+', '*').replace('/', '-').replace('=', '.');
	}
}
