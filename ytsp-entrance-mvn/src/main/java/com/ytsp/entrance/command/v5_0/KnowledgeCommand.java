package com.ytsp.entrance.command.v5_0;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.google.gson.Gson;
import com.ytsp.common.util.StringUtil;
import com.ytsp.db.domain.Album;
import com.ytsp.db.domain.AlbumCategory;
import com.ytsp.db.domain.AnimeInfo;
import com.ytsp.db.domain.EbPoster;
import com.ytsp.db.domain.HotKnowledge;
import com.ytsp.db.domain.Recommend;
import com.ytsp.db.enums.AlbumCategoryTypeEnum;
import com.ytsp.db.enums.AnimeInfoTypeEnum;
import com.ytsp.db.enums.EbPosterAppLocationEnum;
import com.ytsp.db.enums.EbPosterLocationEnum;
import com.ytsp.db.enums.MobileTypeEnum;
import com.ytsp.db.enums.RecommendTypeEnum;
import com.ytsp.db.enums.RecommendVersionEnum;
import com.ytsp.db.enums.SelectAppTypeConditionEnum;
import com.ytsp.db.exception.SqlException;
import com.ytsp.db.vo.AlbumCategoryVO;
import com.ytsp.db.vo.AlbumVO;
import com.ytsp.db.vo.PosterVO;
import com.ytsp.db.vo.RecommendVO;
import com.ytsp.entrance.command.base.AbstractCommand;
import com.ytsp.entrance.command.base.CommandList;
import com.ytsp.entrance.command.base.ExecuteResult;
import com.ytsp.entrance.service.BaseConfigService;
import com.ytsp.entrance.service.v5_0.AlbumServiceV5_0;
import com.ytsp.entrance.service.v5_0.AnimeInfoService;
import com.ytsp.entrance.service.v5_0.EbCatagoryService;
import com.ytsp.entrance.service.v5_0.KnowledgeService;
import com.ytsp.entrance.service.v5_0.RecommendServiceV5_0;
import com.ytsp.entrance.system.IConstants;
import com.ytsp.entrance.system.SystemInitialization;
import com.ytsp.entrance.util.NumericUtil;
import com.ytsp.entrance.util.Util;

public class KnowledgeCommand extends AbstractCommand{
	
	private static final Integer INFOSIZE = 8;
	
	@Override
	public boolean canExecute() {
		int code = getContext().getHead().getCommandCode();
		return (code == CommandList.CMD_KNOWLEDGE_HOMEPAGE
				||code == CommandList.CMD_KNOWLEDGE_BYCATEGORY);
	}

	@Override
	public ExecuteResult execute() {
		int code = getContext().getHead().getCommandCode();
		try {
			if (code == CommandList.CMD_KNOWLEDGE_HOMEPAGE) {
				return getKnowledgeHomePage();
			}else if(code == CommandList.CMD_KNOWLEDGE_BYCATEGORY){
				return getKnowledgeByCategory();
			}
		} catch (Exception e) {
			logger.error("BabyCommand," + " HeadInfo :"
					+ getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}
		return null;
	}
	
	/**
	 * 入参：categoryId,page,pageSize
	* <p>功能描述:分页获取某一分类知识</p>
	* <p>参数：@return</p>
	* <p>返回类型：ExecuteResult</p>
	 */
	private ExecuteResult getKnowledgeByCategory(){
		try {
			JSONObject result = new JSONObject();
			JSONObject reqBody = getContext().getBody().getBodyObject();
			int page = -1;
			int pageSize = -1;
			Integer categoryId = null;
			
			if(reqBody.isNull("categoryId")){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"categoryId不能为空", result, this);
			}
			if(!reqBody.isNull("page")){
				page = reqBody.getInt("page");
			}
			if(!reqBody.isNull("pageSize")){
				pageSize = reqBody.getInt("pageSize");
			}
			categoryId = reqBody.getInt("categoryId");
			KnowledgeService knowleServ = SystemInitialization.getApplicationContext().getBean(KnowledgeService.class);
			//获取所有分类下的所有知识
			List<Album> knowledges = knowleServ.getKnowledgeByCategoryId(categoryId, page, pageSize, 2, AlbumCategoryTypeEnum.KNOWLEDGE.getValue(), getContext().getHead().getPlatform());
			AlbumInfoVO info = new AlbumInfoVO();
			//构建返回数据
			info.setAlbums(buildAlbumVO(knowledges));
			Gson gson = new Gson();
			result = new JSONObject(gson.toJson(info));
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,"分类获取知识成功", result, this);
		} catch (Exception e) {
			logger.error("getKnowledgeByCategory() error," + " HeadInfo :"
					+ getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}
	}
	
