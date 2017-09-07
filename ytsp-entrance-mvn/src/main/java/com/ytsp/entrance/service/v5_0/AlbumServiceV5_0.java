package com.ytsp.entrance.service.v5_0;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ytsp.common.util.StringUtil;
import com.ytsp.db.dao.AgeTagRelationDao;
import com.ytsp.db.dao.AlbumDao;
import com.ytsp.db.dao.CustomerCollectionDao;
import com.ytsp.db.dao.EbPosterDao;
import com.ytsp.db.dao.LeShiVideoDao;
import com.ytsp.db.dao.LogVideoDao;
import com.ytsp.db.dao.RecentPlayVideoRecordDao;
import com.ytsp.db.dao.VideoDao;
import com.ytsp.db.domain.AgeTagRelation;
import com.ytsp.db.domain.Album;
import com.ytsp.db.domain.EbPoster;
import com.ytsp.db.domain.EbProduct;
import com.ytsp.db.domain.LeShiVideo;
import com.ytsp.db.domain.RecentPlayVideoRecord;
import com.ytsp.db.domain.Tag;
import com.ytsp.db.domain.Video;
import com.ytsp.db.enums.AgeSelectEnum;
import com.ytsp.db.enums.AlbumCategoryTypeEnum;
import com.ytsp.db.enums.AlbumTypeEnum;
import com.ytsp.db.enums.EbPosterAppLocationEnum;
import com.ytsp.db.enums.EbProductValidStatusEnum;
import com.ytsp.db.enums.LeShiStatusEnum;
import com.ytsp.db.enums.SelectAppTypeConditionEnum;
import com.ytsp.db.enums.TagUseTypeEnum;
import com.ytsp.db.exception.SqlException;
import com.ytsp.db.vo.AlbumDetailVO;
import com.ytsp.db.vo.AlbumVO;
import com.ytsp.db.vo.EbProductVO;
import com.ytsp.db.vo.VideoVO;
import com.ytsp.entrance.system.IConstants;
import com.ytsp.entrance.system.SystemManager;
import com.ytsp.entrance.system.SystemParamInDB;
import com.ytsp.entrance.util.DateFormatter;
import com.ytsp.entrance.util.NumericUtil;
import com.ytsp.entrance.util.URLParse;
import com.ytsp.entrance.util.Util;

@Service("albumServiceV5_0")
@Transactional
public class AlbumServiceV5_0 {
	static final Logger logger = Logger.getLogger(AlbumServiceV5_0.class);

	@Resource(name = "albumDao")
	private AlbumDao albumDao;
	@Resource(name = "videoDao")
	private VideoDao videoDao;
	@Resource(name = "ebPosterDao")
	private EbPosterDao ebPosterDao;
	@Resource(name = "ageTagRelationDao")
	private AgeTagRelationDao ageTagRelationDao;
	@Resource(name = "logVideoDaoYTSPLOG")
	private LogVideoDao logVideoDaoYTSPLOG;
	@Resource(name = "leShiVideoDao")
	private LeShiVideoDao leShiVideoDao;
	
	@Resource(name="customerCollectionDao")
	private CustomerCollectionDao customerCollectionDao;
	
	@Resource(name="recentPlayVideoRecordDao")
	private RecentPlayVideoRecordDao recentPlayVideoRecordDao;
	
	public Album getAlbum(int albumId) throws Exception {
		return albumDao.findById(albumId);
	}
	
	/**
	* <p>功能描述:保存或更新最近播放记录</p>
	* <p>参数：@param record
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：void</p>
	 */
	public void saveOrUpdateRecentPlayVideo(int userId,int type,int albumId,int videoId,int duration) throws SqlException{
		if(userId <= 0){
			return;
		}
		RecentPlayVideoRecord rec = getRecentPlayVideoRecord(userId, type);
		if(rec == null){
			rec = buildRecentPlayVideoRecord(userId, type, albumId, videoId, duration);
		}else{
			rec.setAlbumId(albumId);
			rec.setVideoId(videoId);
			rec.setDuration(duration);
			rec.setCreateDate(new Date());
		}
		recentPlayVideoRecordDao.saveOrUpdate(rec);
	}
	
