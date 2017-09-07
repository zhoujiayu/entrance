package com.ytsp.entrance.service.v5_0;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ytsp.db.dao.EbCouponDao;
import com.ytsp.db.dao.EbProductDao;
import com.ytsp.db.dao.EbSpecialDao;
import com.ytsp.db.domain.EbCoupon;
import com.ytsp.db.domain.EbSpecial;
import com.ytsp.db.exception.SqlException;

@Service("ebSpecialService")
@Transactional
public class EbSpecialService {
	@Resource(name="ebSpecialDao")
	private EbSpecialDao ebSpecialDao;

	@Resource(name="ebCouponDao")
	private EbCouponDao ebCouponDao;

	@Resource(name="ebProductDao")
	private EbProductDao EbProductDao;
	
	public EbSpecial getEbSpecialById(int id) throws SqlException{
		EbSpecial ret = ebSpecialDao.findById(id);
		if(ret != null && ret.getCouponTemplates() != null && !"".equals(ret.getCouponTemplates())){
			List<EbCoupon> ls = ebCouponDao.findAllByHql(" where id in("+ret.getCouponTemplates()+")");
			ret.setEbCoupons(ls);
		}
		return ret;
	}
	
	public List<EbSpecial> getEbSpecialList(int page,int pageSize) throws SqlException{
		return ebSpecialDao.findAllByHql(" WHERE 1=1 order by endDate desc", page*pageSize, pageSize, new Object[]{});
	}
}
