package com.ytsp.entrance.service.v4_0;

import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.ytsp.common.util.StringUtil;
import com.ytsp.db.audit.AuditAction;
import com.ytsp.db.dao.CreditPolicyDao;
import com.ytsp.db.dao.CreditsRecordDao;
import com.ytsp.db.dao.CustomerDao;
import com.ytsp.db.domain.CreditPolicy;
import com.ytsp.db.domain.CreditsRecord;
import com.ytsp.db.domain.Customer;
import com.ytsp.db.enums.CreditStrategyTriggerEnum;
import com.ytsp.db.exception.SqlException;
import com.ytsp.entrance.util.Util;

public class CreditService {
	static final Logger logger = Logger.getLogger(CreditService.class);
	
	private CreditPolicyDao creditPolicyDao ;
	@Resource(name = "creditsRecordDao")
	private CreditsRecordDao creditsRecordDao;
	
	public CustomerDao getCustomerDao() {
		return customerDao;
	}
	public void setCustomerDao(CustomerDao customerDao) {
		this.customerDao = customerDao;
	}
	private CustomerDao customerDao;
	public JSONObject getCreditPolicy() throws Exception{
		JSONObject jo = new JSONObject();
		List<CreditPolicy> ls = creditPolicyDao.findAllByHql("");
		JSONArray arr = new JSONArray();
		for (CreditPolicy creditPolicy : ls) {
			JSONObject foo = new JSONObject();
			foo.put("platform", creditPolicy.getPlatform().getText());
			foo.put("trigger", creditPolicy.getTrigger().getText());
			foo.put("num", creditPolicy.getNum());
			arr.put(foo);
		}
		jo.put("creditPolicyArray", arr);
		return jo;
	}
	public int getCreditByUser(int uid) throws SqlException{
		return customerDao.findById(uid).getCredits();
	}
	public boolean transactionCreditAdd(int uid,int num,String action) throws SqlException{
		Customer c = customerDao.findById(uid);
		int credits = c.getCredits();
		c.setCredits(c.getCredits()+num >=0 ?c.getCredits()+num : 0);
		customerDao.update(c);
		try {
			CreditStrategyTriggerEnum creditStr  = CreditStrategyTriggerEnum.valueOf(action);
			Util.saveCreditRecord(creditStr.getValue(), uid, creditStr.getDescription(), num, credits);
			String serialNumber = UUID.randomUUID().toString();
			String creditUseDes = "";
			if(StringUtil.isNotNullNotEmpty(action)){
				creditUseDes = creditStr.getDescription();
			}
			customerDao.manualAudit(Util.getAudit(AuditAction.UPDATE, c, creditUseDes+",赠送用户积分："+num,serialNumber));
		} catch (Exception e) {
			logger.error("------>>用户增加积分审记失败."+e.getMessage());
		}
		return true;
	}
	
	/**
	* <p>功能描述:根据类型获取积分来源记录</p>
	* <p>参数：@param type
	* <p>参数：@param userId
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：CreditsRecord</p>
	 */
	public CreditsRecord findCreditRecordByType(int type,int userId) throws SqlException{
		return creditsRecordDao.findOneByHql("WHERE creditType = ? and userId = ?", new Object[]{type,userId});
	}
	
	public CreditPolicyDao getCreditPolicyDao() {
		return creditPolicyDao;
	}
	public void setCreditPolicyDao(CreditPolicyDao creditPolicyDao) {
		this.creditPolicyDao = creditPolicyDao;
	}
}
