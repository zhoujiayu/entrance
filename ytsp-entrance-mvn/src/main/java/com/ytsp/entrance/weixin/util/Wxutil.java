package com.ytsp.entrance.weixin.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.httpclient.HttpClient;
import org.json.JSONArray;
import org.json.JSONObject;


public class Wxutil {

	public static String appid = "wx3ab6c3b6b75c24ec";//appid
	public static String secret = "44de22e8a556d35cd51ce37b92c78dbc";//appkey
	
	public static final String   HD_APPID = "wx21868467d81212ed";
	public static final String  HD_SECRET = "e15d4e63961d2e4db5a8296950486305";
	
	
	
	public static String iType = "i 推荐";
	public static String sType= "家长说";
	public static String pType = "动漫人";
	public static String mType = "动漫与我";
	//循环获取图文消息次数
	public static int num = 3;
	//请求图文条目返回条目数
	public static int pageSize = 10;
	
	//返回数据模型
	public static ArticleList al;
	
	public static List<Article> iList;
	
	public static List<Article> sayList;

	public static List<Article> mList;
	
	public static List<ArticleList> lList;
	
	public static Map<String,List<Article>> resMap;
	
	public  static List<Map<String,List<Article>>> l;
	
	
	
	
	
	//初始化
	public static void init(){
		al = new ArticleList();
		iList = new ArrayList<Article>();
		sayList = new ArrayList<Article>();
		mList = new ArrayList<Article>();
		lList = new ArrayList<ArticleList>();
		resMap = new HashMap<String, List<Article>>();
		
	}
	
	
	 public static String byteToHex(final byte[] hash) {
	        Formatter formatter = new Formatter();
	        for (byte b : hash)
	        {
	            formatter.format("%02x", b);
	        }
	        String result = formatter.toString();
	        formatter.close();
	        return result;
	    }
	
	
	 public static JSONObject sign(String jsapi_ticket, String url) {
		 	JSONObject ret = new JSONObject();
	        String nonce_str = create_nonce_str();
	        String timestamp = create_timestamp();
	        String string1;
	        String signature = "";

	        //注意这里参数名必须全部小写，且必须有序
	        string1 = "jsapi_ticket=" + jsapi_ticket +
	                  "&noncestr=" + nonce_str +
	                  "&timestamp=" + timestamp +
	                  "&url=" + url;

	        try
	        {
	            MessageDigest crypt = MessageDigest.getInstance("SHA-1");
	            crypt.reset();
	            crypt.update(string1.getBytes("UTF-8"));
	            signature = byteToHex(crypt.digest());
	            ret.put("url", url);
		        ret.put("jsapi_ticket", jsapi_ticket);
		        ret.put("nonceStr", nonce_str);
		        ret.put("timestamp", timestamp);
		        ret.put("signature", signature);
	        }
	        catch (Exception e)
	        {
	            e.printStackTrace();
	        }
	        return ret;
	    }
	
	
	public static String create_nonce_str() {
           return UUID.randomUUID().toString();
       }

	public static String create_timestamp() {
           return Long.toString(System.currentTimeMillis() / 1000);
       }
	
	
	
