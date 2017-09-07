package com.ytsp.entrance.service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import cn.dongman.util.UtilDate;

import com.tencent.wxpay.WXPay;
import com.ytsp.db.audit.AuditAction;
import com.ytsp.db.dao.CustomerDao;
import com.ytsp.db.dao.CustomerMemberDao;
import com.ytsp.db.dao.EbCouponDao;
import com.ytsp.db.dao.EbOrderCouponRecordDao;
import com.ytsp.db.dao.EbOrderDao;
import com.ytsp.db.dao.EbOrderDetailDao;
import com.ytsp.db.dao.EbOrderPayLogDao;
import com.ytsp.db.dao.EbProductDao;
import com.ytsp.db.dao.EbPromotionDao;
import com.ytsp.db.dao.EbReturnOrderDao;
import com.ytsp.db.dao.EbSalesDao;
import com.ytsp.db.dao.EbShoppingCartDao;
import com.ytsp.db.dao.EbSkuDao;
import com.ytsp.db.dao.EbStorageDao;
import com.ytsp.db.domain.Customer;
import com.ytsp.db.domain.CustomerMember;
import com.ytsp.db.domain.EbCoupon;
import com.ytsp.db.domain.EbOrder;
import com.ytsp.db.domain.EbOrderCouponRecord;
import com.ytsp.db.domain.EbOrderDetail;
import com.ytsp.db.domain.EbOrderPayLog;
import com.ytsp.db.domain.EbOrderPromotionRecord;
import com.ytsp.db.domain.EbProduct;
import com.ytsp.db.domain.EbProductCollection;
import com.ytsp.db.domain.EbPromotion;
import com.ytsp.db.domain.EbPromotionItem;
import com.ytsp.db.domain.EbReturnOrder;
import com.ytsp.db.domain.EbSales;
import com.ytsp.db.domain.EbShoppingCart;
import com.ytsp.db.domain.EbSku;
import com.ytsp.db.domain.EbStorage;
import com.ytsp.db.enums.CreditSourceTypeEnum;
import com.ytsp.db.enums.EbCouponSourceEnum;
import com.ytsp.db.enums.EbCouponTypeEnum;
import com.ytsp.db.enums.EbOrderPayLogStatusEnum;
import com.ytsp.db.enums.EbOrderSourceEnum;
import com.ytsp.db.enums.EbOrderStatusEnum;
import com.ytsp.db.enums.EbOrderTypeEnum;
import com.ytsp.db.enums.EbPromotionTypeEnum;
import com.ytsp.db.enums.PayStatusEnum;
import com.ytsp.db.enums.ValidStatusEnum;
import com.ytsp.db.exception.SqlException;
import com.ytsp.db.vo.GiftItem;
import com.ytsp.entrance.command.base.CommandContext;
import com.ytsp.entrance.command.v4_0.EbOrderCommand;
import com.ytsp.entrance.command.v4_0.EbOrderCommand.OutOfStorageException;
import com.ytsp.entrance.command.v5_0.OOSExceprion;
import com.ytsp.entrance.service.v5_0.EbPromotionService;
import com.ytsp.entrance.system.SystemInitialization;
import com.ytsp.entrance.util.Base32;
import com.ytsp.entrance.util.DateTimeFormatter;
import com.ytsp.entrance.util.DoubleUtil;
import com.ytsp.entrance.util.Md5Encrypt;
import com.ytsp.entrance.util.Util;
import com.ytsp.entrance.util.alipay.AlipaySubmit;
@Transactional (propagation = Propagation.REQUIRED,isolation=Isolation.DEFAULT)
public class EbOrderService {

	static final Logger logger = Logger.getLogger(EbOrderService.class);

	private EbOrderDao ebOrderDao;
	private EbStorageDao ebStorageDao;
	private EbReturnOrderDao ebReturnOrderDao;
	private EbSkuDao ebSkuDao;
	private CustomerDao customerDao;
	private EbCouponDao ebCouponDao;
	private EbOrderDetailDao ebOrderDetailDao;
	private EbShoppingCartDao ebShoppingCartDao;
	private EbOrderCouponRecordDao ebOrderCouponRecordDao;
	private EbSalesDao ebSalesDao;
	private EbOrderPayLogDao ebOrderPayLogDao;
	private EbPromotionDao ebPromotionDao;
	private EbProductDao ebProductDao;
	private CustomerMemberDao customerMemberDao;
	// private EbProductDao ebProductDao;
	
	public EbOrderDao getEbOrderDao() {
		return ebOrderDao;
	}

	public CustomerMemberDao getCustomerMemberDao() {
		return customerMemberDao;
	}

	public void setCustomerMemberDao(CustomerMemberDao customerMemberDao) {
		this.customerMemberDao = customerMemberDao;
	}

	public EbPromotionDao getEbPromotionDao() {
		return ebPromotionDao;
	}

	public void setEbPromotionDao(EbPromotionDao ebPromotionDao) {
		this.ebPromotionDao = ebPromotionDao;
	}

	public EbProductDao getEbProductDao() {
		return ebProductDao;
	}

	public void setEbProductDao(EbProductDao ebProductDao) {
		this.ebProductDao = ebProductDao;
	}

	public void setEbOrderDao(EbOrderDao ebOrderDao) {
		this.ebOrderDao = ebOrderDao;
	}

	public EbStorageDao getEbStorageDao() {
		return ebStorageDao;
	}

	public void setEbStorageDao(EbStorageDao ebStorageDao) {
		this.ebStorageDao = ebStorageDao;
	}

	public EbReturnOrderDao getEbReturnOrderDao() {
		return ebReturnOrderDao;
	}

	public void setEbReturnOrderDao(EbReturnOrderDao ebReturnOrderDao) {
		this.ebReturnOrderDao = ebReturnOrderDao;
	}

	public EbSkuDao getEbSkuDao() {
		return ebSkuDao;
	}

	public void setEbSkuDao(EbSkuDao ebSkuDao) {
		this.ebSkuDao = ebSkuDao;
	}

	public CustomerDao getCustomerDao() {
		return customerDao;
	}

	public void setCustomerDao(CustomerDao customerDao) {
		this.customerDao = customerDao;
	}

	public EbCouponDao getEbCouponDao() {
		return ebCouponDao;
	}

	public void setEbCouponDao(EbCouponDao ebCouponDao) {
		this.ebCouponDao = ebCouponDao;
	}

	public EbOrderDetailDao getEbOrderDetailDao() {
		return ebOrderDetailDao;
	}

	public void setEbOrderDetailDao(EbOrderDetailDao ebOrderDetailDao) {
		this.ebOrderDetailDao = ebOrderDetailDao;
	}

	public EbShoppingCartDao getEbShoppingCartDao() {
		return ebShoppingCartDao;
	}

	public void setEbShoppingCartDao(EbShoppingCartDao ebShoppingCartDao) {
		this.ebShoppingCartDao = ebShoppingCartDao;
	}

	public EbOrderCouponRecordDao getEbOrderCouponRecordDao() {
		return ebOrderCouponRecordDao;
	}

	public void setEbOrderCouponRecordDao(
			EbOrderCouponRecordDao ebOrderCouponRecordDao) {
		this.ebOrderCouponRecordDao = ebOrderCouponRecordDao;
	}

	public EbSalesDao getEbSalesDao() {
		return ebSalesDao;
	}

	public void setEbSalesDao(EbSalesDao ebSalesDao) {
		this.ebSalesDao = ebSalesDao;
	}

	public EbOrderPayLogDao getEbOrderPayLogDao() {
		return ebOrderPayLogDao;
	}

	public void setEbOrderPayLogDao(EbOrderPayLogDao ebOrderPayLogDao) {
		this.ebOrderPayLogDao = ebOrderPayLogDao;
	}