	/**
	* <p>功能描述:知识首页</p>
	* <p>参数：@return</p>
	* <p>返回类型：ExecuteResult</p>
	 */
	private ExecuteResult getKnowledgeHomePage(){
		try {
			int version = 5;
			String headVersion = getContext().getHead().getVersion();
			if(StringUtil.isNotNullNotEmpty(headVersion) 
					&& Util.validateVersion(headVersion)){
				version = Integer.parseInt(headVersion.split("\\.")[0]);
			}
			// 知识首页VO
			KnowledgeHomePageVO knowledgeHome = new KnowledgeHomePageVO();
			RecommendServiceV5_0 rs = SystemInitialization
					.getApplicationContext()
					.getBean(RecommendServiceV5_0.class);
			List<Recommend> rds = rs.getRecommend(RecommendTypeEnum.IKNOWLEDGE,RecommendVersionEnum.valueOf(version));
			AlbumServiceV5_0 albumServ = SystemInitialization
					.getApplicationContext().getBean(AlbumServiceV5_0.class);
			//1 设置知识分类
			knowledgeHome.setCategorys(getAlbumCategoryVOs(2,AlbumCategoryTypeEnum.KNOWLEDGE.getValue()));
			//2 处理知识推荐页
			fillRecommend(rds, knowledgeHome);
			//3处理知识资讯
			fillAnimeInfo(knowledgeHome);
			List<EbPoster> posters = albumServ.getAnimePoster(SelectAppTypeConditionEnum.KNOWLEDGE);
			//4 处理知识首页海报
			fillPosters(posters, knowledgeHome);
			//5 处理热门知识
			fillHotKnowledge(knowledgeHome);
			//6 处理最近观看知识，若没有自动设置为热播知识的第一个
			fillRecentKnowledge(knowledgeHome);
			//7.设置所有搜索热词
			Map<String,List<String>> allHotKeys = getAllHotSearchKeys();
			List<String> knowledgeHotKeys = allHotKeys.get(IConstants.CONFIG_KNOWLEDGE_SK);
			//设置动漫热搜词
			//由于iphone5.0.4和android5.0.2版本还有用hotKyes,所以兼容以前版本，保留该数据。
			knowledgeHome.setHotKeys(knowledgeHotKeys);
			knowledgeHome.setKnowledgeHotKeys(knowledgeHotKeys);
			knowledgeHome.setProductHotKeys(allHotKeys.get(IConstants.CONFIG_PRODUCT_SK));
			knowledgeHome.setAlbumHotKeys(allHotKeys.get(IConstants.CONFIG_ALBUM_SK));
			knowledgeHome.setRecommendHotKeys(allHotKeys.get(IConstants.CONFIG_RECOMMEND_SK));
			Gson gson = new Gson();
			JSONObject result = new JSONObject(gson.toJson(knowledgeHome));
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
					"获取知识首页成功", result, this);
		} catch (Exception e) {
			logger.error("animeHomePage() error," + " HeadInfo :"
					+ getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}
	
	}
	
	/**
	* <p>功能描述:获取所有热搜词</p>
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：List<String></p>
	 */
	private Map<String,List<String>> getAllHotSearchKeys() throws SqlException{
		BaseConfigService baseConfServ = SystemInitialization.getApplicationContext().getBean(BaseConfigService.class);
		return baseConfServ.getAllHotSearchKeys();
	}
	
	/**
	* <p>功能描述:获取推荐热搜词</p>
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：List<String></p>
	 */
	private List<String> getHotSearchKeys(String code) throws SqlException{
		BaseConfigService baseConfServ = SystemInitialization.getApplicationContext().getBean(BaseConfigService.class);
		return baseConfServ.getHotSearchKeys(code);
	}
	
