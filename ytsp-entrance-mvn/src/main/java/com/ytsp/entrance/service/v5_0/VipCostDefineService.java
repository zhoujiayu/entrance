package com.ytsp.entrance.service.v5_0;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ytsp.db.dao.VipCostDefineDao;
import com.ytsp.db.domain.VipCostDefine;
import com.ytsp.db.enums.ValidStatusEnum;
import com.ytsp.db.exception.SqlException;

@Service("vipCostDefineService")
@Transactional
public class VipCostDefineService {
	@Resource(name = "vipCostDefineDao")
	private VipCostDefineDao vipCostDefineDao;
	
	public List<VipCostDefine> getVipCostDefine() throws SqlException{
		return vipCostDefineDao.findAllByHql(" WHERE status = ? order by sortNum ", new Object[]{ValidStatusEnum.VALID});
	}
	
	public VipCostDefine getVipCostDefineBySkuCode(int skuCode) throws SqlException{
		StringBuffer sql = new StringBuffer();
		sql.append("select * from vip_cost_define where status = 1 and skuCode = ").append(skuCode);
		List<VipCostDefine> vipCostDefList = vipCostDefineDao.sqlFetch(sql.toString(), VipCostDefine.class, 0, 2);
		if(vipCostDefList == null || vipCostDefList.size() <= 0){
			return null;
		}
		return vipCostDefList.get(0);
	}
}
