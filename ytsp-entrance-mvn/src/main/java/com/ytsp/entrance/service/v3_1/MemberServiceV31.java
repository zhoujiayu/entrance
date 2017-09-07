package com.ytsp.entrance.service.v3_1;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import cn.dongman.util.UtilDate;

import com.ytsp.common.util.StringUtil;
import com.ytsp.db.dao.AdVideoDao;
import com.ytsp.db.dao.AlbumDao;
import com.ytsp.db.dao.CustomerDao;
import com.ytsp.db.dao.CustomerMemberDao;
import com.ytsp.db.dao.LeShiVideoDao;
import com.ytsp.db.dao.LogVideoDao;
import com.ytsp.db.dao.RecentPlayVideoRecordDao;
import com.ytsp.db.dao.VideoDao;
import com.ytsp.db.domain.AdVideo;
import com.ytsp.db.domain.Album;
import com.ytsp.db.domain.CustomerMember;
import com.ytsp.db.domain.LeShiVideo;
import com.ytsp.db.domain.LogVideo;
import com.ytsp.db.domain.RecentPlayVideoRecord;
import com.ytsp.db.domain.Video;
import com.ytsp.db.enums.AlbumCategoryTypeEnum;
import com.ytsp.db.enums.LeShiStatusEnum;
import com.ytsp.db.enums.MemberVideoStatusEnum;
import com.ytsp.db.enums.PlayVideoEnum;
import com.ytsp.db.enums.ValidStatusEnum;
import com.ytsp.db.exception.SqlException;
import com.ytsp.entrance.command.base.CommandContext;
import com.ytsp.entrance.system.IConstants;
import com.ytsp.entrance.system.SystemManager;
import com.ytsp.entrance.system.SystemParamInDB;
import com.ytsp.entrance.util.IPSeeker;
import com.ytsp.entrance.util.LowPriorityExecutor;
import com.ytsp.entrance.util.URLParse;
import com.ytsp.entrance.util.Util;

public class MemberServiceV31 {
//	private RechargeRecordCardDao rechargeRecordCardDao;
//	private RechargeRecordAppleDao rechargeRecordAppleDao;
//	private MemberCostDefineDao memberCostDefineDao;
	private CustomerDao customerDao;
	private CustomerMemberDao customerMemberDao;
//	private DmsCardDao dmsCardDao;
	private AlbumDao albumDao;
	private VideoDao videoDao;
	private LogVideoDao logVideoDao;
	@Resource(name = "leShiVideoDao")
	private LeShiVideoDao leShiVideoDao;
	@Resource(name="recentPlayVideoRecordDao")
	private RecentPlayVideoRecordDao recentPlayVideoRecordDao;
	public AdVideoDao getAdVideoDao() {
		return adVideoDao;
	}

	public void setAdVideoDao(AdVideoDao adVideoDao) {
		this.adVideoDao = adVideoDao;
	}

	private AdVideoDao adVideoDao;
	private static final Logger logger = Logger.getLogger(MemberServiceV31.class);
	

	public boolean isMember(int userId) throws SqlException  {
		boolean isMember = false;
		CustomerMember customerMember = customerMemberDao.findOneByHql(" WHERE customer.id = ? order by endTime desc", new Object[]{userId});
		if(customerMember != null)
		{
			if (customerMember.getValid()) {
				Date endTime = customerMember.getEndTime();
				if(UtilDate.isOverLiveTime(endTime) <= 0) {
					isMember = true;
				}
			}
		}
		return isMember;
	}
	
