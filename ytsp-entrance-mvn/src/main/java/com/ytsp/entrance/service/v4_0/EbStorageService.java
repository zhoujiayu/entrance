package com.ytsp.entrance.service.v4_0;

import java.util.List;

import org.apache.log4j.Logger;

import com.ytsp.db.dao.EbStorageDao;
import com.ytsp.db.domain.EbStorage;
import com.ytsp.db.exception.SqlException;
import com.ytsp.entrance.service.EbSkuService;

public class EbStorageService {
	static final Logger logger = Logger.getLogger(EbSkuService.class);
	
	private EbStorageDao ebStorageDao;

	public EbStorageDao getEbStorageDao() {
		return ebStorageDao;
	}

	public void setEbStorageDao(EbStorageDao ebStorageDao) {
		this.ebStorageDao = ebStorageDao;
	}
	
	
	public void saveStorage(EbStorage ebStorage) throws SqlException {
		ebStorageDao.save(ebStorage);
	}
	
	public void updateStorage(EbStorage ebStorage) throws SqlException {
		ebStorageDao.update(ebStorage);
	}
	
	public void updateBatchStorage(List<EbStorage> ebStorages) {
		ebStorageDao.updateBatch(ebStorages, 50);
	}
}
