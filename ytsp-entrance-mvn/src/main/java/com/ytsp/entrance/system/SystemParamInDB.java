package com.ytsp.entrance.system;

import java.util.HashMap;
import java.util.Map;

/**
 * @author gene
 * @description 系统参数（记录与数据库的参数信息）
 */
public class SystemParamInDB {

	private Map<String, String> kv = new HashMap<String, String>();
	
	public void put(String key, String value){
		kv.put(key, value);
	}
	
	public String getValue(String key){
		return kv.get(key);
	}
	
	public void clear(){
		this.kv.clear();
	}

}
