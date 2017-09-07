package com.ytsp.entrance.command.v5_0;

import java.util.ArrayList;
import java.util.Date;
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
import com.ytsp.db.domain.EbTrack;
import com.ytsp.db.domain.Recommend;
import com.ytsp.db.enums.AlbumCategoryTypeEnum;
import com.ytsp.db.enums.AnimeInfoTypeEnum;
import com.ytsp.db.enums.EbPosterAppLocationEnum;
import com.ytsp.db.enums.EbPosterLinkUrlEnum;
import com.ytsp.db.enums.EbTrackTypeEnum;
import com.ytsp.db.enums.MobileTypeEnum;
import com.ytsp.db.enums.RecommendTypeEnum;
import com.ytsp.db.enums.RecommendVersionEnum;
import com.ytsp.db.enums.SelectAppTypeConditionEnum;
import com.ytsp.db.exception.SqlException;
import com.ytsp.db.vo.AlbumCategoryVO;
import com.ytsp.db.vo.AlbumDetailVO;
import com.ytsp.db.vo.AlbumVO;
import com.ytsp.db.vo.PosterVO;
import com.ytsp.db.vo.RecommendVO;
import com.ytsp.entrance.command.base.AbstractCommand;
import com.ytsp.entrance.command.base.CommandList;
import com.ytsp.entrance.command.base.ExecuteResult;
import com.ytsp.entrance.service.BaseConfigService;
import com.ytsp.entrance.service.VideoService;
import com.ytsp.entrance.service.v5_0.AlbumServiceV5_0;
import com.ytsp.entrance.service.v5_0.AnimeInfoService;
import com.ytsp.entrance.service.v5_0.EbCatagoryService;
import com.ytsp.entrance.service.v5_0.RecommendServiceV5_0;
import com.ytsp.entrance.service.v5_0.TrackService;
import com.ytsp.entrance.system.IConstants;
import com.ytsp.entrance.system.SystemInitialization;
import com.ytsp.entrance.util.DateFormatter;
import com.ytsp.entrance.util.ImagePropertyUtil;
import com.ytsp.entrance.util.NumericUtil;
import com.ytsp.entrance.util.Util;

public class AlbumCommandV5_0 extends AbstractCommand {
	private static final Integer ANIMEINFOSIZE = 8;

	private static final Integer HOTANIMESIZE = 12;

	@Override
	public boolean canExecute() {
		int code = getContext().getHead().getCommandCode();
		return code == CommandList.CMD_ALBUM_DETAIL_V5
				|| code == CommandList.CMD_ALBUM_HOMEPAGE
				|| code == CommandList.CMD_ALBUM_PAGE;
	}

	@Override
	public ExecuteResult execute() {
		int code = getContext().getHead().getCommandCode();
		if (code == CommandList.CMD_ALBUM_DETAIL_V5) {
			return albumDetail();
		} else if (code == CommandList.CMD_ALBUM_HOMEPAGE) {
			return animeHomePage();
		} else if (code == CommandList.CMD_ALBUM_PAGE) {
			return getHotAnimeByPage();
		}
		return null;
	}
	
