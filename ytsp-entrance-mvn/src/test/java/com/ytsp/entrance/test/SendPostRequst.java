package com.ytsp.entrance.test;

import java.lang.reflect.Field;

import org.json.JSONObject;

import com.ytsp.entrance.command.base.HeadInfo;
import com.ytsp.entrance.util.WebClient;

public class SendPostRequst {
	
//	private static String SERVER_INTERFACE = "http://entrance.ikan.cn/newentrance/entrance";
	private static String SERVER_INTERFACE = "http://172.16.218.44:8080/entrance/entrance";
	
	public static void sendPostRequest(HeadInfo head,JSONObject body) throws Exception{
		JSONObject jObject = new JSONObject();
		entityToJSON(head,jObject);
		byte[] rspByte = WebClient.getWebContentByPost(
				SERVER_INTERFACE, body.toString(), jObject.toString());
		String rspString = new String(rspByte);
		// 整体json数据
		JSONObject totalJsonObj = new JSONObject(rspString);
		System.out.println(totalJsonObj.toString());
	}
	
	/**
	* @功能描述:将实体类转换成json对象，只支持基本类型数据  
	 */
	public static void entityToJSON(Object source, JSONObject dest) {
		Class clzss = source.getClass();
		Field[] fields = clzss.getDeclaredFields();
		try {
			for (Field field : fields) {
				dest.put(field.getName(), getFieldValue(source, field.getName()));
			}
		}catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	* @功能描述: 获取对象中某个属性的值 
	 */
	private static Object getFieldValue(Object data, String fieldName) {
		StringBuilder sb = new StringBuilder();
		Class clzss = data.getClass();
		// 将字段首字母大写
		String firstWord = fieldName.substring(0, 1).toUpperCase();
		sb.append(firstWord);
		sb.append(fieldName.substring(1, fieldName.length()));

		try {
			Field field = clzss.getDeclaredField(fieldName);
			return field.get(data);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}
	
}
