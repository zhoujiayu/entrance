package com.ytsp.entrance.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.ytsp.common.util.StringUtil;
import com.ytsp.db.dao.AlbumDao;
import com.ytsp.db.dao.LogVideoDao;
import com.ytsp.db.dao.MonthlyDao;
import com.ytsp.db.dao.VideoDao;
import com.ytsp.db.domain.Album;
import com.ytsp.db.domain.LogVideo;
import com.ytsp.db.domain.Video;
import com.ytsp.db.enums.AlbumCategoryTypeEnum;
import com.ytsp.db.enums.AlbumTypeEnum;
import com.ytsp.db.enums.PlayVideoEnum;
import com.ytsp.db.exception.SqlException;
import com.ytsp.db.vo.AlbumVO;
import com.ytsp.entrance.command.VersionCommand;
import com.ytsp.entrance.command.base.Command;
import com.ytsp.entrance.command.base.CommandList;
import com.ytsp.entrance.command.base.ExecuteResult;
import com.ytsp.entrance.system.IConstants;
import com.ytsp.entrance.system.SystemManager;
import com.ytsp.entrance.system.SystemParamInDB;
import com.ytsp.entrance.util.DateTimeFormatter;
import com.ytsp.entrance.util.IPSeeker;
import com.ytsp.entrance.util.LowPriorityExecutor;
import com.ytsp.entrance.util.URLParse;
import com.ytsp.entrance.util.Util;

/**
 * @author GENE
 * @description 视频服务
 */
public class VideoService {
	private static final Logger logger = Logger.getLogger(VideoService.class);

	private AlbumDao albumDao;
	private VideoDao videoDao;
	private MonthlyDao monthlyDao;
	private LogVideoDao logVideoDao;
	
	/**
	* <p>功能描述:获取最近播放的剧集</p>
	* <p>参数：@param userId 用户id
	* <p>参数：@param type 视频类型：0为动漫视频1为知识视频
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：AlbumVO</p>
	 */
	public AlbumVO getRecentPlayAlbum(int userId,int type) throws SqlException{
		if(userId == 0){
			return null;
		}
		List<LogVideo> logVideos = new ArrayList<LogVideo>();
		if(type == 2){
			logVideos = logVideoDao.findAllByHql(" WHERE customerId = ? order by time desc", new Object[]{userId});
		}else{
			logVideos = logVideoDao.findAllByHql(" WHERE customerId = ? and specialType = ? order by time desc", new Object[]{userId,AlbumCategoryTypeEnum.valueOf(type)});
		}
		if(logVideos == null || logVideos.size() <= 0){
			return null;
		}
		LogVideo logVideo = logVideos.get(0);
		Album alb = albumDao.findById(logVideo.getAlbumId());
		AlbumVO albumVo = new AlbumVO();
		albumVo.setName(alb.getName());
		albumVo.setId(alb.getId());
		albumVo.setNowCount(alb.getNowCount() == null? 0 : alb.getNowCount());
		albumVo.setSnapshot(Util.getFullImageURL(alb.getCover()));
		albumVo.setTotalCount(alb.getTotalCount() == null? 0 : alb.getTotalCount());
		albumVo.setVideoId(logVideo.getVideoId());
		albumVo.setType(alb.getType() == null ? 0 : alb.getType().getValue());
		albumVo.setVip(alb.getVip());
		return albumVo;
	}
	
	/**
	* <p>功能描述:获取最近播放的视频记录</p>
	* <p>参数：@param userId 用户id
	* <p>参数：@param type 视频类型：0为动漫视频1为知识视频
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：LogVideo</p>
	 */
	public LogVideo getRecentPlayVideo(int userId,int type) throws SqlException{
		List<LogVideo> logVideos = logVideoDao.findAllByHql(" WHERE customerId = ? and specialType = ? order by time desc", new Object[]{userId,AlbumCategoryTypeEnum.valueOf(type)});
		if(logVideos == null || logVideos.size() <= 0){
			return null;
		}
		LogVideo logVideo = logVideos.get(0);
		return logVideo;
	}
	
