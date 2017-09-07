package com.ytsp.entrance.util;


import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * 
 * @author Administrator
 * 
 */
public class WebClient {

	private static String PROXY_IP = "http://10.0.0.172:80";
	public static boolean isCMWAP = false;
	public static String SESSION_ID = "";

	/**
	 * 移动资费标记
	 */
//	private final static String WAP_FLAG = "text/vnd.wap.wml";

	/**
	 * POST方法
	 * 
	 * @param urlString
	 *            接口URL
	 * @param data
	 *            请求数据
	 * @param charset
	 *            编码
	 * @param timeout
	 *            超时时间
	 * @return
	 * @throws IOException
	 */
	public static byte[] getWebContentByPost(String urlString, String data, final String charset, int timeout, String headInfo) throws IOException {
		if (urlString == null || urlString.length() == 0) {
			return null;
		}
		urlString = (urlString.startsWith("http://") || urlString.startsWith("https://")) ? urlString : ("http://" + urlString).intern();
		URL url = null;
		URLParse urlparam = new URLParse(urlString);
		if (isCMWAP) {
			url = new URL(PROXY_IP + urlparam.getPath());
		} else {
			url = new URL(urlString);
		}

		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		if (isCMWAP) {
			connection.setRequestProperty("X-Online-Host", urlparam.getHost() + ":" + urlparam.getPort());
		}
		// 设置是否向connection输出，因为这个是post请求，参数要放在 http正文内，因此需要设为true
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setRequestMethod("POST");
		// Post 请求不能使用缓存
		connection.setUseCaches(false);
		connection.setInstanceFollowRedirects(true);
		// connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
		connection.setRequestProperty("Content-Type", "text/xml;charset=UTF-8");
		// 增加报头，模拟浏览器，防止屏蔽
		connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows xp)");
		// 只接受text/html类型，当然也可以接受图片,pdf, */*任意
		connection.setRequestProperty("Accept", "text/xml");// text/html
		if (headInfo != "" && headInfo != null)
			connection.setRequestProperty("HEAD_INFO", headInfo);
		if (SESSION_ID != null&&SESSION_ID.trim().equals("") )
			connection.setRequestProperty("Cookie", SESSION_ID);
		connection.setConnectTimeout(timeout);
		connection.connect();
		DataOutputStream out = new DataOutputStream(connection.getOutputStream());

		byte[] content = data.getBytes("UTF-8");// +URLEncoder.encode("中文 ",
		// "utf-8");
		// byte[] content = StringUtils.decodeBase64(data);
		out.write(content);
		out.flush();
		out.close();
		try {
			// 必须写在发送数据的后面
			if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
				return null;
				
			}
			String SESSION_ID_TEMP = connection.getHeaderField("Set-Cookie");
			if (SESSION_ID_TEMP != "" && SESSION_ID_TEMP != null)
				SESSION_ID = SESSION_ID_TEMP;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		ByteArrayOutputStream bos = new ByteArrayOutputStream(500000);
		InputStream input = connection.getInputStream();
		int ch = 0;
		while ((ch = input.read()) != -1) {
			bos.write(ch);
		}

