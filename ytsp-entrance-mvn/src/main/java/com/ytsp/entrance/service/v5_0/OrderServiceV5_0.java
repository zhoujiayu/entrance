package com.ytsp.entrance.service.v5_0;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Resource;

import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tencent.wxpay.WXPay;
import com.ytsp.db.audit.AuditAction;
import com.ytsp.db.dao.CustomerDao;
import com.ytsp.db.dao.EbCouponDao;
import com.ytsp.db.dao.EbOrderCouponRecordDao;
import com.ytsp.db.dao.EbOrderDao;
import com.ytsp.db.dao.EbOrderDetailDao;
import com.ytsp.db.dao.EbOrderPromotionRecordDao;
import com.ytsp.db.dao.EbProductDao;
import com.ytsp.db.dao.EbShoppingCartDao;
import com.ytsp.db.dao.EbSkuDao;
import com.ytsp.db.dao.EbStorageDao;
import com.ytsp.db.domain.Customer;
import com.ytsp.db.domain.EbCoupon;
import com.ytsp.db.domain.EbOrder;
import com.ytsp.db.domain.EbOrderCouponRecord;
import com.ytsp.db.domain.EbOrderDetail;
import com.ytsp.db.domain.EbOrderPromotionRecord;
import com.ytsp.db.domain.EbProduct;
import com.ytsp.db.domain.EbShoppingCart;
import com.ytsp.db.domain.EbSku;
import com.ytsp.db.domain.EbStorage;
import com.ytsp.db.enums.EbOrderStatusEnum;
import com.ytsp.db.exception.SqlException;
import com.ytsp.entrance.command.base.CommandContext;
import com.ytsp.entrance.util.Util;
import com.ytsp.entrance.util.alipay.AlipaySubmit;

@Service("orderServiceV5_0")
@Transactional
public class OrderServiceV5_0 {

	@Resource(name = "ebOrderDao")
	private EbOrderDao ebOrderDao;

	@Resource(name = "ebOrderCouponRecordDao")
	private EbOrderCouponRecordDao ebOrderCouponRecordDao;

	@Resource(name = "ebOrderPromotionRecordDao")
	private EbOrderPromotionRecordDao ebOrderPromotionRecordDao;

	@Resource(name = "ebStorageDao")
	private EbStorageDao ebStorageDao;

	@Resource(name = "ebCouponDao")
	private EbCouponDao ebCouponDao;

	@Resource(name = "customerDao")
	private CustomerDao customerDao;

	@Resource(name = "ebOrderDetailDao")
	private EbOrderDetailDao ebOrderDetailDao;
	
	@Resource(name = "ebShoppingCartDao")
	private EbShoppingCartDao ebShoppingCartDao;
	
	@Resource(name = "ebSkuDao")
	private EbSkuDao ebSkuDao;
	
	@Resource(name = "ebProductDao")
	private EbProductDao ebProductDao;
	
	public List<EbOrder> getOrderByUserId(int userId, int start, int limit)
			throws SqlException {
		String hql = " WHERE userId =? ORDER BY orderId desc";
		return ebOrderDao.findAllByHql(hql, start, limit,
				new Object[] { userId });

	}
	
	/**
	* <p>功能描述:再次购买</p>
	* <p>参数：@param orderId</p>
	* <p>返回类型：void</p>
	 * @throws SqlException 
	 */
	public void buyAgain(EbOrder order,CommandContext context) throws SqlException{
		
		Set<EbOrderDetail> orderDetails = order.getOrderDetails();
		StringBuffer sql = new StringBuffer();
		sql.append(" select s.* from ytsp_ebiz_orderdetail od,ytsp_ebiz_sku s ");
		sql.append(" where od.skucode = s.skucode and (od.isgift is null or od.isgift = 0) and od.orderid = ").append(order.getOrderid());
		
		List<EbSku> skus = ebSkuDao.sqlFetch(sql.toString(), EbSku.class, -1, -1);
		
		for (EbOrderDetail detail : orderDetails) {
			if(detail.getIsGift() != null && detail.getIsGift()){
				continue;
			}
			EbShoppingCart cart = new EbShoppingCart();
			EbSku ebSku = getSku(skus,detail.getSkuCode());
			cart.setProductCode(ebSku.getProductCode());
			cart.setProductName(ebSku.getProductName());
			cart.setSkuCode(ebSku.getSkuCode());
			cart.setProductColor(ebSku.getColor());
			cart.setProductSize(ebSku.getSize());
			cart.setAmount(detail.getAmount());
			cart.setAddTime(new Date());
			cart.setStatus(1);
			cart.setUserId(order.getUserId());
			cart.setChecked(true);
			EbProduct ebProduct = ebSku.getParent();
			if(ebProduct == null){
				ebProduct = ebProductDao.findById(ebSku.getProductCode());
			}
			cart.setProductImage(ebProduct.getImgUrl());
			ebShoppingCartDao.save(cart);
			//添加统计信息
			Util.addStatistics(context, cart);
		}
	}
	