	public EbOrder retrieveOrderByOrderId(long orderId) throws SqlException {
		return ebOrderDao.findById(orderId);
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
		int closeStatus = 0;
		EbOrder ebOrder = (EbOrder) ebOrderDao.getSessionFactory()
				.getCurrentSession()
				.load(EbOrder.class, order.getOrderid(), LockMode.UPGRADE);
		int payType = ebOrder.getPayType();
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
			cancelOrder(ebOrder, userId);
			ret.put("msg", "订单取消成功");
			ret.put("success", true);
		}else if(closeStatus == 3){//交易状态不符合
			if(payType == 1){//支付宝
				//调用支付网关查询订单支付状态
				int payStatus = AlipaySubmit.queryPaySuccess("", String.valueOf(order.getOrderid()), order.getTotalPrice());
				if(payStatus == 1){
					//因为订单状态为下单状态，只更新订单状态就可以
					ebOrder.setStatus(EbOrderStatusEnum.PAYSUCCESS);
					ebOrderDao.update(ebOrder);
					try {
						//发送审记信息
						ebOrderDao.manualAudit(Util.getAudit(AuditAction.UPDATE, ebOrder, "用户取消订单,订单付款成功,订单状态修改为付款成功",UUID.randomUUID().toString()));
					} catch (Exception e) {
						logger.error("用户取消订单更新订单状态发送审记信息失败");
					}
					ret.put("msg", "您的订单已支付成功,不可以取消,请刷新后再操作");
					ret.put("success", false);
				}
			}else if(payType == 3){//微信支付
				boolean wxPayStatus = WXPay.payQuery(String.valueOf(order.getOrderid()), order.getOrderSource(),isUseNewWXPay);
				if(wxPayStatus){
					// 更新订单状态
					ebOrder.setStatus(EbOrderStatusEnum.PAYSUCCESS);
					ebOrderDao.update(ebOrder);
					try {
						//发送审记信息
						ebOrderDao.manualAudit(Util.getAudit(AuditAction.UPDATE, ebOrder, "用户取消订单,订单付款成功,订单状态修改为付款成功",UUID.randomUUID().toString()));
					} catch (Exception e) {
						logger.error("用户取消订单更新订单状态发送审记信息失败");
					}
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
			cancelOrder(ebOrder, userId);
		}
		
		Util.addStatistics(context, ebOrder);
		return ret;
	}
	/**
	* <p>功能描述:取消订单</p>
	* <p>参数：@param order 订单
	* <p>参数：@param userId 用户id
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：void</p>
	 * @throws Exception 
	 */
	public void cancelOrder(EbOrder ebOrder, int userId) throws Exception {
		// 更新订单状态
		ebOrder.setStatus(EbOrderStatusEnum.CANCEL);
		ebOrder.setCancelType(2);
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
		updateCoupon(ebOrder, userId);
		// 返还用户的积分
		updateUserCredits(userId, userCredits,serialNumber,ebOrder.getOrderid());
		try {
			//发送审记信息
			ebOrderDao.manualAudit(Util.getAudit(AuditAction.UPDATE, ebOrder, "用户取消订单,订单状态变化为取消状态",serialNumber));
			for (EbOrderDetail ebOrderDetail : ebOrder.getOrderDetails()) {
				EbStorage storage = ebStorageDao.findOneByHql("WHERE skuCode = "+ebOrderDetail.getSkuCode());
				ebStorageDao.manualAudit(Util.getAudit(AuditAction.UPDATE, storage, "用户取消订单,skuCode:"+ebOrderDetail.getSkuCode()+" 可订量增加："+ebOrderDetail.getAmount(),serialNumber));
			}
		} catch (Exception e2) {
			logger.error("订单取消发送审记信息失败");
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
	private void updateUserCredits(int userId, int credits,String serialNumber,long orderId) throws SqlException {
		if (userId == 0 || credits == 0) {
			return;
		}
		int userOldCredits = 0;
		Customer cust = (Customer) customerDao
				.getSessionFactory()
				.getCurrentSession()
				.load(Customer.class, userId,
						LockMode.UPGRADE);
		userOldCredits = cust.getCredits();
		cust.setCredits(credits + cust.getCredits());
		customerDao.update(cust);
		//发送审记消息
		try {
			customerDao.manualAudit(Util.getAudit(AuditAction.UPDATE, cust, "用户取消订单返回积分："+credits,serialNumber));
			Util.saveCreditRecord(CreditSourceTypeEnum.CANCELORDER.getValue(), userId, "取消订单返还积分（订单号："+orderId+"）", credits, userOldCredits);
		} catch (Exception e) {
			logger.error("用户取消订单，返回积分，发送审记消息失败");
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
			logger.error("用户取消订单更新订单状态发送审记信息失败");
		}
	}
	
	/**
	 * 促销
	 * 
	 * @param ebOrder
	 * @param giftItems
	 * @throws SqlException
	 * @throws OOSExceprion
	 */
	public void promotionProcess(EbOrder ebOrder, List<GiftItem> giftItems)
			throws SqlException, OOSExceprion {
		EbPromotionService ebPromotionService = SystemInitialization
				.getApplicationContext().getBean(EbPromotionService.class);
		List<EbPromotion> ebPromotions = ebPromotionService
				.retrieveEbPromotionList();
		if (ebPromotions != null && ebPromotions.size() > 0) {
			for (EbPromotion foo : ebPromotions) {
				if (foo.getPromotionType() == EbPromotionTypeEnum.REDUCE) {
					reducePromotionProcess(foo, ebOrder);
				} else if (foo.getPromotionType() == EbPromotionTypeEnum.GIFT) {
					giftPromotionProcess(foo, ebOrder, giftItems);
				}
			}
		}
	}

	/**
	 * 满赠
	 * 
	 * @param ebOrder
	 * @param giftItems
	 * @throws SqlException
	 * @throws OOSExceprion
	 */
	private void giftPromotionProcess(EbPromotion ebPromotion, EbOrder ebOrder,
			List<GiftItem> giftItems) throws SqlException, OOSExceprion {
		// 判断促销政策的有效性
		Date current = new Date();
		if (ebPromotion != null && ebPromotion.getEbPromotionItems() != null
				&& ebPromotion.getEbPromotionItems().size() > 0
				&& ebPromotion.getStatus() == ValidStatusEnum.VALID
				&& ebPromotion.getPromotionType() == EbPromotionTypeEnum.GIFT
				&& current.before(ebPromotion.getEndDate())
				&& current.after(ebPromotion.getStartDate())) {

			double totalPrice = 0d;
			boolean isSuit = false;
			List<EbOrderDetail> record = new ArrayList<EbOrderDetail>();
			for (EbOrderDetail ebOrderDetail : ebOrder.getOrderDetails()) {
				if (ebPromotion.getIsForAll()) {// 促销范围：全部商品
					totalPrice += ebOrderDetail.getPrice()
							* ebOrderDetail.getAmount();
					isSuit = true;
					record.add(ebOrderDetail);
				} else if (ebPromotion.getEbProductCollection() != null) {
					if (isInCollection(ebPromotion.getEbProductCollection(),
							ebOrderDetail.getProductCode())) {
						totalPrice += ebOrderDetail.getPrice()
								* ebOrderDetail.getAmount();
						isSuit = true;
						record.add(ebOrderDetail);
					}
				}
			}
			// 同一促销活动只能适用一种促销策略
			EbPromotionItem item = null;
			for (EbPromotionItem foo : ebPromotion.getEbPromotionItems()) {
				if (isSuit) {
					if (totalPrice >= foo.getStandardPrice()) {
						if (item == null) {
							item = foo;
						} else {
							item = item.getStandardPrice() > foo
									.getStandardPrice() ? item : foo;
						}
					}
				}
			}
			if (item != null) {
				if (!item.getIsOptional()) {
					List<EbSku> gifts = getGiftSkus(item.getGifts());
					// 礼品是不可选的,直接放进商品列表
					if (gifts != null && gifts.size() > 0) {
						Map<Integer, EbProduct> prodMap = new HashMap<Integer, EbProduct>();
						try {
							prodMap = getEbProductHash(gifts);
						} catch (Exception e) {
							e.printStackTrace();
						}
						for (EbSku ebSku : gifts) {
							if (ebSku != null && ebSku.getStorage() != null
									&& ebSku.getStorage().getAvailable() > 0) {
								EbOrderDetail ebOrderDetail = new EbOrderDetail();
								ebOrderDetail.setAmount(1);
								ebOrderDetail.setPrice(0d);
								ebOrderDetail.setProductCode(ebSku
										.getProductCode());
								ebOrderDetail.setVendorProductCode(prodMap.get(
										ebSku.getProductCode())
										.getVendorProductCode());
								ebOrderDetail.setSkuCode(ebSku.getSkuCode());
								ebOrderDetail.setTotalPrice(0d);
								ebOrderDetail.setProductName(ebSku
										.getProductName());
								ebOrderDetail.setColor(ebSku.getColor());
								ebOrderDetail.setSize(ebSku.getSize());
								//
								ebOrderDetail.setImageSrc(prodMap.get(
										ebSku.getProductCode()).getImgUrl());
								ebOrderDetail.setOrderId(ebOrder.getOrderid());
								ebOrderDetail.setParent(ebOrder);
								ebOrderDetail.setUseCredits(0);

								// 标识礼品
								ebOrderDetail.setIsGift(true);
								ebOrder.getOrderDetails().add(ebOrderDetail);
								if (!item.getIsOptional()) {
									break;
								}
							}
						}
					}
				} else {
					// 礼品是可选的
					// 判断所选的商品在不在满足的促销活动礼品列表中
					EbSkuService ebSkuService = SystemInitialization
							.getApplicationContext()
							.getBean(EbSkuService.class);
					// 赠品库存不足
					List<GiftItem> oosGiftItems = new ArrayList<GiftItem>();
					for (GiftItem gift : giftItems) {
						if (item.getPromotionItemId().intValue() == gift
								.getPromotionItemId()) {
							EbSku ebSku = ebSkuService
									.retrieveEbSkuBySkuCode(gift.getSkuCode());
							if (ebSku != null) {
								if (ebSku.getStorage() == null
										|| ebSku.getStorage().getAvailable() <= 0) {
									oosGiftItems.add(gift);
									throw new OOSExceprion("选择的赠品已赠完",
											oosGiftItems);
								} else {
									EbOrderDetail ebOrderDetail = new EbOrderDetail();
									ebOrderDetail.setOrderId(ebOrder
											.getOrderid());
									ebOrderDetail.setParent(ebOrder);
									ebOrderDetail.setProductCode(ebSku
											.getProductCode());
									ebOrderDetail.setProductName(ebSku
											.getProductName());
									ebOrderDetail
											.setSkuCode(ebSku.getSkuCode());
									ebOrderDetail.setColor(ebSku.getColor());
									ebOrderDetail.setSize(ebSku.getSize());
									EbProduct ebProduct = ebSku.getParent();
									ebOrderDetail
											.setVendorProductCode(ebProduct
													.getVendorProductCode());
									ebOrderDetail.setImageSrc(ebProduct
											.getImgUrl());
									ebOrderDetail.setPrice(0d);
									ebOrderDetail.setAmount(1);
									ebOrderDetail.setTotalPrice(0d);
									ebOrderDetail.setUseCredits(0);

									// 标识礼品
									ebOrderDetail.setIsGift(true);

									ebOrder.getOrderDetails()
											.add(ebOrderDetail);
								}
							} else {
								oosGiftItems.add(gift);
								throw new OOSExceprion("选择的赠品不存在", oosGiftItems);
							}
						}
					}
				}
				// 促销记录
				for (EbOrderDetail d : record) {
					d.getPromotionRecords().add(
							recordPromotion(d, item, 0d, ebPromotion));
				}
			}
		}
	}

	private List<EbSku> getGiftSkus(String skuCodes) throws SqlException {
		if (skuCodes == null || skuCodes.length() <= 0) {
			return null;
		}
		Set<Integer> skuCodeSet = new HashSet<Integer>();
		String[] skuCodeArr = skuCodes == null ? null : skuCodes.split(",");
		if (skuCodeArr == null || skuCodeArr.length <= 0) {
			return null;
		}
		for (int i = 0; i < skuCodeArr.length; i++) {
			int code = Integer.parseInt(skuCodeArr[i]);
			skuCodeSet.add(code);
		}
		EbSkuService skuService = SystemInitialization.getApplicationContext()
				.getBean(EbSkuService.class);
		List<EbSku> skus = skuService.retrieveEbSkuBySkuCodes(skuCodeSet);
		return skus;
	}

	/**
	 * @功能描述: 将EbProduct列表转换成Map,其key值为productCode,value值为EbProduct对象
	 * @return Map<Integer,EbSku>
	 * @author yusf
	 */
	private Map<Integer, EbProduct> getEbProductHash(List<EbSku> skus)
			throws Exception {
		if (skus == null || skus.size() <= 0) {
			return null;
		}
		Set<Integer> prodCodeSet = new HashSet<Integer>();
		for (EbSku ebSku : skus) {
			prodCodeSet.add(ebSku.getProductCode());
		}
		EbProductService ebProductService = SystemInitialization
				.getApplicationContext().getBean(EbProductService.class);
		List<EbProduct> productList = ebProductService
				.retrieveEbProductByCodes(prodCodeSet);
		Map<Integer, EbProduct> retMap = new HashMap<Integer, EbProduct>();
		for (int i = 0; i < productList.size(); i++) {
			EbProduct product = productList.get(i);
			if (product == null || product.getProductCode() == null) {
				continue;
			}
			retMap.put(product.getProductCode(), product);
		}
		return retMap;
	}

	protected EbOrderPromotionRecord recordPromotion(
			EbOrderDetail ebOrderDetail, EbPromotionItem item, double subFee,
			EbPromotion ebPromotion) {
		EbOrderPromotionRecord promotionRecord = new EbOrderPromotionRecord();
		promotionRecord.setPromotionId(ebPromotion.getPromotionId());
		promotionRecord.setPromotionName(ebPromotion.getPromotionName());
		promotionRecord.setPromotionItemId(item.getPromotionItemId());
		promotionRecord.setPromotionItemName(item.getItemName());
		promotionRecord.setGifts(item.getGifts());
		promotionRecord.setIsOptional(item.getIsOptional());
		promotionRecord.setReducePrice(item.getReducePrice());
		promotionRecord.setStandardPrice(item.getStandardPrice());
		promotionRecord.setParent(ebOrderDetail);
		promotionRecord.setPromotionPrice(subFee);
		return promotionRecord;
	}

	/**
	 * 满减
	 * 
	 * @param ebOrder
	 * @throws SqlException
	 */
	private void reducePromotionProcess(EbPromotion ebPromotion, EbOrder ebOrder)
			throws SqlException {
		if (ebOrder == null || ebOrder.getOrderDetails() == null
				|| ebOrder.getOrderDetails().size() == 0) {
			return;
		}
		// 判断促销政策的有效性
		Date current = new Date();
		if (ebPromotion != null && ebPromotion.getEbPromotionItems() != null
				&& ebPromotion.getEbPromotionItems().size() > 0
				&& ebPromotion.getStatus() == ValidStatusEnum.VALID
				&& ebPromotion.getPromotionType() == EbPromotionTypeEnum.REDUCE
				&& current.before(ebPromotion.getEndDate())
				&& current.after(ebPromotion.getStartDate())) {
			double totalPrice = 0d;
			List<EbOrderDetail> records = new ArrayList<EbOrderDetail>();

			for (EbOrderDetail foo : ebOrder.getOrderDetails()) {
				if (ebPromotion.getIsForAll()) {// 促销范围：全部商品
					totalPrice += foo.getTotalPrice().doubleValue();
					records.add(foo);
				} else if (ebPromotion.getEbProductCollection() != null) {
					if (isInCollection(ebPromotion.getEbProductCollection(),
							foo.getProductCode())) {
						totalPrice += foo.getTotalPrice().doubleValue();
						records.add(foo);
					}
				}

			}

			EbPromotionItem item = null;
			if (records.size() > 0) {
				for (EbPromotionItem foo : ebPromotion.getEbPromotionItems()) {
					if (totalPrice >= foo.getStandardPrice()) {
						if (item == null) {
							item = foo;
						} else {
							item = item.getStandardPrice() > foo
									.getStandardPrice() ? item : foo;
						}
					}
				}
			}

			if (item != null) {
				double subTotalfee = 0d;
				int count = 0;
				double fee = item.getReducePrice();
				// 首先扣减总金额.
				ebOrder.setTotalPrice(ebOrder.getTotalPrice().doubleValue()
						- fee);
				// 开始均摊.
				for (EbOrderDetail ebOrderDetail : records) {
					double subFee = roundDouble((ebOrderDetail.getTotalPrice()
							.doubleValue() / totalPrice) * fee, 2);
					// 处理尾差
					if (records.size() - 1 == count) {
						subFee = DoubleUtil.sub(fee, subTotalfee);
					}
					ebOrderDetail.setTotalPrice(DoubleUtil.sub(ebOrderDetail.getTotalPrice()
							.doubleValue(),subFee));
					// 促销记录
					ebOrderDetail.getPromotionRecords().add(
							recordPromotion(ebOrderDetail, item, subFee,
									ebPromotion));
					subTotalfee = DoubleUtil.add(subTotalfee, subFee);
					count++;
				}
			}
		}
	}

	/**
	 * <p>
	 * 功能描述:四舍五入double类型
	 * </p>
	 * <p>
	 * 参数：@param d
	 * <p>
	 * 参数：@param length
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：double
	 * </p>
	 */
	private double roundDouble(double d, int length) {
		BigDecimal bd = new BigDecimal(d);
		return bd.setScale(length, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	@SuppressWarnings("deprecation")
	public void saveOrder(EbOrder ebOrder) throws SqlException,
			OutOfStorageException {
		List<EbSku> outOfStorageskus = new ArrayList<EbSku>();// 没有库存的SKU
		boolean outOf = false;
		// 扣减库存,与检查库存是否够用、保存订单在同一个事务中
		for (EbOrderDetail detail : ebOrder.getOrderDetails()) {
			EbStorage ebStorage = (EbStorage) ebStorageDao
					.getSessionFactory()
					.getCurrentSession()
					.load(EbStorage.class, detail.getSkuCode(),
							LockMode.UPGRADE);
			// 下面注释掉的写法是标准写法，但是客户端没有处理库存不足列表，所以可以不要
			// if (ebStorage.getAvailable() <= 0){
			// outOfStorageskus.add(detail.getEbsku());
			// outOf= true;
			// }
			// if(outOf)
			// continue;
			if (ebStorage.getAvailable() <= 0) {
				outOfStorageskus.add(detail.getEbsku());
				outOf = true;
				break;
			}
			ebStorage.setAvailable(ebStorage.getAvailable()
					- detail.getAmount());
			ebStorageDao.update(ebStorage);
		}
		if (outOf) {
			OutOfStorageException e = new EbOrderCommand.OutOfStorageException(
					"超出库存");
			e.addOutOfStorageSkus(outOfStorageskus);
			throw e;
		} else {
			int useCredits = 0;
			for (EbOrderDetail detail : ebOrder.getOrderDetails()) {
				useCredits += detail.getUseCredits();
			}
			Customer customer = customerDao.findById(ebOrder.getUserId());
			int userC = customer.getCredits() == null ? 0 : customer
					.getCredits().intValue();
			useCredits = userC > useCredits ? useCredits : userC;
			customer.setCredits(userC - useCredits);
			customerDao.update(customer);
		}
		// TODO DEBUG
		ebOrderDao.save(ebOrder);
	}
	
	/**
	* <p>功能描述:确认收货</p>
	* <p>参数：@param order
	* <p>参数：@throws Exception</p>
	* <p>返回类型：void</p>
	 */
	public void processCompleteOrder(EbOrder order,CommandContext context) throws Exception{
		//锁表更新订单状态
		EbOrder ebOrder = (EbOrder) ebOrderDao.getSessionFactory()
				.getCurrentSession()
				.load(EbOrder.class, order.getOrderid(), LockMode.UPGRADE);
		ebOrder.setCompleteTime(new Date());
		ebOrder.setStatus(EbOrderStatusEnum.COMPLETE);
		ebOrderDao.update(ebOrder);
		// 下单反积分，VIP双倍积分
		int returnCredits = (int) Math.round(order.getTotalPrice());
		returnCredits = isMember(order.getUserId()) ? returnCredits * 2
				: returnCredits;
		
		//赠送用户积分
		int oldUserCredits = 0;
		Customer c = (Customer) customerDao.getSessionFactory()
				.getCurrentSession()
				.load(Customer.class, ebOrder.getUserId(), LockMode.UPGRADE);
		if (c != null) {
			oldUserCredits = c.getCredits();
			c.setCredits(c.getCredits().intValue() + returnCredits);
			customerDao.update(c);
		}
		// 赠送优惠券
		sendCouponByPromotion(order);
		//发送审记信息
		try {
			Util.saveCreditRecord(CreditSourceTypeEnum.COMPLETEORDER.getValue(), ebOrder.getUserId(), "购物完成送积分(订单号："+ebOrder.getOrderid()+")", returnCredits, oldUserCredits);
			String serialNumber = UUID.randomUUID().toString();
			customerDao.manualAudit(Util.getAudit(AuditAction.UPDATE, c, "用户确认收货，赠送用户积分："+returnCredits,serialNumber));
			ebOrderDao.manualAudit(Util.getAudit(AuditAction.UPDATE, ebOrder, "用户确认收货，修改订单状态为：确认收货状态",serialNumber));
		} catch (Exception e) {
			logger.error("------>>确认收货发送审记信息失败");
		}
		Util.addStatistics(context, ebOrder);
	}
	
	public void createOrder(EbOrder ebOrder, EbCoupon ebCoupon,
			List<EbShoppingCart> shoppingcarts, EbOrderCouponRecord record)
			throws SqlException, OOSExceprion, JSONException {
		OOSExceprion e = new OOSExceprion("超出库存");
		boolean outOf = false;
		int totalUseCredits = 0;
		//排序，解决死锁问题
		EbOrderDetail[] details = new EbOrderDetail[ ebOrder.getOrderDetails().size()];
		ebOrder.getOrderDetails().toArray(details);
		Arrays.sort(details, new Comparator<EbOrderDetail>() {
			@Override
			public int compare(EbOrderDetail o1, EbOrderDetail o2) {
				return o1.getSkuCode().intValue() > o2.getSkuCode().intValue() ? 0:1;
			}
		});
		
		Map<Integer,EbStorage> storageMap = new HashMap<Integer, EbStorage>();
		// 扣减可订量、与判断可订量放在一个hql中，保证原子性
		for (EbOrderDetail detail : details) {
			EbStorage storage = ebStorageDao.findById(detail.getSkuCode());
			int available = storage.getAvailable();
			int rows = 0;
			if(available>=detail.getAmount())
				rows = ebStorageDao.updateByQuery("update EbStorage set available=available-? where " +
					"available>=? and skuCode=?", new Object[]{detail.getAmount(),detail.getAmount(),detail.getSkuCode()});
			if(rows==0){
				if (detail.getIsGift() != null && detail.getIsGift()) {
					e.addOOSGiftItem(detail, available);
				} else {
					e.addOOSCartItem(detail, available);
				}
				outOf = true;
				break;
			}
			totalUseCredits += detail.getUseCredits();
			try {
				EbStorage s = storage._getCopy();
				s.setAvailable(available-detail.getAmount());
				s.setSkuCode(detail.getSkuCode());
				storageMap.put(detail.getSkuCode(), s);
			} catch (Exception e1) {
				logger.error("复制EbStorage出错");
			}
		}
		if (outOf) {
			throw e;
		}
		ebOrderDao.save(ebOrder);
		// TODO 更新积分
		Customer c = (Customer) customerDao.getSessionFactory()
				.getCurrentSession()
				.load(Customer.class, ebOrder.getUserId(), LockMode.UPGRADE);
		//保存积分记录
		Util.saveCreditRecord(CreditSourceTypeEnum.CREATEORDER.getValue(), ebOrder.getUserId(), "下单使用积分（订单号："+ebOrder.getOrderid()+"）", 0-totalUseCredits, c.getCredits());
		c.setCredits(c.getCredits() - totalUseCredits);
		customerDao.update(c);
		
		if (ebCoupon != null) {
			ebCouponDao.update(ebCoupon);
		}

		if (shoppingcarts != null && shoppingcarts.size() > 0) {
			List<EbShoppingCart> carts = getShoppingCarts(shoppingcarts);
			if (carts != null && carts.size() > 0) {
				for (EbShoppingCart ebShoppingCart : carts) {
					ebShoppingCart.setStatus(0);
				}
			}
			ebShoppingCartDao.updateAll(carts);
		}
		if (record != null) {
			// 保存优惠券使用记录
			List<EbOrderDetail> ebOrderDetails = new ArrayList<EbOrderDetail>();
			ebOrderCouponRecordDao.save(record);
			for (EbOrderDetail d : record.getEbOrderDetails()) {
				// TODO
				d.setCouponRecord(record.getId());
				ebOrderDetails.add(d);
			}
			ebOrderDetailDao.updateAll(ebOrderDetails);
		}
		
		try {
			//发送审记信息
			String serialNumber = UUID.randomUUID().toString();
			ebOrderDao.manualAudit(Util.getAudit(AuditAction.CREATE, ebOrder, "创建订单成功",serialNumber));
			customerDao.manualAudit(Util.getAudit(AuditAction.UPDATE, c, "下单用户所使用积分："+totalUseCredits,serialNumber));
			for (EbOrderDetail ebOrderDetail : details) {
				if(!storageMap.containsKey(ebOrderDetail.getSkuCode())){
					continue;
				}
				ebStorageDao.manualAudit(Util.getAudit(AuditAction.UPDATE, storageMap.get(ebOrderDetail.getSkuCode()), "用户下单购买商品,skuCode:"+ebOrderDetail.getSkuCode()+" 可订量减少："+ebOrderDetail.getAmount(),serialNumber));
			}
		} catch (Exception e2) {
			logger.error("订单创建时发送审记信息失败");
		}
	}

	private List<EbShoppingCart> getShoppingCarts(
			List<EbShoppingCart> shoppingcarts) throws SqlException {
		List<EbShoppingCart> cartList = null;
		String ids = "";
		for (int i = 0; i < shoppingcarts.size(); i++) {
			EbShoppingCart ebShoppingCart = shoppingcarts.get(i);
			if (i == shoppingcarts.size() - 1) {
				ids += "" + ebShoppingCart.getId() + "";

			} else {
				ids += "" + ebShoppingCart.getId() + ",";
			}
		}
		cartList = retrieveEbShoppingByIds(ids);
		return cartList;
	}

	public List<EbShoppingCart> retrieveEbShoppingByIds(String cartIds)
			throws SqlException {
		return ebShoppingCartDao.findAllByHql(" WHERE id in(" + cartIds + ")");
	}

	public EbShoppingCart retrieveShoppingCart(int id) throws SqlException {
		EbShoppingCart ebShoppingCart = ebShoppingCartDao.findOneByHql(
				" WHERE id=? and status=0", new Object[] { id });
		return ebShoppingCart;
	}

	public void savePayVipOrder(EbOrder ebOrder) throws SqlException {
		ebOrderDao.save(ebOrder);
		for (EbOrderDetail od : ebOrder.getOrderDetails()) {
			od.getEbsku();
		}
	}

	public void updateOrder(EbOrder ebOrder) throws SqlException {
		ebOrderDao.update(ebOrder);
	}

	public void cancelOrder(EbOrder ebOrder) throws SqlException {
		ebOrder.setStatus(EbOrderStatusEnum.CANCEL);
		for (EbOrderDetail ebOrderDetail : ebOrder.getOrderDetails()) {
			EbSku ebSku = ebSkuDao.findById(ebOrderDetail.getSkuCode());
			EbStorage ebStorage = ebSku.getStorage();
			int num = ebStorage.getAvailable();
			ebStorage.setAvailable(num + ebOrderDetail.getAmount());
			ebStorageDao.update(ebStorage);
		}
	}

	public void saveReturnOrderAndChangeOrderStatus(EbReturnOrder returnOrder)
			throws SqlException {
		EbOrder ebOrder = returnOrder.getOrder();
		ebOrder.setStatus(EbOrderStatusEnum.RETURN);
		ebOrderDao.update(ebOrder);
		ebReturnOrderDao.save(returnOrder);
	}

	public JSONObject getOrderById(long id) throws Exception {
		EbOrder ebOrder = ebOrderDao.findById(id);
		JSONObject jo = new JSONObject();
		jo.put("address", ebOrder.getAddress());
		jo.put("areaName", ebOrder.getAreaName());
		jo.put("cellphone", ebOrder.getCellphone());
		jo.put("cityName", ebOrder.getCityName());
		jo.put("userName", ebOrder.getUserName());
		jo.put("orderTime",
				DateTimeFormatter.dateTime2String(ebOrder.getOrderTime()));
		if (ebOrder.getOrderType() == EbOrderTypeEnum.EGG)
			jo.put("orderType", EbOrderTypeEnum.NORMAL.getValue());
		else
			jo.put("orderType", ebOrder.getOrderType().getValue());
		jo.put("orderid", ebOrder.getOrderid());
		jo.put("payType", ebOrder.getPayType());
		jo.put("imgSrc", ebOrder.getImgSrc());
		jo.put("logistics", ebOrder.getLogistics());
		jo.put("provinceName", ebOrder.getProvinceName());
		jo.put("status", ebOrder.getStatus().getValue());
		jo.put("totalPrice", ebOrder.getTotalPrice());
		// 目前每个订单只有一个detail；VIP会员没有detail
		Object[] objs = ebOrder.getOrderDetails().toArray();
		if (objs.length > 0) {
			EbOrderDetail ed = (EbOrderDetail) objs[0];
			jo.put("vendorName", "爱看儿童乐园");
			jo.put("vendorPhone", "400-600-0977");
			jo.put("returnAddr", "");
			jo.put("productName", ed.getProductName());
			jo.put("description", ed.getProductName());
			jo.put("productCode", ed.getProductCode());
			// jo.put("activityName",
			// ebProduct.getEbActivity().getActivityName());
			jo.put("price", ed.getPrice());
			jo.put("useCredits", ed.getUseCredits());
			jo.put("color", ed.getColor() == null ? "" : ed.getColor());
			jo.put("size", ed.getSize() == null ? "" : ed.getSize());
		}
		return jo;
	}

	public JSONArray getOrderByUser(int uid) throws Exception {
		List<EbOrder> ls = ebOrderDao
				.findAllByHql(
						" where userId=? and (orderSource<>? or orderSource is NULL)  order by orderTime desc",
						new Object[] { uid, EbOrderSourceEnum.WEBSITE });
		JSONArray ja = new JSONArray();
		for (EbOrder ebOrder : ls) {
			JSONObject jo = new JSONObject();
			// ////////////////////
			if (ebOrder.getOrderDetails() != null
					&& ebOrder.getOrderDetails().size() > 1) {
				continue;
			}
			// ////////////////////
			jo.put("address", ebOrder.getAddress());
			jo.put("areaName", ebOrder.getAreaName());
			jo.put("cellphone", ebOrder.getCellphone());
			jo.put("cityName", ebOrder.getCityName());
			jo.put("orderid", ebOrder.getOrderid());
			jo.put("orderTime",
					DateTimeFormatter.dateTime2String(ebOrder.getOrderTime()));
			jo.put("isNeedInvoice", ebOrder.getIsNeedInvoice());
			jo.put("invoiceTitle", ebOrder.getInvoiceTitle());
			if (ebOrder.getOrderType() == EbOrderTypeEnum.EGG)
				jo.put("orderType", EbOrderTypeEnum.NORMAL);
			else
				jo.put("orderType", ebOrder.getOrderType());
			jo.put("userName", ebOrder.getUserName());
			jo.put("payType", ebOrder.getPayType());
			// jo.put("imgSrc", ebOrder.getImgSrc());
			jo.put("provinceName", ebOrder.getProvinceName());
			// ////////////////////
			int status = ebOrder.getStatus().getValue();
			if (status > 2) {
				status = 2;
			}
			jo.put("status", status);
			// ////////////////////
			// jo.put("status", ebOrder.getStatus().getValue());
			// 目前每个订单只有一个detail
			if (ebOrder.getOrderDetails() == null
					|| ebOrder.getOrderDetails().size() == 0)
				continue;
			EbOrderDetail ed = (EbOrderDetail) ebOrder.getOrderDetails()
					.toArray()[0];
			// ////////////////////
			String imgSrc = ebOrder.getImgSrc();
			if (StringUtils.isEmpty(imgSrc)) {
				imgSrc = "http://webimg.ikan.cn/" + ed.getImageSrc();
			}
			jo.put("imgSrc", ebOrder.getImgSrc());
			// ////////////////////
			jo.put("productName", ed.getProductName());
			jo.put("productCode", ed.getProductCode());
			jo.put("vendorName", "爱看儿童乐园");
			jo.put("vendorPhone", "400-600-0977");
			jo.put("returnAddr", "");
			jo.put("totalPrice", ed.getTotalPrice());
			jo.put("useCredits", ed.getUseCredits());
			jo.put("color", ed.getColor() == null ? "" : ed.getColor());
			jo.put("size", ed.getSize() == null ? "" : ed.getSize());
			ja.put(jo);
		}
		return ja;
	}

	public int addOrderPay(long orderId, int uid) throws SqlException {
		EbOrderPayLog ebOrderPayLog = new EbOrderPayLog();
		ebOrderPayLog.setOrderId(orderId);
		ebOrderPayLog.setStartPayTime(new Date());
		ebOrderPayLog.setStatus(EbOrderPayLogStatusEnum.GO2PAY);
		ebOrderPayLog.setUserId(uid);
		EbOrder ebOrder = ebOrderDao.findById(orderId);
		ebOrderPayLog.setPayType(ebOrder.getPayType());// 支付宝
		ebOrderPayLog.setPayMoney(ebOrder.getOrderDetails().iterator().next()
				.getPrice());
		return ebOrderPayLogDao.saveObject(ebOrderPayLog);
	}

	/**
	 * 添加事务
	 * 
	 * @param orderId
	 * @return
	 */
	public int createOrderPaySuccess(long orderId, Integer payType) {
		try {
			EbOrder ebOrder = (EbOrder) ebOrderDao.getSessionFactory()
					.getCurrentSession()
					.load(EbOrder.class, orderId, LockMode.UPGRADE);
			if (ebOrder.getOrderType() == EbOrderTypeEnum.VIPMEMBER) {
				ebOrder.setStatus(EbOrderStatusEnum.COMMENT);
			} else {
				ebOrder.setStatus(EbOrderStatusEnum.PAYSUCCESS);
			}
			ebOrder.setPayStatus(PayStatusEnum.PAYSUCCESS);
			ebOrder.setPayType(payType);
			ebOrder.setPayTime(new Date());
			ebOrderDao.update(ebOrder);
			List<EbOrderPayLog> ebOrderPayLogs = ebOrderPayLogDao.findAllByHql(
					"where orderId=? order by startPayTime desc",
					new Object[] { orderId });
			// 因为下单后会扣去用户积分，所以付款不用减积分
			// int useCredits = 0;
			// for (EbOrderDetail detail : ebOrder.getOrderDetails()) {
			// useCredits += detail.getUseCredits();
			// }
			// Customer customer = customerDao.findById(ebOrder.getUserId());
			// customer.setCredits(customer.getCredits() - useCredits);
			// customerDao.update(customer);
			for (int i = 0; i < ebOrderPayLogs.size(); i++) {
				if (i == 0)
					ebOrderPayLogs.get(i).setStatus(
							EbOrderPayLogStatusEnum.PAYSUCCESS);
				else
					ebOrderPayLogs.get(i).setStatus(
							EbOrderPayLogStatusEnum.VERIFYMSG_FAILURE);
			}
			ebOrderPayLogDao.updateAll(ebOrderPayLogs);
			// TODO
			// sendCouponByPromotion(ebOrder);
			try {
				String serialNumber = UUID.randomUUID().toString();
				//复制一个副本
				EbOrder auditOrder = ebOrder._getCopy();
				Set<EbOrderDetail> details = new HashSet<EbOrderDetail>();
				//有的实体类是懒加载，所以事务提交后将实体转成json时，此时session已结束，无法获取到懒加载数据。
				for (EbOrderDetail detail : ebOrder.getOrderDetails()) {
					detail.getPromotionRecords();
					details.add(detail._getCopy());
				}
				auditOrder.setOrderid(ebOrder.getOrderid());
				auditOrder.setOrderDetails(details);
				//发送审记信息
				ebOrderDao.manualAudit(Util.getAudit(AuditAction.UPDATE, auditOrder, "订单支付成功,订单状态变化为支付成功",serialNumber));
			} catch (Exception e2) {
				logger.error("订单号："+ebOrder.getOrderid()+"支付成功时发送审记消息失败.\n"+e2.getMessage());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	public void updateOrderClientPaySuccess(long orderId) throws SqlException {
		EbOrder ebOrder = (EbOrder) ebOrderDao.getSessionFactory()
				.getCurrentSession()
				.load(EbOrder.class, orderId, LockMode.UPGRADE);

		if (ebOrder.getStatus().getValue() >= EbOrderStatusEnum.PAYSUCCESS
				.getValue())
			return;
		ebOrder.setStatus(EbOrderStatusEnum.WAIT);
		ebOrder.setPayStatus(PayStatusEnum.WAITPAY);
		ebOrder.setPayTime(new Date());
		ebOrderDao.update(ebOrder);
	}

	public EbOrder getOrderByOrderId(long orderId) throws SqlException {
		String hql = "WHERE orderId =? ";
		return ebOrderDao.findOneByHql(hql, new Object[] { orderId });
	}

	public void updateOrderPaySuccess(long orderId) throws Exception {
		EbOrder ebOrder = (EbOrder) ebOrderDao.getSessionFactory()
				.getCurrentSession()
				.load(EbOrder.class, orderId, LockMode.UPGRADE);

		if (ebOrder.getStatus().getValue() >= EbOrderStatusEnum.PAYSUCCESS
				.getValue())
			return;
		ebOrder.setStatus(EbOrderStatusEnum.WAIT);
		ebOrder.setPayStatus(PayStatusEnum.WAITPAY);
		ebOrder.setPayTime(new Date());
		ebOrderDao.update(ebOrder);
	}

	/**
	 * 找到那些无效的订单 无效订单的定义: 下单超过35分钟没有支付
	 * 
	 * @return
	 * @throws SqlException
	 */
	public List<Long> findInvalidEbOrder() throws SqlException {
		List<EbOrder> list = null;
		List<EbOrder> list1 = null;
		List<Long> ret = new ArrayList<Long>();
		Calendar c = Calendar.getInstance();
		c.add(Calendar.MINUTE, -35);
		list = ebOrderDao
				.findAllByHql(
						"where orderTime<? and status=0 and (payStatus = 0 or payStatus is null) and (orderSource<>? or orderSource is null)",
						new Object[] { c.getTime(), EbOrderSourceEnum.WEBSITE });
		c.add(Calendar.MINUTE, -155);
		list1 = ebOrderDao
				.findAllByHql(
						"where orderTime<? and status=0 and (payStatus = 0 or payStatus is null) and orderSource=?",
						new Object[] { c.getTime(), EbOrderSourceEnum.WEBSITE });
		if (list != null) {
			list.addAll(list1);
			for (EbOrder o : list) {
				ret.add(o.getOrderid());
			}
			return ret;
		}
		for (EbOrder o : list1) {
			ret.add(o.getOrderid());
		}
		return ret;
	}

	public Map<Integer, EbStorage> findStorages(final Set<Integer> skuCodes) {
		List<EbStorage> ebStorages = null;
		Map<Integer, EbStorage> ebStoragesMap = new HashMap<Integer, EbStorage>();
		ebStorages = ebStorageDao.getHibernateTemplate().execute(
				new HibernateCallback<List<EbStorage>>() {

					@Override
					public List<EbStorage> doInHibernate(Session session)
							throws HibernateException, SQLException {
						Query query = session
								.createQuery("from EbStorage where skuCode in (:skuCodes)");
						query.setParameterList("skuCodes", skuCodes);
						return query.list();
					}

				});
		if (ebStorages != null && ebStorages.size() > 0) {
			for (EbStorage ebStorage : ebStorages) {
				if (!ebStoragesMap.containsKey(ebStorage.getSkuCode())) {
					ebStoragesMap.put(ebStorage.getSkuCode(), ebStorage);
				}
			}
		}
		return ebStoragesMap;
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

	public EbStorage retrieveStorageBySkuCode(Integer skuCode)
			throws SqlException {
		return ebStorageDao.findOneByHql("where skuCode = ?",
				new Object[] { skuCode });
	}
	
	/**
	* 功能描述:1.更新15天后没有确认收货的订单为确认收货
	* 	    2.返回积分，vip双倍积分
	*       3.如果买的商品赠送优惠券，赠送优惠券
	* 参数：@param orderid
	* 返回类型：void
	 * @throws Exception 
	 */
	public void updateCompleteOrder(Long orderid) throws Exception{
		if(orderid == null || orderid <= 0){
			return;
		}
		EbOrder ebOrder = (EbOrder) ebOrderDao
				.getSessionFactory()
				.getCurrentSession()
				.load(EbOrder.class, orderid,
						LockMode.UPGRADE);
		
		if(ebOrder == null){
			return;
		}
		
		//更新订单完成时间和状态
		ebOrder.setCompleteTime(new Date());
		ebOrder.setStatus(EbOrderStatusEnum.COMPLETE);
		ebOrderDao.update(ebOrder);
		
		//返回客户所用的积分
		int returnCredits = (int) Math.round(ebOrder.getTotalPrice());
		//是否vip会员,vip会员双倍积分
		if(isMember(ebOrder.getUserId())){
			returnCredits = returnCredits * 2;
		}
		//更新用户积分，vip用户返双倍积分
		if(returnCredits > 0){
			Customer customer = (Customer) customerDao
					.getSessionFactory()
					.getCurrentSession()
					.load(Customer.class, ebOrder.getUserId(),
							LockMode.UPGRADE);
			customer.setCredits(customer.getCredits() + returnCredits);
			customerDao.update(customer);
		}
		//确认收货后返回优惠券
		sendCouponByPromotion(ebOrder);
	}
	
	/**
	* <p>功能描述:校验是否是会员</p>
	* <p>参数：@param userId
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：boolean</p>
	 */
	public boolean isMember(int userId) throws SqlException  {
		boolean isMember = false;
		CustomerMember customerMember = customerMemberDao.findOneByHql(" WHERE customer.id = ? order by endTime desc", new Object[]{userId});
		if(customerMember != null)
		{
			if (customerMember.getValid()) {
				Date endTime = customerMember.getEndTime();
				if(UtilDate.isOverLiveTime(endTime) <= 0) {
					isMember = true;
				}
			}
		}
		return isMember;
	}
	
	/**
	* <p>功能描述:获取订单明细所有的积分</p>
	* <p>参数：@param orderDetails
	* <p>参数：@return</p>
	* <p>返回类型：int</p>
	 */
	private int getOrderUsedCredits(Set<EbOrderDetail> orderDetails){
		int usedCredits = 0;
		if(orderDetails == null || orderDetails.size() <= 0){
			return usedCredits;
		}
		for (EbOrderDetail ebOrderDetail : orderDetails) {
			if(ebOrderDetail != null && ebOrderDetail.getUseCredits() != null){
				usedCredits += ebOrderDetail.getUseCredits();
			}
		}
		return usedCredits;
	}
	
	public void saveOrderInvalidAndAddSkuStorage(Long ebOrderId)
			throws SqlException {
		EbOrder ebOrder = ebOrderDao.findById(ebOrderId);
		int credits = 0;
		if (ebOrder.getOrderType() != EbOrderTypeEnum.VIPMEMBER) {
			for (EbOrderDetail ebOrderDetail : ebOrder.getOrderDetails()) {
				if (ebOrder.getOrderType() == EbOrderTypeEnum.NORMAL) {
					EbStorage ebStorage = (EbStorage) ebStorageDao
							.getSessionFactory()
							.getCurrentSession()
							.load(EbStorage.class, ebOrderDetail.getSkuCode(),
									LockMode.UPGRADE);
					ebStorage.setAvailable(ebStorage.getAvailable()
							+ ebOrderDetail.getAmount());
					credits += ebOrderDetail.getUseCredits();
					ebStorageDao.update(ebStorage);
				}
				if (ebOrderDetail.getProductCode() == 1005003
						|| ebOrderDetail.getProductCode() == 1005002
						|| ebOrderDetail.getProductCode() == 1004022
						|| ebOrderDetail.getProductCode() == 1005007
						|| ebOrderDetail.getProductCode() == 1005008
						|| ebOrderDetail.getProductCode() == 1005009) {
					int userid = ebOrder.getUserId();
					int pcode = ebOrderDetail.getProductCode();
					EbSales s = ebSalesDao.findOneByHql(
							" where userid=? and product.productCode=? ",
							new Object[] { userid, pcode });
					if (s == null)
						return;
					if (s.getCount() == null)
						s.setCount(0);
					s.setCount(s.getCount() + 1);
					ebSalesDao.update(s);
				}
			}

		}
		ebOrder.setStatus(EbOrderStatusEnum.CANCEL);
		EbCoupon coupon = ebCouponDao.findOneByHql(" where ebOrder=?",
				new Object[] { ebOrder });
		if (coupon != null) {
			coupon.setUsed(false);
			coupon.setEbOrder(null);
			coupon.setUseTime(null);
			ebCouponDao.update(coupon);
		}
		// 返回用户积分
		if (credits != 0 && ebOrder.getUserId() > 0) {
			Customer customer = (Customer) customerDao
					.getSessionFactory()
					.getCurrentSession()
					.load(Customer.class, ebOrder.getUserId(), LockMode.UPGRADE);
			customer.setCredits(customer.getCredits() + credits);
			customerDao.update(customer);
		}
		ebOrderDao.update(ebOrder);
	}

	public boolean isFirstOrder(int userId) throws SqlException {
		return ebOrderDao
				.findOneByHql(
						"WHERE userId=? AND orderType != ? AND status IN (2,1,4,5,-8)",
						new Object[] { userId, EbOrderTypeEnum.VIPMEMBER }) == null;
	}

	public boolean isFirstOrder(int userId, int orderId) throws SqlException {
		return ebOrderDao
				.findOneByHql(
						"WHERE userId=? AND orderType != ? AND status IN (2,1,4,5,-8) AND orderid!=?",
						new Object[] { userId, EbOrderTypeEnum.VIPMEMBER,
								orderId }) == null;
	}

	/**
	 * 满返促销
	 * 
	 * @param ebOrder
	 * @throws Exception
	 */
	public void sendCouponByPromotion(EbOrder ebOrder) throws Exception {
		String hql = "WHERE status=1 AND promotionType=2 AND startDate <? AND endDate>? ";
		Date now = new Date();
		List<EbPromotion> ebPromotions = ebPromotionDao.findAllByHql(hql,
				new Object[] { now, now });
		if (ebPromotions == null || ebPromotions.size() == 0) {
			return;
		}
		for (EbPromotion foo : ebPromotions) {
			sendCouponByPromotion(foo, ebOrder.getUserId().intValue(),
					ebOrder.getOrderDetails());
		}

	}

	private void sendCouponByPromotion(EbPromotion ebPromotion, int userId,
			Set<EbOrderDetail> ebOrderDetails) throws Exception {
		double totalPrice = 0;
		for (EbOrderDetail foo : ebOrderDetails) {
			if (ebPromotion.getIsForAll()) {
				totalPrice += foo.getTotalPrice().doubleValue();
			} else if (isInCollection(ebPromotion.getEbProductCollection(),
					foo.getProductCode())) {
				totalPrice += foo.getTotalPrice().doubleValue();
			}
		}
		EbPromotionItem item = null;
		for (EbPromotionItem foo : ebPromotion.getEbPromotionItems()) {
			if (totalPrice >= foo.getStandardPrice()) {
				if (item == null) {
					item = foo;
				} else {
					item = item.getStandardPrice() > foo.getStandardPrice() ? item
							: foo;
				}
			}
		}
		if (item != null && StringUtils.isNotEmpty(item.getCoupons())) {
			StringBuffer sb = new StringBuffer("WHERE used=0 AND couponType=2");
			sb.append(" AND id IN (");
			for (Integer id : item.getCouponTemplates()) {
				sb.append(id).append(",");
			}
			if (sb.charAt(sb.length() - 1) == ',') {
				sb.deleteCharAt(sb.length() - 1);
			}
			sb.append(")");
			List<EbCoupon> templates = ebCouponDao.findAllByHql(sb.toString());
			List<EbCoupon> ebCoupons = new ArrayList<EbCoupon>();
			for (EbCoupon foo : templates) {
				if (foo.getAvailableCount() > 0) {
					EbCoupon ebCoupon = new EbCoupon();
					ebCoupon.setCouponName(foo.getCouponName());
					ebCoupon.setDescription(foo.getDescription());
					ebCoupon.setBatch(foo.getBatch());
					ebCoupon.setMinAmount(foo.getMinAmount());
					ebCoupon.setMoney(foo.getMoney());
					ebCoupon.setOneOnly(foo.getOneOnly());
					ebCoupon.setForShipping(foo.getForShipping());
					ebCoupon.setEbProductCollection(foo
							.getEbProductCollection());
					ebCoupon.setUserId(userId);
					ebCoupon.setCreateTime(new Date());
					ebCoupon.setCouponType(EbCouponTypeEnum.NORMAL);
					ebCoupon.setCouponSource(EbCouponSourceEnum.PROMOTION);
					ebCoupon.setUsed(false);
					if (foo.getAvailableTime() != null
							&& foo.getAvailableTime().intValue() > 0) {
						Calendar calendar = Calendar.getInstance();
						ebCoupon.setStartTime(calendar.getTime());
						calendar.add(Calendar.DAY_OF_MONTH, foo
								.getAvailableTime().intValue());
						ebCoupon.setEndTime(calendar.getTime());
					} else {
						ebCoupon.setStartTime(foo.getStartTime());
						ebCoupon.setEndTime(foo.getEndTime());
					}
					ebCoupons.add(ebCoupon);
					foo.setAvailableCount(foo.getAvailableCount() - 1);
				}
			}
			ebCouponDao.saveAll(ebCoupons);
			ebCouponDao.updateAll(templates);
			for (EbCoupon ebCoupon : ebCoupons) {
				ebCoupon.setSerialNumber(generateNum(ebCoupon));
			}
			ebCouponDao.updateAll(ebCoupons);
		}
	}

	private String generateNum(EbCoupon ebCoupon) {
		byte[] head4 = Md5Encrypt.int2byte(ebCoupon.hashCode());
		byte[] tail4 = Md5Encrypt.int2byte(ebCoupon.getId());
		char timeChar = (char) ((new Date().getTime()) % 0xffff);
		char a = (char) ((ebCoupon.hashCode() + ebCoupon.getId()) % 0xffff);
		byte[] last2 = Md5Encrypt.char2byte((char) ((timeChar + a) % 0xffff));
		byte[] input = new byte[10];
		for (int i = 0; i < 8; i++) {
			if (i % 2 == 0)
				input[i] = tail4[i / 2];
			else
				input[i] = head4[(i + 1) / 2 - 1];
		}
		for (int j = 0; j < 2; j++) {
			input[8 + j] = last2[j];
		}
		return Base32.encode(input);
	}

	private boolean isInCollection(EbProductCollection ebProductCollection,
			int productCode) throws SqlException {

		EbProduct ebProduct = ebProductDao.findById(productCode);
		if (ebProduct == null || ebProductCollection == null) {
			return false;
		}
		if (StringUtils.isNotEmpty(ebProductCollection.getBrandIds())) {// 品牌
			if (ebProduct.getEbBrand() != null
					&& ebProductCollection.getBrandIds().contains(
							ebProduct.getEbBrand().getBrandId().toString())) {
				return true;
			}
		}
		if (StringUtils.isNotEmpty(ebProductCollection.getCategoryIds())) {// 分类
			if (ebProduct.getEbCatagory() != null
					&& ebProductCollection.getCategoryIds().contains(
							ebProduct.getEbCatagory().getId().toString())) {
				return true;
			}
		}
		if (StringUtils.isNotEmpty(ebProductCollection.getProductCodes())) {// 商品
			if (ebProductCollection.getProductCodes().contains(
					ebProduct.getProductCode().toString())) {
				return true;
			}
		}
		return false;
	}

	public EbOrder retrieveOrder(long orderId) {
		EbOrder ebOrder = null;
		try {
			ebOrder = ebOrderDao.findById(orderId);
		} catch (SqlException e) {
			e.printStackTrace();
		}
		return ebOrder;
	}
	
	/**
	* <p>功能描述:获取15天未确认收货的订单</p>
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：List<EbOrder></p>
	 */
	public List<Long> findAutoCompleteOrder() throws SqlException {
		List<Long> orderIds = new ArrayList<Long>();
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DAY_OF_MONTH, -15);
		//获取发货15天没有确认收货的订单（包括网站的和APP的订单）
		List<EbOrder> list = ebOrderDao
				.findAllByHql(
						"where deliveryTime <= ? and status = 2 and orderType = ?",
						new Object[] { c.getTime(),EbOrderTypeEnum.NORMAL});
		
		if(list != null && list.size() > 0){
			for (EbOrder ebOrder : list) {
				orderIds.add(ebOrder.getOrderid());
			}
		}
		return orderIds;
	}
	
	/**
	 * 获取支付有问题的订单 <br/>
	 * 1.客户说支付成功了，但是服务器没有收到通知。 <br/>
	 * 2.客户端没说支付成功，服务器也没收到通知，但是支付平台已经完成支付。
	 * 
	 * @return
	 * @throws SqlException
	 */
	public List<EbOrder> findUnnomalPayOrder() throws SqlException {
		List<EbOrder> list = null;
		Calendar c = Calendar.getInstance();
		// 客户端支付有问题的订单()
		c.add(Calendar.MINUTE, -29);
		list = ebOrderDao
				.findAllByHql(
						"where orderTime<? and status in (0,-8) and payStatus in(0,1) and (orderSource<>? or orderSource is null)",
						new Object[] { c.getTime(), EbOrderSourceEnum.WEBSITE });
		// 网站支付有问题的订单()
		c.add(Calendar.MINUTE, -120);
		List<EbOrder> list1 = ebOrderDao
				.findAllByHql(
						"where orderTime<? and status in (0,-8) and payStatus in(0,1) and orderSource=?",
						new Object[] { c.getTime(), EbOrderSourceEnum.WEBSITE });
		if (list != null) {
			list.addAll(list1);
			return list;
		}
		return list1;
	}
}
