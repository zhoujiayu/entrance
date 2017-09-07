package com.rest.service;

import org.springframework.stereotype.Repository;

import com.rest.bean.OrderSource;
import com.ytsp.db.domain.EbOrder;
import com.ytsp.db.exception.SqlException;
import com.ytsp.entrance.service.v5_0.OrderServiceV5_0;
import com.ytsp.entrance.system.SystemInitialization;

@Repository
public class RestOrderService {
	
	public OrderSource getOrderResource(String id){
		OrderServiceV5_0 orderServ = SystemInitialization.getApplicationContext().getBean(OrderServiceV5_0.class);
		EbOrder order = null;
		OrderSource os = new OrderSource();
		try {
			order = orderServ.getOrderByOrderId(Long.parseLong(id));
			os.setOrderid(order.getOrderid());
			os.setOrderSource(order.getOrderSource().getValue());
			os.setAddress(order.getAddress());
			os.setUserId(order.getUserId());
			os.setAddressId(order.getAddressId());
		} catch (SqlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return os;
	}
}
