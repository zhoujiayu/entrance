package com.ytsp.entrance.quartz;

import java.util.List;

import org.apache.log4j.Logger;

import com.tencent.wxpay.WXPay;
import com.ytsp.db.domain.EbOrder;
import com.ytsp.db.enums.EbOrderTypeEnum;
import com.ytsp.db.exception.SqlException;
import com.ytsp.entrance.service.EbOrderService;
import com.ytsp.entrance.service.v4_0.MemberServiceV4_0;
import com.ytsp.entrance.system.SystemInitialization;
import com.ytsp.entrance.util.alipay.AlipaySubmit;

public class UnnormalPayOrderQuery {
	public static boolean skip = true;
	private EbOrderService ebOrderService;
	protected static final Logger logger = Logger
			.getLogger(InvalidOrderCleaner.class);

	public void process() {
		if (skip)
			return;
		try {
			logger.info("###> start wxpay order query");
			int success = 0;
			int failure = 0;
			int total = 0;
			long startTime = System.currentTimeMillis();
			List<EbOrder> list = ebOrderService.findUnnomalPayOrder();
			if (list != null && list.size() > 0) {
				total = list.size();
				for (EbOrder order : list) {
					Long orderId = order.getOrderid().longValue();
					try {
						boolean result = false;
						if (order.getPayType() == null) {
							// TODO 支付方式？
							if (WXPay.payQuery(String.valueOf(orderId),
									order.getOrderSource(),0)) {
								order.setPayType(3);
								result = true;
							} else if (AlipaySubmit.queryPaySuccess("", String
									.valueOf(orderId), order.getTotalPrice()
									.doubleValue()) == 1) {
								order.setPayType(1);
								result = true;
							}
						} else if (order.getPayType().intValue() == 3) {
							result = WXPay.payQuery(String.valueOf(orderId),
									order.getOrderSource(),0);
						} else if (order.getPayType().intValue() == 2) {
							// TODO 银联

						} else if (order.getPayType().intValue() == 1) {
							result = AlipaySubmit.queryPaySuccess("", String
									.valueOf(orderId), order.getTotalPrice()
									.doubleValue()) == 1;
						}
						if (result) {
							// 支付成功
							ebOrderService.createOrderPaySuccess(orderId,
									order.getPayType());
							if (order.getOrderType() == EbOrderTypeEnum.VIPMEMBER) {
								SystemInitialization.getApplicationContext()
										.getBean(MemberServiceV4_0.class)
										.saveVipPaySuccess(orderId);
							}
							success++;
						} else {
							failure++;
						}
					} catch (Exception e) {
						e.printStackTrace();
						failure++;
					}
				}
			}
			long end = System.currentTimeMillis() - startTime;
			logger.info("###> wxpay order query mission accomplished, takes time:"
					+ end
					+ "milliseconds, orders should be cancled:"
					+ total
					+ " completed:" + success + " failed:" + failure);
		} catch (SqlException e) {
			e.printStackTrace();
		}
	}

	private boolean wxpayUnnormalOrder() {
		return false;
	}

	public EbOrderService getEbOrderService() {
		return ebOrderService;
	}

	public void setEbOrderService(EbOrderService ebOrderService) {
		this.ebOrderService = ebOrderService;
	}
}
