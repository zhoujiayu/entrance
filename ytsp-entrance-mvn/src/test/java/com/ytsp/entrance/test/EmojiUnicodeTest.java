//package com.ytsp.entrance.test;
//
//import java.io.UnsupportedEncodingException;
//
//import org.apache.commons.lang3.ArrayUtils;
//import org.apache.lucene.util.UnicodeUtil;
//
//import antlr.collections.List;
//
//import com.google.gson.Gson;
//
//public class EmojiUnicodeTest {
//
//	/**
//	 * @param args
//	 * @throws UnsupportedEncodingException
//	 */
//	public static void main(String[] args) throws UnsupportedEncodingException {
//		String nick = "ߍߌ»琴妹儿ߒ";
//		System.err.println(nick);
//		// 编码
//		nick = UnicodeUtil.toHexString(nick);
//		System.err.println(nick);
//		String[] charts = nick.split(" ");
//		StringBuffer sb = new StringBuffer();
//		for (String s : charts) {
//			s = s.substring(2);
//			if (s.length() > 3) {
//				sb.append("\\u").append(s).append(" ");
//			} else {
//				sb.append("\\u0").append(s).append(" ");
//			}
//		}
//		System.err.println(sb.toString());
//
//		// 解码
//		nick = sb.toString();
//		String[] charts1 = nick.split(" ");
//		byte[] bytes = {};
//		for (String s : charts1) {
//			s = s.substring(2);
//			int inthex = Integer.parseInt(s, 16);
//			char[] schar = { (char) inthex };
//			byte[] b = (new String(schar)).getBytes("UTF-8");
//			bytes = ArrayUtils.addAll(bytes, b);
//		}
//		String n = new String(bytes);
//		System.err.println(n);
//	}
//}
