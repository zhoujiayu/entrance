package com.ytsp.entrance.command.v4_0;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ytsp.db.domain.Customer;
import com.ytsp.db.domain.EbOrder;
import com.ytsp.db.domain.EbOrderDetail;
import com.ytsp.db.domain.EbProduct;
import com.ytsp.db.domain.EbProductImage;
import com.ytsp.db.domain.EbReturnOrder;
import com.ytsp.db.domain.EbSku;
import com.ytsp.db.domain.EbUserAddress;
import com.ytsp.db.enums.EbOrderStatusEnum;
import com.ytsp.db.enums.EbOrderTypeEnum;
import com.ytsp.db.enums.EbProductValidStatusEnum;
import com.ytsp.db.enums.EbReturnOrderStatus;
import com.ytsp.db.enums.MobileTypeEnum;
import com.ytsp.db.exception.SqlException;
import com.ytsp.entrance.command.base.AbstractCommand;
import com.ytsp.entrance.command.base.CommandList;
import com.ytsp.entrance.command.base.ExecuteResult;
import com.ytsp.entrance.service.EbOrderService;
import com.ytsp.entrance.service.EbProductService;
import com.ytsp.entrance.service.EbSkuService;
import com.ytsp.entrance.service.v3_1.MemberServiceV31;
import com.ytsp.entrance.service.v4_0.EbUserAddressService;
import com.ytsp.entrance.service.v4_0.MemberServiceV4_0;
import com.ytsp.entrance.system.SessionCustomer;
import com.ytsp.entrance.system.SystemInitialization;
import com.ytsp.entrance.system.SystemManager;
import com.ytsp.entrance.util.DateTimeFormatter;
import com.ytsp.entrance.util.OrderIdGenerationUtil;
import com.ytsp.entrance.util.alipay.AlipaySubmit;

public class EbOrderCommand extends AbstractCommand {

	@Override
	public boolean canExecute() {
		int code = getContext().getHead().getCommandCode();
		return (code == CommandList.CMD_EB_ORDER_ADD
				|| code == CommandList.CMD_EB_ORDER_DETAIL
				|| code == CommandList.CMD_EB_ORDER_RETURN
				|| code == CommandList.CMD_EB_ORDER_LIST
				|| code == CommandList.CMD_EB_ORDER_PAY
				|| code == CommandList.CMD_VIP_PAY || code == CommandList.CMD_EB_ORDER_CLIENT_PAY_SUCCESS);
	}

	@Override
	public ExecuteResult execute() {
		// 验证权限.
		int code = getContext().getHead().getCommandCode();
		JSONObject jsonObj = getContext().getBody().getBodyObject();

		int uid = getContext().getHead().getUid();// UID由客户端传递过来,与当前用户的session中的用户ID做比对
		SessionCustomer sc = getSessionCustomer();
		if (sc == null || sc.getCustomer() == null) {
			return getNoPermissionExecuteResult();
		}
		// 判断操作的用户与当前的session中用户是否一致.
		Customer customer = sc.getCustomer();
		if (uid == 0 || customer.getId().intValue() != uid) {
			return getNoPermissionExecuteResult();
		}
		try {
			if (code == CommandList.CMD_EB_ORDER_ADD) {
				return order(customer, jsonObj, uid);
			} else if (code == CommandList.CMD_EB_ORDER_DETAIL) {
				return orderDetail(jsonObj.getLong("orderId"));
			} else if (code == CommandList.CMD_EB_ORDER_RETURN) {
				return returnOrder(customer, jsonObj, uid);
			} else if (code == CommandList.CMD_EB_ORDER_LIST) {
				return orderList(jsonObj, uid);
			} else if (code == CommandList.CMD_EB_ORDER_PAY) {
				return pay(jsonObj, uid);
			} else if (code == CommandList.CMD_EB_ORDER_CLIENT_PAY_SUCCESS) {
				return clientPaySuccess(jsonObj, uid);
			} else if (code == CommandList.CMD_VIP_PAY) {
				return vipPay(jsonObj, uid);
			}
		} catch (Exception e) {
			logger.info("订单操作:" + code + " 失败 " + e);
			return getExceptionExecuteResult(e);
		}
		return null;
	}

