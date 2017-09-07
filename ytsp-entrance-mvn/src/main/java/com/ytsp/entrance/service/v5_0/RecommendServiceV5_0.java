package com.ytsp.entrance.service.v5_0;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ytsp.db.dao.EbPosterDao;
import com.ytsp.db.dao.EbTopProductDao;
import com.ytsp.db.dao.EbTopToyProductDao;
import com.ytsp.db.dao.RecommendDao;
import com.ytsp.db.dao.TopAlbumDao;
import com.ytsp.db.domain.Album;
import com.ytsp.db.domain.EbPoster;
import com.ytsp.db.domain.EbProduct;
import com.ytsp.db.domain.Recommend;
import com.ytsp.db.enums.EbPosterAppLocationEnum;
import com.ytsp.db.enums.RecommendTypeEnum;
import com.ytsp.db.enums.RecommendVersionEnum;
import com.ytsp.db.enums.SelectAppTypeConditionEnum;
import com.ytsp.db.enums.ValidStatusEnum;
import com.ytsp.db.exception.SqlException;

@Service("recommendServiceV5_0")
@Transactional
public class RecommendServiceV5_0 {

	@Resource(name = "recommendDao")
	private RecommendDao recommendDao;

	@Resource(name = "ebPosterDao")
	private EbPosterDao ebPosterDao;

	@Resource(name = "topAlbumDao")
	private TopAlbumDao topAlbumDao;

	@Resource(name = "ebTopProductDao")
	private EbTopProductDao ebTopProductDao;
	@Resource(name = "ebTopToyProductDao")
	private EbTopToyProductDao ebTopToyProductDao;

	public List<Recommend> getRecommend(RecommendTypeEnum ihead,RecommendVersionEnum version)
			throws SqlException {
		return recommendDao.findAllByHql(
				" where recommendType=? and valid=? and version = ? order by sort",
				new Object[] { ihead, ValidStatusEnum.VALID,version});
	}

	public List<EbPoster> getRecommendPoster() throws SqlException {
		Date now = new Date();
		return ebPosterDao.findAllByHql(
				" where  location in(?,?,?,?,?) and startTime<? and appType =? order by sortNum asc",
				new Object[] { EbPosterAppLocationEnum.APPRECOMMENDLARGE.getValue(),
						EbPosterAppLocationEnum.APPRECOMMEND2ED.getValue(),
						EbPosterAppLocationEnum.APPRECOMMEND3RD.getValue(),
						EbPosterAppLocationEnum.APPBANNER.getValue(),EbPosterAppLocationEnum.NAVIGATIONBAR.getValue(), now ,SelectAppTypeConditionEnum.RECOMMEND});
	}

	/**
	 * 热播动漫
	 * 
	 * @return
	 */
	public List<Album> getRecommendAlbum(boolean isIOSInReview) {
		return topAlbumDao.getRecommendAlbum(13,isIOSInReview);
	}

	/**
	 * 热卖玩具
	 * 
	 * @return
	 */
	public List<EbProduct> getRecommendProduct(int page, int pagesize) {

		return ebTopProductDao.getRecommendProduct(page, pagesize);
	}
	
	/**
	 * 玩具首页推荐热卖玩具
	 * 
	 * @return
	 */
	public List<EbProduct> getRecommendHotProduct(int page, int pagesize) {

		return ebTopToyProductDao.getRecommendProduct(page, pagesize);
	}
}