	/**
	 * 根据 access_token 获取 jsapi_ticket
	 */
	public static String getHDTicket(String access_token){
		String ticket = "";
		 String url = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token="+access_token+"&type=jsapi";
		 StringBuffer response = new StringBuffer();
		 HttpClient client = new HttpClient();
			try {
				URL website = new URL(url);
				InputStream in = website.openStream();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(in, "UTF-8"));
				String line;
				while ((line = reader.readLine()) != null) {
					response.append(line);
				}
				reader.close();
				
				JSONObject obj = new JSONObject(response.toString());
				if(obj.has("ticket")){
					ticket =  obj.getString("ticket");
				}
			} catch (Exception e) {
				
			} finally {
				
			}
		return ticket;
		
	}
	
	
	/**
	 * 获取access_token 用户活动
	 */
	public static String  getHDAccessToken(){
		
		 String accessToken = "";
		 String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid="+HD_APPID+"&secret="+HD_SECRET;
		 StringBuffer response = new StringBuffer();
		 HttpClient client = new HttpClient();
			try {
				URL website = new URL(url);
				InputStream in = website.openStream();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(in, "UTF-8"));
				String line;
				while ((line = reader.readLine()) != null) {
					response.append(line);
				}
				reader.close();
				
				JSONObject obj = new JSONObject(response.toString());
				if(obj.has("access_token")){
					accessToken =  obj.getString("access_token");
				}
			} catch (Exception e) {
				
			} finally {
				
			}
		return accessToken;
	}
	
	
	
	
	/**
	 * 获取access_token
	 */
	public static String  getAccessToken(){
		
		 String accessToken = "";
		 String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid="+appid+"&secret="+secret;
		 StringBuffer response = new StringBuffer();
		 HttpClient client = new HttpClient();
			try {
				URL website = new URL(url);
				InputStream in = website.openStream();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(in, "UTF-8"));
				String line;
				while ((line = reader.readLine()) != null) {
					response.append(line);
				}
				reader.close();
				
				JSONObject obj = new JSONObject(response.toString());
				if(obj.has("access_token")){
					accessToken =  obj.getString("access_token");
				}
			} catch (Exception e) {
				
			} finally {
				
			}
		return accessToken;
	}
	
	
	/**
	 * 获取标题中含有制定分类图文消息
	 */
	public static ArticleList  getArticleList(String accessToken){
		ArticleList  aList = new ArticleList();
		if(!"".equals(accessToken)){
			for (int i = 1; i <= num; i++) {
				try {
					getMaterialList(accessToken,i);
				} catch (Exception e) {
				}
			}
		}
		return aList;
	}
	
	
	/**
	 * 调用微信素材列表接口
	 */
	public static ArticleList  getMaterialList(String accessToken,int page){
		try {
			System.out.println("开始时间:"+System.currentTimeMillis());
			URL postUrl = new URL("https://api.weixin.qq.com/cgi-bin/material/batchget_material?access_token="+accessToken);
	        // 打开连接
	        HttpURLConnection connection = (HttpURLConnection) postUrl.openConnection();
	       
	        // 设置是否向connection输出，因为这个是post请求，参数要放在
	        // http正文内，因此需要设为true
	        connection.setDoOutput(true);
	        // Read from the connection. Default is true.
	        connection.setDoInput(true);
	        // 默认是 GET方式
	        connection.setRequestMethod("POST");
	        
	        // Post 请求不能使用缓存
	        connection.setUseCaches(false);
	        
	        connection.setInstanceFollowRedirects(true);
	        
	        // 配置本次连接的Content-type，配置为application/x-www-form-urlencoded的
	        // 意思是正文是urlencoded编码过的form参数，下面我们可以看到我们对正文内容使用URLEncoder.encode
	        // 进行编码
	        connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
	        // 连接，从postUrl.openConnection()至此的配置必须要在connect之前完成，
	        // 要注意的是connection.getOutputStream会隐含的进行connect。
	        connection.connect();
	        DataOutputStream out = new DataOutputStream(connection
	                .getOutputStream());
	        JSONObject obj = new JSONObject();
	        obj.put("type","news");
	        obj.put("offset", (page-1)*pageSize);
	        obj.put("count", pageSize);
	        out.writeBytes(obj.toString());
	        out.flush();
	        out.close(); 
	         
	        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	        String line;
	        StringBuffer response = new StringBuffer();
	        while ((line = reader.readLine()) != null){
	        	response.append(line);
	        }
	        reader.close();
	        connection.disconnect();
	        System.out.println("结束时间:"+System.currentTimeMillis());
	        if(!"".equals(response.toString())){
		        JSONObject o = new JSONObject(response.toString());
		        JSONArray array = (JSONArray) o.get("item");
		        for (int i = 0; i < array.length(); i++) {
		        	JSONObject item = (JSONObject) array.get(i);
		        	if(item.has("content")){
		        		JSONObject content = (JSONObject) item.get("content");
		        		if(content.has("news_item")){
		        			JSONArray news_item = (JSONArray) content.get("news_item");
		        			for (int j = 0; j < news_item.length(); j++) {
								JSONObject article = (JSONObject) news_item.get(j);
//								System.out.println(article);
								if(null!=article.getString("title") && !"".equals(article.getString("title"))){
									Article ae = new Article();
									ae.setDigest(article.getString("digest"));
									ae.setThumb_url(article.getString("thumb_url"));
									ae.setTitle(article.getString("title"));
									ae.setUrl(article.getString("url"))	;
									if(article.getString("title").indexOf(iType)!=-1){
										iList.add(ae);
										
									}else if(article.getString("title").indexOf(sType)!=-1){
										sayList.add(ae);
										
									}else if(article.getString("title").indexOf(pType)!=-1 || article.getString("title").indexOf(mType)!=-1){
										mList.add(ae);
										
									}
								}
							}
		        		}
		        	}
				}
		        resMap.put("0-"+iType, iList);
		        resMap.put("1-"+sType, sayList);
		        resMap.put("2-"+pType, mList);
		        
	        }
		} catch (Exception e) {
			
		}
		return al;
	}
	
	
//	public static void main(String[] args) {
//		init();
//		String accesstoken = getAccessToken();
//		getArticleList(accesstoken);
//		System.out.println(al);
////		getMaterialList(accesstoken,1);
//		
////		String title = "【 i 推荐】我叫你一声，你敢答应吗？？？";
////		if(title.indexOf(iType)!=-1){
////			System.out.println(title);
////		}
//	}
	
}