	private ExecuteResult vipPay(JSONObject jsonObject, int uid)
			throws JSONException, SqlException {
		EbOrder ebOrder = new EbOrder();
		ebOrder.setOrderid(OrderIdGenerationUtil.getInstance().genOrderId());
		String deviceCode = jsonObject.getString("deviceCode");
		ebOrder.setUserId(uid);
		ebOrder.setStatus(EbOrderStatusEnum.ORDERSUCCESS);
		ebOrder.setDeviceCode(deviceCode);
		ebOrder.setOrderTime(new Date());
		ebOrder.setOrderType(EbOrderTypeEnum.VIPMEMBER);

		Set<EbOrderDetail> details = new HashSet<EbOrderDetail>();
		EbOrderDetail detail = null;
		EbProductService eps = SystemInitialization.getApplicationContext()
				.getBean(EbProductService.class);

		int productCode = jsonObject.getInt("productCode");
		EbProduct ebProduct = eps.retrieveEbProductById(productCode);
		EbSku sku = (EbSku) ebProduct.getSkus().toArray()[0];
		detail = new EbOrderDetail();
		detail.setActivityId(ebProduct.getEbActivity().getActivityId());
		detail.setOrderId(ebOrder.getOrderid());
		detail.setPrice(ebProduct.getPrice());
		detail.setProductCode(productCode);
		detail.setTotalPrice(ebProduct.getPrice());
		detail.setSkuCode(sku.getSkuCode());
		detail.setParent(ebOrder);
		detail.setProductName(ebProduct.getProductName());
		details.add(detail);
		ebOrder.setImgSrc(SystemManager.getInstance().getSystemConfig()
				.getImgServerUrl()
				+ eps.getEbProductImages(MobileTypeEnum.iphone, productCode)
						.get(0).getImageCut());
		ebOrder.setOrderDetails(details);
		EbOrderService eos = SystemInitialization.getApplicationContext()
				.getBean(EbOrderService.class);
		eos.savePayVipOrder(ebOrder);
		eos.addOrderPay(ebOrder.getOrderid(), uid);// 流水号
		JSONObject ret = new JSONObject();
		ret.put("orderId", ebOrder.getOrderid());
		ret.put("productName", ebProduct.getProductName());
		ret.put("price", ebProduct.getPrice());
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "客户端支付成功",
				ret, this);
	}

	private ExecuteResult clientPaySuccess(JSONObject jsonObj, int uid)
			throws JSONException, SqlException {
		long orderId = jsonObj.getLong("orderId");
		EbOrderService eos = SystemInitialization.getApplicationContext()
				.getBean(EbOrderService.class);
		EbOrder order = eos.retrieveOrderByOrderId(orderId);
		if (order.getStatus() == EbOrderStatusEnum.PAYSUCCESS
				|| order.getStatus() == EbOrderStatusEnum.SUCCESS
				|| order.getStatus() == EbOrderStatusEnum.COMPLETE
				|| order.getStatus() == EbOrderStatusEnum.COMMENT) {
		} else {
			try {
				int payStatus = AlipaySubmit.queryPaySuccess("", String.valueOf(order
						.getOrderid()), order.getTotalPrice().doubleValue());
				if (payStatus == 1) {
					// TODO 支付成功,支付宝的支付方式是1
					eos.createOrderPaySuccess(orderId, 1);
					if (order.getOrderType() == EbOrderTypeEnum.VIPMEMBER) {
						SystemInitialization.getApplicationContext()
								.getBean(MemberServiceV4_0.class)
								.saveVipPaySuccess(orderId);
					}
				}else if(payStatus == -2){//支付失败
					
				} else {
					// 尚未支付,网络不好？或者？但客户说支付成功了，先确保该commondCode在客户端支付成功之后调用的
					eos.updateOrderPaySuccess(orderId);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
//		eos.updateOrderClientPaySuccess(orderId);
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "客户端支付成功",
				new JSONObject(), this);
	}

	private ExecuteResult pay(JSONObject jsonObj, int uid)
			throws JSONException, SqlException {
		long orderId = jsonObj.getLong("orderId");
		EbOrderService eos = SystemInitialization.getApplicationContext()
				.getBean(EbOrderService.class);
		int payLogId = eos.addOrderPay(orderId, uid);// 流水号
		JSONObject jo = new JSONObject();
		jo.put("payLogId", payLogId);
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "创建支付", jo,
				this);
	}

	private ExecuteResult orderDetail(long id) throws JSONException, Exception {
		EbOrderService eos = SystemInitialization.getApplicationContext()
				.getBean(EbOrderService.class);
		JSONObject jo = eos.getOrderById(id);
		jo.put("currentTime", DateTimeFormatter.dateTime2String(new Date()));
		jo.put("shareUrl", "http://m.ikan.cn/mobileAppAddress.action?from=14");// TODO
																				// 写分享的url
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取订单成功", jo,
				this);
	}

	private ExecuteResult orderList(JSONObject jsonObj, int uid)
			throws JSONException, Exception {
		EbOrderService eos = SystemInitialization.getApplicationContext()
				.getBean(EbOrderService.class);
		JSONObject jo = new JSONObject();
		jo.put("orderList", eos.getOrderByUser(uid));
		jo.put("currentTime", DateTimeFormatter.dateTime2String(new Date()));
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取订单列表成功",
				jo, this);
	}

	/**
	 * 取消订单-需要增加可订量
	 * 
	 * @param customer
	 * @param jsonObj
	 * @param uid
	 * @return
	 * @throws JSONException
	 * @throws SqlException
	 */
	private ExecuteResult cancelOrder(Customer customer, JSONObject jsonObj,
			int uid) throws JSONException, SqlException {

		long orderId = jsonObj.getLong("orderId");
		int userid = jsonObj.getInt("userid");
		EbOrderService eos = SystemInitialization.getApplicationContext()
				.getBean(EbOrderService.class);

		JSONObject result = new JSONObject();
		EbOrder ebOrder = eos.retrieveOrderByOrderId(orderId);
		if (ebOrder.getUserId().intValue() != userid) {
			return new ExecuteResult(CommandList.RESPONSE_STATUS_NOLOGIN,
					"取消失败,非本账户订单.", null, this);
		}
		eos.cancelOrder(ebOrder);
		result.put("orderId", ebOrder.getOrderid());
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "取消订单成功",
				result, this);
	}

	/**
	 * 下单
	 * 
	 * @param customer
	 * @param jsonObj
	 * @param uid
	 * @return
	 * @throws JSONException
	 * @throws SqlException
	 */
	private ExecuteResult order(Customer customer, JSONObject jsonObj, int uid)
			throws JSONException, SqlException {
		EbOrder ebOrder = new EbOrder();
		ebOrder.setOrderid(OrderIdGenerationUtil.getInstance().genOrderId());
		bindBasicOrderInfo(jsonObj, ebOrder, uid);
		bindUserAddress(jsonObj, ebOrder);
		try {
			bindDetails(jsonObj, ebOrder, customer);
		} catch (OutOfStorageException e) {
			JSONObject result = e.getOutOfStorageSkus();
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "商品无库存",
					result, this);
		} catch (Exception e) {
			e.printStackTrace();
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
					"下单发生错误", null, this);
		}
		JSONObject result = new JSONObject();
		EbOrderService eos = SystemInitialization.getApplicationContext()
				.getBean(EbOrderService.class);
		try {
			eos.saveOrder(ebOrder);
		} catch (OutOfStorageException e) {
			result = e.getOutOfStorageSkus();
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "商品无库存",
					result, this);
		}
		result.put("orderId", ebOrder.getOrderid());
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "下单成功",
				result, this);
	}

	/**
	 * 申请退款
	 * 
	 * @param customer
	 * @param jsonObj
	 * @param uid
	 * @return
	 * @throws JSONException
	 * @throws SqlException
	 */
	private ExecuteResult returnOrder(Customer customer, JSONObject jsonObj,
			int uid) throws JSONException, SqlException {

		long orderId = jsonObj.getLong("orderId");
		int userid = jsonObj.getInt("userid");
		if (orderId <= 0) {
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
					"申请退款失败,没有订单号.", null, this);
		}
		EbOrderService eos = SystemInitialization.getApplicationContext()
				.getBean(EbOrderService.class);
		EbOrder order = eos.retrieveOrderByOrderId(orderId);// 获取订单
		// 判断订单下单用户和当前操作用户是否是同一个人
		if (order.getUserId().intValue() != userid) {
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
					"申请退款失败,非本账户订单.", null, this);
		}
		// 判断是否是退款中,把这两个判断单独拿出来是为了给每个状态,提示用户的信息会不一样,而且会更精准
		if (order.getStatus() == EbOrderStatusEnum.RETURN) {
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
					"申请退款失败,订单已经申请退款.", null, this);
		}
		// 其实客户端如果发现该订单已经处于该状态,那么客户端应该不能让客户点申请退款按钮,此时的判断是以防万一,下面的判断也一样的功能
		if (order.getStatus() == EbOrderStatusEnum.RETURNSUCCESS) {
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
					"申请退款失败,订单已经申请退款成功.", null, this);
		}
		// 判断订单的状态是否允许退款,只有当订单状态为 支付成功,配送成功的时候才允许退款
		if (order.getStatus() != EbOrderStatusEnum.PAYSUCCESS
				|| order.getStatus() != EbOrderStatusEnum.SUCCESS) {
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
					"申请退款失败,订单不允许退款.", null, this);
		}

		String retLogisiticNumber = jsonObj.getString("retLogisiticNumber");
		// 需要还是不需要这个用户退货的时候发的物流单号?
		if (retLogisiticNumber == null || "".equals(retLogisiticNumber.trim())) {

		}
		// 开始订单退款,原则上申请退款的时候不会增加库存的可订量,所以申请退款过程不会增加库存.
		EbReturnOrder ebRetOrder = new EbReturnOrder();
		ebRetOrder.setAddTime(new Date());
		ebRetOrder.setOrder(order);
		ebRetOrder.setRetLogisiticNumber(retLogisiticNumber);
		ebRetOrder.setStatus(EbReturnOrderStatus.REQUEST);
		ebRetOrder.setUserId(userid);
		eos.saveReturnOrderAndChangeOrderStatus(ebRetOrder);
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "申请退款成功.",
				null, this);
	}

	protected void bindUserAddress(JSONObject jsonObject, EbOrder ebOrder)
			throws JSONException, SqlException {
		int addressId = jsonObject.getInt("addressId");
		// 通过addressId来获取用户地址
		EbUserAddressService euas = SystemInitialization
				.getApplicationContext().getBean(EbUserAddressService.class);
		EbUserAddress ebUserAddress = euas.retrieveEbUserAddressById(addressId);
		ebOrder.setAddressId(addressId);
		ebOrder.setAddress(ebUserAddress.getAddress());
		ebOrder.setAreaId(ebUserAddress.getAreaId());
		ebOrder.setAreaName(ebUserAddress.getAreaName());
		ebOrder.setCellphone(ebUserAddress.getCellphone());
		ebOrder.setCityId(ebUserAddress.getCityId());
		ebOrder.setCityName(ebUserAddress.getCityName());
		ebOrder.setEmail(ebUserAddress.getPostalCode());// 暂时把email当做ZIP用
		ebOrder.setProvinceName(ebUserAddress.getProvinceName());
		ebOrder.setUserName(ebUserAddress.getUserName());
	}

	protected void bindBasicOrderInfo(JSONObject jsonObject, EbOrder ebOrder,
			int uid) throws JSONException {
		String deviceCode = jsonObject.getString("deviceCode");
		int payType = jsonObject.getInt("payType");
		int isNeedInvoice = 0;
		if (!jsonObject.isNull("invoice"))
			isNeedInvoice = jsonObject.getInt("invoice");
		ebOrder.setPayType(payType);
		ebOrder.setUserId(uid);
		ebOrder.setStatus(EbOrderStatusEnum.ORDERSUCCESS);
		ebOrder.setDeviceCode(deviceCode);
		ebOrder.setOrderTime(new Date());
		ebOrder.setIsNeedInvoice(isNeedInvoice);
		ebOrder.setOrderType(EbOrderTypeEnum.NORMAL);
		if (isNeedInvoice > 0)
			ebOrder.setInvoiceTitle(jsonObject.getString("invoiceTitle"));
	}

	private void bindDetails(JSONObject jsonObject, EbOrder ebOrder,
			Customer customer) throws Exception {
		JSONArray array = jsonObject.getJSONArray("details");
		if (array == null || array.length() == 0) {
			throw new JSONException("没有订单详情");
		}
		MemberServiceV31 ms = SystemInitialization.getApplicationContext()
				.getBean(MemberServiceV31.class);
		boolean isMember = ms.isMember(customer.getId());
		MobileTypeEnum plat = MobileTypeEnum.valueOf(getContext().getHead()
				.getPlatform());
		ebOrder.setOrderPlat(plat);
		EbProductService eps = SystemInitialization.getApplicationContext()
				.getBean(EbProductService.class);
		int length = array.length();
		ebOrder.setTotalPrice(0d);
		Set<EbOrderDetail> details = new HashSet<EbOrderDetail>();
		JSONObject j = null;
		EbSkuService ebss = SystemInitialization.getApplicationContext()
				.getBean(EbSkuService.class);
		List<EbSku> outOfStorageskus = new ArrayList<EbSku>();// 没有库存的SKU
		EbOrderDetail detail = null;
		int productCode = 0;
		EbProduct ebProduct = null;
		int useCredits = 0;
		for (int i = 0; i < length; i++) {
			j = array.getJSONObject(i);
			int activityId = j.getInt("activityId");
			int amount = j.getInt("amount");
			String color = "";
			if (!j.isNull("color"))
				color = j.getString("color");
			String size = "";
			if (!j.isNull("size"))
				size = j.getString("size");
			if (!j.isNull("useCredits"))
				useCredits = j.getInt("useCredits");
			productCode = j.getInt("productCode");
			int skuCode = j.getInt("skuCode");
			String productName = j.getString("productName");
			ebProduct = eps.retrieveEbProductById(productCode);
			// 商品有效性判断
			EbSku ebSku = ebss.retrieveEbSkuBySkuCode(skuCode);
			// 下面这一段sku重赋值是因为4.*版本是iphone有选择型号的bug，应该按照型号来确定选择的sku而不是skucode
			if (ebSku != null && ebSku.getSize() != null && !ebSku.getSize().equals(size)) {
				for (EbSku _ebSku : ebProduct.getSkus()) {
					if (_ebSku.getStatus() == null
							|| _ebSku.getStatus() != EbProductValidStatusEnum.VALID)
						continue;
					if (_ebSku.getSize().equals(size))
						ebSku = _ebSku;
				}
			}
			if (ebSku == null
					|| ebSku.getStatus() != EbProductValidStatusEnum.VALID) {
				throw new JSONException("提交订单中的SKU信息不正确");
			}
			if (amount <= 0 || amount >= 10) {
				throw new JSONException("提交订单中商品的数量不正确");
			}

			if (ebSku.getStorage().getAvailable() <= 0) {
				outOfStorageskus.add(ebSku);
			}
			// double price = j.getDouble("actualPrice");
			detail = new EbOrderDetail();
			detail.setActivityId(activityId);
			detail.setAmount(amount);
			detail.setColor(color);
			detail.setUseCredits(useCredits);
			if (customer.getCredits() < useCredits) {
				throw new JSONException("使用的积分超过了用户当前积分");
			}
			detail.setOrderId(ebOrder.getOrderid());
			detail.setPrice(isMember ? ebProduct.getSvprice() : ebProduct
					.getVprice());
			double total = detail.getPrice() * amount;
			detail.setSize(size);
			detail.setSkuCode(ebSku.getSkuCode());
			// TODO 实际上od的total是均摊总支付金额（不含运费、没有减去积分抵用部分）的，当客户端要加购物车、满减等时需要修改
			detail.setTotalPrice(total);
			// VO属性,以便扣减库存的时候使用
			detail.setEbsku(ebSku);
			detail.setProductCode(productCode);
			detail.setVendorProductCode(ebProduct.getVendorProductCode());
			detail.setProductName(productName);
			details.add(detail);
			ebOrder.setTotalPrice(ebOrder.getTotalPrice() + total);
		}

		// 针对特殊活动的
		if (ebProduct.getShipping() != null && ebProduct.getShipping() > 0) {
			ebOrder.setShipping(ebProduct.getShipping());
			ebOrder.setTotalPrice(ebOrder.getTotalPrice()
					+ ebProduct.getShipping());
		}
		ebOrder.setTotalPrice(ebOrder.getTotalPrice() - (double) useCredits
				/ 100);
		String img = ebProduct.getImgUrl();
		if (img == null || img.equals("")) {
			List<EbProductImage> ls = eps.getEbProductImages(plat, productCode);
			if (ls != null && ls.size() > 0)
				img = ls.get(0).getImageSrc();
		}
		ebOrder.setImgSrc(SystemManager.getInstance().getSystemConfig()
				.getImgServerUrl()
				+ img);
		ebOrder.setOrderDetails(details);
		// 如果有发现缺库存的商品则抛出异常
		if (outOfStorageskus.size() > 0) {
			OutOfStorageException e = new OutOfStorageException("超出库存");
			e.addOutOfStorageSkus(outOfStorageskus);
			throw e;
		}
	}

	public static class OutOfStorageException extends Exception {

		private static final long serialVersionUID = 1L;
		private String message;
		private List<EbSku> ebSkus = new ArrayList<EbSku>();

		public void addOutOfStorageSku(EbSku ebSku) {
			this.ebSkus.add(ebSku);
		}

		public void addOutOfStorageSkus(List<EbSku> ebSkus) {
			this.ebSkus.addAll(ebSkus);
		}

		public JSONObject getOutOfStorageSkus() throws JSONException {
			JSONObject ret = new JSONObject();
			JSONArray array = new JSONArray();
			JSONObject o = null;
			for (EbSku ebSku : ebSkus) {
				o = new JSONObject();
				o.put("skuCode", ebSku.getSkuCode());
				array.put(o);
			}
			ret.put("skuCodes", array);
			return ret;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public OutOfStorageException() {
			super();
		}

		public OutOfStorageException(String message) {
			super(message);
			this.message = message;
		}

	}

}
