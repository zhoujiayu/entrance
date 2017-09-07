package com.ytsp.entrance.service.v5_0;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.log4j.Logger;

import com.ytsp.db.dao.EbCouponDao;
import com.ytsp.db.domain.EbCoupon;
import com.ytsp.db.enums.EbCouponSourceEnum;
import com.ytsp.db.enums.EbCouponTypeEnum;
import com.ytsp.db.exception.SqlException;
import com.ytsp.entrance.command.base.CommandContext;
import com.ytsp.entrance.util.Util;

public class EbCouponService {
	static final Logger logger = Logger.getLogger(EbCouponService.class);
	private EbCouponDao ebCouponDao;

	public EbCouponDao getEbCouponDao() {
		return ebCouponDao;
	}

	public void setEbCouponDao(EbCouponDao ebCouponDao) {
		this.ebCouponDao = ebCouponDao;
	}

	/**
	 * 未使用且有效(在使用期范围内)的优惠券
	 * 
	 * @param userId
	 * @return
	 * @throws SqlException
	 */
	public List<EbCoupon> retrieveValidEbCouponList(int userId)
			throws SqlException {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
		return ebCouponDao
				.findAllByHql(
						"WHERE userId=? AND used=? AND couponType=? AND endTime>? AND startTime<? ",
						new Object[] { userId, false, EbCouponTypeEnum.NORMAL,
								calendar.getTime(), calendar.getTime() });
	}

	public EbCoupon retrieveEbCoupon(String serialNumber) throws SqlException {
		return ebCouponDao.findOneByHql(
				"WHERE serialNumber=? AND couponType=?", new Object[] {
						serialNumber, EbCouponTypeEnum.NORMAL });
	}

	public EbCoupon retrieveEbCoupon(int userId, int couponId)
			throws SqlException {
		return ebCouponDao.findOneByHql(
				"WHERE id=? AND userId=? AND couponType=?", new Object[] {
						couponId, userId, EbCouponTypeEnum.NORMAL });
	}

	public void update(EbCoupon ebCoupon) throws SqlException {
		ebCouponDao.update(ebCoupon);
	}

	public EbCoupon getCoupon(int couponId) throws SqlException {
		// TODO Auto-generated method stub
		return ebCouponDao.findById(couponId);
	}

	/**
	 * <p>
	 * 功能描述:获取用户下的所有优惠券
	 * </p>
	 * <p>
	 * 参数：@param userId
	 * <p>
	 * 参数：@return
	 * <p>
	 * 参数：@throws SqlException
	 * </p>
	 * <p>
	 * 返回类型：List<EbCoupon>
	 * </p>
	 */
	public List<EbCoupon> getCouponByUserId(int userId) throws SqlException {
		return ebCouponDao.findAllByHql(
				" WHERE couponType = ? and userId = ? order by id desc ",
				new Object[] { EbCouponTypeEnum.NORMAL, userId });
	}
	
	/**
	* <p>功能描述:根据类型分类获取优惠券：type:0未使用优惠券1已使用优惠券2已过期优惠券</p>
	* <p>参数：@param userId
	* <p>参数：@param type
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：List<EbCoupon></p>
	 */
	public List<EbCoupon> getCouponByUserIdAndType(int userId,int type) throws SqlException{
		List<EbCoupon> coupons = null;
		//未使用优惠券
		if(type == 0){
			coupons = ebCouponDao
					.findAllByHql(
							" WHERE endTime > now() and used = 0 and ebOrder is null and  couponType = ? and userId = ? order by id desc ",
							new Object[] { EbCouponTypeEnum.NORMAL, userId });
		}else if(type == 1){//已使用优惠券
			coupons = ebCouponDao
					.findAllByHql(
							" WHERE (used = 1 or ebOrder is not null) and  couponType = ? and userId = ? order by id desc ",
							new Object[] { EbCouponTypeEnum.NORMAL, userId });
		}else if(type == 2){//已过期优惠券
			coupons = ebCouponDao
					.findAllByHql(
							" WHERE endTime <= now() and (used = 0 or ebOrder is null) and  couponType = ? and userId = ? order by id desc ",
							new Object[] { EbCouponTypeEnum.NORMAL, userId });
		}
		
		return coupons;
	}
	