		byte[] rspData = bos.toByteArray();
		// rspDataString = StringUtils.encodeBase64(rspData);
		return rspData;
	}

	/**
	 * POST方法
	 * 
	 * @param urlString
	 *            接口URL
	 * @param data
	 *            请求数据
	 * @return
	 * @throws IOException
	 */
	public static byte[] getWebContentByPost(String urlString, String data) throws IOException {
		return getWebContentByPost(urlString, data, "UTF-8", 30000, "");
	}

	/**
	 * POST方法
	 * 
	 * @param urlString
	 *            接口URL
	 * @param data
	 *            请求数据 jason的形式
	 * @param headInfo
	 *            头部数据，jason的形式
	 * @return
	 * @throws IOException
	 */
	public static byte[] getWebContentByPost(String urlString, String data, String headInfo) throws IOException {
		return getWebContentByPost(urlString, data, "UTF-8", 30000, headInfo);
	}

	/**
	 * GET方法
	 * 
	 * @param urlString
	 *            接口URL
	 * @param charset
	 *            编码
	 * @param timeout
	 *            超时
	 * @return
	 * @throws IOException
	 */
	public static byte[] getWebContentByGet(String urlString, final String charset, int timeout, String headInfo) throws IOException {
		if (urlString == null || urlString.length() == 0) {
			return null;
		}
		urlString = (urlString.startsWith("http://") || urlString.startsWith("https://")) ? urlString : ("http://" + urlString).intern();
		URL url = null;
		URLParse urlparam = new URLParse(urlString);
		if (isCMWAP) {
			url = new URL(PROXY_IP + urlparam.getPath());
		} else {
			url = new URL(urlString);
		}
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		if (isCMWAP) {
			conn.setRequestProperty("X-Online-Host", urlparam.getHost() + ":" + urlparam.getPort());
		}
		conn.setRequestMethod("GET");
		// 增加报头，模拟浏览器，防止屏蔽
		conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727)");
		// 只接受text/html类型，当然也可以接受图片,*/*pdf,任意，就是tomcat/conf/web里面定义那些
		conn.setRequestProperty("Accept", "text/html");
		if (headInfo != null && headInfo.length() > 0)
			conn.setRequestProperty("HEAD_INFO", headInfo);
		if (SESSION_ID != null && SESSION_ID.length() > 0)
			conn.setRequestProperty("Cookie", SESSION_ID);
		conn.setConnectTimeout(timeout);
		try {
			if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
				return null;
			}
			String SESSION_ID_TEMP = conn.getHeaderField("Set-Cookie");
			if (SESSION_ID_TEMP != "" && SESSION_ID_TEMP != null)
				SESSION_ID = SESSION_ID_TEMP;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		InputStream input = conn.getInputStream();
		int ch = 0;
		while ((ch = input.read()) != -1) {
			bos.write(ch);
		}

		byte[] rspData = bos.toByteArray();
		// rspDataString = StringUtils.encodeBase64(rspData);
		return rspData;

	}

	/**
	 * GET方法
	 * 
	 * @param urlString
	 *            接口URL
	 * @return
	 * @throws IOException
	 */
	public static byte[] getWebContentByGet(String urlString) throws IOException {
		return getWebContentByGet(urlString, "UTF-8", 30000, "");
	}

	/**
	 * @param urlString
	 * @param headInfo
	 * @return
	 * @throws IOException
	 */
	public static byte[] getWebContentByGet(String urlString, String headInfo) throws IOException {
		return getWebContentByGet(urlString, "UTF-8", 30000, headInfo);
	}

	public static InputStream getImageString(String urlString, int timeout) throws IOException {
		InputStream i = null;
		urlString = (urlString.startsWith("http://") || urlString.startsWith("https://")) ? urlString : ("http://" + urlString).intern();
		URL url = null;
		URLParse urlparam = new URLParse(urlString);

		// 如果这里不是CMWAP，不必要做那么多设置
		// if (!isCMWAP) {
		// url = new URL(urlString);
		// HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		// conn.setDoInput(true);
		// conn.setConnectTimeout(timeout);
		// conn.setRequestMethod("GET");
		// conn.connect();
		// if (conn.getResponseCode() == HttpURLConnection.HTTP_OK)
		// i = conn.getInputStream();
		// return i;
		// }

		// TODO 这里需求优化
		// http://mbsns.zhangshang.mobi/upload/avatar/91467_s_1303463517.png
		// 下载不了！
		if (isCMWAP) {
			url = new URL(PROXY_IP + urlparam.getPath());
		} else {
			url = new URL(urlString);
		}
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		if (isCMWAP) {
			conn.setRequestProperty("X-Online-Host", urlparam.getHost() + ":" + urlparam.getPort());
		}
		conn.setRequestMethod("GET");
		// 增加报头，模拟浏览器，防止屏蔽
		conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727)");
		// 只接受text/html类型，当然也可以接受图片,*/*pdf,任意，就是tomcat/conf/web里面定义那些
		conn.setRequestProperty("Accept", "text/html");
		conn.setConnectTimeout(timeout);
		try {
			if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
				return null;
			}
			i = conn.getInputStream();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		return i;
	}

}
