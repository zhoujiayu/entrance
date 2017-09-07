package com.ytsp.entrance.quartz;

import java.util.List;

import org.apache.log4j.Logger;

import com.ytsp.db.exception.SqlException;
import com.ytsp.entrance.service.EbOrderService;

public class InvalidOrderCleaner {
	public static boolean skip = true;
	private EbOrderService ebOrderService;
	protected static final Logger logger = Logger.getLogger(InvalidOrderCleaner.class);
	private static long index=0;
	public void process(){
		if(skip)
			return;
		try {
			logger.info("###> start cancle expired orders");
			int success = 0;
			int failure = 0;
			int total = 0;
			long startTime = System.currentTimeMillis();
			List<Long> orders = ebOrderService.findInvalidEbOrder();
			if (orders == null || orders.size() == 0) {
				logger.info("###> no order cancled, over");
				return;
			}
			total = orders.size();
			logger.info("###> order count will be cancled:" + total);
			//要做到足够的原子性,只能一条一条的处理.
			for (Long orderid : orders) {
				try{
					ebOrderService.saveOrderInvalidAndAddSkuStorage(orderid);
					success++;
				} catch (Exception e) {
					logger.error("cancle order exception, order NO:" + orderid + ",exception:" + e.getMessage());
					failure++;
				}
			}
			long end = System.currentTimeMillis() - startTime;
			logger.info("###> cancle-order mission accomplished, takes time:" + end+ "milliseconds, orders should be cancled:" + total +" completed:" + success + " failed:" + failure);
		} catch (SqlException e) {
			e.printStackTrace();
		}
	}

	public EbOrderService getEbOrderService() {
		return ebOrderService;
	}

	public void setEbOrderService(EbOrderService ebOrderService) {
		this.ebOrderService = ebOrderService;
	}
}