	/**
	* <p>功能描述:获取某一级某种类型的视频分类</p>
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：List<AlbumCategoryVO></p>
	 */
	private List<AlbumCategoryVO> getAlbumCategoryVOs(int level,int type) throws SqlException{
		EbCatagoryService categoryServ = SystemInitialization.getApplicationContext().getBean(EbCatagoryService.class);
		List<AlbumCategory> categoryVOs = categoryServ.getAlbumCategoryByLevel(level, type);
		return buildAlbumCategoryVO(categoryVOs);
	}
	
	/**
	* <p>功能描述:构建视频分类VO</p>
	* <p>参数：@param categoryVOs
	* <p>参数：@return</p>
	* <p>返回类型：List<AlbumCategoryVO></p>
	 */
	private List<AlbumCategoryVO> buildAlbumCategoryVO(List<AlbumCategory> categoryVOs){
		if(categoryVOs == null || categoryVOs.size() <= 0){
			return new ArrayList<AlbumCategoryVO>();
		}
		List<AlbumCategoryVO> voList = new ArrayList<AlbumCategoryVO>();
		for (AlbumCategory albCate : categoryVOs) {
			AlbumCategoryVO vo = new AlbumCategoryVO();
			vo.setAlbumCategoryId(albCate.getId());
			vo.setCategoryName(albCate.getCname());
//			vo.setImageSrc(Util.getFullImageURL(albCate.getImageSrc()));
			vo.setImageSrc(Util.getFullImageURLByVersion(albCate.getImageSrc(),
					getContext().getHead().getVersion(), getContext().getHead()
							.getPlatform()));
			voList.add(vo);
		}
		return voList;
	}
	
	/**
	* <p>功能描述:设置热门知识</p>
	* <p>参数：@param knowledgeHome</p>
	* <p>返回类型：void</p>
	 * @throws SqlException 
	 */
	private void fillHotKnowledge(KnowledgeHomePageVO knowledgeHome) throws SqlException{
		KnowledgeService knowServ = SystemInitialization.getApplicationContext().getBean(KnowledgeService.class);
		//视频分类list
		List<AlbumCategory> categoryList = knowServ.getKnowledgeCategorys();
		//分类的Map
		Map<Integer,String> cateMap = buildCategoryMap(categoryList);
		//热门知识VOMap
		Map<Integer,HotKnowledgeVO> knowledgeMap = new HashMap<Integer, HotKnowledgeVO>();
		//数据库里所有的热门知识
		List<HotKnowledge> hotKnowledge = knowServ.getHotKnowledge();
		int sortNum = 0;
		//将热门知识以分类id为key放么到knowledgeMap中
		for (HotKnowledge knowle : hotKnowledge) {
			int categoryId = knowle.getCategoryId();
			//若热门知识的分类不存在，则过滤掉
			if(!cateMap.containsKey(categoryId)){
				continue;
			}
			if(knowledgeMap.containsKey(categoryId)){
				HotKnowledgeVO hotVO = knowledgeMap.get(categoryId);
				hotVO.getKnowledgeList().add(buildKnowledgeVO(knowle));
			}else {
				HotKnowledgeVO hotVo = new HotKnowledgeVO();
				hotVo.setCategoryId(knowle.getCategoryId());
				hotVo.setCategoryName(cateMap.get(knowle.getCategoryId()));
				hotVo.setSortNum(sortNum);
				List<KnowledgeVO> list = new ArrayList<KnowledgeVO>();
				list.add(buildKnowledgeVO(knowle));
				hotVo.setKnowledgeList(list);
				knowledgeMap.put(knowle.getCategoryId(), hotVo);
				sortNum ++;
			}
		}
		//对热门知识进行排序
		HotKnowledgeVO[] hots =knowledgeMap.values().toArray(new HotKnowledgeVO[0]);
		Arrays.sort(hots, new Comparator<HotKnowledgeVO>() {
			@Override
			public int compare(HotKnowledgeVO o1, HotKnowledgeVO o2) {
				return o1.getSortNum() > o2.getSortNum() ? 1:-1;
			}
		});
		knowledgeHome.setCategoryKnowledge(hots);
	}
	
