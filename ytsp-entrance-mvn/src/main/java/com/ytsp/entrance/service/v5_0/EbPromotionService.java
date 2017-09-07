package com.ytsp.entrance.service.v5_0;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ytsp.db.dao.CustomerCollectionDao;
import com.ytsp.db.dao.EbPromotionDao;
import com.ytsp.db.dao.EbPromotionItemDao;
import com.ytsp.db.domain.EbPromotion;
import com.ytsp.db.exception.SqlException;


@Service("ebPromotionService")
@Transactional
public class EbPromotionService {
	static final Logger logger = Logger.getLogger(EbPromotionService.class);
	private EbPromotionDao ebPromotionDao;
	private EbPromotionItemDao ebPromotionItemDao;
	@Resource(name="customerCollectionDao")
	private CustomerCollectionDao customerCollectionDao;

	public CustomerCollectionDao getCustomerCollectionDao() {
		return customerCollectionDao;
	}


	public void setCustomerCollectionDao(CustomerCollectionDao customerCollectionDao) {
		this.customerCollectionDao = customerCollectionDao;
	}


	public EbPromotionDao getEbPromotionDao() {
		return ebPromotionDao;
	}


	public void setEbPromotionDao(EbPromotionDao ebPromotionDao) {
		this.ebPromotionDao = ebPromotionDao;
	}

	public EbPromotionItemDao getEbPromotionItemDao() {
		return ebPromotionItemDao;
	}

	public void setEbPromotionItemDao(EbPromotionItemDao ebPromotionItemDao) {
		this.ebPromotionItemDao = ebPromotionItemDao;
	}

	/**
	 * 检索有效的促销活动列表，status=1，且不过期
	 * 
	 * @return
	 * @throws SqlException
	 */
	public List<EbPromotion> retrieveEbPromotionList() throws SqlException {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
		return ebPromotionDao
				.findAllByHql(
						" WHERE status=1 AND startDate<? AND endDate>? ORDER BY promotionType",
						new Object[] { calendar.getTime(), calendar.getTime() });
	}
	
	public int hasCollection(int productId,int userId){
		StringBuffer sb = new StringBuffer();
		try {
			sb.append("select count(1) from ytsp_customer_collection where ").
			append("userId="+userId+
					" and  productId="+productId);
			return  customerCollectionDao.sqlCount(sb.toString());
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
	
}
