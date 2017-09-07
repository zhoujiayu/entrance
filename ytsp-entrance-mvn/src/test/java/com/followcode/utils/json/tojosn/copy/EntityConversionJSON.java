package com.followcode.utils.json.tojosn.copy;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.followcode.utils.json.annotation.JSONCollection;
import com.followcode.utils.json.annotation.JSONEntity;
import com.followcode.utils.json.annotation.JSONMap;
import com.followcode.utils.json.annotation.JSONValue;

/**
 * 
 * @author tan
 * 
 */
@SuppressWarnings("unchecked")
public final class EntityConversionJSON {

	/**
	 * 实体的值转成JSON对象的值
	 * 
	 * @param source
	 * @param dest
	 */
	public static void entityToJSON(Object source, JSONObject dest) {

		Class clzss = source.getClass();

		// 是否为JOSN实例

		final boolean isJosnEntity = clzss.getAnnotation(JSONEntity.class) != null;

		Field[] fields = clzss.getDeclaredFields();

		try {

			for (Field field : fields) {
				// 确认是否带有JSONValue注解
				if (!isJosnEntity && field.getAnnotation(JSONValue.class) != null) {
					dest.put(field.getName(), getFieldValue(source, field.getName()));
					// 确认是否带有JSONCollection注解
				} else if (!isJosnEntity && field.getAnnotation(JSONCollection.class) != null) {
					Collection collection = null;

					Class fieldType = field.getType();
					if (fieldType.isArray()) {// 为数组

						Object[] objs = (Object[]) getFieldValue(source, field.getName());

						arrayToJSONObject(field.getName(), objs, dest);

					} else {// 为集合
						try {
							collection = (Collection) getFieldValue(source, field.getName());
						} catch (ClassCastException e) {
							continue;
						}
						// 确认是否为空值
						if (collection == null || collection.size() == 0) {
							continue;
						}

						collectionToJSONObject(field.getName(), collection, dest);
					}

				} else if (!isJosnEntity// 为Map类型数据
						&& field.getAnnotation(JSONMap.class) != null) {
					Map map = null;
					try {
						map = (Map) getFieldValue(source, field.getName());
					} catch (ClassCastException e) {
						continue;
					}

					mapToJSONObject(field.getName(), map, dest);

				} else if (isJosnEntity) {// 为JSONEntity实体
					Object val = getFieldValue(source, field.getName());

					if (isBaseType(field)) {// 如果为基本数据
						dest.put(field.getName(), val);
					} else if (val instanceof Collection) {// 如果为集合内
						collectionToJSONObject(field.getName(), (Collection) val, dest);
					} else if (val.getClass().isArray()) {
						arrayToJSONObject(field.getName(), (Object[]) val, dest);
					} else if (val instanceof Map) {
						mapToJSONObject(field.getName(), (Map) val, dest);
					} else {// 为自定义类型
						JSONObject jobj = null;
						if (val != null) {
							jobj = new JSONObject();
							entityToJSON(val, jobj);
						}
						dest.put(field.getName(), jobj);
					}

				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 确认是否为基础数据类型和包装类
	 * 
	 * @param field
	 * @return
	 */
	private static boolean isBaseType(Field field) {

		return field.getType().isAssignableFrom(byte.class) || field.getType().isAssignableFrom(Byte.class) || field.getType().isAssignableFrom(short.class) || field.getType().isAssignableFrom(Short.class) || field.getType().isAssignableFrom(int.class) || field.getType().isAssignableFrom(Integer.class) || field.getType().isAssignableFrom(char.class) || field.getType().isAssignableFrom(long.class) || field.getType().isAssignableFrom(Long.class) || field.getType().isAssignableFrom(float.class)
				|| field.getType().isAssignableFrom(Float.class) || field.getType().isAssignableFrom(double.class) || field.getType().isAssignableFrom(Double.class) || field.getType().isAssignableFrom(boolean.class) || field.getType().isAssignableFrom(Boolean.class) || field.getType().isAssignableFrom(String.class);

	}

	/**
	 * 获取字段的值
	 * 
	 * @param data
	 * @param fieldName
	 * @return
	 */
	private static Object getFieldValue(Object data, String fieldName) {

		StringBuilder sb = new StringBuilder();

		Class clzss = data.getClass();

		// 将字段首字母大写
		String firstWord = fieldName.substring(0, 1).toUpperCase();
		sb.append(firstWord);
		sb.append(fieldName.substring(1, fieldName.length()));

		// final String methodName = "get" + sb.toString();

		try {
			Field field = clzss.getDeclaredField(fieldName);
			return field.get(data);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	/**
	 * 集合类转换成JSONArray
	 * 
	 * @param source
	 * @param dest
	 */
	public static <T> void collectionToJSONObject(String fieldName, Collection<T> source, JSONObject dest) throws Exception {

		if (source == null || source.size() == 0)
			return;

		Iterator<T> iterator = source.iterator();

		JSONObject tempObj = null;
		JSONArray tempArray = new JSONArray();
		while (iterator.hasNext()) {
			tempObj = new JSONObject();
			entityToJSON(iterator.next(), tempObj);
			tempArray.put(tempObj);
		}
		dest.put(fieldName, tempArray);

	}

	/**
	 * 数组转换成JSONArray
	 * 
	 * @param source
	 * @param dest
	 */
	public static <T> void arrayToJSONObject(String fieldName, T[] source, JSONObject dest) throws Exception {

		if (source == null || source.length == 0)
			return;
		JSONObject tempObj = null;
		JSONArray tempArray = new JSONArray();
		for (T obj : source) {
			if (obj == null)
				continue;
			tempObj = new JSONObject();
			entityToJSON(obj, tempObj);
			tempArray.put(tempObj);
		}
		dest.put(fieldName, tempArray);

	}

	/**
	 * 将Map类型数据转成JSONObject
	 * 
	 * @param fieldName
	 * @param source
	 * @param dest
	 * @throws Exception
	 */
	public static void mapToJSONObject(String fieldName, Map source, JSONObject dest) throws Exception {
		if (source == null || source.size() == 0)
			return;

		JSONArray tempArray = new JSONArray();
		JSONObject tempObj = null;

		Iterator iterator = source.keySet().iterator();

		while (iterator.hasNext()) {
			Object key = iterator.next();
			tempObj = new JSONObject();
			entityToJSON(source.get(key), tempObj);
			JSONObject obj = new JSONObject();
			obj.put(key.toString(), tempObj);
			tempArray.put(obj);
		}
		dest.put(fieldName, tempArray);

	}

}
