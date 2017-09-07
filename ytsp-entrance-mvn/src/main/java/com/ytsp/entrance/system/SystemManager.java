/*
 * $Id: SystemManager.java 240 2011-08-06 11:03:29Z louis $
 * All rights reserved
 */
package com.ytsp.entrance.system; 


/**
 * 系统管理器，单例设计，提供对系统获取信息和操作的入口。
 * 
 * @author Louis
 */
public class SystemManager {

	private static final SystemManager INSTANCE = new SystemManager();
	
	private SystemConfig systemConfig;
	private SystemStatus systemStatus;
	private SystemParamInDB systemParamInDB;
	
	private SystemManager() { }
	
	public static final SystemManager getInstance() {
		return INSTANCE;
	}

	public SystemConfig getSystemConfig() {
		return systemConfig;
	}

	void setSystemConfig(SystemConfig systemConfig) {
		this.systemConfig = systemConfig;
	}

	public SystemStatus getSystemStatus() {
		return systemStatus;
	}

	void setSystemStatus(SystemStatus systemStatus) {
		this.systemStatus = systemStatus;
	}

	public SystemParamInDB getSystemParamInDB() {
		return systemParamInDB;
	}

	public void setSystemParamInDB(SystemParamInDB systemParamInDB) {
		this.systemParamInDB = systemParamInDB;
	}
}
