package com.ytsp.entrance.command.v5_0;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.tencent.wxpay.WXPay;
import com.tencent.wxpay.protocol.pay_protocol.PayReqData;
import com.ytsp.db.domain.Customer;
import com.ytsp.db.domain.EbOrder;
import com.ytsp.db.domain.EbOrderDetail;
import com.ytsp.db.domain.EbProduct;
import com.ytsp.db.domain.EbSku;
import com.ytsp.db.domain.VipCostDefine;
import com.ytsp.db.enums.EbOrderStatusEnum;
import com.ytsp.db.enums.EbOrderTypeEnum;
import com.ytsp.db.enums.EbProductValidStatusEnum;
import com.ytsp.db.enums.MobileTypeEnum;
import com.ytsp.db.exception.SqlException;
import com.ytsp.db.vo.VipCostDefineVO;
import com.ytsp.entrance.command.base.AbstractCommand;
import com.ytsp.entrance.command.base.CommandList;
import com.ytsp.entrance.command.base.ExecuteResult;
import com.ytsp.entrance.service.EbOrderService;
import com.ytsp.entrance.service.EbProductService;
import com.ytsp.entrance.service.EbSkuService;
import com.ytsp.entrance.service.v5_0.VipCostDefineService;
import com.ytsp.entrance.system.SessionCustomer;
import com.ytsp.entrance.system.SystemInitialization;
import com.ytsp.entrance.util.OrderIdGenerationUtil;
import com.ytsp.entrance.util.Util;

public class VIPCommandV5_0 extends AbstractCommand {

	@Override
	public boolean canExecute() {
		int code = getContext().getHead().getCommandCode();
		return code == CommandList.CMD_VIP_CREATE_ORDER_V5
				|| code == CommandList.CMD_VIP_COST_DEFINE_LIST;
	}