	/**
	* <p>功能描述:构建播放记录</p>
	* <p>参数：@param userId
	* <p>参数：@param type
	* <p>参数：@param albumId
	* <p>参数：@param videoId
	* <p>参数：@return</p>
	* <p>返回类型：RecentPlayVideoRecord</p>
	 */
	private RecentPlayVideoRecord buildRecentPlayVideoRecord(int userId,int type,int albumId,int videoId,int duration){
		RecentPlayVideoRecord record = new RecentPlayVideoRecord();
		record.setAlbumId(albumId);
		record.setUserId(userId);
		record.setVideoId(videoId);
		record.setSpecialType(AlbumCategoryTypeEnum.valueOf(type));
		record.setCreateDate(new Date());
		return record;
	}
	
	/**
	* <p>功能描述:获取最近播放的视频记录</p>
	* <p>参数：@param userId 用户id
	* <p>参数：@param type 视频类型：0为动漫视频1为知识视频
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：LogVideo</p>
	 */
	public RecentPlayVideoRecord getRecentPlayVideoRecord(int userId,int type) throws SqlException{
		RecentPlayVideoRecord logVideo = recentPlayVideoRecordDao.findOneByHql(" WHERE userId = ? and specialType = ? order by id desc", new Object[]{userId,AlbumCategoryTypeEnum.valueOf(type)});
		return logVideo;
	}
	
	/**
	* <p>功能描述:获取最近播放的视频记录</p>
	* <p>参数：@param userId 用户id
	* <p>参数：@param type 视频类型：0为动漫视频1为知识视频
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：LogVideo</p>
	 * @throws Exception 
	 */
	public AlbumVO queryRecentPlayVideoRecord(int userId,int type,String version,String platform) throws Exception{
		RecentPlayVideoRecord logVideo = null;
		if(type == 2){
			logVideo = recentPlayVideoRecordDao.findOneByHql(" WHERE userId = ? order by createDate desc", new Object[]{userId});
		}else{
			logVideo = recentPlayVideoRecordDao.findOneByHql(" WHERE userId = ? and specialType = ? order by createDate desc", new Object[]{userId,AlbumCategoryTypeEnum.valueOf(type)});
		}
		if(logVideo == null){
			return null;
		}
		int albumId = logVideo.getAlbumId();
		Album alb = getAlbum(albumId);
		if(alb == null){
			return null;
		}
		AlbumVO albumVo = new AlbumVO();
		albumVo.setName(alb.getName());
		albumVo.setId(alb.getId());
		albumVo.setNowCount(alb.getNowCount() == null? 0 : alb.getNowCount());
		albumVo.setSnapshot(Util.getFullImageURLByVersion(alb.getCover(), version, platform));
		albumVo.setTotalCount(alb.getTotalCount() == null? 0 : alb.getTotalCount());
		albumVo.setVideoId(logVideo.getVideoId());
		albumVo.setType(alb.getType() == null ? 0 : alb.getType().getValue());
		albumVo.setVip(alb.getVip());
		return albumVo;
	}
	
	/**
	* <p>功能描述:获取某些</p>
	* <p>参数：@param ids
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：JSONObject</p>
	 * @throws JSONException 
	 */
	public JSONArray getAlbumByIds(List<Integer> ids) throws SqlException, JSONException{
		JSONArray arr = new JSONArray();
		if(ids == null || ids.size() == 0){
			return arr;
		}
		StringBuffer sql = new StringBuffer();
		StringBuffer idsStr = new StringBuffer();
		for (int i = 0; i < ids.size(); i++) {
			if(i == ids.size() - 1){
				idsStr.append(ids.get(i));
			}else{
				idsStr.append(ids.get(i)).append(",");
			}
		}
		sql.append(" select * from ytsp_album where id in (").append(idsStr.toString()).append(")");
		List<Album> albums = albumDao.sqlFetch(sql.toString(), Album.class, 0, -1);
		
		if(albums != null && albums.size() > 0){
			for (Album alb : albums) {
				JSONObject obj = new JSONObject();
				obj.put("albumName", alb.getName());
				obj.put("albumId", alb.getId());
				obj.put("cover", Util.getFullImageURL(alb.getCover()));
				obj.put("totalCount", alb.getTotalCount());
				arr.put(obj);
			}
		}
		return arr;
	}
	
