package com.ytsp.entrance.service.v5_0;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;

import com.ytsp.common.util.StringUtil;
import com.ytsp.db.dao.EbProductCollectionDao;
import com.ytsp.db.dao.EbProductDao;
import com.ytsp.db.domain.EbProduct;
import com.ytsp.db.domain.EbProductCollection;
import com.ytsp.db.exception.SqlException;

public class EbProductCellectionService {
	static final Logger logger = Logger.getLogger(EbCouponService.class);
	private EbProductCollectionDao ebProductCollectionDao;
	@Resource(name="ebProductDao")
	private EbProductDao ebProductDao;

	public EbProductCollectionDao getEbProductCollectionDao() {
		return ebProductCollectionDao;
	}

	public void setEbProductCollectionDao(
			EbProductCollectionDao ebProductCollectionDao) {
		this.ebProductCollectionDao = ebProductCollectionDao;
	}

	public List<EbProductCollection> retrieveNonCouponCollection()
			throws SqlException {
		return ebProductCollectionDao.findAllByHql("WHERE collectionType=-1");
	}

	public List<EbProduct> getProductList(EbProductCollection productCollect) throws SqlException {
		
		return ebProductDao.findAllByHql(hql(productCollect));
	}

	public List<EbProduct> getProductList(EbProductCollection productCollect,
			int page, int pageSize) throws SqlException {
		if(productCollect == null){
			return new ArrayList<EbProduct>();
		}
		String selectSql = " select * from ytsp_ebiz_product ";
		String hql = hql(productCollect);
		return ebProductDao.sqlFetch(selectSql + hql.toString(), EbProduct.class, page*pageSize, pageSize);
	}
	
	private String hql(EbProductCollection productCollect){
		String hql = "";
		if(StringUtil.isNotNullNotEmpty(productCollect.getBrandIds())){
			hql += " where ebBrand in ("+productCollect.getBrandIds()+") ";
		}
		if(StringUtil.isNotNullNotEmpty(productCollect.getCategoryIds())){
			if(StringUtil.isNotNullNotEmpty(hql))
				hql += " or ebCatagory in ("+productCollect.getCategoryIds()+")";
			else
				hql += " where ebCatagory in ("+productCollect.getCategoryIds()+")";
		}
		if(StringUtil.isNotNullNotEmpty(productCollect.getProductCodes())){
			if(StringUtil.isNotNullNotEmpty(hql))
				hql += " or  productCode in ("+productCollect.getProductCodes()+")";
			else
				hql +=  " where productCode in ("+productCollect.getProductCodes()+")";
		}
		if(StringUtil.isNotNullNotEmpty(hql))
			hql += " and status = 1";
		return hql;
	}
}
