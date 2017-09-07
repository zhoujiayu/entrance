package com.ytsp.entrance.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ytsp.db.dao.ShippingRuleDao;
import com.ytsp.db.domain.ShippingRule;
import com.ytsp.db.enums.ValidStatusEnum;
import com.ytsp.db.exception.SqlException;

@Service("shippingRuleService")
@Transactional
public class ShippingRuleService {
	@Resource(name = "shippingRuleDao")
	private ShippingRuleDao shippingRuleDao;
	
	/**
	* <p>功能描述:获取邮费规则</p>
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：Map<Integer,ShippingRule></p>
	 */
	public Map<Integer,ShippingRule> getShippingRule() throws SqlException{
		Map<Integer,ShippingRule> shippingMap = new HashMap<Integer, ShippingRule>();
		StringBuffer sql = new StringBuffer();
		sql.append(" WHERE valid = ? ");
		List<ShippingRule> rules = shippingRuleDao.findAllByHql(sql.toString(), new Object[]{ValidStatusEnum.VALID});
		for (ShippingRule shippingRule : rules) {
			shippingMap.put(shippingRule.getShippingType().getValue(), shippingRule);
		}
		return shippingMap;
	}
}