	/**
	* <p>功能描述:获取列表中相同skucode的sku</p>
	* <p>参数：@param skus
	* <p>参数：@param skuCode
	* <p>参数：@return</p>
	* <p>返回类型：EbSku</p>
	 */
	private EbSku getSku(List<EbSku> skus,int skuCode){
		if(skuCode == 0){
			return null;
		}
		for (EbSku ebSku : skus) {
			if(ebSku.getSkuCode() == skuCode){
				return ebSku;
			}
		}
		return null;
	}
	
	/**
	 * <p>
	 * 功能描述:分页获取我的订单
	 * </p>
	 * <p>
	 * 参数：@param userId 用户id
	 * <p>
	 * 参数：@param orderId 上一面的最后一个订单id
	 * <p>
	 * 参数：@param pageSize 一页显示的订单数量
	 * <p>
	 * 参数：@return
	 * <p>
	 * 参数：@throws SqlException
	 * </p>
	 * <p>
	 * 返回类型：List<EbOrder>
	 * </p>
	 */
	public List<EbOrder> getMyOrderByPage(int userId, int page, int pageSize,String orderTime)
			throws SqlException {
		StringBuffer sb = new StringBuffer();
//		sb.append(" WHERE orderSource <> 5 and userId =? ");
		sb.append(" WHERE userId =? ");
		sb.append(" ORDER BY orderTime desc ");
		return ebOrderDao.findAllByHql(sb.toString(), page*pageSize, pageSize,
					new Object[] { userId });
	}
	
	/**
	* <p>功能描述:根据类型分类获取订单数量</p>
	* <p>参数：@param userId
	* <p>参数：@param type
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：int</p>
	 */
	public int getMyOrderCountByType(int userId,int type)
			throws SqlException {
		StringBuffer sb = new StringBuffer();
		//全部评论
		sb.append(" select count(1) from ytsp_ebiz_order o WHERE o.userId = ").append(userId);
		if(type == 1){//待付款
			sb.append(" and o.status = "+EbOrderStatusEnum.ORDERSUCCESS.getValue());
		}else if(type == 2){//待收货:其中包括已付款未确认收货，已发货
			sb.append(" and o.status in (");
			sb.append(EbOrderStatusEnum.PAYSUCCESS.getValue()).append(",")
					.append(EbOrderStatusEnum.SUCCESS.getValue()).append(",")
					.append(EbOrderStatusEnum.WAIT.getValue());
			sb.append(")");
		}else if(type == 3){//待评价
			sb.append(" and exists (select 1 from ytsp_ebiz_orderdetail od where od.orderid = o.orderid and (od.commentsId is null or od.commentsId = 0))");
			sb.append(" and o.orderType != 3 and o.status = "+EbOrderStatusEnum.COMPLETE.getValue());
		}else if(type == 4){//已取消
			sb.append(" and o.status = "+EbOrderStatusEnum.CANCEL.getValue());
		}
		sb.append(" ORDER BY o.orderTime desc ");
		
		return ebOrderDao.sqlCount(sb.toString());
		
	}
	
	/**
	* <p>功能描述:分类获取订单列表</p>
	* <p>参数：@param userId
	* <p>参数：@param page
	* <p>参数：@param pageSize
	* <p>参数：@param orderTime
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：List<EbOrder></p>
	 */
	public List<EbOrder> getMyOrderByType(int userId, int page, int pageSize,String orderTime,long orderId,int type)
			throws SqlException {
		StringBuffer sb = new StringBuffer();
		//全部评论
		sb.append(" select * from ytsp_ebiz_order o WHERE userId = ").append(userId);
		if(type == 1){//待付款
			sb.append(" and o.status = "+EbOrderStatusEnum.ORDERSUCCESS.getValue());
		}else if(type == 2){//待收货:其中包括已付款未确认收货，已发货
			sb.append(" and o.status in (");
			sb.append(EbOrderStatusEnum.PAYSUCCESS.getValue()).append(",")
					.append(EbOrderStatusEnum.SUCCESS.getValue()).append(",")
					.append(EbOrderStatusEnum.WAIT.getValue());
			sb.append(")");
		}else if(type == 3){//待评价
			sb.append(" and exists (select 1 from ytsp_ebiz_orderdetail od where od.orderid = o.orderid and (od.commentsId is null or od.commentsId = 0))");
			sb.append(" and o.status = "
					+ EbOrderStatusEnum.COMPLETE.getValue());
		}else if(type == 4){//已取消
			sb.append(" and o.status = "+EbOrderStatusEnum.CANCEL.getValue());
		}
		sb.append(" ORDER BY o.orderTime desc ");
		return ebOrderDao.sqlFetch(sb.toString(), EbOrder.class, page*pageSize, pageSize);
		
	}
	
