package com.ytsp.entrance.service.v4_0;

import java.util.Date;
import java.util.List;

import com.ytsp.db.dao.LaunchAdDao;
import com.ytsp.db.domain.LaunchAd;
import com.ytsp.db.enums.MobileTypeEnum;
import com.ytsp.db.enums.ValidStatusEnum;
import com.ytsp.db.exception.SqlException;

public class LaunchAdService {
	private LaunchAdDao launchAdDao;

	public LaunchAdDao getLaunchAdDao() {
		return launchAdDao;
	}

	public void setLaunchAdDao(LaunchAdDao launchAdDao) {
		this.launchAdDao = launchAdDao;
	}
	
	public List<LaunchAd> findLaunchAd() throws SqlException{
		Date today = new Date();
		 List<LaunchAd> ret = launchAdDao.findAllByHql(" where valid=? and  startTime<? and endTime>? and platType != ? ", 
				 new Object[]{ValidStatusEnum.VALID,today,today,MobileTypeEnum.OTTtv.getText()});
		 return ret ;
	}
}
