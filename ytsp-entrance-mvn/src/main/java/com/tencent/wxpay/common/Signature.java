package com.tencent.wxpay.common;

import java.io.IOException;
import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

/**
 * 微信支付签名生成工具
 */
public class Signature {
	/**
	 * 签名算法,先对参数经行ASII码值排序,排除sign字段
	 * 
	 * @param o
	 *            要参与签名的数据对象
	 * @return 签名
	 * @throws IllegalAccessException
	 */
	public static String getSign(Object o, String appKey)
			throws IllegalAccessException {
		ArrayList<String> list = new ArrayList<String>();
		Class cls = o.getClass();
		Field[] fields = cls.getDeclaredFields();
		for (Field f : fields) {
			f.setAccessible(true);
			if (f.get(o) != null && f.get(o) != ""
					&& !f.getName().equals("sign")) {
				list.add(f.getName() + "=" + f.get(o) + "&");
			}
		}
		int size = list.size();
		String[] arrayToSort = list.toArray(new String[size]);
		Arrays.sort(arrayToSort, String.CASE_INSENSITIVE_ORDER);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < size; i++) {
			sb.append(arrayToSort[i]);
		}
		String result = sb.toString();
		result += "key=" + appKey;
		result = MD5.MD5Encode(result).toUpperCase();
		return result;
	}

	/**
	 * 签名算法,先对参数经行ASII码值排序,排除sign字段
	 * 
	 * @param map
	 *            参数列表
	 * @param appKey
	 *            密钥
	 * @return
	 */
	public static String getSign(Map<String, Object> map, String appKey) {
		ArrayList<String> list = new ArrayList<String>();
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			if (entry.getValue() != "" && !entry.getKey().equals("sign")) {
				list.add(entry.getKey() + "=" + entry.getValue() + "&");
			}
		}
		int size = list.size();
		String[] arrayToSort = list.toArray(new String[size]);
		Arrays.sort(arrayToSort, String.CASE_INSENSITIVE_ORDER);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < size; i++) {
			sb.append(arrayToSort[i]);
		}
		String result = sb.toString();
		result += "key=" + appKey;
		result = MD5.MD5Encode(result).toUpperCase();
		return result;
	}

	/**
     * 创建md5摘要,规则是:按参数名称a-z排序,遇到空值的参数不参加签名。
     */
    public static String createSign(SortedMap<String, String> packageParams,String appKey) {
        StringBuffer sb = new StringBuffer();
        Set es = packageParams.entrySet();
        Iterator it = es.iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String k = (String) entry.getKey();
            String v = (String) entry.getValue();
            if (null != v && !"".equals(v) && !"sign".equals(k)
                    && !"key".equals(k)) {
                sb.append(k + "=" + v + "&");
            }
        }
        sb.append("key=" + appKey);
		System.out.println("md5 sb:" + sb);
		String sign = MD5.MD5Encode(sb.toString()).toUpperCase();
        return sign;
 
    }
	
	/**
	 * 从API返回的XML数据里面重新计算一次签名，排除API返回的数据中sign字段
	 * 
	 * @param responseString
	 *            API返回的XML数据
	 * @param appKey
	 *            密钥
	 * @return 新鲜出炉的签名
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 */
	public static String getSignFromResponseString(String responseString,
			String appKey) throws IOException, SAXException,
			ParserConfigurationException {
		Map<String, Object> map = XMLParser.getMapFromXML(responseString);
		// 清掉返回数据对象里面的Sign数据（不能把这个数据也加进去进行签名），然后用签名算法进行签名
		map.put("sign", "");
		// 将API返回的数据根据用签名算法进行计算新的签名，用来跟API返回的签名进行比较
		return Signature.getSign(map, appKey);
	}

	/**
	 * 检验API返回的数据里面的签名是否合法，避免数据在传输的过程中被第三方篡改
	 * 
	 * @param responseString
	 *            API返回的XML数据字符串
	 * @param appKey
	 *            密钥
	 * @return API签名是否合法
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 */
	public static boolean checkIsSignValidFromResponseString(
			String responseString, String appKey)
			throws ParserConfigurationException, IOException, SAXException {

		Map<String, Object> map = XMLParser.getMapFromXML(responseString);

		String signFromAPIResponse = map.get("sign").toString();
		if (signFromAPIResponse == "" || signFromAPIResponse == null) {
			// Util.log("API返回的数据签名数据不存在，有可能被第三方篡改!!!");
			return false;
		}
		// Util.log("服务器回包里面的签名是:" + signFromAPIResponse);
		// 清掉返回数据对象里面的Sign数据（不能把这个数据也加进去进行签名），然后用签名算法进行签名
		map.put("sign", "");
		// 将API返回的数据根据用签名算法进行计算新的签名，用来跟API返回的签名进行比较
		String signForAPIResponse = Signature.getSign(map, appKey);

		if (!signForAPIResponse.equals(signFromAPIResponse)) {
			// 签名验不过，表示这个API返回的数据有可能已经被篡改了
			// Util.log("API返回的数据签名验证不通过，有可能被第三方篡改!!!");
			return false;
		}
		// Util.log("恭喜，API返回的数据签名验证通过!!!");
		return true;
	}
	
	/**
	* <p>功能描述:js调用微信接口时所需要的签名， Sha1签名</p>
	* <p>参数：@param str
	* <p>参数：@return</p>
	* <p>返回类型：String</p>
	 */
	public static String getSha1(String str) {
		if (str == null || str.length() == 0) {
			return null;
		}
		char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'a', 'b', 'c', 'd', 'e', 'f' };
		try {
			MessageDigest mdTemp = MessageDigest.getInstance("SHA1");
			mdTemp.update(str.getBytes("UTF-8"));
			byte[] md = mdTemp.digest();
			int j = md.length;
			char buf[] = new char[j * 2];
			int k = 0;
			for (int i = 0; i < j; i++) {
				byte byte0 = md[i];
				buf[k++] = hexDigits[byte0 >>> 4 & 0xf];
				buf[k++] = hexDigits[byte0 & 0xf];
			}
			return new String(buf);
		} catch (Exception e) {
			return null;
		}
	}
	
}