	/**
	* <p>功能描述:获取某个剧集下的某一集视频</p>
	* <p>参数：@param albumId
	* <p>参数：@param episode
	* <p>参数：@return</p>
	* <p>返回类型：List<Video></p>
	 * @throws Exception 
	 */
	public VideoVO getVideoByAlbumIdAndEpisode(Integer albumId, Integer episode,String terminalType,String terminalVersion) throws Exception {
		if(albumId == null || albumId <= 0){
			return null;
		}
		if(episode == null || episode <= 0){
			return null;
		}
		//获取某个剧集下的某一集视频SQL
		String SQL = "SELECT v.* FROM ytsp_video v,ytsp_album va WHERE v.album=va.id AND  v.review=1 AND v.status=1 AND va.id="
				+ albumId + " and v.episode =" + episode;
		List<Video> videos = videoDao.sqlFetch(SQL, Video.class, 0, -1);
		List<VideoVO> videoVO = fillVideoListVO(videos, terminalType,
				terminalVersion,albumId,false);
		if(videoVO != null && videoVO.size() > 0){
			return videoVO.get(0);
		}
		return null;
	}
	
	public List<Video> getVideosByAlbumId(Album album, int albumId) {
//		String SQL = "SELECT v.* FROM ytsp_video v,ytsp_video_album va WHERE v.id=va.vid AND va.aid="
//				+ albumId + " AND  v.review=1 AND v.status=1 ";
		//修改album与video为一对多关系
		String SQL = "SELECT v.* FROM ytsp_video v,ytsp_album va WHERE v.album=va.id AND va.id="
				+ albumId + " AND  v.review=1 AND v.status=1 ";
		// 如果是栏目的话，则按照集数倒序
		if (album != null
				&& (album.getType() == AlbumTypeEnum.PROGRAM || album.getType() == AlbumTypeEnum.PERIODICALS)) {
			//由于七巧板视频数据的顺序存在问题，所以这里做一下特殊处理
			if(album.getId() == 261 && "七巧板".equals(album.getName())){
				SQL += " ORDER BY v.free , v.name DESC";

			}else{
				SQL += " ORDER BY v.free , v.episode DESC";
			}
		} else {
			SQL += " ORDER BY v.episode ASC";
		}
		return videoDao.sqlFetch(SQL, Video.class, 0, -1);
	}
	
	public int hasCollention(int albumId,int userId){
		StringBuffer sb = new StringBuffer();
		try {
			sb.append("select count(1) from ytsp_customer_collection where ").
			append("userId="+userId+
					" and  albumId="+albumId);
			return customerCollectionDao.sqlCount(sb.toString());
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return 0;
		}
	}
	
	public AlbumDetailVO getAlbumDetailVO(int albumId, String terminalType,
			String terminalVersion,int userId) throws Exception {
		AlbumDetailVO albumDetailVO = new AlbumDetailVO();
		// TODO 获取专辑详情
		Album album = getAlbum(albumId);
		if(album == null){
			throw new RuntimeException();
		}
		//查看是否被收藏
		if(hasCollention(albumId,userId)>0){
			albumDetailVO.setCollectionStatus(true);
		}else{
			albumDetailVO.setCollectionStatus(false);
		}
		
		fillAlbumDetailVO(album, albumDetailVO,terminalVersion,terminalType);

		// TODO 获取专辑的剧集列表
		List<Video> videos = getVideosByAlbumId(album, albumId);
		albumDetailVO.setVideoVOs(fillVideoListVO(videos, terminalType,
				terminalVersion,albumId,false));

		// TODO 获取动漫周边,可为空,CMS编辑
		albumDetailVO.setAnimeProductVOs(fillAnimeProductsVO(album
				.getAnimeProducts(),terminalVersion,terminalType));

		// TODO 获取同类热播
		int albumCategory = album.getAlbumCategory() == null ? 0 : album
				.getAlbumCategory().getId();
		albumDetailVO.setAlbumVOs(getAlbumListVOByTag(albumId, albumCategory,Util.isIOSInReview(terminalType, terminalVersion),terminalType,terminalVersion));

		// 设置分享URL
		// 是否使用新的分享地址
		if (Util.isUseNewShareURL(terminalType, terminalVersion)) {
			albumDetailVO.setShareUrl("####");
		} else {
			albumDetailVO.setShareUrl(IConstants.SHAREURL);
		}
		//获取最近播放的视频
		if(userId > 0){
			albumDetailVO.setRecentPlayVideo(getRecentPlayVideo(userId, album, albumDetailVO.getVideoVOs()));
		}
		
		return albumDetailVO;
	}
	
