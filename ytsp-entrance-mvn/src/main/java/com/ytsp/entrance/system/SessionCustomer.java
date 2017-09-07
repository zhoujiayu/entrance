package com.ytsp.entrance.system;

import com.ytsp.db.domain.Customer;

/**
 * @author GENE
 * @description 用户会话信息
 */
public class SessionCustomer {
	private Customer customer;

	public SessionCustomer() {
		super();
	}

	public SessionCustomer(Customer customer) {
		this.customer = customer;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

}
