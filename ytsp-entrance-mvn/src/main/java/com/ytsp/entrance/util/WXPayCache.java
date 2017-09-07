package com.ytsp.entrance.util;

import java.util.HashMap;
import java.util.Map;

/**
 * 获取mongos所需要的hashKey
 */
public class WXPayCache {
	
	private long limitTime = 3600000l;
	
	private static WXPayCache wxPayCache;
	//缓存微信支付时的accessToken
	private Map<String,String> normalAccessTokenMap = new HashMap<String,String>();
	//缓存微信支付时的jsap_ticket
	private Map<String,String> JSAPIticketMap = new HashMap<String,String>();
	//缓存ticket的关键key
	private String ticketKey = "ticket";
	//缓存accessToken的关键key
	private String accessTokenKey = "accessToken";
	
	private WXPayCache(){
		
	}
	
	public String getTicketKey() {
		return ticketKey;
	}

	public String getAccessTokenKey() {
		return accessTokenKey;
	}

	public static synchronized WXPayCache getInstance(){
		if(wxPayCache == null){
			wxPayCache = new WXPayCache();
		}
		return wxPayCache;
	}

	public long getLimitTime() {
		return limitTime;
	}

	public void setLimitTime(long limitTime) {
		this.limitTime = limitTime;
	}

	public Map<String, String> getNormalAccessTokenMap() {
		return normalAccessTokenMap;
	}

	public void setNormalAccessTokenMap(Map<String, String> normalAccessTokenMap) {
		this.normalAccessTokenMap = normalAccessTokenMap;
	}

	public Map<String, String> getJSAPIticketMap() {
		return JSAPIticketMap;
	}

	public void setJSAPIticketMap(Map<String, String> jSAPIticketMap) {
		JSAPIticketMap = jSAPIticketMap;
	}
	
}