	/**
	* <p>功能描述:获取用户该剧集下的播放记录</p>
	* <p>参数：@param userId
	* <p>参数：@return</p>
	* <p>返回类型：VideoVO</p>
	 * @throws SqlException 
	 */
	private VideoVO getRecentPlayVideo(int userId,Album album,List<VideoVO> videos) throws SqlException{
		RecentPlayVideoRecord recentPlayRec = getRecentPlayVideoByAlbumId(userId, album.getSpecialType().getValue(), album.getId());
		if(recentPlayRec == null){
			return null;
		}
		if(videos == null || videos.size() <= 0){
			return null;
		}
		VideoVO videVO = getVideoVOById(videos, recentPlayRec.getVideoId());
		//设置之前的播放时长
		videVO.setDuration(recentPlayRec.getDuration()==null? 0 : recentPlayRec.getDuration());
		return videVO;
	}
	
	/**
	* <p>功能描述:获取相同id的视频</p>
	* <p>参数：@param videos
	* <p>参数：@param videoId
	* <p>参数：@return</p>
	* <p>返回类型：VideoVO</p>
	 */
	private VideoVO getVideoVOById(List<VideoVO> videos,int videoId){
		for (int i = 0; i < videos.size(); i++) {
			VideoVO videoVO = videos.get(i);
			if(videoVO.getVideoId() == videoId){
				return videoVO;
			}
		}
		return videos.get(0);
	}
	
	/**
	* <p>功能描述:根据albumId获取用户最近播放的视频记录</p>
	* <p>参数：@param userId 用户id
	* <p>参数：@param type 视频类型：0为动漫视频1为知识视频
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：LogVideo</p>
	 */
	public RecentPlayVideoRecord getRecentPlayVideoByAlbumId(int userId,int type,int albumId) throws SqlException{
		RecentPlayVideoRecord recentPlayRec = recentPlayVideoRecordDao.findOneByHql(" WHERE userId = ? and specialType = ? and albumId = ? order by id desc", new Object[]{userId,AlbumCategoryTypeEnum.valueOf(type),albumId});
		return recentPlayRec;
	}
	
	private List<EbProductVO> fillAnimeProductsVO(Set<EbProduct> animeProducts,String version,String platform) {
		List<EbProductVO> animeProductVOs = new ArrayList<EbProductVO>();
		if (animeProducts != null && animeProducts.size() > 0) {
			for (EbProduct p : animeProducts) {
				if(p.getStatus() == EbProductValidStatusEnum.INVALID){
					continue;
				}
				EbProductVO vo = new EbProductVO();
				vo.setProductCode(p.getProductCode());
				vo.setProductName(p.getProductName());
				vo.setSvprice(p.getSvprice());
				vo.setVprice(p.getVprice());
				vo.setImgUrl(Util.getFullImageURLByVersion(p.getImgUrl(), version, platform));
				vo.setPrice(p.getPrice());
				vo.setStatus(p.getStatus().getValue());
				animeProductVOs.add(vo);
			}
		}
		return animeProductVOs;
	}

	/**
	 * 同类热播
	 * 
	 * @param albumId
	 *            专辑编号
	 * @param albumCategoryId
	 *            专辑分类ID
	 * @return
	 */
	public List<AlbumVO> getAlbumListVOByTag(int albumId, int albumCategoryId,boolean isIOSInReview,String platform,String version) {
		List<AlbumVO> albumVOs = new ArrayList<AlbumVO>();
		StringBuffer sb = new StringBuffer("SELECT * FROM ytsp_album a,");
		sb.append(
				"( SELECT DISTINCT albumId FROM ytsp_tag_album c,( SELECT DISTINCT tagId FROM ytsp_tag_album WHERE albumId=")
				.append(albumId)
				.append(") d  WHERE c.tagId=d.tagId AND albumId!=")
				.append(albumId).append(") b ");
		sb.append("WHERE a.id=b.albumId and a.review = 1 ");
		if(isIOSInReview){
			sb.append(" and a.review_hide = 0 ");
		}
		if (albumCategoryId > 0) {
			sb.append(" and a.albumCategory=").append(albumCategoryId);
		}
		sb.append(" ORDER BY a.play_count DESC ");

		List<Album> albums = albumDao.sqlFetch(sb.toString(), Album.class, 0,
				10);
//		String hostUrl = SystemManager.getInstance().getSystemConfig()
//				.getImgServerUrl();
		for (Album a : albums) {
			AlbumVO vo = new AlbumVO();
			if(platform.equals("ipad")){
				vo.setSnapshot(a.getWidthcover() == null ? "" : Util
						.getFullImageURLByVersion(a.getWidthcover(), version,
								platform));
			}else{
				vo.setSnapshot(a.getCover() == null ? "" : Util
						.getFullImageURLByVersion(a.getCover(), version,
								platform));
			}
			vo.setId(a.getId());
			vo.setName(a.getName());
			vo.setType(a.getType().getValue());
			vo.setVip(a.getVip());
			vo.setTotalCount(NumericUtil.parseInt(a.getTotalCount()));
			vo.setNowCount(NumericUtil.parseInt(a.getNowCount()));
			albumVOs.add(vo);
		}
		return albumVOs;
	}