	/**
	 * 入参：page
	 * <p>
	 * 功能描述:分页获取热门动漫，由于热播动漫的首个移除放入到立即观看里，所以热播动漫的分页起始位置+1
	 * </p>
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：ExecuteResult
	 * </p>
	 */
	private ExecuteResult getHotAnimeByPage() {
		JSONObject body = getContext().getBody().getBodyObject();
		try {
			int page = 0;
			int pageSize = HOTANIMESIZE;
			if (!body.isNull("page")) {
				page = body.getInt("page");
			}
			if (!body.isNull("pageSize")) {
				pageSize = body.getInt("pageSize");
			}
			AlbumServiceV5_0 albumServ = SystemInitialization
					.getApplicationContext().getBean(AlbumServiceV5_0.class);
			List<Album> as = albumServ.getHotAlbumAddOne(page, pageSize
					,Util.isIOSInReview(getContext().getHead().getPlatform()
							, getContext().getHead().getVersion()));
			List<AlbumVO> albumVOs = buildAlbumVO(as);
			HotAnimeVO infoVO = new HotAnimeVO();
			infoVO.setHotAlbums(albumVOs);
			Gson gson = new Gson();
			JSONObject result = new JSONObject(gson.toJson(infoVO));
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
					"分页获取热播动漫成功", result, this);
		} catch (Exception e) {
			logger.error("getHotAnimeByPage() error," + " HeadInfo :"
					+ getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}
	}

	
	/**
	* <p>功能描述:保存足迹</p>
	* <p>参数：@param userId
	* <p>参数：@param trackType 足迹类型
	* <p>参数：@param albumDetailVO
	* <p>参数：@throws Exception</p>
	* <p>返回类型：void</p>
	 */
	private void saveAlbumTack(int userId,int trackType,AlbumDetailVO albumDetailVO) throws Exception{
		TrackService trackService = SystemInitialization
				.getApplicationContext().getBean(TrackService.class);
		EbTrack track = trackService.getTrackIdAndType(userId, trackType,
				DateFormatter.date2String(new Date()), albumDetailVO.getAlbumVO().getId());
		// 若已有浏览足迹将相应的足迹浏览次数+1，否则保存足迹
		if (track != null) {
			EbTrack trackBak = track._getCopy();
			track.setStatus(0);
			track.setUpdateTime(new Date());
			trackService.updateTrack(track);
			trackBak.setViewCount(trackBak.getViewCount() + 1);
			trackBak.setUpdateTime(new Date());
			//保存新的
			trackService.saveTrack(trackBak);
		} else {
			trackService.saveTrack(getTrack(albumDetailVO,trackType));
		}
	}
	
