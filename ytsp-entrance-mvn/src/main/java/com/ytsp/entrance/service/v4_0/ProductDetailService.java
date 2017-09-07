package com.ytsp.entrance.service.v4_0;

import java.util.List;

import org.apache.log4j.Logger;

import com.ytsp.db.dao.EbProductDetailDao;
import com.ytsp.db.domain.EbProductDetail;
import com.ytsp.db.exception.SqlException;

public class ProductDetailService {
	static final Logger logger = Logger.getLogger(ProductDetailService.class);
	
	private EbProductDetailDao ebProductDetailDao;

	
	
	public EbProductDetailDao getEbProductDetailDao() {
		return ebProductDetailDao;
	}

	public void setEbProductDetailDao(EbProductDetailDao ebProductDetailDao) {
		this.ebProductDetailDao = ebProductDetailDao;
	}

	public List<EbProductDetail> getProductDetailByProductId(int productId) throws SqlException {
		return ebProductDetailDao.findAllByHql(" where productCode=?", new Object[]{productId});
	}
	
}