	public List<VideoVO> fillVideoListVO(List<Video> videos,
			String terminalType, String terminalVersion,Integer albumId,boolean reviewFlag) throws Exception {
		SystemParamInDB spi = SystemManager.getInstance().getSystemParamInDB();
		String isInReview = spi.getValue(IConstants.IS_IN_REVIEW_KEY);
		String inReviewVersion = spi.getValue(IConstants.IN_REVIEW_VERSION);
		boolean _isInReview = false;
		String url_m3u8 = "http://videoa.ikan.cn/";
		String my_host = "http://114.112.50.220/";
		if (terminalType.equals("iphone")) {
			isInReview = spi.getValue(IConstants.IS_IN_REVIEW_KEY_IPHONE);
			inReviewVersion = spi.getValue(IConstants.IN_REVIEW_VERSION_IPHONE);
			if (StringUtil.isNotNullNotEmpty(isInReview)) {
				_isInReview = "true".equalsIgnoreCase(isInReview.trim())
						&& inReviewVersion.equals(terminalVersion) ? true
						: false;
			}
		}
		if (terminalType.equals("ipad")) {
			if (StringUtil.isNotNullNotEmpty(isInReview)) {
				_isInReview = "true".equalsIgnoreCase(isInReview.trim())
						&& inReviewVersion.equals(terminalVersion) ? true
						: false;
			}
		}
		List<VideoVO> videoVOs = new ArrayList<VideoVO>();
		if (videos != null && videos.size() > 0) {
//			String imgHost = SystemManager.getInstance().getSystemConfig()
//					.getImgServerUrl();
			List<LeShiVideo> lsVideos = leShiVideoDao.findAllByHql(" WHERE albumId = ? and status = ?", new Object[]{albumId,LeShiStatusEnum.VALIDVIDEO});
			Map<Integer,LeShiVideo> videoMap = buildLeShiVideoMap(lsVideos);
			for (Video v : videos) {
				//延时发布，若延时发布时间小于当前时间才显示视频
				if(!reviewFlag){//非审核视频模式
					if (v.getDelayFlag() != null && v.getDelayFlag()
							&& v.getDelayTime() != null
							&& v.getDelayTime().after(new Date())) {
						continue;
					}
				}
				VideoVO vo = new VideoVO();
				vo.setSnapshot(Util.getFullImageURLByVersion(v.getCover(), terminalVersion, terminalType));
				vo.setName(v.getName());
				vo.setVideoId(v.getId());
				vo.setTime(v.getTime());
				vo.setEpisode(NumericUtil.parseInt(v.getEpisode()));
				String saveDir = v.getSaveDir() == null ? "" : v.getSaveDir();
				saveDir = saveDir.endsWith("/") ? saveDir : (saveDir + "/");
				if(v.getType480p().booleanValue()){
//					vo.setV720(URLParse.makeURL(IConstants.VIDEOSAVEPATH480P + v.getNumber()
//							+ "-480P.mp4"));
					vo.setV720(URLParse.makeHttpsURLByVersion(IConstants.VIDEOSAVEPATH480P + v.getNumber()
							+ "-480P.mp4",terminalVersion,terminalType));
				}else{
//					vo.setV720(URLParse.makeURL(saveDir + v.getNumber()
//							+ "-720p.mp4"));
					vo.setV720(URLParse.makeHttpsURLByVersion(saveDir + v.getNumber()
							+ "-720p.mp4",terminalVersion,terminalType));
				}
				vo.setV720IOS(url_m3u8 + saveDir + v.getNumber()
						+ "-720p/index.m3u8");
				vo.setAudioIOS(url_m3u8 + saveDir + v.getNumber()
						+ "-audio/index.m3u8");
				if (_isInReview) {// 如果在审核中
					vo.setMainIOS(my_host + saveDir + v.getNumber() + ".m3u8");
				} else {
					vo.setMainIOS(vo.getV720IOS());
				}
				//ios在审核期间不使用乐视视频
				if(Util.isIOSInReview(terminalType, terminalVersion)){
					vo.setUseLSVideo(false);
				}else{
					//如果使用乐视视频源播放,返回乐视id,videoUnique
					vo.setUseLSVideo(v.getUseLeShi() == null ? false : v.getUseLeShi());
					if(v.getUseLeShi() != null && v.getUseLeShi()){
						if(videoMap.containsKey(v.getId())){
							LeShiVideo lsVideo = videoMap.get(v.getId());
							vo.setLsVideoId(lsVideo.getLsVideoId());
							vo.setVideoUnique(lsVideo.getVideoUnique());
						}
					}
				}
				
				videoVOs.add(vo);
			}
		}
		return videoVOs;
	}
	