	/**
	* <p>功能描述:构建热门知识VO</p>
	* <p>参数：@param knowle
	* <p>参数：@return</p>
	* <p>返回类型：KnowledgeVO</p>
	 */
	private KnowledgeVO buildKnowledgeVO(HotKnowledge knowle){
		if(knowle == null){
			return null;
		}
		KnowledgeVO vo = new KnowledgeVO();
		vo.setAlbumId(knowle.getAlbum().getId());
		vo.setCategoryId(knowle.getCategoryId());
//		vo.setImg(Util.getFullImageURL(knowle.getImg()));
		vo.setImg(Util.getFullImageURLByVersion(knowle.getImg(), getContext()
				.getHead().getVersion(), getContext().getHead().getPlatform()));
		vo.setKnowledgeId(knowle.getId());
		vo.setRedirect(knowle.getRedirect());
		vo.setSubTitle(knowle.getSubtitle());
		vo.setTitle(knowle.getTitle());
		if(knowle.getAlbum() != null){
			vo.setVip(knowle.getAlbum().getVip());
		}
		return vo;
	}
	
	/**
	* <p>功能描述:将视频分类hash化，key值为分类id,value值为分类名称</p>
	* <p>参数：@param categoryList
	* <p>参数：@return</p>
	* <p>返回类型：Map<Integer,String></p>
	 */
	private Map<Integer,String> buildCategoryMap(List<AlbumCategory> categoryList){
		Map<Integer,String> ret = new HashMap<Integer, String>();
		if(categoryList == null || categoryList.size() <= 0){
			return ret;
		}
		for (AlbumCategory albumCategory : categoryList) {
			ret.put(albumCategory.getId(), albumCategory.getCname());
		}
		return ret;
	}
	
	/**
	 * <p>
	 * 功能描述:推荐动漫
	 * </p>
	 * <p>
	 * 参数：@param rds
	 * <p>
	 * 参数：@param animeHome
	 * </p>
	 * <p>
	 * 返回类型：void
	 * </p>
	 */
	private void fillRecommend(List<Recommend> rds, KnowledgeHomePageVO knowledgeHome) {
		List<RecommendVO> recommends = new ArrayList<RecommendVO>();
		for (Recommend r : rds) {
			RecommendVO vo = new RecommendVO();
			vo.setId(r.getId());
			//ipad与手机的轮播图大小不一致
			if((MobileTypeEnum.valueOf(getContext().getHead()
					.getPlatform()) == MobileTypeEnum.ipad)){
//				vo.setImg(Util.getFullImageURL(r.getPadimg()));
				vo.setImg(Util.getFullImageURLByVersion(r.getPadimg(),
						getContext().getHead().getVersion(), getContext()
								.getHead().getPlatform()));
			}else{
//				vo.setImg(Util.getFullImageURL(r.getImg()));
				vo.setImg(Util.getFullImageURLByVersion(r.getImg(),
						getContext().getHead().getVersion(), getContext()
								.getHead().getPlatform()));
			}
			vo.setRedirect(r.getRedirect());
			vo.setSort(NumericUtil.parseInt(r.getSort(), 10000));
			vo.setSummary(r.getSummary());
			recommends.add(vo);
		}
		knowledgeHome.setRecommends(recommends);
	}
	
	/**
	 * <p>
	 * 功能描述:知识资讯
	 * </p>
	 * <p>
	 * 参数：@param animeHome
	 * </p>
	 * <p>
	 * 返回类型：void
	 * </p>
	 * 
	 * @throws SqlException
	 */
	private void fillAnimeInfo(KnowledgeHomePageVO knowledgeHome) throws SqlException {
		AnimeInfoService animeServ = SystemInitialization
				.getApplicationContext().getBean(AnimeInfoService.class);
		List<AnimeInfo> animeInfos = animeServ
				.getAnimeInfoByPage(INFOSIZE,AnimeInfoTypeEnum.KNOWLEDGE);
		List<AnimeInfoVO> infoVOs = new ArrayList<AnimeInfoVO>();
		for (AnimeInfo info : animeInfos) {
			AnimeInfoVO animeInfoVO = new AnimeInfoVO();
			animeInfoVO.setContent(info.getContent());
			animeInfoVO.setInfoId(info.getId());
			animeInfoVO.setInfoUrl(info.getInfoUrl());

			infoVOs.add(animeInfoVO);
		}
		knowledgeHome.setKnowledgeInfos(infoVOs);
	}
	