	@Override
	public ExecuteResult execute() {
		int code = getContext().getHead().getCommandCode();
		try {
			if (code == CommandList.CMD_VIP_CREATE_ORDER_V5) {
				return createVIPOrder();
			} else if (code == CommandList.CMD_VIP_COST_DEFINE_LIST) {
				return vipCostDefineList();
			}
		} catch (SqlException e) {
			e.printStackTrace();
			return getExceptionExecuteResult(e);
		} catch (JSONException e) {
			e.printStackTrace();
			return getExceptionExecuteResult(e);
		} catch (OOSExceprion e) {
			e.printStackTrace();
			try {
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"商品无库存", e.getOOSResult(), this);
			} catch (JSONException e1) {
				e1.printStackTrace();
				return getExceptionExecuteResult(e);
			}
		}
		return null;
	}
	
	/**
	* <p>功能描述:</p>
	* <p>参数：@return</p>
	* <p>返回类型：ExecuteResult</p>
	 */
	private ExecuteResult vipCostDefineList(){
		try {
			VipCostDefineService vipCostServ = SystemInitialization.getApplicationContext().getBean(VipCostDefineService.class);
			//获取vip购买价格定义
			List<VipCostDefine> vipCostDefList = vipCostServ.getVipCostDefine();
			//构建vip购买价格定义vo
			List<VipCostDefineVO> vipDefVOs = buildVipCostDefineVO(vipCostDefList);
			VipCostDefineInfo info = new VipCostDefineInfo();
			info.setVipCostDefineList(vipDefVOs);
			Gson gson = new Gson();
			JSONObject result = new JSONObject(gson.toJson(info));
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取购买价格定义成功",result, this);
		} catch (Exception e) {
			logger.error("vipCostDefineList() error," + " HeadInfo :"
					+ getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}
	}
	
	/**
	* <p>功能描述:构建vip购买定义VO</p>
	* <p>参数：@param vipCostDefList
	* <p>参数：@return</p>
	* <p>返回类型：List<VipCostDefineVO></p>
	 */
	private List<VipCostDefineVO> buildVipCostDefineVO(List<VipCostDefine> vipCostDefList){
		List<VipCostDefineVO> vipCostVOList = new ArrayList<VipCostDefineVO>();
		if(vipCostDefList == null || vipCostDefList.size() <= 0){
			return vipCostVOList;
		}
		for (VipCostDefine vipCostDef : vipCostDefList) {
			vipCostVOList.add(new VipCostDefineVO(vipCostDef));
		}
		return vipCostVOList;
	}
	
	private ExecuteResult createVIPOrder() throws SqlException, JSONException,
			OOSExceprion {
		// 验证权限.
		int userId = getContext().getHead().getUid();// UID由客户端传递过来,与当前用户的session中的用户ID做比对
		SessionCustomer sc = getSessionCustomer();
		if (sc == null || sc.getCustomer() == null) {
			return getNoPermissionExecuteResult();
		}
		// 判断操作的用户与当前的session中用户是否一致.
		Customer customer = sc.getCustomer();
		if (userId == 0 || customer.getId().intValue() != userId) {
			return getNoPermissionExecuteResult();
		}
		//
		JSONObject jsonObj = getContext().getBody().getBodyObject();
		int skuCode = jsonObj.optInt("skuCode");
		int payType = jsonObj.optInt("payType");
		EbProductService prodcutService = SystemInitialization
				.getApplicationContext().getBean(EbProductService.class);
		EbSkuService ebSkuService = SystemInitialization
				.getApplicationContext().getBean(EbSkuService.class);
		//
		EbSku ebSku = ebSkuService.retrieveEbSkuBySkuCode(skuCode);
		if (ebSku == null
				|| ebSku.getStatus() != EbProductValidStatusEnum.VALID) {
			throw new JSONException("提交订单中的SKU信息不正确");
		}
		VipCostDefine vipDefine = getVipCostDefineBySkuCode(skuCode);
		EbOrder ebOrder = new EbOrder();
		ebOrder.setOrderid(OrderIdGenerationUtil.getInstance().genOrderId());
		String deviceCode = getContext().getHead().uniqueId;
		ebOrder.setPayType(payType);
		ebOrder.setUserId(userId);
		ebOrder.setStatus(EbOrderStatusEnum.ORDERSUCCESS);
		ebOrder.setDeviceCode(deviceCode);
		ebOrder.setOrderTime(new Date());
		ebOrder.setIsNeedInvoice(0);
		ebOrder.setInvoiceTitle("");
		ebOrder.setBook("");
		ebOrder.setOrderType(EbOrderTypeEnum.VIPMEMBER);
		ebOrder.setOrderTime(new Date());
		MobileTypeEnum plat = MobileTypeEnum.valueOf(getContext().getHead()
				.getPlatform());
		ebOrder.setOrderPlat(plat);
		ebOrder.setIsDelete(0);
		//
		Set<EbOrderDetail> details = new HashSet<EbOrderDetail>();
		EbOrderDetail detail = new EbOrderDetail();
		detail.setAmount(1);
		detail.setSkuCode(skuCode);
		double totalPrice = 0d;
		EbProduct ebProduct = prodcutService.retrieveEbProductById(ebSku
				.getProductCode());
		detail.setOrderId(ebOrder.getOrderid());
		detail.setProductCode(ebSku.getProductCode());
		detail.setVendorProductCode(ebProduct.getVendorProductCode());
		detail.setProductName(ebProduct.getProductName());
		detail.setSkuCode(ebSku.getSkuCode());
		detail.setSize(ebSku.getSize());
		detail.setColor(ebSku.getColor());
		detail.setImageSrc(vipDefine.getProductImgUrl());
		detail.setParent(ebOrder);
		detail.setUseCredits(0);
		detail.setPrice(vipDefine.getPrice());
		detail.setTotalPrice(detail.getAmount() * detail.getPrice());
		totalPrice += detail.getTotalPrice();

		details.add(detail);
		//
		ebOrder.setOrderDetails(details);
		ebOrder.setTotalPrice(totalPrice);
		//添加版本号，用于微信支付合并。由于以前版本还用以前的微信支付APPID等信息。所以定时取消订单时根据版本号区分
		ebOrder.setTerminalVersion(getContext().getHead().getVersion());
		
		EbOrderService ebOrderService = SystemInitialization
				.getApplicationContext().getBean(EbOrderService.class);
		ebOrderService.createOrder(ebOrder, null, null, null);

		JSONObject result = new JSONObject();
		Gson gson = new Gson();
		// TODO 1)根据不同的支付方式放回不同的参数，因为微信支付的参数是由服务器向微信后台获取生成的
		if (ebOrder.getPayType().intValue() == 3) {// 微信支付
			// TODO 待优化，当出现异常时，要再次请求，最多3次
			try {
				int isUseNewWXPay = Util.isUseNewWXpay(getContext()
						.getHead().getVersion(), getContext().getHead()
						.getPlatform());
				
				String prepayId = "";
				PayReqData payreqData = null;
				//移动端网站微信支付获取请求数据
				if ((MobileTypeEnum.valueOf(getContext().getHead().getPlatform()) == MobileTypeEnum.wapmobile)) {
					String code = jsonObj.optString("code");
					String currentPageURL = jsonObj.optString("currentPageURL");
					prepayId = WXPay.getJSAPIPrepayid(
							String.valueOf(ebOrder.getOrderid()), "",
							String.valueOf(ebOrder.getOrderid()),
							(int) (Math.round(ebOrder.getTotalPrice() * 100)),
							"", "", ebOrder.getOrderSource(),code);
					payreqData = WXPay.getWapMobilePayReqData(prepayId,currentPageURL);
				}else {//手机APP微信支付获取请求数据
					prepayId = WXPay.getPrepayid(
							String.valueOf(ebOrder.getOrderid()), "",
							String.valueOf(ebOrder.getOrderid()),
							(int) (Math.round(ebOrder.getTotalPrice() * 100)),
							"", "", ebOrder.getOrderSource(),isUseNewWXPay);
					payreqData = WXPay.getPayReqData(prepayId,
							ebOrder.getOrderSource(),isUseNewWXPay);
				}
				
				result.put("payreqData", gson.toJson(payreqData));
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// TODO 2)加一个支付日志,可优化，该方法里面又从数据库取了一次订单信息
		ebOrderService.addOrderPay(ebOrder.getOrderid(), userId);

		result.put("payPrice", ebOrder.getTotalPrice());
		result.put("orderId", ebOrder.getOrderid());
		result.put("result", true);
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "下单成功",
				result, this);
	}
	
	/**
	* <p>功能描述:获取vip价格定义</p>
	* <p>参数：@param skuCode
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：VipCostDefine</p>
	 */
	private VipCostDefine getVipCostDefineBySkuCode(int skuCode) throws SqlException{
		VipCostDefineService vipDefineServ = SystemInitialization.getApplicationContext().getBean(VipCostDefineService.class);
		return vipDefineServ.getVipCostDefineBySkuCode(skuCode);
	}
	
	private ExecuteResult getVIPProductList() throws JSONException {
		EbSkuService ebSkuService = SystemInitialization
				.getApplicationContext().getBean(EbSkuService.class);
		Gson gson = new Gson();
		JSONObject result = new JSONObject();
		result.put("VipProductList", gson.toJson(ebSkuService.getVIPProducts()));
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "VIP充值商品获取成功",
				result, this);
	}
	
	class VipCostDefineInfo{
		
		private List<VipCostDefineVO> vipCostDefineList = null;

		public List<VipCostDefineVO> getVipCostDefineList() {
			return vipCostDefineList;
		}

		public void setVipCostDefineList(List<VipCostDefineVO> vipCostDefineList) {
			this.vipCostDefineList = vipCostDefineList;
		}
		
	}
	
}
