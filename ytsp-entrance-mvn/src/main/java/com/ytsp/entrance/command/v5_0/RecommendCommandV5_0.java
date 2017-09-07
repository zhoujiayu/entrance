package com.ytsp.entrance.command.v5_0;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.ytsp.common.util.StringUtil;
import com.ytsp.db.domain.Album;
import com.ytsp.db.domain.EbPoster;
import com.ytsp.db.domain.EbProduct;
import com.ytsp.db.domain.Recommend;
import com.ytsp.db.enums.AlbumCategoryTypeEnum;
import com.ytsp.db.enums.EbPosterAppLocationEnum;
import com.ytsp.db.enums.MobileTypeEnum;
import com.ytsp.db.enums.RecommendTypeEnum;
import com.ytsp.db.enums.RecommendVersionEnum;
import com.ytsp.db.exception.SqlException;
import com.ytsp.db.vo.AlbumVO;
import com.ytsp.db.vo.EbProductVO;
import com.ytsp.db.vo.PosterVO;
import com.ytsp.db.vo.RecommendVO;
import com.ytsp.entrance.command.base.AbstractCommand;
import com.ytsp.entrance.command.base.CommandList;
import com.ytsp.entrance.command.base.ExecuteResult;
import com.ytsp.entrance.service.BaseConfigService;
import com.ytsp.entrance.service.VideoService;
import com.ytsp.entrance.service.v5_0.RecommendServiceV5_0;
import com.ytsp.entrance.system.IConstants;
import com.ytsp.entrance.system.SystemInitialization;
import com.ytsp.entrance.util.NumericUtil;
import com.ytsp.entrance.util.Util;

public class RecommendCommandV5_0 extends AbstractCommand {

	@Override
	public boolean canExecute() {
		int code = getContext().getHead().getCommandCode();
		return CommandList.CMD_RECOMMEND == code||
				CommandList.CMD_RECOMMEND_PRODUCTPAGE==code;
	}

