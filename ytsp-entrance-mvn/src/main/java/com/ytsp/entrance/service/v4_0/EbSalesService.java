package com.ytsp.entrance.service.v4_0;

import java.util.List;

import org.apache.log4j.Logger;

import com.ytsp.db.dao.EbSalesDao;
import com.ytsp.db.domain.EbSales;
import com.ytsp.db.exception.SqlException;

public class EbSalesService {
	static final Logger logger = Logger.getLogger(CommentService.class);
	private EbSalesDao ebSalesDao;
	public EbSalesDao getEbSalesDao() {
		return ebSalesDao;
	}
	public void setEbSalesDao(EbSalesDao ebSalesDao) {
		this.ebSalesDao = ebSalesDao;
	}
	
	public void saveEbSales(EbSales obj) throws SqlException{
		ebSalesDao.saveOrUpdate(obj);
	}
	
	public EbSales findOne(int userid,int productCode) throws SqlException{
		return ebSalesDao.findOneByHql(" where userid=? and product="+productCode,
				new Object[]{userid});
	}
	public List<EbSales> find(int uid) throws SqlException {
		return ebSalesDao.findAllByHql(" where userid=?",
				new Object[]{uid});
	}
}