	/**
	 * 会员播放视频
	 * @param userId
	 * @param videoId
	 * @param albumId
	 * @param terminalType
	 * @param terminalVersion
	 * @param terminalNumber
	 * @param ip
	 * @return
	 * @throws Exception
	 */
	public JSONObject savePlayVideo(final int userId, final int videoId, int albumId,
			final String terminalType,final String terminalVersion,final String terminalNumber,final String ip,CommandContext context,final int playerType) 
					throws Exception{
		
		final JSONObject json = new JSONObject();
		String url_m3u8 = "http://videoa.ikan.cn/";

//		int _version = VersionCommand.convert2Num(terminalVersion);
//		if(_version<400000){
//			json.put("result", MemberVideoStatusEnum.NO_VIDEO.getValue()); //低版本不给看了
//			return json;
//		}
		//判断视频是否存在
		final Video video = videoDao.findById(videoId);
		if (video == null) {
			json.put("result", MemberVideoStatusEnum.NO_VIDEO.getValue()); //视频不存在
			return json;
		}

		final Album album = albumDao.findById(albumId);

		String my_host = "http://114.112.50.220/";//审核期间用
		
		String saveDir = video.getSaveDir() == null ? "" : video.getSaveDir();
		saveDir = saveDir.endsWith("/") ? saveDir : (saveDir + "/");
//		json.put("v720", URLParse.makeURL(saveDir + video.getNumber() + "-720p.mp4"));
		json.put(
				"v720",
				URLParse.makeHttpsURLByVersion(saveDir + video.getNumber()
						+ "-720p.mp4",  context.getHead().getVersion(), context.getHead().getPlatform()));
		//TODO 临时，针对特定会员的专门通道
//		if(userId==21){
//			json.put("v720", host_mp4+ saveDir + video.getNumber() + "-720p.mp4");
//		}
		json.put("v720-ios", url_m3u8 + saveDir + video.getNumber() + "-720p/index.m3u8");
		json.put("audio-ios", url_m3u8 + saveDir + video.getNumber() + "-audio/index.m3u8");
		json.put("index-ios", url_m3u8 + saveDir + video.getNumber() + ".m3u8");
		
		if(video.getType480p()){
//			json.put("v720", URLParse.makeURL(IConstants.VIDEOSAVEPATH480P + video.getNumber() + "-480P.mp4"));
			json.put(
					"v720",
					URLParse.makeHttpsURLByVersion(IConstants.VIDEOSAVEPATH480P + video.getNumber()
							+ "-480P.mp4",  context.getHead().getVersion(), context.getHead().getPlatform()));
		}
		
		SystemParamInDB spi = SystemManager.getInstance().getSystemParamInDB();
		String isInReview = spi.getValue(IConstants.IS_IN_REVIEW_KEY);
		String inReviewPlatform = spi.getValue(IConstants.IN_REVIEW_PLATFORM);
		String inReviewVersion = spi.getValue(IConstants.IN_REVIEW_VERSION);
		if(terminalType.equals("iphone"))
		{
			 isInReview = spi.getValue(IConstants.IS_IN_REVIEW_KEY_IPHONE);
			 inReviewPlatform = spi.getValue(IConstants.IN_REVIEW_PLATFORM_IPHONE);
			 inReviewVersion = spi.getValue(IConstants.IN_REVIEW_VERSION_IPHONE);
			 boolean _isInReview = false;
				if(StringUtil.isNotNullNotEmpty(isInReview)){
					_isInReview = "true".equalsIgnoreCase(isInReview.trim()) ? true : false;
				}
				if(_isInReview){
					//如果在审核中
					if(inReviewPlatform.equals(terminalType) && inReviewVersion.equals(terminalVersion))
						json.put("main-ios", my_host + saveDir + video.getNumber() + ".m3u8");
					else
						json.put("main-ios", json.get("v720-ios"));
				}else{
					json.put("main-ios", json.get("v720-ios"));
				}
		}
		if(terminalType.equals("ipad"))
		{
			 isInReview = spi.getValue(IConstants.IS_IN_REVIEW_KEY);
			 inReviewPlatform = spi.getValue(IConstants.IN_REVIEW_PLATFORM);
			 inReviewVersion = spi.getValue(IConstants.IN_REVIEW_VERSION);
			 boolean _isInReview = false;
				if(StringUtil.isNotNullNotEmpty(isInReview)){
					_isInReview = "true".equalsIgnoreCase(isInReview.trim()) ? true : false;
				}
				if(_isInReview){
					//如果在审核中
					if(inReviewPlatform.equals(terminalType) && inReviewVersion.equals(terminalVersion))
						json.put("main-ios", my_host + saveDir + video.getNumber() + ".m3u8");
					else
						json.put("main-ios", json.get("v720-ios"));
				}else{
					json.put("main-ios", json.get("v720-ios"));
				}
		}
		
		//ios审核期间不使用乐视视频
		if(Util.isIOSInReview(terminalType, terminalVersion)){
			json.put("isUseLSVideo",false);
		}else{
			json.put("isUseLSVideo",
					video.getUseLeShi() == null ? false : video.getUseLeShi());
			//如果使用乐视视频源播放,返回乐视id,videoUnique
			if (video.getUseLeShi() != null && video.getUseLeShi()) {
				LeShiVideo lsVideo = leShiVideoDao.findOneByHql(" WHERE videoId = ? and status = ?", new Object[]{video.getId(),LeShiStatusEnum.VALIDVIDEO});
				if(lsVideo != null){
					json.put("videoUnique", lsVideo.getVideoUnique());
					json.put("lsVideoId", lsVideo.getLsVideoId());
				}
			}
		}
		//记录用户看过的视频
		final LogVideo log = new LogVideo();
		logVideoDao.save(log);
		final String[] a = IPSeeker.getAreaNameByIp(ip);
		LowPriorityExecutor.execLog(new Runnable() {
			@Override
			public void run() {
				try {
					log.setAlbumId(album.getId());
					log.setVideoId(videoId);
					log.setEpisode(video.getEpisode());
					log.setTerminalNumber(terminalNumber);
					log.setTerminalType(terminalType);
					log.setTerminalVersion(terminalVersion);
					log.setType(PlayVideoEnum.PLAY_ONLINE);
					log.setIp(ip);
					log.setProvince(a[0]);
					log.setCity(a[1]);
					log.setTime(new Date());
					log.setCustomerId(userId);
					log.setDuration(0);
					//设置视频的类型
					log.setSpecialType(album.getSpecialType());
					//播放器类型
					log.setPlayerType(playerType);
					logVideoDao.saveOrUpdate(log);
					int count = album.getPlayCount() == null ? 0 : album.getPlayCount();
					album.setPlayCount(count + 1); //专辑播放次数加1
					albumDao.update(album);
				} catch (Exception e) {
					logger.error("Save video play log error", e);
				}
			}
		});
		//将实体类添加到统计里
		Util.addStatistics(context, log);
		
		JSONArray array = new JSONArray();
		//一旦出现异常，则不播放广告，不影响正常视频播放
		try {
//			if(terminalVersion.equals("4.4")&&
//					(terminalType.equals("iphone")||terminalType.equals("ipad"))){
//				json.put("ads", array);
//				json.put("logid", log.getId()); //返回播放时长用
//				return json;
//			}

			List<AdVideo> adList = adVideoDao.sqlFetch("select v.* from ytsp_ad_video v , " +
					"ytsp_ad_album a   where a.adid = v.id " +
//					" and v.terminal_type='"+terminalType +
					" and a.aid =" +album.getId()+
					" and v.valid=1 GROUP BY v.id", AdVideo.class, 0, -1);
			for (AdVideo adVideo : adList) {
				JSONObject jsonAd = new JSONObject();
				jsonAd.put("adId", adVideo.getId());
				jsonAd.put("adUrl", Util.getAdHttpsUrlByVersion(
						adVideo.getVideoUrl(), terminalVersion, terminalType));
				jsonAd.put("adRedirectUrl", adVideo.getRedirectUrl()==null||
						adVideo.getRedirectUrl().trim().equalsIgnoreCase("null")?"":adVideo.getRedirectUrl());
				array.put(jsonAd);
			}
		
		} catch (Exception e) {
			array = new JSONArray();
			e.printStackTrace();
		}
		json.put("ads", array);
		json.put("logid", log.getId()); //返回播放时长用
//		if(userId==21){
//			System.err.println("ming8469访问######正常,"+json.get("v720-ios")+
//					" ::MP4:: "+json.get("v720"));
//		}
		try {
			saveOrUpdateRecentPlayVideo(userId, album.getSpecialType().getValue(), albumId, videoId, 0);
		} catch (Exception e) {
			logger.error("保存最近播放记录失败", e);
		}
		return json;
	}
	