	/**
	* <p>功能描述:处理过期的海报</p>
	* <p>参数：@param ret
	* <p>参数：@param overDatePoster</p>
	* <p>返回类型：void</p>
	 */
	private void dealPoster(KnowledgeHomePageVO ret,Map<Integer,List<PosterVO>> overDatePoster){
		//如果没有生效的海报位，显示过期的
		if(ret.getLargePoster() == null){
			List<PosterVO> volist = overDatePoster.get(EbPosterAppLocationEnum.APPRECOMMENDLARGE.getValue());
			if(volist != null && volist.size() > 0){
				ret.setLargePoster(volist.get(0));
			}
		}
		//1个二级海报位
		int secondCount = ret.getSeconedPosters() != null ?ret.getSeconedPosters().size() : 0;
		if(secondCount < 1){
			List<PosterVO> volist = overDatePoster.get(EbPosterAppLocationEnum.APPRECOMMEND2ED.getValue());
			List<PosterVO> seconedPosters = ret.getSeconedPosters() != null ? ret.getSeconedPosters():new ArrayList<PosterVO>();
			if (volist != null && volist.size() > 0) {
				seconedPosters.add(volist.get(0));
			}
			ret.setSeconedPosters(seconedPosters);
		}
		//处理三级海报
		int thirdCount = ret.getThirdPosters() != null ?ret.getThirdPosters().size() : 0;
		if(thirdCount < 1){
			List<PosterVO> volist = overDatePoster.get(EbPosterAppLocationEnum.APPRECOMMEND3RD.getValue());
			List<PosterVO> thirdPosters =  ret.getThirdPosters() != null ? ret.getThirdPosters():new ArrayList<PosterVO>();
			if (volist != null && volist.size() > 0) {
				thirdPosters.add(volist.get(0));
			}
			ret.setThirdPosters(thirdPosters);
		}
		
	}
	
	/**
	 * <p>
	 * 功能描述:知识海报
	 * </p>
	 * <p>
	 * 参数：@param posters
	 * <p>
	 * 参数：@param albumHome
	 * </p>
	 * <p>
	 * 返回类型：void
	 * </p>
	 */
	private void fillPosters(List<EbPoster> posters, KnowledgeHomePageVO knowledgeHome) {
		Map<Integer,List<PosterVO>> overDatePoster = new HashMap<Integer, List<PosterVO>>();
		for (EbPoster p : posters) {
			PosterVO vo = new PosterVO();
			vo.setId(p.getId());
			vo.setDescription(p.getDescription());
			//ipad与手机端的图片大小不一致
			if((MobileTypeEnum.valueOf(getContext().getHead()
					.getPlatform()) == MobileTypeEnum.ipad)){
//				vo.setImg(Util.getFullImageURL(p.getIpadimg()));
				vo.setImg(Util.getFullImageURLByVersion(p.getIpadimg(),
						getContext().getHead().getVersion(), getContext()
								.getHead().getPlatform()));
			}else{
//				vo.setImg(Util.getFullImageURL(p.getImg()));
				vo.setImg(Util.getFullImageURLByVersion(p.getImg(),
						getContext().getHead().getVersion(), getContext()
								.getHead().getPlatform()));
			}
			vo.setSortNum(NumericUtil.parseInt(p.getSortNum(), 10000));
			vo.setUrl(p.getUrl());
			vo.setPostertitle(p.getPostertitle());
			vo.setPostersubtitle(p.getPostersubtitle());
			//将无效的海报放到map里
			if(Util.checkDateValidate(p.getStartTime(), p.getEndTime()) != 1){
				if(overDatePoster.containsKey(p.getLocation())){
					overDatePoster.get(p.getLocation()).add(vo);
				}else{
					List<PosterVO> voList = new ArrayList<PosterVO>();
					voList.add(vo);
					overDatePoster.put(p.getLocation(), voList);
				}
				continue;
			}
			if (p.getLocation().intValue() == EbPosterAppLocationEnum.APPRECOMMENDLARGE.getValue()) {
				knowledgeHome.setLargePoster(vo);
			}
			if (p.getLocation().intValue() == EbPosterAppLocationEnum.APPRECOMMEND2ED.getValue()) {
				if (knowledgeHome.getSeconedPosters() == null) {
					List<PosterVO> seconedPosters = new ArrayList<PosterVO>();
					seconedPosters.add(vo);
					knowledgeHome.setSeconedPosters(seconedPosters);
				} else if (knowledgeHome.getSeconedPosters().size() <= 1) {// 2级海报只有1个
					knowledgeHome.getSeconedPosters().add(vo);
				} else if (MobileTypeEnum.valueOf(getContext().getHead()
						.getPlatform()) == MobileTypeEnum.ipad
						&& knowledgeHome.getSeconedPosters().size() <= 2) {//ipad有3个2级海报
					knowledgeHome.getSeconedPosters().add(vo);
				}
			}
			if (p.getLocation().intValue() == EbPosterLocationEnum.APPRECOMMEND3RD.getValue()) {
				if (knowledgeHome.getThirdPosters() == null) {
					List<PosterVO> thirdPosters = new ArrayList<PosterVO>();
					thirdPosters.add(vo);
					knowledgeHome.setThirdPosters(thirdPosters);
				} else if (knowledgeHome.getThirdPosters().size() < 2) {
					knowledgeHome.getThirdPosters().add(vo);
				} else if (MobileTypeEnum.valueOf(getContext().getHead()
						.getPlatform()) == MobileTypeEnum.ipad
						&& knowledgeHome.getThirdPosters().size() <= 3) {//ipad有3个2级海报
					knowledgeHome.getThirdPosters().add(vo);
				}
			}
		}
		dealPoster(knowledgeHome, overDatePoster);
	}
	
