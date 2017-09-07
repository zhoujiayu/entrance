package com.ytsp.entrance.service.v5_0;

import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ytsp.db.dao.AlbumCategoryDao;
import com.ytsp.db.dao.EbCatagoryDao;
import com.ytsp.db.domain.AlbumCategory;
import com.ytsp.db.domain.EbCatagory;
import com.ytsp.db.enums.AlbumCategoryTypeEnum;
import com.ytsp.db.enums.ValidStatusEnum;
import com.ytsp.db.exception.SqlException;
@Service("ebCatagoryService")
@Transactional
public class EbCatagoryService {
	static final Logger logger = Logger.getLogger(EbCouponService.class);
	
	@Resource(name="ebCatagoryDao")
	private EbCatagoryDao ebCatagoryDao;
	
	@Resource(name="albumCategoryDao")
	private AlbumCategoryDao albumCategoryDao;
	
	/**
	* @功能描述: 获取所有分类
	* @return
	* @throws SqlException     
	* List<EbCatagory>   
	* @author yusf
	 */
	public List<EbCatagory> getAllCatagorys() throws SqlException{
		String sql = " order by sortNum desc";
		return ebCatagoryDao.findAllByHql(sql);
	}

	public EbCatagoryDao getEbCatagoryDao() {
		return ebCatagoryDao;
	}

	public void setEbCatagoryDao(EbCatagoryDao ebCatagoryDao) {
		this.ebCatagoryDao = ebCatagoryDao;
	}
	
	/**
	* <p>功能描述:获取视频的某个级次的分类</p>
	* <p>参数：@param level
	* <p>参数：@param excludeId  排除某个Id
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：List<AlbumCategory></p>
	 */
	public List<AlbumCategory> getAlbumCategoryByLevel(int level,int type) throws SqlException{
		return albumCategoryDao.findAllByHql("WHERE type= ? and  status = ? and level = ? order by sortNum ",new Object[]{AlbumCategoryTypeEnum.valueOf(type),ValidStatusEnum.VALID,level});
	}
	/**
	* <p>功能描述:获取动漫分类</p>
	* <p>参数：@param type
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：List<AlbumCategory></p>
	 */
	public List<AlbumCategory> getAnimeCategory(int type) throws SqlException{
		return albumCategoryDao.findAllByHql("WHERE type= ? and  status = ? order by sortNum ",new Object[]{AlbumCategoryTypeEnum.valueOf(type),ValidStatusEnum.VALID});
	}
}
