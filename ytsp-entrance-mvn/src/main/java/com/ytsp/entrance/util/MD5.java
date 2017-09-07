package com.ytsp.entrance.util;

import java.security.MessageDigest;
import java.util.UUID;

public class MD5 {
	private static final char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	private static String bytesToHex(byte[] bytes) {
		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < 16; ++i) {
			int t = bytes[i];
			if (t < 0)
				t += 256;
			sb.append(hexDigits[(t >>> 4)]);
			sb.append(hexDigits[(t % 16)]);
		}
		return sb.toString();
	}
	public static String code(String input) {
		try {
			MessageDigest md = MessageDigest.getInstance(System.getProperty("MD5.algorithm", "MD5"));
			
			return bytesToHex(md.digest(input.getBytes("utf-8")));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public static void main(String[] args) throws Exception {
		System.out.println(code(UUID.randomUUID().toString()));
	}
}
