package com.ytsp.entrance.util;

/**
 * 获取mongos所需要的hashKey
 */
public class HashKeyGeneration {
	private static HashKeyGeneration hashKeyGeneration;
	
	private int hashKey = 0;
	
	private HashKeyGeneration(){
		
	}
	
	public static synchronized HashKeyGeneration getInstance(){
		if(hashKeyGeneration == null){
			hashKeyGeneration = new HashKeyGeneration();
		}
		return hashKeyGeneration;
	}
	
	/**
	* <p>功能描述:获取mongos所需要的hashKey</p>
	* <p>参数：@return</p>
	* <p>返回类型：int</p>
	 */
	public int getHashKey(){
		synchronized (this) {
			return hashKey++%256;
		}
	}
	
}