	public JSONArray getAlbumVideoArray(int albumid, int channelid, String searchName, int start, int limit) throws Exception {

	    JSONArray array = new JSONArray();
	    String SQL = "select v.* from ytsp_video v ";
	    if (albumid > 0) {
	      SQL = SQL + ",ytsp_video_album va where v.id=va.vid and va.aid=" + albumid + " and  v.review=1 AND v.status=1 ";
	    }
	    else if ((searchName != null) && (!searchName.trim().equals(""))) {
	      searchName = searchName.replaceAll("'", "");
	      SQL = SQL + ",ytsp_video_album va,ytsp_album a where v.id=va.vid and va.aid=a.id and a.name like '%" + searchName + "%' and v.review=1 AND v.status=1 ";
	    }
	    else
	    {
	      return array;
	    }Album album = this.albumDao.findById(Integer.valueOf(albumid));

		//如果是栏目的话，则按照集数倒序
		if(album != null && (album.getType() == AlbumTypeEnum.PROGRAM||
				album.getType() == AlbumTypeEnum.PERIODICALS))
		{
			SQL += " ORDER BY v.episode DESC";
		}
		else
		{
			SQL += " ORDER BY v.episode ASC";
		}

	    List<Video> videos = this.videoDao.sqlFetch(SQL, Video.class, start, limit);
		for (int i =0;i<videos.size();i++) {
			Video video = videos.get(i);
			JSONObject obj = new JSONObject();
			obj.put("vid", video.getId());
			obj.put("name", video.getName() == null ? "" : video.getName());
			obj.put("snapshot", SystemManager.getInstance().getSystemConfig().getImgServerUrl() + video.getCover());
			obj.put("summary", video.getDescription() == null ? "" : video.getDescription());
			obj.put("time", video.getTime());
			obj.put("starring", "");
			obj.put("director", "");
			obj.put("episode", video.getEpisode() == null ? 0 : video.getEpisode());
			obj.put("years", "");
			obj.put("submitTime", DateTimeFormatter.dateTime2String(video.getCreteTime()));
//			if(i==0){
//				obj.put("isFree", video.getFree());
//			}
			array.put(obj);
		}
		return array;
	}

	public int getAlbumVideoCount(int albumid, String searchName) throws Exception {
		String HQL = "select count(v.id) from ytsp_video v ";
	    if (albumid > 0) {
	      HQL = HQL + ",ytsp_video_album va where v.id=va.vid and va.aid=" + albumid + " and  v.review=1 AND v.status=1 ";
	    }
	    else if ((searchName != null) && (!searchName.trim().equals(""))) {
	      searchName = searchName.replaceAll("'", "");
	      HQL = HQL + ",ytsp_video_album va,ytsp_album a where v.id=va.vid and va.aid=a.id and a.name like '%" + searchName + "%' and v.review=1 AND v.status=1 ";
	    }
	    else
	      return 0;
	    return this.videoDao.sqlCount(HQL);
	}
	
