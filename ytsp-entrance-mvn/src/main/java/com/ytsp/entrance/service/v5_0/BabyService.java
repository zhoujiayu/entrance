package com.ytsp.entrance.service.v5_0;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ytsp.db.dao.BabyDao;
import com.ytsp.db.domain.Baby;
import com.ytsp.db.domain.Customer;
import com.ytsp.db.enums.ValidStatusEnum;
import com.ytsp.db.exception.SqlException;

@Service("babyService")
@Transactional
public class BabyService {
	@Resource(name = "babyDao")
	private BabyDao babyDao;
	
	/**
	* <p>功能描述:获取用户的宝宝信息</p>
	* <p>参数：@param userId 用户id
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：List<Baby></p>
	 */
	public List<Baby> getCustomerBaby(int userId) throws SqlException{
		StringBuffer sb = new StringBuffer();
		sb.append("select * from ytsp_baby WHERE customer = ").append(userId).append(" and status = ").append(ValidStatusEnum.VALID.getValue());
		return babyDao.sqlFetch(sb.toString(), Baby.class, 0, 3);
	}
	
	/**
	* <p>功能描述:保存用户的宝宝</p>
	* <p>参数：@param baby
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：void</p>
	 */
	public void saveBaby(Baby baby) throws SqlException{
		babyDao.save(baby);
	}
	
	public void deleteBaby(int id) throws SqlException{
		String sql = " SET status = 0 where id = "+id;
		babyDao.updateByHql(sql);
	}
	
	public void updateBaby(Baby baby) throws SqlException{
		babyDao.update(baby);
	}
	
	
	public Baby getBabyById(int id) throws SqlException{
		return babyDao.findById(id);
	}
}