	public EbOrder getOrderByOrderId(long orderId) throws SqlException {
		String hql = "WHERE orderId =? ";
		return ebOrderDao.findOneByHql(hql, new Object[] { orderId });
	}

	/**
	 * <p>
	 * 功能描述:
	 * </p>
	 * <p>
	 * 参数：@param orderDetailIds 订单详情id集合
	 * <p>
	 * 参数：@return
	 * <p>
	 * 参数：@throws SqlException
	 * </p>
	 * <p>
	 * 返回类型：List<EbOrderPromotionRecord>
	 * </p>
	 */
	public List<EbOrderPromotionRecord> retrievePromotionRecordByIds(
			final Set<Integer> orderDetailIds) throws SqlException {
		if (orderDetailIds == null || orderDetailIds.size() <= 0) {
			return null;
		}
		return ebOrderPromotionRecordDao.getHibernateTemplate().execute(
				new HibernateCallback<List<EbOrderPromotionRecord>>() {

					@Override
					public List<EbOrderPromotionRecord> doInHibernate(
							Session session) throws HibernateException,
							SQLException {
						Query query = session
								.createQuery("from EbOrderPromotionRecord where orderDetailId in (:orderDetailIds)");
						query.setParameterList("orderDetailIds", orderDetailIds);
						return query.list();
					}

				});
	}
	