	/**
	* <p>功能描述:根据类型获取我的优惠券的数量</p>
	* <p>参数：@param userId
	* <p>参数：@param type
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：int</p>
	 */
	public int getMyCouponQuantityByType(int userId,int type) throws SqlException{
		StringBuffer sb = new StringBuffer();
		//未使用优惠券
		if(type == 0){
			sb.append("select count(1) from ytsp_ebiz_coupon where ").append(
					"  endTime > now() and used = 0 and ebOrder is null and  couponType = "
							+ EbCouponTypeEnum.NORMAL.getValue()
							+ " and userId = " + userId);
			return ebCouponDao.sqlCount(sb.toString());
		}else if(type == 1){//已使用优惠券
			sb.append("select count(1) from ytsp_ebiz_coupon ").append(
					"WHERE (used = 1 or ebOrder is not null) and couponType = "
							+ EbCouponTypeEnum.NORMAL.getValue()
							+ " and userId = " + userId);
			return ebCouponDao.sqlCount(sb.toString()); 
		}else if(type == 2){//已过期优惠券
			sb.append("select count(1) from ytsp_ebiz_coupon ")
			  .append(" WHERE endTime <= now() and (used = 0 or ebOrder is null) and  couponType = "+EbCouponTypeEnum.NORMAL.getValue()+" and userId = "+userId);
			return ebCouponDao.sqlCount(sb.toString()); 
		}
		
		return 0;
	}
	
	/**
	 * <p>
	 * 功能描述:分页获取用户的不可用现金券
	 * </p>
	 * <p>
	 * 参数：@param userId
	 * <p>
	 * 参数：@return
	 * <p>
	 * 参数：@throws SqlException
	 * </p>
	 * <p>
	 * 返回类型：List<EbCoupon>
	 * </p>
	 */
	public List<EbCoupon> getUnableCashCouponByPage(int userId,
			Integer couponId, int page, int pageSize) throws SqlException {
		StringBuffer sb = new StringBuffer();
		sb.append(" select * from ytsp_ebiz_coupon WHERE couponType = ")
				.append(EbCouponTypeEnum.NORMAL.getValue());
		sb.append(" and (used = 1 or starttime > now() OR endtime < now() or eborder IS NOT NULL)");
		sb.append(" and (minamount = 0 or minamount is null ) ");
		sb.append(" and userId = ").append(userId);
		if (couponId != null && couponId > 0) {
			sb.append(" and id < ").append(couponId);
		}
		sb.append(" order by id desc ");
		return ebCouponDao.sqlFetch(sb.toString(), EbCoupon.class, page,
				pageSize);
	}

	/**
	 * <p>
	 * 功能描述:分页获取用户的不可用满减券
	 * </p>
	 * <p>
	 * 参数：@param userId
	 * <p>
	 * 参数：@return
	 * <p>
	 * 参数：@throws SqlException
	 * </p>
	 * <p>
	 * 返回类型：List<EbCoupon>
	 * </p>
	 */
	public List<EbCoupon> getUnableReduceCouponByPage(int userId,
			Integer couponId, int page, int pageSize) throws SqlException {
		StringBuffer sb = new StringBuffer();
		sb.append(" select * from ytsp_ebiz_coupon WHERE couponType = ")
				.append(EbCouponTypeEnum.NORMAL.getValue());
		sb.append(" and (used = 1 or starttime > now() OR endtime < now() or eborder IS NOT NULL)");
		sb.append(" and minamount > 0 ");
		sb.append(" and userId = ").append(userId);
		if (couponId != null && couponId > 0) {
			sb.append(" and id < ").append(couponId);
		}
		sb.append(" order by id desc ");
		return ebCouponDao.sqlFetch(sb.toString(), EbCoupon.class, page,
				pageSize);
	}

	/**
	 * <p>
	 * 功能描述:分页获取用户的可用现金券
	 * </p>
	 * <p>
	 * 参数：@param userId
	 * <p>
	 * 参数：@return
	 * <p>
	 * 参数：@throws SqlException
	 * </p>
	 * <p>
	 * 返回类型：List<EbCoupon>
	 * </p>
	 */
	public List<EbCoupon> getCashCouponByPage(int userId, Integer couponId,
			int page, int pageSize) throws SqlException {
		StringBuffer sb = new StringBuffer();
		sb.append("select * from ytsp_ebiz_coupon WHERE couponType = ").append(
				EbCouponTypeEnum.NORMAL.getValue());
		sb.append(" and used = 0 and eborder is null");
		sb.append(" and (minamount = 0 or minamount is null ) ");
		sb.append(" and starttime < now() and endtime > now() ");
		sb.append(" and userId = ").append(userId);
		if (couponId != null && couponId > 0) {
			sb.append(" and id < ").append(couponId);
		}
		sb.append(" order by id desc ");
		return ebCouponDao.sqlFetch(sb.toString(), EbCoupon.class, page,
				pageSize);
	}

