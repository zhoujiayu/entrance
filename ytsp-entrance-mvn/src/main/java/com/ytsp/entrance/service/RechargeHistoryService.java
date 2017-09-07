package com.ytsp.entrance.service;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.ytsp.common.util.StringUtil;
import com.ytsp.db.dao.HardwareRegisterDao;
import com.ytsp.db.dao.MonthlyDao;
import com.ytsp.db.dao.RechargeHistoryDao;
import com.ytsp.db.domain.Customer;
import com.ytsp.db.domain.HardwareRegister;
import com.ytsp.db.domain.Monthly;
import com.ytsp.db.domain.RechargeHistory;
import com.ytsp.entrance.util.DateFormatter;
import com.ytsp.entrance.util.DateTimeFormatter;

/**
 * @author GENE
 * @description 充值服务
 */
public class RechargeHistoryService {
	private static final Logger logger = Logger.getLogger(RechargeHistoryService.class);

	private MonthlyDao monthlyDao;
	private RechargeHistoryDao rechargeHistoryDao;
	private HardwareRegisterDao hardwareRegisterDao;
	
	public JSONObject getRechargeHistoryArray(Customer customer, String basePath, String hardwareNumber) throws Exception {
//		List<RechargeHistory> rechargeHistorys = rechargeHistoryDao.findAllByHql(" WHERE customer.id = ? ORDER BY time DESC", start, limit, new Object[]{customerId});
		List<RechargeHistory> rechargeHistorys = rechargeHistoryDao.findAllByHql(" WHERE customer.id = ? ORDER BY time DESC", new Object[]{customer.getId()});
		Monthly monthly = monthlyDao.findOneByHql(" WHERE customer.id=?", new Object[]{customer.getId()});
		JSONObject obj = new JSONObject();
		obj.put("accountName", StringUtil.isNullOrEmpty(customer.getNick()) ? customer.getAccount() : customer.getNick());
		
		boolean isVip = (monthly != null && monthly.getExpireTime() != null && monthly.getExpireTime().after(new Date())) ? true : false;
		String accountStatus = "普通用户";
		String expire = DateFormatter.date2String(monthly != null ? monthly.getExpireTime() : null);
		if(!isVip){
			//检查是否超过试用期
			HardwareRegister hw = hardwareRegisterDao.findOneByHql(" WHERE number=?", new Object[]{hardwareNumber});
			if(hw != null){
				Date probation = hw.getProbation();
				if(probation != null && probation.getTime() > new Date().getTime()){//仍在试用期内
					accountStatus = "试用用户";
					expire = DateFormatter.date2String(probation);
				}
			}
		}else{
			accountStatus = "VIP用户";
		}
		
		obj.put("accountStatus", accountStatus);
		obj.put("vipExpire", expire);
		
		String url = basePath + "rc/mobiless.wtf?uid=" + customer.getId() + "&method=";
		obj.put("diankaURL", url + "prepaid");//点卡充值
		obj.put("savingsURL", url + "debitcard");//储蓄卡充值
		obj.put("creditURL", url + "creditcard");//信用卡充值
		obj.put("alipayURL", url + "ali");//支付宝充值
		
		JSONArray array = new JSONArray();
		for (RechargeHistory rechargeHistory : rechargeHistorys) {
			JSONObject hi = new JSONObject();
			hi.put("time", DateTimeFormatter.dateTime2String(rechargeHistory.getTime()));
			hi.put("subject", String.format("购买VIP:%s个月", rechargeHistory.getDuration()));
			hi.put("amount", rechargeHistory.getRechargeAmount());
			hi.put("method", convertPayMethod(rechargeHistory.getPayMethod()));
			hi.put("status", rechargeHistory.getStatus() == null ? "" : rechargeHistory.getStatus().getText());
			array.put(hi);
		}
		obj.put("history", array);
		return obj;
	}
	
	private String convertPayMethod(String code){
		// 支付宝余额-》1.余额支付，储蓄卡-》2.网银支付，信用卡-》4.信用支付，充值-》10.点卡支付, 11.推广赠送-》通过推荐15个好友后
		if(StringUtil.isNullOrEmpty(code)){
			return "未知";
		}
		code = code.trim();
		if("1".equals(code)){
			return "支付宝余额";
		}else if("2".equals(code)){
			return "储蓄卡";
		}else if("4".equals(code)){
			return "信用卡";
		}else if("10".equals(code)){
			return "充值";
		}else if("11".equals(code)){
			return "推广赠送";
		}
		
		return "未知";
	}

	public int getRechargeHistoryCount(int customerId) throws Exception {
		return rechargeHistoryDao.getRecordCount(" WHERE customer.id = ?", new Object[]{customerId});
	}
	
	public void saveRechargeHistory(RechargeHistory rechargeHistory) throws Exception {
		rechargeHistoryDao.save(rechargeHistory);
	}

	public void saveOrUpdate(RechargeHistory rechargeHistory) throws Exception {
		rechargeHistoryDao.saveOrUpdate(rechargeHistory);
	}

	public void updateRechargeHistory(RechargeHistory rechargeHistory) throws Exception {
		rechargeHistoryDao.update(rechargeHistory);
	}

	public void deleteRechargeHistory(RechargeHistory rechargeHistory) throws Exception {
		rechargeHistoryDao.delete(rechargeHistory);
	}

	public RechargeHistory findRechargeHistoryById(int rechargeHistoryid) throws Exception {
		return rechargeHistoryDao.findById(rechargeHistoryid);
	}

	public List<RechargeHistory> getAllRechargeHistorys() throws Exception {
		return rechargeHistoryDao.getAll();
	}

	public void deleteRechargeHistoryById(int rechargeHistoryid) throws Exception {
		rechargeHistoryDao.deleteById(rechargeHistoryid);
	}

	public RechargeHistoryDao getRechargeHistoryDao() {
		return rechargeHistoryDao;
	}

	public void setRechargeHistoryDao(RechargeHistoryDao rechargeHistoryDao) {
		this.rechargeHistoryDao = rechargeHistoryDao;
	}

	public MonthlyDao getMonthlyDao() {
		return monthlyDao;
	}

	public void setMonthlyDao(MonthlyDao monthlyDao) {
		this.monthlyDao = monthlyDao;
	}

	public HardwareRegisterDao getHardwareRegisterDao() {
		return hardwareRegisterDao;
	}

	public void setHardwareRegisterDao(HardwareRegisterDao hardwareRegisterDao) {
		this.hardwareRegisterDao = hardwareRegisterDao;
	}
}