	/**
	* <p>功能描述:获取热播知识</p>
	* <p>参数：@return</p>
	* <p>返回类型：AlbumVO</p>
	 * @throws Exception 
	 */
	private AlbumVO getFirstHotAlbum(KnowledgeHomePageVO knowledgeHome) throws Exception{
		AlbumServiceV5_0 albumServ = SystemInitialization
				.getApplicationContext().getBean(AlbumServiceV5_0.class);
		HotKnowledgeVO[] hotKnow = knowledgeHome.getCategoryKnowledge();
		
		Album album = null;
		if(hotKnow != null && hotKnow.length > 0){
			int albumId = hotKnow[0].getKnowledgeList().get(0).getAlbumId();
			album = albumServ.getAlbum(albumId);
		}
		if(album == null){
			return null;
		}
		AlbumVO hotAlbum = buildAlbumVO(album);
//		Video video = albumServ.getTheVideoByAlbumId(hotAlbum.getId(), 1);
//		hotAlbum.setVideoId(video.getId());
		return hotAlbum;
	}
	
	/**
	 * <p>
	 * 功能描述:处理最近观看知识，若没有自动设置为热播知识的第一个
	 * </p>
	 * <p>
	 * 参数：@param animeHome
	 * <p>
	 * 参数：@throws Exception
	 * </p>
	 * <p>
	 * 返回类型：void
	 * </p>
	 */
	private void fillRecentKnowledge(KnowledgeHomePageVO knowledgeHome) throws Exception {
		// 未登录把热播的第一个作为默认推荐过去
		AlbumVO hotAlbum = getFirstHotAlbum(knowledgeHome);
		knowledgeHome.setPushAlbum(hotAlbum);
	}
	
	
	/**
	 * <p>
	 * 功能描述:构建专辑VO
	 * </p>
	 * <p>
	 * 参数：@param ablums
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：List<AlbumVO>
	 * </p>
	 */
	private List<AlbumVO> buildAlbumVO(List<Album> ablums) {
		if (ablums == null || ablums.size() <= 0) {
			return null;
		}
		List<AlbumVO> vos = new ArrayList<AlbumVO>();
		for (Album a : ablums) {
			AlbumVO vo = new AlbumVO();
			vo.setSnapshot(a.getCover());
			vo.setId(a.getId());
			vo.setName(a.getName());
			vo.setType(a.getType().getValue());
			vo.setVip(a.getVip());
			vo.setTotalCount(NumericUtil.parseInt(a.getTotalCount()));
			vo.setNowCount(NumericUtil.parseInt(a.getNowCount()));
			vos.add(vo);
		}
		return vos;
	}
	