	@Override
	public ExecuteResult execute() {
		try {
			int code = getContext().getHead().getCommandCode();
			if (CommandList.CMD_RECOMMEND == code) {
				return recommend();
			}
			if (CommandList.CMD_RECOMMEND_PRODUCTPAGE == code) {
				return productPage();
			}
		} catch (Exception e) {
			logger.error("execute() error," + " HeadInfo :"
					+ getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}
		return null;

	}

	private ExecuteResult productPage() throws JSONException {
		RecommendServiceV5_0 rs =  SystemInitialization.getApplicationContext()
				.getBean(RecommendServiceV5_0.class);
		JSONObject jsonObj = getContext().getBody().getBodyObject();
		int pageSize = jsonObj.optInt("pageSize",12);
		int page = jsonObj.getInt("page");
		List<EbProduct> ps = rs.getRecommendProduct(page,pageSize);
		Gson gson = new Gson();
		RecommendPageVO ret = new RecommendPageVO();
		ret.products=fillProduct(ps);
		JSONObject jo = new JSONObject(gson.toJson(ret));
		Util.addStatistics(getContext(), ret);
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取推荐商品分页成功！",jo, this);
	}

	private ExecuteResult recommend() throws Exception {
		//热门商品默认大小，由于ipad与手机大小不一致，所以定义该变量
		int defaultSize = 12;
		RecommendServiceV5_0 rs = SystemInitialization.getApplicationContext()
				.getBean(RecommendServiceV5_0.class);
		int version = 5;
		String headVersion = getContext().getHead().getVersion();
		if(StringUtil.isNotNullNotEmpty(headVersion) 
				&& Util.validateVersion(headVersion)){
			version = Integer.parseInt(headVersion.split("\\.")[0]);
		}
		List<Recommend> rds = rs.getRecommend(RecommendTypeEnum.IHEAD,RecommendVersionEnum.valueOf(version));
		RecommendPageVO ret = new RecommendPageVO();
		fillRecommend(rds, ret);
		List<EbPoster> posters = rs.getRecommendPoster();
		fillPosters(posters, ret);
		List<Album> as = rs.getRecommendAlbum(Util.isIOSInReview(getContext().getHead().getPlatform(), getContext().getHead().getVersion()));
		//判断是否为pad,pad首页默认显示24个
		if((MobileTypeEnum.valueOf(getContext().getHead()
				.getPlatform()) == MobileTypeEnum.ipad)){
			defaultSize = 24;
		}
		List<EbProduct> ps = rs.getRecommendProduct(0,defaultSize);
		ret.setProducts(fillProduct(ps));
		fillAlbum(as, ret);
		//设置推荐热搜词
		ret.setHotKeys(getHotSearchKeys(IConstants.CONFIG_RECOMMEND_SK));
		//获取所有搜索热词
		Map<String,List<String>> allHotKeys = getAllHotSearchKeys();
		List<String> recommendHotKeys = allHotKeys.get(IConstants.CONFIG_RECOMMEND_SK);
		//设置动漫热搜词
		//由于iphone5.0.4和android5.0.2版本还有用hotKyes,所以兼容以前版本，保留该数据。
		ret.setHotKeys(recommendHotKeys);
		ret.setRecommendHotKeys(recommendHotKeys);
		ret.setAlbumHotKeys(allHotKeys.get(IConstants.CONFIG_ALBUM_SK));
		ret.setProductHotKeys(allHotKeys.get(IConstants.CONFIG_PRODUCT_SK));
		ret.setKnowledgeHotKeys(allHotKeys.get(IConstants.CONFIG_KNOWLEDGE_SK));
		Gson gson = new Gson();
		JSONObject jo = new JSONObject(gson.toJson(ret));
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取推荐成功！", jo,
				this);
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
	
	private void fillAlbum(List<Album> as, RecommendPageVO ret) throws Exception {
		List<AlbumVO> vos = new ArrayList<AlbumVO>();
		for (Album a : as) {
			AlbumVO vo = new AlbumVO();
//			vo.setSnapshot(Util.getFullImageURL(a.getCover()));
			vo.setSnapshot(Util.getFullImageURLByVersion(a.getCover(),getContext()
					.getHead().getVersion(),getContext()
					.getHead().getPlatform()));
			vo.setId(a.getId());
			vo.setName(a.getName());
			vo.setType(a.getType().getValue());
			vo.setVip(a.getVip());
			vo.setTotalCount(NumericUtil.parseInt(a.getTotalCount()));
			vo.setNowCount(NumericUtil.parseInt(a.getNowCount()));
			vos.add(vo);
		}
		
		//暂时就把热播的第一个作为默认推荐过去
		ret.setPushAlbum(vos.get(0));
		ret.setAlbums(vos);
		vos.remove(0);
	}
	
	/**
	* <p>功能描述:设置最近观看地的动漫</p>
	* <p>参数：@param animeHome
	* <p>参数：@throws Exception</p>
	* <p>返回类型：void</p>
	 */
	@SuppressWarnings("unused")
	private void fillRecentAnime(RecommendPageVO ret) throws Exception {
		// 未登录把热播的第一个作为默认推荐过去
		if (getContext().getHead().getUid() == 0) {
			return;
		} else {
			VideoService videokServ = SystemInitialization
					.getApplicationContext().getBean(VideoService.class);
			//获取最近观看的视频
			Album album = videokServ.getUserLastPlayAlbum(getContext().getHead()
					.getUid(),  AlbumCategoryTypeEnum.VIDEO.getValue());
			if (album == null) {
				ret.setPushAlbum(ret.getAlbums().get(0));
			} else {
				AlbumVO vo = buildAlbumVO(album);
				ret.setPushAlbum(vo);
			}
		}
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
		vo.setSnapshot(Util.getFullImageURLByVersion(ablum.getCover(),getContext()
				.getHead().getVersion(),getContext()
				.getHead().getPlatform()));
		vo.setId(ablum.getId());
		vo.setName(ablum.getName());
		vo.setType(ablum.getType().getValue());
		vo.setVip(ablum.getVip());
		vo.setTotalCount(NumericUtil.parseInt(ablum.getTotalCount()));
		vo.setNowCount(NumericUtil.parseInt(ablum.getNowCount()));
		
		return vo;
	}
	
	private List<EbProductVO> fillProduct(List<EbProduct> ps) {
		List<EbProductVO> ls = new ArrayList<EbProductVO>();
		for (EbProduct p : ps) {
			EbProductVO vo = new EbProductVO();
//			vo.setImgUrl(Util.getFullImageURL(p.getImgUrl()));
			vo.setImgUrl(Util.getFullImageURLByVersion(p.getImgUrl(),getContext()
					.getHead().getVersion(),getContext()
					.getHead().getPlatform()));
			vo.setPrice(p.getPrice());
			vo.setProductCode(p.getProductCode());
			vo.setProductName(p.getProductName());
			vo.setStatus(p.getStatus().getValue());
			vo.setSvprice(p.getSvprice());
			vo.setVprice(p.getVprice());
			//计算商品下所有sku总库存
			Integer storageNum = Util.countProductStorage(p);
			vo.setStorageStatus(storageNum > 0? 1 : 0);
			ls.add(vo);
		}
		return ls;
	}
	
	private void fillPosters(List<EbPoster> posters, RecommendPageVO ret) {
		Map<Integer,List<PosterVO>> overDatePoster = new HashMap<Integer, List<PosterVO>>();
		for (EbPoster p : posters) {
			PosterVO vo = new PosterVO();
			vo.setId(p.getId());
			vo.setDescription(p.getDescription());
			//ipad与手机端的图片大小不一致
			if((MobileTypeEnum.valueOf(getContext().getHead()
					.getPlatform()) == MobileTypeEnum.ipad)){
//				vo.setImg(Util.getFullImageURL(p.getIpadimg()));
				vo.setImg(Util.getFullImageURLByVersion(p.getIpadimg(),getContext()
						.getHead().getVersion(),getContext()
						.getHead().getPlatform()));
			}else{
//				vo.setImg(Util.getFullImageURL(p.getImg()));
				vo.setImg(Util.getFullImageURLByVersion(p.getImg(),getContext()
						.getHead().getVersion(),getContext()
						.getHead().getPlatform()));
			}
			vo.setSortNum(NumericUtil.parseInt(p.getSortNum(),10000));
//			vo.setUrl(p.getUrl());
			vo.setUrl(Util.replaceURLByVersion(p.getUrl(),getContext()
					.getHead().getVersion(),getContext()
					.getHead().getPlatform()));
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
				//推荐首页1级海报图片大小与手机不一样，所以重新获取ipad的1级海报图
//				if ((MobileTypeEnum.valueOf(getContext().getHead().getPlatform()) == MobileTypeEnum.ipad)) {
//					vo.setImg(Util.getFullImageURL(p.getIpadimg()));
//				}
				
				ret.setLargePoster(vo);
			}
			if (p.getLocation().intValue() == EbPosterAppLocationEnum.APPBANNER.getValue() && ret.getBanner() == null) {
				ret.setBanner(vo);
				//增加ipad横幅，由于横幅有多个，以前的banner之前版本还在用，所以加了个多个横幅。
				List<PosterVO> banners = new ArrayList<PosterVO>();
				banners.add(vo);
				ret.setBanners(banners);
			} else if (p.getLocation().intValue() == EbPosterAppLocationEnum.APPBANNER
					.getValue()
					&& ret.getBanners() != null
					&& MobileTypeEnum.valueOf(getContext().getHead()
							.getPlatform()) == MobileTypeEnum.ipad) {
				ret.getBanners().add(vo);
			}
			if (p.getLocation().intValue() == EbPosterAppLocationEnum.APPRECOMMEND2ED.getValue()) {
				if (ret.getSeconedPosters() == null) {
					List<PosterVO> seconedPosters = new ArrayList<PosterVO>();
					seconedPosters.add(vo);
					ret.setSeconedPosters(seconedPosters);
				} else if (ret.getSeconedPosters().size() < 2) {// 2级海报只有两个
					ret.getSeconedPosters().add(vo);
				}
			}
			if (p.getLocation().intValue() == EbPosterAppLocationEnum.APPRECOMMEND3RD.getValue()) {
				if (ret.getThirdPosters() == null) {
					List<PosterVO> thirdPosters = new ArrayList<PosterVO>();
					thirdPosters.add(vo);
					ret.setThirdPosters(thirdPosters);
				} else if (ret.getThirdPosters().size() <= 3) {
					ret.getThirdPosters().add(vo);
				}
			}
			//处理推荐页的导航
			if (p.getLocation().intValue() == EbPosterAppLocationEnum.NAVIGATIONBAR.getValue()) {
				//如果ios在审核期间，将活动页的vip转换成
				if (Util.isIOSInReview(
						getContext().getHead().getPlatform(), getContext()
								.getHead().getVersion())) {
					if (vo.getUrl() != null
							&& vo.getUrl()
									.trim()
									.equals("https://entrance.ikan.cn/act/vipRechange/vipRecharge.html")) {
						// 设置为成为vip充值界面
						vo.setUrl(IConstants.VIPURL);
					}
				}
				if (ret.getNavigations() == null) {
					List<PosterVO> navigationPoster = new ArrayList<PosterVO>();
					navigationPoster.add(vo);
					ret.setNavigations(navigationPoster);
				} else if (ret.getNavigations().size() <= 4) {
					ret.getNavigations().add(vo);
				}
			}
		}
		dealPoster(ret, overDatePoster);
	}
	
	
	/**
	* <p>功能描述:处理过期的海报</p>
	* <p>参数：@param ret
	* <p>参数：@param overDatePoster</p>
	* <p>返回类型：void</p>
	 */
	private void dealPoster(RecommendPageVO ret,Map<Integer,List<PosterVO>> overDatePoster){
		//如果没有生效的海报位，显示过期的
		if(ret.getLargePoster() == null){
			List<PosterVO> volist = overDatePoster.get(EbPosterAppLocationEnum.APPRECOMMENDLARGE.getValue());
			if(volist != null && volist.size() > 0){
				ret.setLargePoster(volist.get(0));
			}
		}
		//2个二级海报位
		int secondCount = ret.getSeconedPosters() != null ?ret.getSeconedPosters().size() : 0;
		if(secondCount < 2){
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
		if(thirdCount < 3){
			List<PosterVO> volist = overDatePoster.get(EbPosterAppLocationEnum.APPRECOMMEND3RD.getValue());
			List<PosterVO> thirdPosters =  ret.getThirdPosters() != null ? ret.getThirdPosters():new ArrayList<PosterVO>();
			for (int i = 0; i < 3 - thirdCount; i++) {
				if(volist != null && i < volist.size()){
					thirdPosters.add(volist.get(i));
				}
			}
			ret.setThirdPosters(thirdPosters);
		}
		//ipad2个横幅
		if(MobileTypeEnum.valueOf(getContext().getHead()
				.getPlatform()) == MobileTypeEnum.ipad){
			int bannerCount = ret.getBanners() != null ?ret.getBanners().size() : 0;
			if(bannerCount < 2){
				List<PosterVO> volist = overDatePoster.get(EbPosterAppLocationEnum.APPBANNER.getValue());
				List<PosterVO> bannerPosters =  ret.getBanners() != null ? ret.getBanners():new ArrayList<PosterVO>();
				for (int i = 0; i < 2 - bannerCount; i++) {
					if(volist != null && i < volist.size()){
						bannerPosters.add(volist.get(i));
					}
				}
				ret.setBanners(bannerPosters);
			}
			
		}else{
			//处理横幅
			if(ret.getBanner() == null){
				List<PosterVO> volist = overDatePoster.get(EbPosterAppLocationEnum.APPBANNER.getValue());
				if(volist != null && volist.size() > 0){
					ret.setBanner(volist.get(0));
				}
			}
		}
		//导航处理
		int navigationCount = ret.getNavigations() != null ?ret.getNavigations().size() : 0;
		if(navigationCount < 4){
			List<PosterVO> volist = overDatePoster.get(EbPosterAppLocationEnum.NAVIGATIONBAR.getValue());
			List<PosterVO> navigationsPosters =  ret.getNavigations() != null ? ret.getNavigations():new ArrayList<PosterVO>();
			for (int i = 0; i < 4 - navigationCount; i++) {
				if(volist != null && i < volist.size()){
					navigationsPosters.add(volist.get(i));
				}
			}
			ret.setNavigations(navigationsPosters);
		}
		
	}
	
	private void fillRecommend(List<Recommend> rds, RecommendPageVO ret) {
		List<RecommendVO> recommends = new ArrayList<RecommendVO>();
		for (Recommend r : rds) {
			RecommendVO vo = new RecommendVO();
			vo.setId(r.getId());
			//ipad与手机的轮播图大小不一致
			if((MobileTypeEnum.valueOf(getContext().getHead()
					.getPlatform()) == MobileTypeEnum.ipad)){
//				vo.setImg(Util.getFullImageURL(r.getPadimg()));
				vo.setImg(Util.getFullImageURLByVersion(r.getPadimg(),getContext()
						.getHead().getVersion(),getContext()
						.getHead().getPlatform()));
			}else{
//				vo.setImg(Util.getFullImageURL(r.getImg()));
				vo.setImg(Util.getFullImageURLByVersion(r.getImg(),getContext()
						.getHead().getVersion(),getContext()
						.getHead().getPlatform()));
			}
			vo.setRedirect(r.getRedirect());
			vo.setSort(NumericUtil.parseInt(r.getSort(),10000));
			vo.setSummary(r.getSummary());
			recommends.add(vo);
		}
		ret.setRecommends(recommends);
	}

	class RecommendPageVO {
		private List<EbProductVO> products;
		private List<AlbumVO> albums;
		private List<RecommendVO> recommends;
		private PosterVO largePoster;
		private PosterVO banner;
		private AlbumVO pushAlbum;
		private List<PosterVO>  seconedPosters;
		private List<PosterVO>  thirdPosters;
		//导航栏
		private List<PosterVO> navigations;
		//推荐页热搜词
		private List<String> hotKeys;
		//ipad横幅
		private List<PosterVO> banners;
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
		public List<PosterVO> getBanners() {
			return banners;
		}
		public void setBanners(List<PosterVO> banners) {
			this.banners = banners;
		}
		public List<String> getHotKeys() {
			return hotKeys;
		}
		public void setHotKeys(List<String> hotKeys) {
			this.hotKeys = hotKeys;
		}
		public List<PosterVO> getNavigations() {
			return navigations;
		}
		public void setNavigations(List<PosterVO> navigations) {
			this.navigations = navigations;
		}
		public AlbumVO getPushAlbum() {
			return pushAlbum;
		}
		public void setPushAlbum(AlbumVO pushAlbum) {
			this.pushAlbum = pushAlbum;
		}
		public PosterVO getBanner() {
			return banner;
		}

		public void setBanner(PosterVO banner) {
			this.banner = banner;
		}

		public List<EbProductVO> getProducts() {
			return products;
		}

		public void setProducts(List<EbProductVO> products) {
			this.products = products;
		}

		public List<AlbumVO> getAlbums() {
			return albums;
		}

		public void setAlbums(List<AlbumVO> albums) {
			this.albums = albums;
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
}