	/**
	* <p>功能描述:构建足迹VO</p>
	* <p>参数：@param albumDetailVO 视频详情
	* <p>参数：@param trackType 视频类型
	* <p>参数：@return
	* <p>参数：@throws Exception</p>
	* <p>返回类型：EbTrack</p>
	 */
	private EbTrack getTrack(AlbumDetailVO albumDetailVO,int trackType) throws Exception {
		EbTrack track = new EbTrack();
		AlbumVO album = albumDetailVO.getAlbumVO();
		track.setUserId(getContext().getHead().getUid());
		track.setTrackType(EbTrackTypeEnum.valueOf(trackType));
		track.setUpdateTime(new Date());
		track.setViewCount(1);
		track.setStatus(1);
		track.setCreateTime(DateFormatter.date2String(new Date()));
		track.setAlbumId(album.getId());
		track.setAlbumCount(album.getTotalCount());
		track.setNowCount(NumericUtil.parseInt(album.getNowCount()));
		String imageServerHost = ImagePropertyUtil.getPropertiesValue("custImageHost").trim();
		String httpsCustImageHost = ImagePropertyUtil.getPropertiesValue("httpsCustImageHost").trim();
		track.setImageSrc(album.getSnapshot().replaceAll(imageServerHost, "").replaceAll(httpsCustImageHost, ""));
		track.setTrackName(album.getName());
		track.setAge(albumDetailVO.getAge());
		track.setTypeName(albumDetailVO.getCategory());
		track.setVip(album.getVip());
		if(albumDetailVO.getVideoVOs() != null && albumDetailVO.getVideoVOs().size() > 0){
			track.setVideoId(albumDetailVO.getVideoVOs().get(0).getVideoId());
		}
		return track;
	}
	
	
	/**
	 * <p>
	 * 功能描述:动漫首页
	 * </p>
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：ExecuteResult
	 * </p>
	 */
	private ExecuteResult animeHomePage() {
		int page = 0;
		int pageSize = 13;
		int version = 5;
		String headVersion = getContext().getHead().getVersion();
		if(StringUtil.isNotNullNotEmpty(headVersion) 
				&& Util.validateVersion(headVersion)){
			version = Integer.parseInt(headVersion.split("\\.")[0]);
		}
		try {
			// 动漫首页VO
			AnimeHomePageVO animeHome = new AnimeHomePageVO();
			//获取动漫分类
			animeHome.setCategorys(getAlbumCategoryVOs(1,AlbumCategoryTypeEnum.VIDEO.getValue()));
			RecommendServiceV5_0 rs = SystemInitialization
					.getApplicationContext()
					.getBean(RecommendServiceV5_0.class);
			List<Recommend> rds = rs.getRecommend(RecommendTypeEnum.ICARTOON,RecommendVersionEnum.valueOf(version));
			AlbumServiceV5_0 albumServ = SystemInitialization
					.getApplicationContext().getBean(AlbumServiceV5_0.class);
			// 处理动漫推荐页
			fillRecommend(rds, animeHome);
			// 处理动漫资讯
			fillAnimeInfo(animeHome);
			List<EbPoster> posters = albumServ.getAnimePoster(SelectAppTypeConditionEnum.ANIMAL);
			// 处理动漫首页海报
			fillPosters(posters, animeHome);
			//判断是否为pad,pad首页默认显示36个
			if ((MobileTypeEnum.valueOf(getContext().getHead().getPlatform()) == MobileTypeEnum.ipad)) {
				pageSize = 37;
			}
			List<Album> as = albumServ.getHotAlbum(page, pageSize,Util.isIOSInReview(getContext().getHead().getPlatform(), getContext().getHead().getVersion()));
			// 处理热播动漫
			fillAlbum(as, animeHome);
			// 处理最近观看动漫，若没有自动设置为热播动漫的第一个
			fillRecentAnime(animeHome);
			//获取所有搜索热词
			Map<String,List<String>> allHotKeys = getAllHotSearchKeys();
			List<String> albumHotKeys = allHotKeys.get(IConstants.CONFIG_ALBUM_SK);
			//设置动漫热搜词
			//由于iphone5.0.4和android5.0.2版本还有用hotKyes,所以兼容以前版本，保留该数据。
			animeHome.setHotKeys(albumHotKeys);
			animeHome.setAlbumHotKeys(albumHotKeys);
			animeHome.setProductHotKeys(allHotKeys.get(IConstants.CONFIG_PRODUCT_SK));
			animeHome.setKnowledgeHotKeys(allHotKeys.get(IConstants.CONFIG_KNOWLEDGE_SK));
			animeHome.setRecommendHotKeys(allHotKeys.get(IConstants.CONFIG_RECOMMEND_SK));
			Gson gson = new Gson();
			JSONObject result = new JSONObject(gson.toJson(animeHome));
			Util.addStatistics(getContext(), animeHome);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
					"获取动漫首页成功", result, this);
		} catch (Exception e) {
			logger.error("animeHomePage() error," + " HeadInfo :"
					+ getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}
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
	 * <p>
	 * 功能描述:处理最近观看动漫，若没有自动设置为热播动漫的第一个
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
	private void fillRecentAnime(AnimeHomePageVO animeHome) throws Exception {
		// 未登录把热播的第一个作为默认推荐过去
		animeHome.setPushAlbum(animeHome.getHotAlbums().get(0));
		animeHome.getHotAlbums().remove(0);
	}

	
	/**
	* <p>功能描述:构建视频VO</p>
	* <p>参数：@param ablum
	* <p>参数：@return</p>
	* <p>返回类型：AlbumVO</p>
	 */
	private AlbumVO buildAlbumVO(Album ablum,String platform,String version) {
		if (ablum == null) {
			return null;
		}
		AlbumVO vo = new AlbumVO();
		vo.setSnapshot(Util.getFullImageURLByVersion(ablum.getCover(),version,platform));
		vo.setId(ablum.getId());
		vo.setName(ablum.getName());
		vo.setType(ablum.getType().getValue());
		vo.setVip(ablum.getVip());
		vo.setTotalCount(NumericUtil.parseInt(ablum.getTotalCount()));
		vo.setNowCount(NumericUtil.parseInt(ablum.getNowCount()));
		
		return vo;
	}
	
	/**
	 * <p>
	 * 功能描述:动漫资讯
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
	private void fillAnimeInfo(AnimeHomePageVO animeHome) throws SqlException {
		AnimeInfoService animeServ = SystemInitialization
				.getApplicationContext().getBean(AnimeInfoService.class);
		List<AnimeInfo> animeInfos = animeServ
				.getAnimeInfoByPage(ANIMEINFOSIZE,AnimeInfoTypeEnum.ANIME);
		List<AnimeInfoVO> infoVOs = new ArrayList<AnimeInfoVO>();
		for (AnimeInfo info : animeInfos) {
			AnimeInfoVO animeInfoVO = new AnimeInfoVO();
			animeInfoVO.setContent(info.getContent());
			animeInfoVO.setInfoId(info.getId());
			animeInfoVO.setInfoUrl(info.getInfoUrl());

			infoVOs.add(animeInfoVO);
		}
		animeHome.setAnimeInfos(infoVOs);
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
			return new ArrayList<AlbumVO>();
		}
		List<AlbumVO> vos = new ArrayList<AlbumVO>();
		for (Album a : ablums) {
			AlbumVO vo = new AlbumVO();
			vo.setSnapshot(Util.getFullImageURLByVersion(a.getCover(),
					getContext().getHead().getVersion(), getContext().getHead()
							.getPlatform()));
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
	 * <p>
	 * 功能描述:
	 * </p>
	 * <p>
	 * 参数：@param as
	 * <p>
	 * 参数：@param ret
	 * </p>
	 * <p>
	 * 返回类型：void
	 * </p>
	 * 
	 * @throws SqlException
	 */
	private void fillAlbum(List<Album> as, AnimeHomePageVO ret)
			throws SqlException {
		List<AlbumVO> vos = new ArrayList<AlbumVO>();
		for (Album a : as) {
			AlbumVO vo = new AlbumVO();
			vo.setSnapshot(Util.getFullImageURLByVersion(a.getCover(),
					getContext().getHead().getVersion(), getContext().getHead()
							.getPlatform()));
			vo.setId(a.getId());
			vo.setName(a.getName());
			vo.setType(a.getType().getValue());
			vo.setVip(a.getVip());
			vo.setTotalCount(NumericUtil.parseInt(a.getTotalCount()));
			vo.setNowCount(NumericUtil.parseInt(a.getNowCount()));
			vos.add(vo);
		}

		ret.setHotAlbums(vos);
	}

	/**
	* <p>功能描述:处理过期的海报</p>
	* <p>参数：@param ret
	* <p>参数：@param overDatePoster</p>
	* <p>返回类型：void</p>
	 */
	private void dealPoster(AnimeHomePageVO ret,Map<Integer,List<PosterVO>> overDatePoster){
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
			for (int i = 0; i < 2 - secondCount; i++) {
				if(volist != null && i < volist.size()){
					seconedPosters.add(volist.get(i));
				}
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
	 * 功能描述:动漫海报
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
	private void fillPosters(List<EbPoster> posters, AnimeHomePageVO animeHome) {
		Map<Integer,List<PosterVO>> overDatePoster = new HashMap<Integer, List<PosterVO>>();
		for (EbPoster p : posters) {
			PosterVO vo = new PosterVO();
			vo.setId(p.getId());
			vo.setDescription(p.getDescription());
			//ipad与手机端的图片大小不一致
			if((MobileTypeEnum.valueOf(getContext().getHead()
					.getPlatform()) == MobileTypeEnum.ipad)){
				vo.setImg(Util.getFullImageURLByVersion(p.getIpadimg(),
						getContext().getHead().getVersion(), getContext()
								.getHead().getPlatform()));
			}else{
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
				animeHome.setLargePoster(vo);
			}
			if (p.getLocation().intValue() == EbPosterAppLocationEnum.APPRECOMMEND2ED.getValue()) {
				if (animeHome.getSeconedPosters() == null) {
					List<PosterVO> seconedPosters = new ArrayList<PosterVO>();
					seconedPosters.add(vo);
					animeHome.setSeconedPosters(seconedPosters);
				} else if (animeHome.getSeconedPosters().size() <= 1) {// 2级海报只有1个
					animeHome.getSeconedPosters().add(vo);
				} else if (MobileTypeEnum.valueOf(getContext().getHead()
						.getPlatform()) == MobileTypeEnum.ipad
						&& animeHome.getSeconedPosters().size() <= 2) {//ipad有2个2级海报
					animeHome.getSeconedPosters().add(vo);
				}
			}
			if (p.getLocation().intValue() == EbPosterAppLocationEnum.APPRECOMMEND3RD.getValue()) {
				if (animeHome.getThirdPosters() == null) {
					List<PosterVO> thirdPosters = new ArrayList<PosterVO>();
					thirdPosters.add(vo);
					animeHome.setThirdPosters(thirdPosters);
				} else if (animeHome.getThirdPosters().size() < 2) {
					animeHome.getThirdPosters().add(vo);
				} else if (MobileTypeEnum.valueOf(getContext().getHead()
						.getPlatform()) == MobileTypeEnum.ipad
						&& animeHome.getThirdPosters().size() <= 3) {//ipad有3个3级海报
					vo.setImg(Util.getFullImageURLByVersion(p.getIpadimg(),
							getContext().getHead().getVersion(), getContext()
									.getHead().getPlatform()));
					animeHome.getThirdPosters().add(vo);
				}
			}
		}
		dealPoster(animeHome, overDatePoster);
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
	private void fillRecommend(List<Recommend> rds, AnimeHomePageVO animeHome) {
		List<RecommendVO> recommends = new ArrayList<RecommendVO>();
		for (Recommend r : rds) {
			RecommendVO vo = new RecommendVO();
			vo.setId(r.getId());
			//ipad与手机的轮播图大小不一致
			if((MobileTypeEnum.valueOf(getContext().getHead()
					.getPlatform()) == MobileTypeEnum.ipad)){
				vo.setImg(Util.getFullImageURLByVersion(r.getPadimg(),
						getContext().getHead().getVersion(), getContext()
								.getHead().getPlatform()));
			}else{
				vo.setImg(Util.getFullImageURLByVersion(r.getImg(),
						getContext().getHead().getVersion(), getContext()
						.getHead().getPlatform()));
			}
			vo.setRedirect(r.getRedirect());
			vo.setSort(NumericUtil.parseInt(r.getSort(), 10000));
			vo.setSummary(r.getSummary());
			recommends.add(vo);
		}
		animeHome.setRecommends(recommends);
	}

	private ExecuteResult albumDetail() {
		try {
			JSONObject jsonObj = getContext().getBody().getBodyObject();
			if (jsonObj.isNull("albumId") || jsonObj.optInt("albumId") == 0) {
				return new ExecuteResult(
						CommandList.RESPONSE_STATUS_BODY_JSON_ERROR, "请求体错误",
						null, this);
			}
			int albumId = jsonObj.optInt("albumId");
			String terminalType = getContext().getHead().getPlatform();
			String terminalVersion = getContext().getHead().getVersion();
			JSONObject obj = new JSONObject();
			AlbumServiceV5_0 as = SystemInitialization.getApplicationContext()
					.getBean(AlbumServiceV5_0.class);
			VideoService vs = SystemInitialization.getApplicationContext()
					.getBean(VideoService.class);
			// TODO 获取专辑详情
			AlbumDetailVO albumDetailVO = as.getAlbumDetailVO(albumId,
					terminalType, terminalVersion,getContext().getHead().getUid());
			if (Util.isUseNewShareURL(getContext().getHead().getPlatform(),
					getContext().getHead().getVersion())) {
				String shareUrl = Util.getShareURL(EbPosterLinkUrlEnum.ALBUM, 0,"" + albumId);
				obj.put("albumDetail", new Gson().toJson(albumDetailVO)
						.toString().replaceAll("####", shareUrl));
			}else{
				obj.put("albumDetail", new Gson().toJson(albumDetailVO));
			}
			
			//若登录记入足迹
			if (isLogin()) {
				int trackType = albumDetailVO.getAlbumVO().getSpecialType();
				if(trackType == 0){
					trackType = EbTrackTypeEnum.CARTOON.getValue();
				}else{
					trackType = EbTrackTypeEnum.KNOWLEDGE.getValue();
				}
				saveAlbumTack(getContext().getHead().getUid(), trackType,albumDetailVO);
			}
			Util.addStatistics(getContext(), albumDetailVO);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
					"获取专辑信息成功", obj, this);
		} catch (Exception e) {
			logger.error("albumDetail() error," + " HeadInfo :"
					+ getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}
	}
	
	/**
	* <p>功能描述:是否登录</p>
	* <p>参数：@return</p>
	* <p>返回类型：boolean</p>
	 */
	private boolean isLogin(){
		if(getSessionCustomer() == null){
			return false;
		}else if(getSessionCustomer().getCustomer() == null){
			return false;
		}else if(getContext().getHead().getUid() == 0){
			return false;
		}
		return true;
	}
	
	/**
	* <p>功能描述:获取某一级某种类型的视频分类</p>
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：List<AlbumCategoryVO></p>
	 */
	private List<AlbumCategoryVO> getAlbumCategoryVOs(int level,int type) throws SqlException{
		EbCatagoryService categoryServ = SystemInitialization.getApplicationContext().getBean(EbCatagoryService.class);
		List<AlbumCategory> categoryVOs = categoryServ.getAnimeCategory(type);
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
			return null;
		}
		String version = getContext().getHead().getVersion();
		List<AlbumCategoryVO> voList = new ArrayList<AlbumCategoryVO>();
		for (AlbumCategory albCate : categoryVOs) {
			AlbumCategoryVO vo = new AlbumCategoryVO();
			vo.setAlbumCategoryId(albCate.getId());
			vo.setCategoryName(albCate.getCname());
			//解决5.0.1版和之后版本对图片url的host处理不兼容的问题
			if(version.equals("5.0.1") && MobileTypeEnum.valueOf(getContext().getHead()
					.getPlatform()) == MobileTypeEnum.iphone)
				vo.setImageSrc(albCate.getImageSrc());
			else
				//5.0.2版本是不带host的
				vo.setImageSrc(Util.getFullImageURLByVersion(albCate.getImageSrc(), version, getContext().getHead().getPlatform()));
			voList.add(vo);
		}
		return voList;
	}
	
	/**
	 * 动漫资讯VO
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

	class HotAnimeVO {
		private List<AlbumVO> hotAlbums;

		public List<AlbumVO> getHotAlbums() {
			return hotAlbums;
		}

		public void setHotAlbums(List<AlbumVO> hotAlbums) {
			this.hotAlbums = hotAlbums;
		}

	}

	/**
	 *
	 */
	class AnimeHomePageVO {
		// 动漫推荐
		private List<RecommendVO> recommends;
		// 一级海报
		private PosterVO largePoster;
		// 二级海报
		private List<PosterVO> seconedPosters;
		// 三级海报
		private List<PosterVO> thirdPosters;
		// 最近播放
		private AlbumVO pushAlbum;
		// 热门动漫
		private List<AlbumVO> hotAlbums;
		// 动漫资讯
		private List<AnimeInfoVO> animeInfos;
		//动漫分类
		private List<AlbumCategoryVO> categorys;
		//动漫热搜词
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

		public List<AnimeInfoVO> getAnimeInfos() {
			return animeInfos;
		}

		public void setAnimeInfos(List<AnimeInfoVO> animeInfos) {
			this.animeInfos = animeInfos;
		}

		public List<RecommendVO> getRecommends() {
			return recommends;
		}

		public void setRecommends(List<RecommendVO> recommends) {
			this.recommends = recommends;
		}

		public List<AlbumVO> getHotAlbums() {
			return hotAlbums;
		}

		public void setHotAlbums(List<AlbumVO> hotAlbums) {
			this.hotAlbums = hotAlbums;
		}

		public PosterVO getLargePoster() {
			return largePoster;
		}

		public void setLargePoster(PosterVO largePoster) {
			this.largePoster = largePoster;
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

		public AlbumVO getPushAlbum() {
			return pushAlbum;
		}

		public void setPushAlbum(AlbumVO pushAlbum) {
			this.pushAlbum = pushAlbum;
		}

	}

}
