package com.ytsp.entrance.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

public class HttpUtil {
	
	static HostnameVerifier hv = new HostnameVerifier() {  
        public boolean verify(String urlHostName, SSLSession session) {  
            System.out.println("Warning: URL Host: " + urlHostName + " vs. "  
                               + session.getPeerHost());  
            return true;  
        }
    };  
      
    
	/**
	 * java.net实现 HTTP POST方法提交
	 * 
	 * @param url
	 * @param paramContent
	 * @return
	 */
	public static StringBuffer submitPost(String url, String paramContent) {
		StringBuffer responseMessage = null;
		java.net.URLConnection connection = null;
		java.net.URL reqUrl = null;
		OutputStreamWriter reqOut = null;
		InputStream in = null;
		BufferedReader br = null;
		String param = paramContent;
		try {
			//服务器不信任我们自己创建的证书，所以在代码中必须要忽略证书信任问题
			trustAllHttpsCertificates();
			HttpsURLConnection.setDefaultHostnameVerifier(hv);
			
			responseMessage = new StringBuffer();
			reqUrl = new java.net.URL(url);
			connection = reqUrl.openConnection();
			connection.setDoOutput(true);
			reqOut = new OutputStreamWriter(connection.getOutputStream());
			reqOut.write(param);
			reqOut.flush();
			int charCount = -1;
			in = connection.getInputStream();

			br = new BufferedReader(new InputStreamReader(in, "GBK"));
			while ((charCount = br.read()) != -1) {
				responseMessage.append((char) charCount);
			}
		} catch (Exception ex) {
			System.out.println("url=" + url + "?" + paramContent + "\n e=" + ex);
		} finally {
			try {
				in.close();
				reqOut.close();
			} catch (Exception e) {
				System.out
						.println("paramContent=" + paramContent + "|err=" + e);
			}

		}
		return responseMessage;
	}

	/**
	 * java.net实现 HTTP或HTTPs GET方法提交
	 * 
	 * @param strUrl
	 *            提交的地址及参数
	 * @return 返回的response信息
	 */
	public static String submitGet(String strUrl) {
		URLConnection connection = null;
		BufferedReader reader = null;
		String str = null;
		try {
			System.out.println("send getmethod=" + strUrl);
			URL url = new URL(strUrl);
			connection = url.openConnection();
			connection.setDoInput(true);
			connection.setDoOutput(false);
			// 取得输入流，并使用Reader读取
			reader = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
			System.out
					.println("============Contents of get request===============");
			String lines;
			StringBuffer linebuff = new StringBuffer("");
			while ((lines = reader.readLine()) != null) {
				linebuff.append(lines);
			}
			System.out.println(linebuff);
			System.out
					.println("============Contents of get request ends==========");
			str = linebuff.toString();
		} catch (Exception e) {
			System.out.println("getmethod is err=" + e);
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return str;
	}
	
    private static void trustAllHttpsCertificates() throws Exception {  
        javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];  
        javax.net.ssl.TrustManager tm = new miTM();  
        trustAllCerts[0] = tm;  
        javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext  
                .getInstance("SSL");  
        sc.init(null, trustAllCerts, null);  
        javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sc  
                .getSocketFactory());  
    }  
  
    static class miTM implements javax.net.ssl.TrustManager,  
            javax.net.ssl.X509TrustManager {  
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {  
            return null;  
        }  
  
        public boolean isServerTrusted(  
                java.security.cert.X509Certificate[] certs) {  
            return true;  
        }  
  
        public boolean isClientTrusted(  
                java.security.cert.X509Certificate[] certs) {  
            return true;  
        }  
  
        public void checkServerTrusted(  
                java.security.cert.X509Certificate[] certs, String authType)  
                throws java.security.cert.CertificateException {  
            return;  
        }  
  
        public void checkClientTrusted(  
                java.security.cert.X509Certificate[] certs, String authType)  
                throws java.security.cert.CertificateException {  
            return;  
        }  
    }  
}