	/**
	* <p>功能描述:处理取消订单</p>
	* <p>参数：@param order
	* <p>参数：@param userId
	* <p>参数：@return</p>
	* <p>返回类型：int</p>
	 * @throws Exception 
	 */
	public Map<String,Object> updateCancelOrder(EbOrder order, int userId,CommandContext context) throws Exception{
		Map<String,Object> ret = new HashMap<String,Object>();
		int payType = order.getPayType();
		int closeStatus = 0;
		EbOrder ebOrder = (EbOrder) ebOrderDao.getSessionFactory()
				.getCurrentSession()
				.load(EbOrder.class, order.getOrderid(), LockMode.UPGRADE);
		int isUseNewWXPay = Util.isUseNewWXpay(context
				.getHead().getVersion(), context.getHead()
				.getPlatform());
		//支付宝
		if(payType == 1){
			//调用支付宝支付网关闭交易
			closeStatus = AlipaySubmit.closeTrade("", String.valueOf(order.getOrderid()));
		}else if(payType == 3){//微信
			closeStatus = WXPay.closeTrade(String.valueOf(order.getOrderid()), order.getOrderSource(),isUseNewWXPay);
		}
				
		//关闭交易成功或者交易不存在,直接取消订单
		if(closeStatus == 1 || closeStatus == 2){
			// 更新订单状态
			ebOrder.setStatus(EbOrderStatusEnum.CANCEL);
			ebOrderDao.update(ebOrder);
			int userCredits = 0;
			// 排序，解决死锁问题
			EbOrderDetail[] details = new EbOrderDetail[ebOrder.getOrderDetails()
					.size()];
			ebOrder.getOrderDetails().toArray(details);
			Arrays.sort(details, new Comparator<EbOrderDetail>() {
				@Override
				public int compare(EbOrderDetail o1, EbOrderDetail o2) {
					return o1.getSkuCode().intValue() > o2.getSkuCode().intValue() ? 0
							: 1;
				}
			});
			// 更新库存
			for (EbOrderDetail detail : details) {
				EbStorage ebStorage = (EbStorage) ebStorageDao
						.getSessionFactory()
						.getCurrentSession()
						.load(EbStorage.class, detail.getSkuCode(),
								LockMode.UPGRADE);
				ebStorage.setAvailable(ebStorage.getAvailable()
						+ detail.getAmount());
				userCredits += detail.getUseCredits();
				ebStorageDao.update(ebStorage);
			}

			String serialNumber = UUID.randomUUID().toString();
			
			// 将使用的优惠券返回给用户
			updateCoupon(order, userId);
			// 返还用户的积分
			updateUserCredits(userId, userCredits,serialNumber);
			try {
				//发送审记信息
				ebOrderDao.manualAudit(Util.getAudit(AuditAction.UPDATE, ebOrder, "用户取消订单,订单状态变化为取消状态",serialNumber));
				for (EbOrderDetail ebOrderDetail : order.getOrderDetails()) {
					EbStorage storage = ebStorageDao.findOneByHql("WHERE skuCode = "+ebOrderDetail.getSkuCode());
					ebStorageDao.manualAudit(Util.getAudit(AuditAction.UPDATE, storage, "用户取消订单,skuCode:"+ebOrderDetail.getSkuCode()+" 可订量增加："+ebOrderDetail.getAmount(),serialNumber));
				}
			} catch (Exception e2) {
				
			}
			ret.put("msg", "订单取消成功");
			ret.put("success", true);
		}else if(closeStatus == 3){//交易状态不符合
			if(payType == 1){//支付宝
				//调用支付网关查询订单支付状态
				int payStatus = AlipaySubmit.queryPaySuccess("", String.valueOf(order.getOrderid()), order.getTotalPrice());
				if(payStatus == 1){
					//因为订单状态为下单状态，只更新订单状态就可以
					updateOrderStautsByLock(order.getOrderid(), EbOrderStatusEnum.PAYSUCCESS);
					ret.put("msg", "您的订单已支付成功,不可以取消,请刷新后再操作");
					ret.put("success", false);
				}
			}else if(payType == 3){//微信支付
				boolean wxPayStatus = WXPay.payQuery(String.valueOf(order.getOrderid()), order.getOrderSource(),isUseNewWXPay);
				if(wxPayStatus){
					updateOrderStautsByLock(order.getOrderid(), EbOrderStatusEnum.PAYSUCCESS);
					//因为订单状态为下单状态，只更新订单状态就可以
					ret.put("msg", "您的订单已支付成功,不可以取消,请刷新后再操作");
					ret.put("success", false);
				}
			}
			
		}else if(closeStatus == 4){
			ret.put("msg", "网络错误，请稍后再试");
			ret.put("success", true);		
		}else if(closeStatus == 5){
			ret.put("msg", "未获取到支付信息，请稍后再试");
			ret.put("success", true);
		}else{
			cancelOrder(order, userId);
		}
		
		return ret;
	}
	
	/**
	* <p>功能描述:加锁更新订单状态</p>
	* <p>参数：@param orderId
	* <p>参数：@param status
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：void</p>
	 */
	public void updateOrderStautsByLock(long orderId,EbOrderStatusEnum status) throws SqlException{
		EbOrder ebOrder = (EbOrder) ebOrderDao.getSessionFactory()
				.getCurrentSession()
				.load(EbOrder.class, orderId, LockMode.UPGRADE);
		// 更新订单状态
		ebOrder.setStatus(status);
		ebOrderDao.update(ebOrder);
		try {
			//发送审记信息
			ebOrderDao.manualAudit(Util.getAudit(AuditAction.UPDATE, ebOrder, "用户取消订单,订单付款成功,订单状态修改为付款成功",UUID.randomUUID().toString()));
		} catch (Exception e) {
			
		}
	}
	
