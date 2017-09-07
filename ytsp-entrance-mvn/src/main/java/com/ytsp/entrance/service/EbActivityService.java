package com.ytsp.entrance.service;

import java.util.List;

import org.apache.log4j.Logger;

import com.ytsp.db.dao.EbActivityDao;
import com.ytsp.db.domain.EbActivity;
import com.ytsp.db.enums.EbActivityStatusEnum;
import com.ytsp.db.exception.SqlException;

public class EbActivityService {
	
	static final Logger logger = Logger.getLogger(EbActivityService.class);
	
	private EbActivityDao ebActivityDao;

	public EbActivityDao getEbActivityDao() {
		return ebActivityDao;
	}

	public void setEbActivityDao(EbActivityDao ebActivityDao) {
		this.ebActivityDao = ebActivityDao;
	}

	public EbActivityDao getActivityDao() {
		return ebActivityDao;
	}

	public void setEbOrderDao(EbActivityDao ebActivityDao) {
		this.ebActivityDao = ebActivityDao;
	}
	
	public List<EbActivity> retrieveValidActivities() throws SqlException{
//		return ebActivityDao.getAll();
		return ebActivityDao.findAllByHql(" where activityStatus=? order by sortNum", new Object[]{EbActivityStatusEnum.NORMAL});
	}

}
