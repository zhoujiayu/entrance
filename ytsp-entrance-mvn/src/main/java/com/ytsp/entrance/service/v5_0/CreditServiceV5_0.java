package com.ytsp.entrance.service.v5_0;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ytsp.db.dao.CreditStrategyDao;
import com.ytsp.db.dao.CreditsRecordDao;
import com.ytsp.db.domain.CreditStrategy;
import com.ytsp.db.domain.CreditsRecord;
import com.ytsp.db.enums.CreditSourceTypeEnum;
import com.ytsp.db.exception.SqlException;

@Service("creditServiceV5_0")
@Transactional
public class CreditServiceV5_0 {
	
	@Resource(name = "creditStrategyDao")
	private CreditStrategyDao creditStrategyDao;
	@Resource(name = "creditsRecordDao")
	private CreditsRecordDao creditsRecordDao;
	
	/**
	* <p>功能描述:根据类型获取积分来源记录</p>
	* <p>参数：@param type
	* <p>参数：@param userId
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：CreditsRecord</p>
	 */
	public CreditsRecord findCreditRecordByType(int type,int userId) throws SqlException{
		return creditsRecordDao.findOneByHql("WHERE creditType = ? and userId = ?", new Object[]{CreditSourceTypeEnum.valueOf(type),userId});
	}
	
	/**
	* <p>功能描述:保存积分记录</p>
	* <p>参数：@param type
	* <p>参数：@param userId
	* <p>参数：@param desc
	* <p>参数：@param credits
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：void</p>
	 */
	public void saveCreditRecord(int type,int userId,String desc,int credits) throws SqlException{
		CreditsRecord record = new CreditsRecord();
		record.setCreditType(CreditSourceTypeEnum.valueOf(type));
		record.setUserId(userId);
		record.setCreditNumber(credits);
		record.setCreditSourceDesc(desc);
		record.setCreateDate(new Date());
		creditsRecordDao.save(record);
	}
	
	/**
	* <p>功能描述:保存积分记录</p>
	* <p>参数：@param type 积分类型：0：历史积分 1：非历史积分
	* <p>参数：@param uid
	* <p>参数：@param desc
	* <p>参数：@param credits
	* <p>参数：@param userOldCredits
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：void</p>
	 */
	public void saveCreditRecord(int type,int uid,String desc,int credits,int userOldCredits) throws SqlException{
		//获取历史积分
		CreditsRecord creditRec = findCreditRecordByType(0, uid);
		//将已有的用户积分保存为历史积分
		if(creditRec != null){
			saveCreditRecord(0, uid, "历史积分", userOldCredits);
		}
		//若积分大于0，保存积分来源记录
		if(credits != 0){
			saveCreditRecord(1, uid, desc, credits);
		}
	}
	
	/**
	* <p>功能描述:获取用户所有积分来源记录</p>
	* <p>参数：@param userId
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：CreditsRecord</p>
	 */
	public List<CreditsRecord> findUserCreditRecord(int userId,int page,int pageSize) throws SqlException{
		Calendar ca = Calendar.getInstance();
		ca.add(Calendar.YEAR, -1);
		return creditsRecordDao.findAllByHql("WHERE createDate >= ? and userId = ? and creditType != ? order by createDate desc",page*pageSize,pageSize, new Object[]{ca.getTime(),userId,CreditSourceTypeEnum.HISTORY});
	}
	
	/**
	* <p>功能描述:获取积分策略</p>
	* <p>参数：@return
	* <p>参数：@throws Exception</p>
	* <p>返回类型：JSONObject</p>
	 */
	public JSONObject getCreditPolicy() throws Exception{
		JSONObject jo = new JSONObject();
		List<CreditStrategy> ls = creditStrategyDao.findAllByHql("");
		JSONArray arr = new JSONArray();
		for (CreditStrategy creditPolicy : ls) {
			JSONObject foo = new JSONObject();
			foo.put("platform", creditPolicy.getPlatform().getText());
			foo.put("trigger", creditPolicy.getTrigger().getText());
			foo.put("num", creditPolicy.getNum());
			arr.put(foo);
		}
		jo.put("creditPolicyArray", arr);
		return jo;
	}
}
