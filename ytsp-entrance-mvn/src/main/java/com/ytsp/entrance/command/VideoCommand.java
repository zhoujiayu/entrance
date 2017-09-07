package com.ytsp.entrance.command;

import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.ytsp.common.util.StringUtil;
import com.ytsp.db.domain.LogVideo;
import com.ytsp.db.enums.MobileTypeEnum;
import com.ytsp.db.vo.AlbumVO;
import com.ytsp.entrance.command.base.AbstractCommand;
import com.ytsp.entrance.command.base.CommandList;
import com.ytsp.entrance.command.base.ExecuteResult;
import com.ytsp.entrance.service.VideoService;
import com.ytsp.entrance.service.v5_0.AlbumServiceV5_0;
import com.ytsp.entrance.system.SystemInitialization;
import com.ytsp.entrance.util.Util;

/**
 * @author GENE
 * @description 播放视屏
 * 
 */
public class VideoCommand extends AbstractCommand {

	@Override
	public boolean canExecute() {
		int code = getContext().getHead().getCommandCode();
		return 
//				CommandList.CMD_VIDEO_PLAY == code || 
				CommandList.CMD_VIDEO_LIST == code || 
				CommandList.CMD_VIDEO_COUNT == code || 
				CommandList.CMD_VIDEO_PLAY_TIME == code 
				||code == CommandList.CMD_VIDEO_QUERY_RECENT_PLAY;
	}

	@Override
	public ExecuteResult execute() {
		try {
			int code = getContext().getHead().getCommandCode();
			if (CommandList.CMD_VIDEO_PLAY == code) {
				return playVideo();

			} else if (CommandList.CMD_VIDEO_LIST == code) {
				return listVideo();

			} else if (CommandList.CMD_VIDEO_COUNT == code) {
				return countVideo();

			} else if (CommandList.CMD_VIDEO_PLAY_TIME == code) {
				return saveVideoDuration();

			}else if (CommandList.CMD_VIDEO_QUERY_RECENT_PLAY == code) {
				return queryRecentPlayVideo();

			}
		} catch (Exception e) {
			logger.error("execute() error," +
					" HeadInfo :"+getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}

		return null;
	}
	
	/**
	* <p>功能描述:获取最近观看的视频</p>
	* <p>参数：@return
	* <p>参数：@throws Exception</p>
	* <p>返回类型：ExecuteResult</p>
	 */
	private ExecuteResult queryRecentPlayVideo() throws Exception {

		JSONObject reqBody = getContext().getBody().getBodyObject();
		JSONObject result = new JSONObject();
		if(reqBody.isNull("type"))
		{
			return new ExecuteResult(CommandList.RESPONSE_STATUS_BODY_JSON_ERROR, "请求体错误", null, this);
		}
		int userId = getContext().getHead().getUid();
		int type =  reqBody.getInt("type");
//		VideoService vs = SystemInitialization.getApplicationContext().getBean(VideoService.class);
//		AlbumVO albumVO =  vs.getRecentPlayAlbum(userId, type);
		AlbumServiceV5_0 albumServ = SystemInitialization.getApplicationContext().getBean(AlbumServiceV5_0.class);
		AlbumVO albumVO = albumServ.queryRecentPlayVideoRecord(userId, type,
				getContext().getHead().getVersion(), getContext().getHead()
						.getPlatform());
		Gson gson = new Gson();
		result.put("recentPlayVideo", gson.toJson(albumVO));
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取用户最近观看视频成功", result, this);
	}
	
	private ExecuteResult playVideo() throws Exception {
		//add by gene 2011.11.25 can play video no login while system setup.
//		SystemParamInDB spi = SystemManager.getInstance().getSystemParamInDB();
//		String videoNoLogin = spi.getValue(IConstants.VIDEO_NO_LOGIN);
//		boolean _videoNoLogin = false;
//		if(StringUtil.isNotNullNotEmpty(videoNoLogin)){
//			_videoNoLogin = "true".equalsIgnoreCase(videoNoLogin.trim()) ? true : false;
//		}
		
//		SessionCustomer sc = getSessionCustomer();
//		if(!_videoNoLogin){
//			if (sc == null || sc.getCustomer() == null) {
//				return getNoPermissionExecuteResult();
//			}
//		}

//		Node node = BalanceManager.getInstance().getSelector().findFree(null);
//		if (node != null) {
//			String url = node.getDatas().getProperty("service");

			JSONObject jsonObj = getContext().getBody().getBodyObject();

			String platform = getContext().getHead().getPlatform();
			String version = getContext().getHead().getVersion();
			String appDiv = getContext().getHead().getAppDiv();
			String ip = getContext().getHead().getIp();

			if(platform.equals("gpadtv")&&
					!"alibaba".equals(getContext().getHead().getOtherInfo().trim())){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_NOPERMISSION,
						"不支持电视播放！", null, this);
			}
			//某些错误的调用，导致vid没有值
			if(jsonObj.isNull("vid") || jsonObj.isNull("aid"))
			{
				return new ExecuteResult(CommandList.RESPONSE_STATUS_BODY_JSON_ERROR, "请求体错误", null, this);
			}
			
			
			int videoid = jsonObj.getInt("vid");
			int albumid = jsonObj.getInt("aid");
			String hardwareNumber = getContext().getHead().getUniqueId();
			VideoService vd = SystemInitialization.getApplicationContext().getBean(VideoService.class);
			
			return vd.saveAndGetVideoInfo(videoid, albumid, hardwareNumber, true, this,platform,version,appDiv,ip);
			
//		} else {
//			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "镜像服务器忙！", null, this);
//		}
	}

