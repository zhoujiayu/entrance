package com.ytsp.entrance.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ytsp.db.dao.ImageVerifyDao;
import com.ytsp.db.domain.ImageVerify;
import com.ytsp.db.exception.SqlException;

@Service("imageVerifyService")
@Transactional
public class ImageVerifyService {
	
	@Resource(name="imageVerifyDao")
	private ImageVerifyDao imageVerifyDao;
	
	/**
	* <p>功能描述:保存图片验证</p>
	* <p>参数：@param imageVerify
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：void</p>
	 */
	public void saveImageVerify(ImageVerify imageVerify) throws SqlException{
		imageVerifyDao.save(imageVerify);
	}
	
	/**
	* <p>功能描述:根据code获取验证码</p>
	* <p>参数：@param Code
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：ImageVerify</p>
	 */
	public ImageVerify getImageVerifyByCode(String code) throws SqlException{
		return imageVerifyDao.findOneByHql(" WHERE code = ? ", new Object[]{code});
	}
	
	/**
	* <p>功能描述:根据code和设备号获取验证码</p>
	* <p>参数：@param Code
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：ImageVerify</p>
	 */
	public ImageVerify getImageVerifyByCodeAndDevice(String code,String device) throws SqlException{
		return imageVerifyDao.findOneByHql(" WHERE code = ? and deviceNumber = ?", new Object[]{code,device});
	}
}
