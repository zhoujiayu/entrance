package com.ytsp.entrance.command.v5_0;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.tencent.wxpay.WXPay;
import com.tencent.wxpay.protocol.pay_protocol.PayReqData;
import com.ytsp.db.domain.Customer;
import com.ytsp.db.domain.EbOrder;
import com.ytsp.db.enums.EbOrderStatusEnum;
import com.ytsp.db.enums.EbOrderTypeEnum;
import com.ytsp.db.exception.SqlException;
import com.ytsp.entrance.command.base.AbstractCommand;
import com.ytsp.entrance.command.base.CommandList;
import com.ytsp.entrance.command.base.ExecuteResult;
import com.ytsp.entrance.service.EbOrderService;
import com.ytsp.entrance.service.v4_0.MemberServiceV4_0;
import com.ytsp.entrance.service.v5_0.OrderServiceV5_0;
import com.ytsp.entrance.system.SessionCustomer;
import com.ytsp.entrance.system.SystemInitialization;
import com.ytsp.entrance.util.Util;

/**
 * @description 微信支付统一订单生成接口，生成预支付信息
 * 
 */
public class WXPayCmd extends AbstractCommand {

	@Override
	public boolean canExecute() {
		int code = getContext().getHead().getCommandCode();
		return code == CommandList.CMD_WXPAY_QUERY
				|| code == CommandList.CMD_WXPAY_PAY;
	}

	@Override
	public ExecuteResult execute() {
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

		int code = getContext().getHead().getCommandCode();
		if (code == CommandList.CMD_WXPAY_QUERY) {
			return payQuery(userId);
		} else if (code == CommandList.CMD_WXPAY_PAY) {
			return payByWX();
		}
		return null;
	}

	/**
	 * <p>
	 * 功能描述:微信支付
	 * </p>
	 * <p>
	 * 参数：@param userId
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：ExecuteResult
	 * </p>
	 */
	private ExecuteResult payByWX() {
		JSONObject jsonObj = getContext().getBody().getBodyObject();
		JSONObject result = new JSONObject();
		try {
			long orderId = jsonObj.optLong("orderId", 0);
			EbOrder ebOrder = getOrderByOrderId(orderId);
			if (ebOrder == null) {
				result.put("result", CommandList.RESPONSE_STATUS_FAIL);
				result.put("msg", "根据订单id未找到相应的订单");
				return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
						"操作失败", result, this);
			}
			// 校验订单状态
			if (ebOrder.getStatus() == EbOrderStatusEnum.PAYSUCCESS
					|| ebOrder.getStatus() == EbOrderStatusEnum.SUCCESS
					|| ebOrder.getStatus() == EbOrderStatusEnum.COMPLETE
					|| ebOrder.getStatus() == EbOrderStatusEnum.COMMENT) {
				result.put("result", true);
				return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
						"操作成功", result, this);
			}
			try {
				int isUseNewWXPay = Util.isUseNewWXpay(getContext()
						.getHead().getVersion(), getContext().getHead()
						.getPlatform());
				String prepayId = "";
				if(isUseNewWXPay == 2){
					String code = jsonObj.optString("code");
					prepayId = WXPay.getJSAPIPrepayid(
							String.valueOf(ebOrder.getOrderid()), "",
							String.valueOf(ebOrder.getOrderid()),
							(int) (Math.round(ebOrder.getTotalPrice() * 100)), "",
							"", ebOrder.getOrderSource(),code);
				}else{
					prepayId = WXPay.getPrepayid(
							String.valueOf(ebOrder.getOrderid()), "",
							String.valueOf(ebOrder.getOrderid()),
							(int) (Math.round(ebOrder.getTotalPrice() * 100)), "",
							"", ebOrder.getOrderSource(),isUseNewWXPay);
				}
				PayReqData payreqData = WXPay.getPayReqData(prepayId,
						ebOrder.getOrderSource(),isUseNewWXPay);
				Gson gson = new Gson();
				result.put("payreqData", gson.toJson(payreqData));
			} catch (Exception e) {
				result.put("result", false);
			}
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "操作成功",
					result, this);
		} catch (SqlException e) {
			e.printStackTrace();
			return getExceptionExecuteResult(e);
		} catch (JSONException e) {
			e.printStackTrace();
			return getExceptionExecuteResult(e);
		}
	}

	/**
	 * <p>
	 * 功能描述:根据订单id获取订单
	 * </p>
	 * <p>
	 * 参数：@param orderId
	 * <p>
	 * 参数：@return
	 * <p>
	 * 参数：@throws SqlException
	 * </p>
	 * <p>
	 * 返回类型：EbOrder
	 * </p>
	 */
	private EbOrder getOrderByOrderId(long orderId) throws SqlException {
		OrderServiceV5_0 orderService = SystemInitialization
				.getApplicationContext().getBean(OrderServiceV5_0.class);
		EbOrder order = orderService.getOrderByOrderId(orderId);
		return order;
	}

	/**
	 * 统一支付订单接口，现在的移动客户端分为3个微信商户：iPhone、Android手机、HD（iPad和Android平板）<br>
	 * 要根据不同的客户端分别传入不同的参数发起微信接口调用
	 */
	private ExecuteResult payQuery(int userId) {
		JSONObject jsonObj = getContext().getBody().getBodyObject();
		long orderId = jsonObj.optLong("orderId", 0);
		EbOrderService ebOrderService = SystemInitialization
				.getApplicationContext().getBean(EbOrderService.class);
		JSONObject result = new JSONObject();
		try {
			EbOrder order = ebOrderService.retrieveOrderByOrderId(orderId);
			if(order == null){
				result.put("result", false);
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "订单不存在",
						result, this);
			}
			if (order.getStatus() == EbOrderStatusEnum.PAYSUCCESS
					|| order.getStatus() == EbOrderStatusEnum.SUCCESS
					|| order.getStatus() == EbOrderStatusEnum.COMPLETE
					|| order.getStatus() == EbOrderStatusEnum.COMMENT) {
				// TODO 支付成功
				result.put("result", true);
			} else {
				try {
					int isUseNewWXPay = Util.isUseNewWXpay(getContext()
							.getHead().getVersion(), getContext().getHead()
							.getPlatform());
					if (WXPay.payQuery(String.valueOf(orderId),
							order.getOrderSource(),isUseNewWXPay)) {
						// TODO 支付成功
						ebOrderService.createOrderPaySuccess(orderId,3);
						if (order.getOrderType() == EbOrderTypeEnum.VIPMEMBER) {
							SystemInitialization.getApplicationContext()
									.getBean(MemberServiceV4_0.class)
									.saveVipPaySuccess(orderId);
						}
						result.put("result", true);
					} else {
						// TODO
						// 尚未支付,网络不好？或者？但客户说支付成功了，先确保该commondCode在客户端支付成功之后调用的
//						ebOrderService.updateOrderPaySuccess(orderId);
						result.put("result", false);
					}
				} catch (Exception e) {
					e.printStackTrace();
					result.put("result", false);
				}
			}
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "操作成功",
					result, this);
		} catch (SqlException e) {
			e.printStackTrace();
			return getExceptionExecuteResult(e);
		} catch (JSONException e) {
			e.printStackTrace();
			return getExceptionExecuteResult(e);
		}
	}
}
