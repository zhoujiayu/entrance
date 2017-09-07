package com.ytsp.entrance.command.v5_0;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;

import com.ytsp.db.domain.EbOrder;
import com.ytsp.db.domain.EbOrderDetail;
import com.ytsp.db.enums.EbOrderStatusEnum;
import com.ytsp.entrance.command.base.AbstractCommand;
import com.ytsp.entrance.command.base.CommandList;
import com.ytsp.entrance.command.base.ExecuteResult;
import com.ytsp.entrance.service.EbOrderService;
import com.ytsp.entrance.service.v5_0.OrderServiceV5_0;
import com.ytsp.entrance.system.SystemInitialization;
import com.ytsp.entrance.util.ImagePropertyUtil;
import com.ytsp.entrance.util.alipay.AlipayConfig;
import com.ytsp.entrance.util.alipay.AlipaySubmit;
import com.ytsp.entrance.util.alipay.params.AlipayRequestParams;

public class AlipayCommand extends AbstractCommand{

	@Override
	public boolean canExecute() {
		int code = getContext().getHead().getCommandCode();
		return code == CommandList.CMD_ALIPAY_REQUEST_PAY_FORM;
	}

	@Override
	public ExecuteResult execute() {
		int code = getContext().getHead().getCommandCode();
		try {
			if (code == CommandList.CMD_ALIPAY_REQUEST_PAY_FORM) {
				return getAlipayRequestParam();
			}
		} catch (Exception e) {
			logger.info("AlipayCommand:" + code + " 失败 " + ",headInfo:"
					+ getContext().getHead().toString() + "bodyParam:"
					+ getContext().getBody().getBodyObject().toString()
					+ e.getMessage());
			return getExceptionExecuteResult(e);
		}
		return null;
	}
	
	/**
	* <p>功能描述:获取支付宝请求参数</p>
	* <p>参数：@return
	* <p>参数：@throws Exception</p>
	* <p>返回类型：ExecuteResult</p>
	 */
	private ExecuteResult getAlipayRequestParam() throws Exception {
		JSONObject reqParam = getContext().getBody().getBodyObject();
		JSONObject result = new JSONObject(); 
		if(!reqParam.has("orderId")){
			return new ExecuteResult(CommandList.RESPONSE_STATUS_BODY_JSON_ERROR,
					"请求参数错误！", result, this);
		}
		long orderId = reqParam.optLong("orderId");
		String redirectURL = reqParam.optString("redirectURL");
		OrderServiceV5_0 orderServ = SystemInitialization.getApplicationContext().getBean(OrderServiceV5_0.class);
		EbOrder order = orderServ.getOrderByOrderId(orderId);
		if(order == null){
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
					"未找到相应订单！", result, this);
		}
		int status = order.getStatus().getValue();
		if(status != EbOrderStatusEnum.ORDERSUCCESS.getValue()){
			if (status == EbOrderStatusEnum.CANCEL.getValue()) {
				result.put("msg", "提示：订单已取消");
			} else if (status == EbOrderStatusEnum.PAYSUCCESS.getValue()
					|| status == EbOrderStatusEnum.SUCCESS.getValue()) {
				result.put("msg", "提示：订单已支付");
			} else if (status == EbOrderStatusEnum.RETURN.getValue()) {
				result.put("msg", "提示：订单退款中，不能付款");
			}else if (status == EbOrderStatusEnum.COMMENT.getValue()) {
				result.put("msg", "提示：订单已评论，不能付款");
			}else if (status == EbOrderStatusEnum.COMPLETE.getValue()) {
				result.put("msg", "提示：订单已完成，不能付款");
			}else if (status == EbOrderStatusEnum.RETURNSUCCESS.getValue()) {
				result.put("msg", "提示：订单已退款，不能付款");
			} else if (status == EbOrderStatusEnum.WAIT.getValue()) {
				result.put("msg",
						"提示：" + EbOrderStatusEnum.WAIT.getDescription());
			} else {
				result.put("msg", "提示：订单不可支付，等待客服处理");
			}
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
					"获取支付宝支付信息失败", result, this);
		}
		EbOrderService ebOrderService = SystemInitialization
				.getApplicationContext().getBean(EbOrderService.class);
		//添加支付日志
		ebOrderService.addOrderPay(order.getOrderid(), order.getUserId());
		//构建支付宝请求参数
		Map<String, String> payReqParam = buildAlipayRequestParameter(order);
		//获取支付宝支付表单字符串
		String sHtmlText = AlipaySubmit.buildRequest(payReqParam, "get", "确认");
		result.put("result", true);
		result.put("payForm", sHtmlText);
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
				"获取支付宝请求", result, this);
	}
	
	private Map<String, String> buildAlipayRequestParameter(EbOrder ebOrder) {
		// //////////////////////////////////请求参数//////////////////////////////////////
		AlipayRequestParams params = new AlipayRequestParams();
		// 支付调用接口
		params.service = "alipay.wap.create.direct.pay.by.user";
		// 签约的支付宝账号
		params.partner = AlipayConfig.partner;
		//
		params._input_charset = AlipayConfig.input_charset;
		// 支付类型
		params.payment_type = "1";
		// 服务器异步通知页面路径
		params.notify_url = ImagePropertyUtil.getPropertiesValue("AliNotifyUrl").trim();
		// 服务器同步通知页面路径
		params.return_url = ImagePropertyUtil.getPropertiesValue("AliReturnUrl").trim();
		// 卖家支付宝帐户
		params.seller_id = AlipayConfig.seller_id;
		// 商户订单号
		params.out_trade_no = ebOrder.getOrderid().toString();
		// 订单名称
		params.subject = "订单：" + ebOrder.getOrderid();
		DecimalFormat df = new DecimalFormat(".##");
		// 付款金额
		params.total_fee = Double.valueOf(df.format(ebOrder.getTotalPrice()));
		// 订单描述
		Set<EbOrderDetail> list = ebOrder.getOrderDetails();
		EbOrderDetail[] detailList = list.toArray(new EbOrderDetail[0]);
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < detailList.length; i++) {
			EbOrderDetail d = detailList[i];
			sb.append(String.valueOf(d.getProductCode()));
			if (i != list.size() - 1) {
				sb.append(",");
			}
			if (sb.length() > 50) {
				break;
			}
		}
		if (sb.length() > 50) {
			params.body = sb.substring(0, 50) + "......";
		} else {
			params.body = sb.toString();
		}
		// ////////////////////////////////////////////////////////////////////////////////

		// 把请求参数打包成数组
		Map<String, String> sParaTemp = new HashMap<String, String>();
		sParaTemp.put("service", params.service);
		sParaTemp.put("partner", params.partner);
		sParaTemp.put("_input_charset", AlipayConfig.input_charset);
		sParaTemp.put("payment_type", params.payment_type);
		sParaTemp.put("notify_url", params.notify_url);
		sParaTemp.put("return_url", params.return_url);
		sParaTemp.put("seller_id", params.seller_id);
		sParaTemp.put("out_trade_no", params.out_trade_no);
		sParaTemp.put("subject", params.subject);
		sParaTemp.put("total_fee", String.valueOf(params.total_fee));
		sParaTemp.put("body", params.body);
		sParaTemp.put("it_b_pay", AlipayConfig.ORDER_EFFECTIVE_TIME+"h");
		return sParaTemp;
	}
	
}
