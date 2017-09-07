package com.ytsp.entrance.service.v4_0;

import java.util.List;

import org.apache.log4j.Logger;

import com.ytsp.db.dao.EbProductParamDao;
import com.ytsp.db.domain.EbProductParam;
import com.ytsp.db.exception.SqlException;

public class ProductParamService {
	static final Logger logger = Logger.getLogger(ProductParamService.class);
	
	private EbProductParamDao ebProductParamDao;

	
	public EbProductParamDao getEbProductParamDao() {
		return ebProductParamDao;
	}


	public void setEbProductParamDao(EbProductParamDao ebProductParamDao) {
		this.ebProductParamDao = ebProductParamDao;
	}


	public List<EbProductParam> getProductParamByProductId(int productId) throws SqlException {
		return ebProductParamDao.findAllByHql(" where productCode=?", new Object[]{productId});
	}
	
}