	/**
	* <p>功能描述:保存或更新最近播放记录</p>
	* <p>参数：@param record
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：void</p>
	 */
	public void saveOrUpdateRecentPlayVideo(int userId,int type,int albumId,int videoId,int duration) throws SqlException{
		if(userId == 0){
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
		record.setVideoId(videoId);
		record.setUserId(userId);
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
	

	public JSONArray getAdVideos(String platform, String ip) throws Exception {
		JSONArray array = new JSONArray();
		List<AdVideo> adList = adVideoDao.findAllByHql("where valid=? ", new Object[]{ValidStatusEnum.VALID});
//		List<AdVideo> adList = adVideoDao.findAllByHql("where   id=39 ", new Object[]{});
		
		for (AdVideo adVideo : adList) {
			JSONObject json = new JSONObject();
			json.put("adId", adVideo.getId());
			json.put("adUrl", adVideo.getVideoUrl());
			array.put(json);
		}
		return array;
	}
	
	public CustomerDao getCustomerDao() {
		return customerDao;
	}

	public void setCustomerDao(CustomerDao customerDao) {
		this.customerDao = customerDao;
	}

	public CustomerMemberDao getCustomerMemberDao() {
		return customerMemberDao;
	}

	public void setCustomerMemberDao(CustomerMemberDao customerMemberDao) {
		this.customerMemberDao = customerMemberDao;
	}
	
	public AlbumDao getAlbumDao() {
		return albumDao;
	}

	public void setAlbumDao(AlbumDao albumDao) {
		this.albumDao = albumDao;
	}

	public VideoDao getVideoDao() {
		return videoDao;
	}

	public void setVideoDao(VideoDao videoDao) {
		this.videoDao = videoDao;
	}

	public LogVideoDao getLogVideoDao() {
		return logVideoDao;
	}

	public void setLogVideoDao(LogVideoDao logVideoDao) {
		this.logVideoDao = logVideoDao;
	}

}