	/**
	* <p>功能描述:取消订单</p>
	* <p>参数：@param order 订单
	* <p>参数：@param userId 用户id
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：void</p>
	 * @throws Exception 
	 */
	public void cancelOrder(EbOrder order, int userId) throws Exception {
		EbOrder ebOrder = (EbOrder) ebOrderDao.getSessionFactory()
				.getCurrentSession()
				.load(EbOrder.class, order.getOrderid(), LockMode.UPGRADE);
		// 更新订单状态
		ebOrder.setStatus(EbOrderStatusEnum.CANCEL);
		ebOrderDao.update(ebOrder);
		int userCredits = 0;
		// 排序，解决死锁问题
		EbOrderDetail[] details = new EbOrderDetail[ebOrder.getOrderDetails()
				.size()];
		ebOrder.getOrderDetails().toArray(details);
		Arrays.sort(details, new Comparator<EbOrderDetail>() {
			@Override
			public int compare(EbOrderDetail o1, EbOrderDetail o2) {
				return o1.getSkuCode().intValue() > o2.getSkuCode().intValue() ? 0
						: 1;
			}
		});
		// 更新库存
		for (EbOrderDetail detail : details) {
			EbStorage ebStorage = (EbStorage) ebStorageDao
					.getSessionFactory()
					.getCurrentSession()
					.load(EbStorage.class, detail.getSkuCode(),
							LockMode.UPGRADE);
			ebStorage.setAvailable(ebStorage.getAvailable()
					+ detail.getAmount());
			userCredits += detail.getUseCredits();
			ebStorageDao.update(ebStorage);
		}

		String serialNumber = UUID.randomUUID().toString();
		
		// 将使用的优惠券返回给用户
		updateCoupon(order, userId);
		// 返还用户的积分
		updateUserCredits(userId, userCredits,serialNumber);
		try {
			//发送审记信息
			ebOrderDao.manualAudit(Util.getAudit(AuditAction.UPDATE, ebOrder, "用户取消订单,订单状态变化为取消状态",serialNumber));
			for (EbOrderDetail ebOrderDetail : order.getOrderDetails()) {
				EbStorage storage = ebStorageDao.findOneByHql("WHERE skuCode = "+ebOrderDetail.getSkuCode());
				ebStorageDao.manualAudit(Util.getAudit(AuditAction.UPDATE, storage, "用户取消订单,skuCode:"+ebOrderDetail.getSkuCode()+" 可订量增加："+ebOrderDetail.getAmount(),serialNumber));
			}
		} catch (Exception e2) {
			
		}
	}

	/**
	 * <p>
	 * 功能描述:更新返回用户的积分
	 * </p>
	 * <p>
	 * 参数：@param userId 用户id
	 * <p>
	 * 参数：@param credits 订单使用的积分数量
	 * <p>
	 * 参数：@throws SqlException
	 * </p>
	 * <p>
	 * 返回类型：void
	 * </p>
	 */
	private void updateUserCredits(int userId, int credits,String serialNumber) throws SqlException {
		if (userId == 0 || credits == 0) {
			return;
		}
		Customer cust = (Customer) customerDao
				.getSessionFactory()
				.getCurrentSession()
				.load(Customer.class, userId,
						LockMode.UPGRADE);
		cust.setCredits(credits + cust.getCredits());
		customerDao.update(cust);
		//发送审记消息
		try {
			customerDao.manualAudit(Util.getAudit(AuditAction.UPDATE, cust, "下单用户所使用积分："+credits,serialNumber));
		} catch (Exception e) {
		}
	}

	/**
	 * <p>
	 * 功能描述:将使用的优惠券返回给用户
	 * </p>
	 * <p>
	 * 参数：@param order　　订单
	 * <p>
	 * 参数：@param userId　用户id
	 * <p>
	 * 参数：@throws SqlException
	 * </p>
	 * <p>
	 * 返回类型：void
	 * </p>
	 */
	private void updateCoupon(EbOrder order, int userId) throws SqlException {
		// 将使用的优惠券返回给用户
		List<EbCoupon> couponsList = ebCouponDao.findAllByHql(
				" WHERE eborder = ? and userId = ?",
				new Object[] { order.getOrderid(), userId });
		EbCoupon coupon = null;
		if (couponsList != null && couponsList.size() > 0) {
			coupon = couponsList.get(0);
			coupon.setEbOrder(null);
			coupon.setUsed(false);
			coupon.setUseTime(null);
			ebCouponDao.update(coupon);
		}
	}

	public void updateOrder(EbOrder order) throws SqlException {
		ebOrderDao.update(order);
	}

	/**
	 * <p>
	 * 功能描述:获取未评论的订单
	 * </p>
	 * <p>
	 * 参数：@param userId 用户id
	 * <p>
	 * 参数：@param orderStatus 订单状态
	 * <p>
	 * 参数：@param page 页数
	 * <p>
	 * 参数：@param pageSize 每页显示个数
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：List<EbOrder>
	 * </p>
	 */
	public List<EbOrder> getWaitCommentOrder(int userId, String orderTime,
			int page, int pageSize) {
		StringBuffer sb = new StringBuffer();
		sb.append(" select od.* from ytsp_ebiz_order od where ");
		sb.append(" exists(select 1 from ytsp_ebiz_orderdetail a where a.orderid = od.orderid and (a.commentsId is null or a.commentsId = 0)) ");
		sb.append(" and od.orderType!=3 and od.status = ").append(EbOrderStatusEnum.COMPLETE.getValue())
//		  .append(" and od.orderSource <> 5 ")
		  .append(" and od.userId = ").append(userId);
//		if (StringUtil.isNotNullNotEmpty(orderTime)) {
//			sb.append(" and od.orderTime < '").append(orderTime).append("'");
//		}
		sb.append(" order by od.orderTime desc");
		return ebOrderDao.sqlFetch(sb.toString(), EbOrder.class, page
				* pageSize, pageSize);
	}

