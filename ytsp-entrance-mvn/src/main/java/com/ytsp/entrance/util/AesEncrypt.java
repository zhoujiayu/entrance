package com.ytsp.entrance.util;
import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class AesEncrypt {

	public static final String KEY = "5c866ece-097b-4b8a-8759-b7d6ec8255f7";
	
    public static String encrypt(String strKey, String strIn) throws Exception {
    	Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    	if(strKey==null)
    		strKey = KEY;
        SecretKeySpec skeySpec = getKey(strKey);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
        IvParameterSpec iv = new IvParameterSpec(new byte[16]);
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
        byte[] encrypted = cipher.doFinal(strIn.getBytes());
        return new BASE64Encoder().encode(encrypted);
    }

    public static String decrypt(String strKey, String strIn) throws Exception {
    	Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    	if(strKey==null)
    		strKey = KEY;
        SecretKeySpec skeySpec = getKey(strKey);
        //兼容iOS加密，使用PKCS7Padding填充
        Cipher cipher =null;
        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
		} catch (Throwable e) {
			e.printStackTrace();
		}
        byte[] ivs = new byte[16];
        IvParameterSpec iv = new IvParameterSpec(ivs);
        cipher.init(Cipher.DECRYPT_MODE, skeySpec,iv);
        byte[] encrypted1 = new BASE64Decoder().decodeBuffer(strIn);
        byte[] original = cipher.doFinal(encrypted1);
        String originalString = new String(original);
        return originalString;
    }

    //在使用UUID作为密钥的时候，肯定是16字节128位加密的
    private static SecretKeySpec getKey(String strKey) throws Exception {
        byte[] arrBTmp = strKey.getBytes();
        byte[] arrB = new byte[16];
        for (int i = 0; i < arrBTmp.length && i < arrB.length; i++) {
            arrB[i] = arrBTmp[i];
        }
        SecretKeySpec skeySpec = new SecretKeySpec(arrB, "AES");
        return skeySpec;
    }

    public static void main(String[] args) throws Exception {
    	Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        String Code ="167CLVDmPAbVH9XDOKkPIg40oI9sQufyxbpXyyYiUEG1uqEVbtGfn2mnPPFU7HrAjoj70/DfyyWryccc2y4UvuK7WsKKL7hvzMh45MdNzJ+/KrZes61vl3NXgKPWx2nAAnP09udIt/GQAGDCTYT2IAR7vAJEKU0r01+mqhrdUrYQ3hYSupHBjkBmRunrA7CN";
        Code = Code.replaceAll(" ", "");
        String key = "5c866ece-097b-4b8a-8759-b7d6ec8255f7";
//        codE = AesEncrypt.encrypt(key, Code);

//        System.out.println("原文：" + Code);
//        System.out.println("密钥：" + key);
//        System.out.println("密文：" + codE);
        System.out.println("解密：" + AesEncrypt.decrypt(key,Code));
        System.out.println("解密：" + AesEncrypt.decrypt(key,Code));
    }
}