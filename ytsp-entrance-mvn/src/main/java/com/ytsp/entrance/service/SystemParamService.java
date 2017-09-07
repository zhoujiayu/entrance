package com.ytsp.entrance.service;

import java.util.List;

import org.apache.log4j.Logger;

import com.ytsp.db.dao.SystemParamDao;
import com.ytsp.db.domain.SystemParam;
import com.ytsp.db.exception.SqlException;
import com.ytsp.entrance.system.SystemManager;
import com.ytsp.entrance.system.SystemParamInDB;

/**
 * @author GENE
 * @description 系统存于DB中的参数服务
 */
public class SystemParamService {
	private static final Logger logger = Logger.getLogger(SystemParamService.class);

	private SystemParamDao systemParamDao;

	public  List<SystemParam> readAllSystemParam() throws Exception {
		return systemParamDao.findAllByHql(null);
	}
	
	public void syncVar() throws Exception {
		SystemParamInDB sysParamInDB = new SystemParamInDB();
		List<SystemParam> sps;
		try {
			sps = systemParamDao.findAllByHql(null);
			for(SystemParam sp : sps){
				sysParamInDB.put(sp.getKey(), sp.getValue());
			}
		} catch (SqlException e) {
			logger.error("", e);
		}
		
		SystemManager.getInstance().setSystemParamInDB(sysParamInDB);
	}
	

	public SystemParamDao getSystemParamDao() {
		return systemParamDao;
	}

	public void setSystemParamDao(SystemParamDao systemParamDao) {
		this.systemParamDao = systemParamDao;
	}
	
	
}