	public int getWaitCommentOrderCount(int userId) {
		StringBuffer sb = new StringBuffer();
		sb.append(" select count(1) from ytsp_ebiz_order o where ");
		sb.append(" exists(select 1 from ytsp_ebiz_orderdetail od where o.orderid = od.orderid and (od.commentsId is null or od.commentsId = 0))");
		sb.append(" and o.orderType!=3 and o.status = ").append(EbOrderStatusEnum.COMPLETE.getValue())
//		  .append(" and o.orderSource <> 5 ")
		  .append(" and o.userid = ").append(userId);
		return ebOrderDao.sqlCount(sb.toString());
	}

	public List<EbOrderDetail> getDetailsByCommentIds(
			final Set<Integer> commtenIds) {
		return ebOrderDetailDao.getHibernateTemplate().execute(
				new HibernateCallback<List<EbOrderDetail>>() {
					@Override
					public List<EbOrderDetail> doInHibernate(Session session)
							throws HibernateException, SQLException {
						Query query = session
								.createQuery("from EbOrderDetail where commentsId in (:commentsIds) order by commentsid desc ");
						query.setParameterList("commentsIds", commtenIds);
						return query.list();
					}
				});
	}

	/**
	 * <p>
	 * 功能描述:获取已评论的订单
	 * </p>
	 * <p>
	 * 参数：@param userId 用户id
	 * <p>
	 * 参数：@param orderId 分页的最后一个orderId
	 * <p>
	 * 参数：@param orderStatus 订单状态
	 * <p>
	 * 参数：@param page 页数
	 * <p>
	 * 参数：@param pageSize 每页显示个数
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：List<EbOrder>
	 * </p>
	 */
	// public List<EbOrder> getHaveCommentOrder(int userId,long orderId,int
	// orderStatus,int page,int pageSize){
	// StringBuffer sb = new StringBuffer();
	// sb.append(" select od.* from ytsp_ebiz_order od,ytsp_ebiz_orderdetail a where od.status = ").append(orderStatus);
	// sb.append(" and od.orderid = a.orderId and a.commentsId is not null and od.userId = ").append(userId);
	// if(orderId != 0){
	// sb.append(" and orderId < ").append(orderId);
	// }
	// sb.append(" order by orderId desc");
	// return ebOrderDao.sqlFetch(sb.toString(), EbOrder.class, page*pageSize,
	// pageSize);
	// }

	/**
	 * <p>
	 * 功能描述:根据订单id获取优惠券记录
	 * </p>
	 * <p>
	 * 参数：@param orderId
	 * <p>
	 * 参数：@return
	 * <p>
	 * 参数：@throws SqlException
	 * </p>
	 * <p>
	 * 返回类型：EbOrderCouponRecord
	 * </p>
	 */
	public EbOrderCouponRecord getCouponRecordByOrderId(long orderId)
			throws SqlException {
		return ebOrderCouponRecordDao.findOneByHql(" WHERE orderId =?",
				new Object[] { orderId });
	}

	public EbOrderDao getEbOrderDao() {
		return ebOrderDao;
	}

	public void setEbOrderDao(EbOrderDao ebOrderDao) {
		this.ebOrderDao = ebOrderDao;
	}

	public EbOrderCouponRecordDao getEbOrderCouponRecordDao() {
		return ebOrderCouponRecordDao;
	}

	public void setEbOrderCouponRecordDao(
			EbOrderCouponRecordDao ebOrderCouponRecordDao) {
		this.ebOrderCouponRecordDao = ebOrderCouponRecordDao;
	}

	public EbOrderPromotionRecordDao getEbOrderPromotionRecordDao() {
		return ebOrderPromotionRecordDao;
	}

	public void setEbOrderPromotionRecordDao(
			EbOrderPromotionRecordDao ebOrderPromotionRecordDao) {
		this.ebOrderPromotionRecordDao = ebOrderPromotionRecordDao;
	}

	public EbStorageDao getEbStorageDao() {
		return ebStorageDao;
	}

	public void setEbStorageDao(EbStorageDao ebStorageDao) {
		this.ebStorageDao = ebStorageDao;
	}
	
	

}
