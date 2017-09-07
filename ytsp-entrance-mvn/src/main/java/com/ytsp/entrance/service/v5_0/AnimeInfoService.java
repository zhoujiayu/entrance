package com.ytsp.entrance.service.v5_0;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ytsp.db.dao.AnimeInfoDao;
import com.ytsp.db.domain.AnimeInfo;
import com.ytsp.db.enums.AnimeInfoTypeEnum;
import com.ytsp.db.enums.ValidStatusEnum;
import com.ytsp.db.exception.SqlException;

@Service("animeInfoService")
@Transactional
public class AnimeInfoService {

	@Resource(name = "animeInfoDao")
	private AnimeInfoDao animeInfoDao;

	public AnimeInfoDao getAnimeInfoDao() {
		return animeInfoDao;
	}

	public void setAnimeInfoDao(AnimeInfoDao animeInfoDao) {
		this.animeInfoDao = animeInfoDao;
	}
	
	/**
	* <p>功能描述:获取指定个数的动漫资讯</p>
	* <p>参数：@param pageSize
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：List<AnimeInfo></p>
	 */
	public List<AnimeInfo> getAnimeInfoByPage(int pageSize,AnimeInfoTypeEnum infoType) throws SqlException{
		return animeInfoDao.findAllByHql(" WHERE status = ? and infoType = ? ORDER BY sortNum desc ", 0, pageSize, new Object[]{ValidStatusEnum.VALID,infoType});
	}
}