	private Map<Integer,LeShiVideo> buildLeShiVideoMap(List<LeShiVideo> lsVideos){
		Map<Integer,LeShiVideo> ret = new HashMap<Integer,LeShiVideo>();
		if(lsVideos == null ||lsVideos.size() == 0){
			return ret;
		}
		for (LeShiVideo leShiVideo : lsVideos) {
			ret.put(leShiVideo.getVideoId(), leShiVideo);
		}
		return ret;
	}
	
	public void fillAlbumDetailVO(Album a, AlbumDetailVO albumDetailVO,String version,String platform)
			throws SqlException {
		AlbumVO albumVO = new AlbumVO();
//		String hostUrl = SystemManager.getInstance().getSystemConfig()
//				.getImgServerUrl();
		albumVO.setSnapshot(a.getCover() == null ? "" : Util.getFullImageURLByVersion(a.getCover(), version, platform));
		albumVO.setId(a.getId());
		albumVO.setName(a.getName());
		albumVO.setTotalCount(NumericUtil.parseInt(a.getTotalCount()));
		albumVO.setNowCount(NumericUtil.parseInt(a.getNowCount()));
		albumVO.setType(a.getType().getValue());
		albumVO.setVip(Boolean.TRUE.equals(a.getVip()) ? true : false);
		albumVO.setSpecialType(a.getSpecialType().getValue());
		albumDetailVO.setAlbumVO(albumVO);
		// TODO 年龄段
		Set<Tag> tags = a.getTags();
		StringBuffer area = new StringBuffer();
		for (Tag foo : tags) {
			if (foo.getTagGroup() != null
					&& foo.getTagGroup().getId() == IConstants.ANIMEAREAGROUPID) {
				if (area.length() == 0) {
					area.append(foo.getTagValue());
				} else {
					area.append("/" + foo.getTagValue());
				}
			}
		}
		// 计算年龄
		albumDetailVO.setAge(getAlbumAge(a.getId()));
		// TODO 类型
		albumDetailVO.setCategory(a.getAlbumCategory() == null ? "" : a
				.getAlbumCategory().getCname());
		// 设置动漫分类id
		albumDetailVO.setAlbumCategoryId(a.getAlbumCategory() == null?0:a.getAlbumCategory().getId());
		// TODO 地域
		albumDetailVO.setArea(area.toString());

		albumDetailVO.setSummary(a.getDescription());
		albumDetailVO.setYear(DateFormatter.date2YearString(a.getYears()));
	}

