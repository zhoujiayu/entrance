package com.ytsp.entrance.service.v5_0;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ytsp.db.dao.SendSmsConfigDao;
import com.ytsp.db.domain.SendSmsConfig;
import com.ytsp.db.exception.SqlException;

@Service("sendSmsConfigService")
@Transactional
public class SendSmsConfigService {
	@Resource(name = "sendSmsConfigDao")
	private SendSmsConfigDao sendSmsConfigDao;
	
	/**
	* <p>功能描述:获取发送短信配置</p>
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：List<SendSmsConfig></p>
	 */
	public List<SendSmsConfig> getSendSmsConfig() throws SqlException{
		return sendSmsConfigDao.findAllByHql(" WHERE status = 1 order by sortNum");
	}
}
