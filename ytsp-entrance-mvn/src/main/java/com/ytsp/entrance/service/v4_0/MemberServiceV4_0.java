package com.ytsp.entrance.service.v4_0;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.ytsp.db.dao.CustomerDao;
import com.ytsp.db.dao.CustomerMemberDao;
import com.ytsp.db.dao.EbOrderDao;
import com.ytsp.db.domain.Customer;
import com.ytsp.db.domain.CustomerMember;
import com.ytsp.db.domain.EbOrder;
import com.ytsp.db.domain.EbOrderDetail;
import com.ytsp.db.domain.VipCostDefine;
import com.ytsp.db.exception.SqlException;
import com.ytsp.entrance.service.v5_0.VipCostDefineService;
import com.ytsp.entrance.system.SystemInitialization;

public class MemberServiceV4_0 {
	private CustomerDao customerDao;
	private CustomerMemberDao customerMemberDao;
	private EbOrderDao ebOrderDao;

	public EbOrderDao getEbOrderDao() {
		return ebOrderDao;
	}

	public void setEbOrderDao(EbOrderDao ebOrderDao) {
		this.ebOrderDao = ebOrderDao;
	}

	public CustomerDao getCustomerDao() {
		return customerDao;
	}

	public void setCustomerDao(CustomerDao customerDao) {
		this.customerDao = customerDao;
	}

	public CustomerMemberDao getCustomerMemberDao() {
		return customerMemberDao;
	}

	public void setCustomerMemberDao(CustomerMemberDao customerMemberDao) {
		this.customerMemberDao = customerMemberDao;
	}

	public boolean saveVipPaySuccess(long orderId) throws SqlException {
		Date dateNow = new Date();
		EbOrder ebOrder = ebOrderDao.findById(orderId);
		Customer customer = customerDao.getObject(Customer.class,
				ebOrder.getUserId());
		CustomerMember member = customerMemberDao.findOneByHql(
				"where valid=? and customer=? order by endTime desc",
				new Object[] { true, customer });
		int skuCode = ((EbOrderDetail) (ebOrder.getOrderDetails().toArray()[0]))
				.getSkuCode();
		int month = getMonth(skuCode);
		if (month == 0) {
			return true;
		}
		Calendar endTime = Calendar.getInstance();
		if (member == null) {
			// 从未是会员
			member = new CustomerMember();
			member.setCustomer(customer);
			member.setCreateTime(dateNow);
			member.setStartTime(dateNow);
		} else if (member.getEndTime().before(dateNow)) {
			// 过期
			member.setStartTime(dateNow);
		} else {
			// 续费
			endTime.setTime(member.getEndTime());
		}
		endTime.add(Calendar.MONTH, month);
		member.setEndTime(endTime.getTime());
		member.setValid(true);
		customerMemberDao.saveOrUpdate(member);
		//
		// if (member == null || member.getEndTime().before(dateNow)) {
		// member = new CustomerMember();
		// member.setCustomer(customer);
		// member.setCreateTime(dateNow);
		// member.setStartTime(dateNow);
		// Calendar endTime = Calendar.getInstance();
		// endTime.add(Calendar.MONTH, month);
		// member.setEndTime(endTime.getTime());
		// member.setValid(true);
		// customerMemberDao.save(member);
		// } else {
		// Calendar endTime = Calendar.getInstance();
		// endTime.setTime(member.getEndTime());
		// member = new CustomerMember();
		// member.setCustomer(customer);
		// member.setCreateTime(dateNow);
		// member.setStartTime(dateNow);
		// endTime.add(Calendar.MONTH, month);
		// member.setEndTime(endTime.getTime());
		// member.setValid(true);
		// customerMemberDao.save(member);
		// }
		return true;
	}

	/**
	 * 99开头的商品是预留的虚拟商品，下面四个特殊code是用于会员充值
	 * 
	 * @param productId
	 * @return
	 * @throws SqlException
	 */
	private int getMonth(int skuCode) throws SqlException {
		VipCostDefineService vipDefServ = SystemInitialization
				.getApplicationContext().getBean(VipCostDefineService.class);
		List<VipCostDefine> vipDefList = vipDefServ.getVipCostDefine();
		if (vipDefList == null || vipDefList.size() <= 0) {
			return 0;
		}
		for (VipCostDefine vipCostDefine : vipDefList) {
			if (vipCostDefine.getSkuCode() == skuCode) {
				return vipCostDefine.getVipType().getValue();
			}
		}
		return 0;
	}
}