	public ExecuteResult saveAndGetVideoInfo(
								final int videoid, int albumid,
								final String hardwareNumber, 
								boolean videoNoLogin, 
								Command Command,
								final String platform,
								final String version,
								String appDiv,
								final String ip) throws Exception {
		final Video video = videoDao.findById(videoid);
		if (video == null) {
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "视频不存在！", null, Command);
		} else {
			int _version = VersionCommand.convert2Num(version);
			if(_version>=400000){
				//返回链接
				final JSONObject json = new JSONObject();
				String saveDir = video.getSaveDir() == null ? "" : video.getSaveDir();
				saveDir = saveDir.endsWith("/") ? saveDir : (saveDir + "/");
				
				final String url_m3u8 = "http://videoa.ikan.cn/";
				String my_host = "http://114.112.50.220/";
				
//				json.put("v720", url_mp4 + saveDir + video.getNumber() + "-720p.mp4");
				json.put("v720", URLParse.makeURL( saveDir + video.getNumber() + "-720p.mp4"));
				json.put("v360",json.get("v720"));
				json.put("v480",json.get("v720"));
				
				json.put("v720-ios", url_m3u8 + saveDir + video.getNumber() + "-720p/index.m3u8");
				json.put("v360-ios",json.get("v720-ios"));
				json.put("v480-ios",json.get("v720-ios"));
				json.put("audio-ios", url_m3u8 + saveDir + video.getNumber() + "-audio/index.m3u8");
				json.put("index-ios", url_m3u8 + saveDir + video.getNumber() + ".m3u8");
				SystemParamInDB spi = SystemManager.getInstance().getSystemParamInDB();
				String isInReview = spi.getValue(IConstants.IS_IN_REVIEW_KEY);
				String inReviewPlatform = spi.getValue(IConstants.IN_REVIEW_PLATFORM);
				String inReviewVersion = spi.getValue(IConstants.IN_REVIEW_VERSION);
				if(platform.equals("iphone"))
				{
					 isInReview = spi.getValue(IConstants.IS_IN_REVIEW_KEY_IPHONE);
					 inReviewPlatform = spi.getValue(IConstants.IN_REVIEW_PLATFORM_IPHONE);
					 inReviewVersion = spi.getValue(IConstants.IN_REVIEW_VERSION_IPHONE);
				}
				
				boolean _isInReview = false;
				if(StringUtil.isNotNullNotEmpty(isInReview)){
					_isInReview = "true".equalsIgnoreCase(isInReview.trim()) ? true : false;
				}
				if(_isInReview)
				{
					//如果在审核中，由于可能会取音频文件，所以上我们自己的服务器取
					if(inReviewPlatform.equals(platform) && inReviewVersion.equals(version))
					{
						json.put("main-ios", my_host + saveDir + video.getNumber() + ".m3u8");
					}
					else
					{
						json.put("main-ios", json.get("v720-ios"));
					}
				}
				else
				{
					json.put("main-ios", json.get("v720-ios"));
				}
				String snapshot = "";
				try{
					final Album album = albumDao.findById(albumid);
					if(album != null){
						int count = 0;
						count = album.getPlayCount() == null ? 0 : album.getPlayCount();
						album.setPlayCount(count + 1);
						albumDao.update(album);
						snapshot = SystemManager.getInstance().getSystemConfig().getImgServerUrl() + album.getCover();
						
						//记录用户看过的视频
						final LogVideo log = new LogVideo();
						logVideoDao.save(log);
						json.put("logid", log.getId());
						LowPriorityExecutor.execLog(new Runnable() {
							@Override
							public void run() {
								log.setAlbumId(album.getId());
								log.setVideoId(videoid);
								log.setEpisode(video.getEpisode());
								log.setTerminalNumber(hardwareNumber);
								log.setTerminalType(platform);
								log.setTerminalVersion(version);
								log.setType(PlayVideoEnum.PLAY_ONLINE);
								log.setVideoServer(url_m3u8);
								log.setIp(ip);
								String[] a = IPSeeker.getAreaNameByIp(ip);
								log.setProvince(a[0]);
								log.setCity(a[1]);
								log.setTime(new Date());
								try {
									if("ipad".equals(platform) || "iphone".equals(platform) )
										log.setVideoUrl(json.getString("main-ios"));
									else
										log.setVideoUrl(json.getString("v720"));
									logVideoDao.saveOrUpdate(log);
								} catch (Exception e) {
									logger.error("", e);
								}
							}
						});
					}
				}catch(Throwable ex){
					logger.error("", ex);
				}
				json.put("snapshot", snapshot);//封面
				return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "播放影片成功！", json, Command);
			}else{
				String tips = "您需要升级到最新版本才能观看！";
				return new ExecuteResult(CommandList.RESPONSE_STATUS_NOT_VIP, tips, null, Command);
			}
		}
	}
	
	/**
	* <p>功能描述:通过id获取LogVideo</p>
	* <p>参数：@param logId
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：LogVideo</p>
	 */
	public LogVideo getLogVideoById(int logId) throws SqlException{
		return logVideoDao.findById(logId);
	}
	
	/**
	 * 更新视频的观看时长
	 * @param logId
	 * @param duration
	 * @throws Exception
	 */
	public void saveVideoDuration(final int logId,final int duration) throws Exception {
		LowPriorityExecutor.execLog(new Runnable() {
			
			@Override
			public void run() {
				try {
					LogVideo log = logVideoDao.findById(logId);
					if(log != null)
					{
						log.setUpdateTime(new Date());
						log.setDuration(duration);
						logVideoDao.update(log);
					}
				} catch (SqlException e) {
					e.printStackTrace();
				}
			}
		});
		
	}
	
	/**
	* <p>功能描述:获取用户最新观的视频记录</p>
	* <p>参数：@param userId 用户id
	* <p>参数：@param albumId 视频id
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：LogVideo</p>
	 */
	public Album getUserLastPlayAlbum(int userId,int type) throws SqlException{
		StringBuffer sql = new StringBuffer();
		sql.append(" select * from ytsp_album a where specialtype = "+type+" and review = 1 ");
		sql.append(" and EXISTS(select 1 from ytsp_log_video v where a.id = v.album_id and customer = "+userId+" ORDER BY time desc) ");
		List<Album> albums = albumDao.sqlFetch(sql.toString(), Album.class, 0, 1);
		if(albums == null || albums.size() <= 0){
			return null;
		}
		return albums.get(0);
	}
	
	public void saveVideo(Video video) throws Exception {
		videoDao.save(video);
	}

	public void saveOrUpdate(Video video) throws Exception {
		videoDao.saveOrUpdate(video);
	}

	public void updateVideo(Video video) throws Exception {
		videoDao.update(video);
	}

	public void deleteVideo(Video video) throws Exception {
		videoDao.delete(video);
	}

	public Video findVideoById(int videoid) throws Exception {
		return videoDao.findById(videoid);
	}

	public Video findVideoByNumber(String number) throws Exception {
		return videoDao.findOneByHql(" WHERE number=?", new Object[] { number });
	}

	public List<Video> getAllVideos() throws Exception {
		return videoDao.getAll();
	}

	public void deleteVideoById(int videoid) throws Exception {
		videoDao.deleteById(videoid);
	}

	public VideoDao getVideoDao() {
		return videoDao;
	}

	public void setVideoDao(VideoDao videoDao) {
		this.videoDao = videoDao;
	}

	public AlbumDao getAlbumDao() {
		return albumDao;
	}

	public void setAlbumDao(AlbumDao albumDao) {
		this.albumDao = albumDao;
	}

	public MonthlyDao getMonthlyDao() {
		return monthlyDao;
	}

	public void setMonthlyDao(MonthlyDao monthlyDao) {
		this.monthlyDao = monthlyDao;
	}

	public LogVideoDao getLogVideoDao() {
		return logVideoDao;
	}

	public void setLogVideoDao(LogVideoDao logVideoDao) {
		this.logVideoDao = logVideoDao;
	}

}
