/*
 * $Id: AuthLevel.java 287 2011-08-07 09:35:56Z louis $
 * All rights reserved
 */
package com.ytsp.entrance.api; 

/**
 * 权限级别模型，包括：资源可读取、资源可删除、资源可更新三种权限。
 * 并提供相应方法进行权限代码的设置和判断操作。
 * 
 * @author Louis
 */
public class AuthLevel {
	
	/** 权限代码：可读 */
	public static final int AUTH_CODE_READ = 1;
	/** 权限代码：可删除 */
	public static final int AUTH_CODE_DELETE = AUTH_CODE_READ << 1;
	/** 权限代码：可更新 */
	public static final int AUTH_CODE_UPDATE = AUTH_CODE_DELETE << 1;
	
    /**
     * 判断给定的权限代码是否有指定级别的权限，例如：
     * can(yourAuthCode, AUTH_CODE_READ) // 返回是否有读取权限
     * 
     * @param authCode 权限代码
     * @param level 判断级别
     * @return 是否有该权限
     */
    public static boolean can(int authCode, int level) {
    	return (authCode & level) != 0;
    }
    
    /**
     * 设置权限代码的级别，使用bitmask对权限级别进行添加或删除，例如：
     * int authCode = AUTH_CODE_READ; // 获得读取权限
     * authCode = set(authCode, AUTH_CODE_DELETE, true); // 加入删除权限，此时authCode已经拥有读取和删除权限
     * authCode = set(authCode, AUTH_CODE_READ, false); // 去除读取权限，此时authCode只剩删除权限
     * 
     * @param authCode 权限代码
     * @param level 更新的权限级别
     * @param value true则添加该权限，false则删除该权限
     * @return 更新后的权限代码
     */
    public static int set(int authCode, int level, boolean value) {
    	if (value) {
    		authCode |= level;
    	} else {
    		authCode &= ~level;
    	}
    	return authCode;
    }

	/**
	 * 判断权限代码是否包含可读取权限。
	 * 
	 * @param authCode 权限代码
	 * @return 是否可读取
	 */
	public static boolean canRead(int authCode) {
		return can(authCode, AUTH_CODE_READ);
	}
	
	/**
	 * 判断权限代码是否包含可删除权限。
	 * 
	 * @param authCode 权限代码
	 * @return 是否可删除
	 */
	public static boolean canDelete(int authCode) {
		return can(authCode, AUTH_CODE_DELETE);
	}
	
	/**
	 * 判断权限代码是否包含可更新权限。
	 * 
	 * @param authCode 权限代码
	 * @return 是否可更新
	 */
	public static boolean canUpdate(int authCode) {
		return can(authCode, AUTH_CODE_UPDATE);
	}
	
}