	/**
	 * <p>
	 * 功能描述:分页获取用户的可用满减券
	 * </p>
	 * <p>
	 * 参数：@param userId
	 * <p>
	 * 参数：@return
	 * <p>
	 * 参数：@throws SqlException
	 * </p>
	 * <p>
	 * 返回类型：List<EbCoupon>
	 * </p>
	 */
	public List<EbCoupon> getReduceCouponByPage(int userId, Integer couponId,
			int page, int pageSize) throws SqlException {
		StringBuffer sb = new StringBuffer();
		sb.append(" select * from ytsp_ebiz_coupon WHERE couponType = ")
				.append(EbCouponTypeEnum.NORMAL.getValue());
		sb.append(" and used = 0 and eborder is null");
		sb.append(" and minamount > 0 ");
		sb.append(" and starttime < now() and endtime > now() ");
		sb.append(" and userId = ").append(userId);
		if (couponId != null && couponId > 0) {
			sb.append(" and id < ").append(couponId);
		}
		sb.append(" order by id desc ");
		return ebCouponDao.sqlFetch(sb.toString(), EbCoupon.class, page,
				pageSize);
	}

	/**
	 * <p>
	 * 功能描述:校验专场优惠券同一个用户只能领一次
	 * </p>
	 * <p>
	 * 参数：@param userId
	 * <p>
	 * 参数：@param couponId
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：boolean
	 * </p>
	 */
	public boolean isCouponObtain(int userId, int couponId) {
		StringBuffer sb = new StringBuffer();
		sb.append(" select * from ytsp_ebiz_coupon c where c.userid = ")
				.append(userId);
		sb.append(" and exists(select 1 from ytsp_ebiz_coupon d where d.id = ")
				.append(couponId)
				.append(" and c.minAmount = d.minAmount and couponType = 2 and c.batch = d.batch )");

		List<EbCoupon> coupons = ebCouponDao.sqlFetch(sb.toString(),
				EbCoupon.class, -1, -1);
		if (coupons == null || coupons.size() == 0) {
			return false;
		}
		return true;
	}

	/**
	 * <p>
	 * 功能描述:分页获取用户下的现金券
	 * </p>
	 * <p>
	 * 参数：@param userId
	 * <p>
	 * 参数：@return
	 * <p>
	 * 参数：@throws SqlException
	 * </p>
	 * <p>
	 * 返回类型：List<EbCoupon>
	 * </p>
	 */
	public List<EbCoupon> getCanUseCashCouponByPage(int userId, int couponId,
			int page, int pageSize) throws SqlException {
		StringBuffer sql = new StringBuffer();
		sql.append(" WHERE couponType = ? and (minAmount = 0 or minAmount is null) and userId = ? ");
		if (couponId != 0) {
			sql.append(" and id < ").append(couponId);
		}
		sql.append(" order by id desc ");
		return ebCouponDao.findAllByHql(sql.toString(), new Object[] {
				EbCouponTypeEnum.NORMAL, userId });
	}

	public boolean saveObtainEbCoupon(int userId, EbCoupon coupon,CommandContext context)
			throws SqlException {
		int allreadygot = ebCouponDao.getRecordCount(
				" where id !=? and batch=? and minAmount=?",
				new Object[] { coupon.getId(), coupon.getBatch(),
						coupon.getMinAmount() });
		if (coupon.getAvailableCount() != null
				&& allreadygot >= coupon.getAvailableCount())
			return false;
		EbCoupon cp = new EbCoupon();
		Date now = new Date();
		cp.setBatch(coupon.getBatch());
		cp.setCouponMoney(coupon.getCouponMoney());
		cp.setCouponName(coupon.getCouponName());
		cp.setCouponSource(EbCouponSourceEnum.OBTAIN);
		cp.setCouponType(EbCouponTypeEnum.NORMAL);
		cp.setCreateTime(now);
		cp.setDescription(coupon.getDescription());
		cp.setEbProductCollection(coupon.getEbProductCollection());
		Calendar nowDate = Calendar.getInstance();
		nowDate.add(Calendar.DATE, coupon.getAvailableTime() == null ? 0
				: coupon.getAvailableTime());
		cp.setEndTime(nowDate.getTime());
		cp.setForShipping(coupon.getForShipping());
		cp.setMinAmount(coupon.getMinAmount());
		cp.setMoney(coupon.getMoney());
		cp.setOneOnly(coupon.getOneOnly());
		cp.setStartTime(now);
		cp.setUsed(false);
		cp.setUserId(userId);
		ebCouponDao.save(cp);
		//减少优惠券模板可领张数
//		EbCoupon tepCoupon = (EbCoupon) ebCouponDao
//				.getSessionFactory()
//				.getCurrentSession()
//				.load(EbCoupon.class, coupon.getId(),
//						LockMode.UPGRADE);
//		tepCoupon.setAvailableCount(tepCoupon.getAvailableCount() - 1);
//		ebCouponDao.update(tepCoupon);
		
		//将领取的优惠券添加到统计数据中
		Util.addStatistics(context, cp);
 		return true;
	}
}