	/**
	 * <p>
	 * 功能描述:计算年龄
	 * </p>
	 * <p>
	 * 参数：@param albumId
	 * <p>
	 * 参数：@return
	 * <p>
	 * 参数：@throws SqlException
	 * </p>
	 * <p>
	 * 返回类型：String
	 * </p>
	 */
	private String getAlbumAge(int albumId) throws SqlException {
		String age = "";
		List<AgeTagRelation> ageRelations = ageTagRelationDao.findAllByHql(
				" WHERE useType = ? AND relationcode = ? ", new Object[] {
						TagUseTypeEnum.ALBUM, albumId });
		if (ageRelations == null || ageRelations.size() <= 0) {
			return age;
		}
		AgeTagRelation ageRel = ageRelations.get(0);
		int startAgeValue = ageRel.getStartValue().getValue();
		int endAgeValue = ageRel.getEndValue().getValue();
		StringBuffer ageSb = new StringBuffer();

		if (startAgeValue == 0 && endAgeValue == 0) {
			ageSb.append("0岁以上");
		} else if (startAgeValue == AgeSelectEnum.age_99y.getValue()
				&& endAgeValue == AgeSelectEnum.age_99y.getValue()) {
			ageSb.append("12岁以上");
		} else if (startAgeValue == endAgeValue) {
			ageSb.append(computeAge(startAgeValue, true));
		} else if (startAgeValue > endAgeValue) {
			if (startAgeValue != AgeSelectEnum.age_99y.getValue()) {
				ageSb.append(computeAge(endAgeValue, false)).append("~")
						.append(computeAge(startAgeValue, true));
			} else {
				ageSb.append(computeAge(startAgeValue, false));
			}
		} else if (startAgeValue == 0
				&& endAgeValue == AgeSelectEnum.age_99y.getValue()) {
			ageSb.append("适用所有年龄段");
		} else {
			if(endAgeValue == AgeSelectEnum.age_99y.getValue()){
				ageSb.append(computeAge(startAgeValue, true)).append("以上");
			}else{
				ageSb.append(computeAge(startAgeValue, false)).append("~")
				.append(computeAge(endAgeValue, true));
			}
		}

		return ageSb.toString();
	}

	/**
	 * <p>
	 * 功能描述:计算视频显示年龄
	 * </p>
	 * <p>
	 * 参数：@param ageValue 标签年龄值
	 * <p>
	 * 参数：@param isEnd 是否为结束年龄段
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：String
	 * </p>
	 */
	private String computeAge(int ageValue, boolean isEnd) {
		if (ageValue == 0) {
			return "0";
		}
		if (ageValue == AgeSelectEnum.age_99y.getValue()) {
			return "12岁以上";
		}
		if ((ageValue % 12) == 0) {
			if (isEnd) {
				return "" + ageValue / 12 + "岁";
			} else {
				return "" + ageValue / 12;
			}
		} else {
			StringBuffer age = new StringBuffer();
			int start = ageValue / 12;
			int rest = ageValue % 12;

			if (start == 0) {
				age.append(rest).append("个月");
			} else {
				age.append(start);
				if (rest != 0) {
					age.append("岁").append(rest).append("个月");
				}
				if (isEnd) {
					age.append("岁");
				}
			}
			return age.toString();
		}

	}

	/**
	 * <p>
	 * 功能描述:获取动漫首页海报
	 * </p>
	 * <p>
	 * 参数：@return
	 * <p>
	 * 参数：@throws SqlException
	 * </p>
	 * <p>
	 * 返回类型：List<EbPoster>
	 * </p>
	 */
	public List<EbPoster> getAnimePoster(SelectAppTypeConditionEnum type)
			throws SqlException {
		Date now = new Date();
		return ebPosterDao
				.findAllByHql(
						" where  location in(?,?,?) and startTime<? and appType =? order by sortNum ",
						new Object[] {
								EbPosterAppLocationEnum.APPRECOMMENDLARGE
										.getValue(),
								EbPosterAppLocationEnum.APPRECOMMEND2ED
										.getValue(),
								EbPosterAppLocationEnum.APPRECOMMEND3RD
										.getValue(), now, type });
	}

	/**
	 * <p>
	 * 功能描述:分页获取热播动漫
	 * </p>
	 * <p>
	 * 参数：@param page 页数
	 * <p>
	 * 参数：@param pageSize 每页显示个数
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：List<Album>
	 * </p>
	 */
	public List<Album> getHotAlbum(final int page, final int pageSize,final boolean isIOSInReview) {
		return (List<Album>) albumDao.getHibernateTemplate().execute(
				new HibernateCallback<List<Album>>() {
					@Override
					public List<Album> doInHibernate(Session session)
							throws HibernateException, SQLException {
						StringBuffer sql = new StringBuffer();
						sql.append(" select a.* from ytsp_album a ,ytsp_top_album t where ")
						   .append(" a.id=t.albumCode and a.review=1 and a.specialType = 0 ");
						//苹果审核中不显示某些视频
						if(isIOSInReview){
							sql.append(" and a.review_hide = 0 ");
						}
						sql.append(" ORDER BY t.sort limit ");
						sql.append(page * pageSize).append(",").append(pageSize);
						// 已上架的，review=1
						return session.createSQLQuery(sql.toString()).addEntity(Album.class).list();
					}
				});
	}
	
