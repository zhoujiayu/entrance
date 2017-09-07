package com.ytsp.entrance.service.v5_0;

import java.util.Date;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ytsp.db.dao.ScanningRedirectDao;
import com.ytsp.db.domain.ScanningRedirect;
import com.ytsp.db.enums.ValidStatusEnum;
import com.ytsp.db.exception.SqlException;


@Service("scanningServiceV5_0")
@Transactional
public class ScanningServiceV5_0 {
	
	@Resource(name = "scanningRedirectDao")
	private ScanningRedirectDao scanningRedirectDao;
	
	/**
	* <p>功能描述:根据md5Code获取对应的扫一扫跳转链接</p>
	* <p>参数：@param md5Code
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：String</p>
	 */
	public String getScanningRedirectURL(String md5Code) throws SqlException{
		String url = "";
		Date now = new Date();
		ScanningRedirect scanning = scanningRedirectDao.findOneByHql(" WHERE startTime < ? and status = ?  and endTime > ? and  MD5Code = ? ", new Object[]{now,ValidStatusEnum.VALID,now,md5Code});
		if(scanning != null){
			return scanning.getRedirectUrl();
		}
		return url;
	}
}