	/**
	* <p>功能描述:构建视频VO</p>
	* <p>参数：@param ablum
	* <p>参数：@return</p>
	* <p>返回类型：AlbumVO</p>
	 */
	private AlbumVO buildAlbumVO(Album ablum) {
		if (ablum == null) {
			return null;
		}
		AlbumVO vo = new AlbumVO();
//		vo.setSnapshot(Util.getFullImageURL(ablum.getCover()));
		vo.setSnapshot(Util.getFullImageURLByVersion(ablum.getCover(),
				getContext().getHead().getVersion(), getContext().getHead()
						.getPlatform()));
		vo.setId(ablum.getId());
		vo.setName(ablum.getName());
		vo.setType(ablum.getType().getValue());
		vo.setVip(ablum.getVip());
		vo.setTotalCount(NumericUtil.parseInt(ablum.getTotalCount()));
		vo.setNowCount(NumericUtil.parseInt(ablum.getNowCount()));
		
		return vo;
	}
	
	/**
	 *知识分类
	 */
	class KnowledgeCategory{
		//知识分类id
		private Integer categoryId;
		//知识分类名称
		private String categoryName = null;
		//知识分类的图片
		private String imageSrc = null;

		public Integer getCategoryId() {
			return categoryId;
		}

		public void setCategoryId(Integer categoryId) {
			this.categoryId = categoryId;
		}

		public String getCategoryName() {
			return categoryName;
		}

		public void setCategoryName(String categoryName) {
			this.categoryName = categoryName;
		}

		public String getImageSrc() {
			return imageSrc;
		}

		public void setImageSrc(String imageSrc) {
			this.imageSrc = imageSrc;
		}
		
	}
	
	/**
	 *知识首页返回VO
	 */
	class KnowledgeHomePageVO{
		//知识首页的分类
		private List<AlbumCategoryVO> categorys;
		//顶端推荐
		private List<RecommendVO> recommends;
		//一级海报 1个
		private PosterVO largePoster;
		//最近观看 1个
		private AlbumVO pushAlbum;
		//二级海报 1个
		private List<PosterVO> seconedPosters;
		//三级海报 1个
		private List<PosterVO> thirdPosters;
		//分类知识
		private HotKnowledgeVO[] categoryKnowledge;
		//知识百科
		private List<AnimeInfoVO> knowledgeInfos;
		//知识热搜词
		private List<String> hotKeys;
		// 商品热搜词
		private List<String> productHotKeys;
		// 知识热搜词
		private List<String> knowledgeHotKeys;
		// 推荐热搜词
		private List<String> recommendHotKeys;
		// 视频热搜词
		private List<String> albumHotKeys;
		
		public List<String> getProductHotKeys() {
			return productHotKeys;
		}

		public void setProductHotKeys(List<String> productHotKeys) {
			this.productHotKeys = productHotKeys;
		}

		public List<String> getKnowledgeHotKeys() {
			return knowledgeHotKeys;
		}

		public void setKnowledgeHotKeys(List<String> knowledgeHotKeys) {
			this.knowledgeHotKeys = knowledgeHotKeys;
		}

		public List<String> getRecommendHotKeys() {
			return recommendHotKeys;
		}

		public void setRecommendHotKeys(List<String> recommendHotKeys) {
			this.recommendHotKeys = recommendHotKeys;
		}

		public List<String> getAlbumHotKeys() {
			return albumHotKeys;
		}

		public void setAlbumHotKeys(List<String> albumHotKeys) {
			this.albumHotKeys = albumHotKeys;
		}

		public List<String> getHotKeys() {
			return hotKeys;
		}

		public void setHotKeys(List<String> hotKeys) {
			this.hotKeys = hotKeys;
		}

		public List<AlbumCategoryVO> getCategorys() {
			return categorys;
		}

		public void setCategorys(List<AlbumCategoryVO> categorys) {
			this.categorys = categorys;
		}

		public List<AnimeInfoVO> getKnowledgeInfos() {
			return knowledgeInfos;
		}

		public void setKnowledgeInfos(List<AnimeInfoVO> knowledgeInfos) {
			this.knowledgeInfos = knowledgeInfos;
		}

		public HotKnowledgeVO[] getCategoryKnowledge() {
			return categoryKnowledge;
		}