	/**
	 * 
	* 功能描述:由于第一页的第1个热播动漫放入到立即观看中，所以后续热播起始位置+1
	* 参数：@param page
	* 参数：@param pageSize
	* 参数：@param isIOSInReview
	* 参数：@return
	* 返回类型：List<Album>
	 */
	public List<Album> getHotAlbumAddOne(final int page, final int pageSize,final boolean isIOSInReview) {
		return (List<Album>) albumDao.getHibernateTemplate().execute(
				new HibernateCallback<List<Album>>() {
					@Override
					public List<Album> doInHibernate(Session session)
							throws HibernateException, SQLException {
						StringBuffer sql = new StringBuffer();
						sql.append(" select a.* from ytsp_album a ,ytsp_top_album t where ")
						   .append(" a.id=t.albumCode and a.review=1 and a.specialType = 0 ");
						//苹果审核中不显示某些视频
						if(isIOSInReview){
							sql.append(" and a.review_hide = 0 ");
						}
						sql.append(" ORDER BY t.sort limit ");
						sql.append((page * pageSize)+1).append(",").append(pageSize);
						// 已上架的，review=1
						return session.createSQLQuery(sql.toString()).addEntity(Album.class).list();
					}
				});
	}
	
	/**
	 * <p>
	 * 功能描述:根据albumId获取指定集数的视频
	 * </p>
	 * <p>
	 * 参数：@param albumId
	 * <p>
	 * 参数：@param episode
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：Video
	 * </p>
	 */
	public Video getTheVideoByAlbumId(int albumId, int episode) {
		String sql = "SELECT v.* FROM ytsp_video v,ytsp_album va WHERE v.album=va.id AND va.id="
				+ albumId
				+ " AND  v.review = 1 AND va.review = 1 AND v.status = 1 and v.episode = "
				+ episode;

		List<Video> videos = videoDao.sqlFetch(sql, Video.class, 0, 10);
		if (videos == null || videos.size() <= 0) {
			return null;
		}
		return videos.get(0);
	}

	/**
	 * <p>
	 * 功能描述:获取某分类下的视频
	 * </p>
	 * <p>
	 * 参数：@param categoryId
	 * <p>
	 * 参数：@param page
	 * <p>
	 * 参数：@param pageSize
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：List<Album>
	 * </p>
	 */
	public List<Album> getAlbumByCategoryId(int categoryId, int page,
			int pageSize, String platform) {
		StringBuffer sql = new StringBuffer();
		sql.append(" select * from ytsp_album a,ytsp_album_category c where a.albumCategory = c.id ");
		sql.append(" and a.review = 1 and c.id = ").append(categoryId);
		// if (MobileTypeEnum.iphone == MobileTypeEnum.valueOf(platform)) {
		// sql.append(" and a.ios_uplow = ").append(
		// UpLowStatusEnum.UPPER.getValue());
		// } else if (MobileTypeEnum.gphone == MobileTypeEnum.valueOf(platform))
		// {
		// sql.append(" and a.android_uplow = ").append(
		// UpLowStatusEnum.UPPER.getValue());
		// }
		sql.append(" order by a.play_count desc");
		return albumDao.sqlFetch(sql.toString(), Album.class, page * pageSize,
				pageSize);
	}

	/**
	 * <p>
	 * 功能描述:分页获取动漫周边
	 * </p>
	 * <p>
	 * 参数：@param platform 平台：iphone,android
	 * <p>
	 * 参数：@param page 页数
	 * <p>
	 * 参数：@param pageSize 每页显示个数
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：List<Album>
	 * </p>
	 */
	public List<Album> getAllAnimeProduct(String platform, int page,
			int pageSize) {
		StringBuffer sql = new StringBuffer();
		sql.append(" select ab.* from ytsp_album ab  ");
		sql.append(" where exists(select 1 from ytsp_anime_products ap where ab.id = ap.album_id) ");
		sql.append(" and ab.review = 1 ");
		// if (MobileTypeEnum.iphone == MobileTypeEnum.valueOf(platform)) {
		// sql.append(" and ab.ios_uplow = ").append(
		// UpLowStatusEnum.UPPER.getValue());
		// } else if (MobileTypeEnum.gphone == MobileTypeEnum.valueOf(platform))
		// {
		// sql.append(" and ab.android_uplow = ").append(
		// UpLowStatusEnum.UPPER.getValue());
		// }
		sql.append(" order by ab.play_count desc ");

		return albumDao.sqlFetch(sql.toString(), Album.class, page, pageSize);
	}
}