	private ExecuteResult listVideo() throws Exception {
//		SessionCustomer sc = getSessionCustomer();
//		if (sc == null || sc.getCustomer() == null) {
//			return getNoPermissionExecuteResult();
//		}

		JSONObject jsonObj = getContext().getBody().getBodyObject();
		int aid = jsonObj.optInt("aid");
		int cid = jsonObj.optInt("cid");
		String searchName = jsonObj.optString("searchName");
		int start = jsonObj.optInt("start");
		int limit = jsonObj.optInt("limit");

		VideoService vs = SystemInitialization.getApplicationContext().getBean(VideoService.class);
		JSONArray array = vs.getAlbumVideoArray(aid, cid, searchName, start, limit);

		JSONObject obj = new JSONObject();
		obj.put("videoList", array);
		
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取视频列表成功！", obj, this);
	}

	private ExecuteResult countVideo() throws Exception {

		JSONObject jsonObj = getContext().getBody().getBodyObject();
		
		//某些错误的调用，导致vid没有值
		if(jsonObj.isNull("aid"))
		{
			return new ExecuteResult(CommandList.RESPONSE_STATUS_BODY_JSON_ERROR, "请求体错误", null, this);
		}
		
		int aid = jsonObj.getInt("aid");
		String searchName = null;
		if(!jsonObj.isNull("searchName"))
			searchName = jsonObj.getString("searchName");
		VideoService vs = SystemInitialization.getApplicationContext().getBean(VideoService.class);
		int count = vs.getAlbumVideoCount(aid, searchName);
		JSONObject obj = new JSONObject();
		obj.put("count", count);

		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取视频列表记录数成功", obj, this);
	}
	
	private ExecuteResult saveVideoDuration() throws Exception {

		JSONObject jsonObj = getContext().getBody().getBodyObject();
		
		//某些错误的调用，导致vid没有值
		if(jsonObj.isNull("logid") || jsonObj.isNull("duration"))
		{
			return new ExecuteResult(CommandList.RESPONSE_STATUS_BODY_JSON_ERROR, "请求体错误", null, this);
		}
		
		int logid = jsonObj.getInt("logid");
		int duration = jsonObj.getInt("duration");
		//视频时长有可能为负数，负数不进行保存视频时长
		if(duration < 0){
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "保存视频播放时长成功", null, this);
		}
		String platform =getContext()
				.getHead().getPlatform();
		if (getContext().getHead().getVersion() != null && StringUtil.isNotNullNotEmpty(platform)
				&& "5.0.4".equals(getContext().getHead().getVersion())
				&& MobileTypeEnum.gphone == MobileTypeEnum.valueOf(platform)) {
			duration = duration / 1000;
		}
		
		VideoService vs = SystemInitialization.getApplicationContext().getBean(VideoService.class);
		//添加观看时长记录，只记增量的数据。若要计算总时长，把mongoDB中的多次日志相加
		LogVideo logVideo = vs.getLogVideoById(logid);
		if(logVideo != null){
			int sub = duration - (logVideo.getDuration() == null? 0:logVideo.getDuration());
			logVideo.setDuration(sub > 0 ? sub : 0);
			logVideo.setTime(new Date());
			try {
				//保存最近观看记录
				AlbumServiceV5_0 albumServ = SystemInitialization
						.getApplicationContext()
						.getBean(AlbumServiceV5_0.class);
				if(albumServ == null){
					System.out.println("获取AlbumServiceV5_0失败");
					return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "保存视频播放时长成功", null, this);
				}
				albumServ.saveOrUpdateRecentPlayVideo(
						logVideo.getCustomerId() == null ? 0 : logVideo
								.getCustomerId(),
						logVideo.getSpecialType() == null ? 0 : logVideo
								.getSpecialType().getValue(),
						logVideo.getAlbumId() == null ? 0 : logVideo
								.getAlbumId(),
						logVideo.getVideoId() == null ? 0 : logVideo
								.getVideoId(), duration);
			} catch (Exception e) {
				logger.error("保存最近观看记录失败",e);
			}
		}
		vs.saveVideoDuration(logid, duration);
		
		Util.addStatistics(getContext(), logVideo);
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "保存视频播放时长成功", null, this);
	}
	
}