		public void setCategoryKnowledge(HotKnowledgeVO[] categoryKnowledge) {
			this.categoryKnowledge = categoryKnowledge;
		}

		public List<RecommendVO> getRecommends() {
			return recommends;
		}

		public void setRecommends(List<RecommendVO> recommends) {
			this.recommends = recommends;
		}

		public PosterVO getLargePoster() {
			return largePoster;
		}

		public void setLargePoster(PosterVO largePoster) {
			this.largePoster = largePoster;
		}

		public AlbumVO getPushAlbum() {
			return pushAlbum;
		}

		public void setPushAlbum(AlbumVO pushAlbum) {
			this.pushAlbum = pushAlbum;
		}

		public List<PosterVO> getSeconedPosters() {
			return seconedPosters;
		}

		public void setSeconedPosters(List<PosterVO> seconedPosters) {
			this.seconedPosters = seconedPosters;
		}

		public List<PosterVO> getThirdPosters() {
			return thirdPosters;
		}

		public void setThirdPosters(List<PosterVO> thirdPosters) {
			this.thirdPosters = thirdPosters;
		}
		
	}
	
	class HotKnowledgeVO{
		//知识 id
		private Integer categoryId;
		//知识分类名称
		private String categoryName;
		//分类下的知识
		private List<KnowledgeVO> knowledgeList;
		
		private int sortNum;
		
		public int getSortNum() {
			return sortNum;
		}

		public void setSortNum(int sortNum) {
			this.sortNum = sortNum;
		}

		public Integer getCategoryId() {
			return categoryId;
		}

		public void setCategoryId(Integer categoryId) {
			this.categoryId = categoryId;
		}

		public String getCategoryName() {
			return categoryName;
		}

		public void setCategoryName(String categoryName) {
			this.categoryName = categoryName;
		}

		public List<KnowledgeVO> getKnowledgeList() {
			return knowledgeList;
		}

		public void setKnowledgeList(List<KnowledgeVO> knowledgeList) {
			this.knowledgeList = knowledgeList;
		}
		
	}
	
	class KnowledgeVO{
		//知识id
		private Integer knowledgeId;
		//图片
		private String img;
		//跳转地址
		private String redirect;
		//主标题
		private String title;
		//副标题
		private String subTitle;
		//视频id
		private Integer albumId;
		//分类id
		private Integer categoryId;
		
		private boolean vip;
		
		public boolean isVip() {
			return vip;
		}

		public void setVip(boolean vip) {
			this.vip = vip;
		}

		public Integer getKnowledgeId() {
			return knowledgeId;
		}

		public void setKnowledgeId(Integer knowledgeId) {
			this.knowledgeId = knowledgeId;
		}

		public String getImg() {
			return img;
		}

		public void setImg(String img) {
			this.img = img;
		}

		public String getRedirect() {
			return redirect;
		}

		public void setRedirect(String redirect) {
			this.redirect = redirect;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getSubTitle() {
			return subTitle;
		}

		public void setSubTitle(String subTitle) {
			this.subTitle = subTitle;
		}

		public Integer getAlbumId() {
			return albumId;
		}

		public void setAlbumId(Integer albumId) {
			this.albumId = albumId;
		}

		public Integer getCategoryId() {
			return categoryId;
		}

		public void setCategoryId(Integer categoryId) {
			this.categoryId = categoryId;
		}
		
	}
	
	/**
	 * 知识百科VO
	 */
	class AnimeInfoVO {
		// 资讯id
		private Integer infoId;
		// 资讯内容
		private String content;
		// 资讯URL
		private String infoUrl;

		public Integer getInfoId() {
			return infoId;
		}

		public void setInfoId(Integer infoId) {
			this.infoId = infoId;
		}

		public String getContent() {
			return content;
		}

		public void setContent(String content) {
			this.content = content;
		}

		public String getInfoUrl() {
			return infoUrl;
		}

		public void setInfoUrl(String infoUrl) {
			this.infoUrl = infoUrl;
		}
	}
	
	/**
	 *视频VO
	 */
	class AlbumInfoVO{
		private List<AlbumVO> albums = null;

		public List<AlbumVO> getAlbums() {
			return albums;
		}

		public void setAlbums(List<AlbumVO> albums) {
			this.albums = albums;
		}
		
	}
}
