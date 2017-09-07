package com.ytsp.entrance.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONObject;

import cn.dongman.util.DongmanNotify;

public class VerifyClientParams {

	public static boolean verifyClient(JSONObject jsonObj) throws Exception
	{
		//先加密认证
		Map<String, String> Params = new HashMap<String, String>();
		for (Iterator<?> iter = jsonObj.keys(); iter.hasNext();) { 
		    String key = (String)iter.next();  
		    Object v = jsonObj.get(key);
		    if(v instanceof Integer)
		    {
		    	Params.put(key, String.valueOf(v));
		    }
		    else
		    {
		    	Params.put(key, (String)v);
		    }
		}
		return DongmanNotify.verifyClient(Params);
	}
}
