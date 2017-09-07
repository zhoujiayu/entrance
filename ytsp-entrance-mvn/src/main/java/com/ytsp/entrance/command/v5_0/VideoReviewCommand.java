package com.ytsp.entrance.command.v5_0;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.ytsp.common.util.StringUtil;
import com.ytsp.db.domain.Album;
import com.ytsp.db.domain.Video;
import com.ytsp.db.enums.ReviewStatusEnum;
import com.ytsp.db.vo.AlbumVO;
import com.ytsp.db.vo.VideoVO;
import com.ytsp.entrance.command.base.AbstractCommand;
import com.ytsp.entrance.command.base.CommandList;
import com.ytsp.entrance.command.base.ExecuteResult;
import com.ytsp.entrance.service.v5_0.AlbumServiceV5_0;
import com.ytsp.entrance.service.v5_0.VideoReviewService;
import com.ytsp.entrance.system.IConstants;
import com.ytsp.entrance.system.SystemInitialization;
import com.ytsp.entrance.util.MD5;
import com.ytsp.entrance.util.NumericUtil;
import com.ytsp.entrance.util.Util;

public class VideoReviewCommand extends AbstractCommand{

	@Override
	public boolean canExecute() {
		int code = getContext().getHead().getCommandCode();
		return code == CommandList.CMD_ALBUM_UNREVIEW_LIST
				|| code == CommandList.CMD_VIDEO_UNREVIEW_LIST
				|| code == CommandList.CMD_VIDEO_REVIEW;
	}

	@Override
	public ExecuteResult execute() {
		try {
			int code = getContext().getHead().getCommandCode();

			if (code == CommandList.CMD_ALBUM_UNREVIEW_LIST) {
				return getUnReviewAlbumList();
			} else if (code == CommandList.CMD_VIDEO_UNREVIEW_LIST) {
				return getUnReviewVideoList();
			} else if (code == CommandList.CMD_VIDEO_REVIEW) {
				return reviewVideo();
			}
		} catch (Exception e) {
			logger.error("VideoReviewCommand error: headInfo: "
					+ getContext().getHead().toString() + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}
	
	
	/**
	* <p>功能描述:获取有未审核视频的剧集列表</p>
	* <p>参数：@return</p>
	* <p>返回类型：ExecuteResult</p>
	 * @throws Exception 
	 */
	private ExecuteResult reviewVideo() throws Exception {
		JSONObject result = new JSONObject();
		JSONObject reqParam = getContext().getBody().getBodyObject();
		if(reqParam.isNull("videoId") || reqParam.isNull("reviewType")){
			return new ExecuteResult(CommandList.RESPONSE_STATUS_BODY_JSON_ERROR, "请求参数错误",result, this);
		}
		if(!isHavePermission(reqParam.optString("key"))){
			return new ExecuteResult(CommandList.RESPONSE_STATUS_NOPERMISSION, "没有访问权限！",result, this);
		}
		int videoId = reqParam.optInt("videoId");
		int reviewType = reqParam.optInt("reviewType");
		VideoReviewService reviewService = SystemInitialization
				.getApplicationContext().getBean(VideoReviewService.class);
		Video video = reviewService.getVideo(videoId);
		if(video == null){
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "审核的视频不存在",result, this);
		}
		if(video.getReview() != ReviewStatusEnum.UNREVIEW){
			return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "视频的状态已发生变化，请刷新再操作",result, this);
		}
		// 审核视频
		reviewService.updateVideo(videoId, reviewType);
		result.put("result", true);
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "审核视频成功",result, this);
	}
	
	
	/**
	* <p>功能描述:获取有未审核视频的剧集列表</p>
	* <p>参数：@return</p>
	* <p>返回类型：ExecuteResult</p>
	 * @throws Exception 
	 */
	private ExecuteResult getUnReviewVideoList() throws Exception {
		JSONObject result = new JSONObject();
		JSONObject reqParam = getContext().getBody().getBodyObject();
		int page = 0;
		int pageSize = -1;
		if(reqParam.isNull("key")){
			return new ExecuteResult(CommandList.RESPONSE_STATUS_BODY_JSON_ERROR, "请求参数错误",result, this);
		}
		if (!reqParam.isNull("page")) {
			page = reqParam.optInt("page");
		}
		if (!reqParam.isNull("pageSize")) {
			pageSize = reqParam.optInt("pageSize");
		}
		if(reqParam.isNull("albumId")){
			return new ExecuteResult(CommandList.RESPONSE_STATUS_BODY_JSON_ERROR, "请求参数错误",result, this);
		}
		int albumId = reqParam.optInt("albumId");
		if (!isHavePermission(reqParam.optString("key"))) {
			return new ExecuteResult(
					CommandList.RESPONSE_STATUS_NOPERMISSION, "没有访问权限！",
					result, this);
		}
		VideoReviewService reviewService = SystemInitialization
				.getApplicationContext().getBean(VideoReviewService.class);
		AlbumServiceV5_0 albumService = SystemInitialization
				.getApplicationContext().getBean(AlbumServiceV5_0.class);
		List<Video> unReviewAlbums = reviewService.getUnReviewVideoList(albumId,page,
				pageSize);
		List<VideoVO> videoVos = albumService.fillVideoListVO(unReviewAlbums, "", "",albumId,true);
		result.put("unReviewVideos", videoVos);
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取剧集下未审核的视频成功",result, this);
	}
	
	/**
	* <p>功能描述:校验是否有访问权限</p>
	* <p>参数：@param key
	* <p>参数：@return</p>
	* <p>返回类型：boolean</p>
	 */
	private boolean isHavePermission(String key){
		if(StringUtil.isNullOrEmpty(key)){
			return false;
		}
		if(key.equals(MD5.code(IConstants.VIDEOREVIEWKEY))){
			return true;
		}
		return false;
	}
	
	/**
	* <p>功能描述:获取有未审核视频的剧集列表</p>
	* <p>参数：@return</p>
	* <p>返回类型：ExecuteResult</p>
	 * @throws JSONException 
	 */
	private ExecuteResult getUnReviewAlbumList() throws JSONException {
			JSONObject result = new JSONObject();
			JSONObject reqParam = getContext().getBody().getBodyObject();
			int page = 0;
			int pageSize = -1;
			if(reqParam.isNull("key")){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_BODY_JSON_ERROR, "请求参数错误",result, this);
			}
			if(!reqParam.isNull("page")){
				page = reqParam.optInt("page");
			}
			if(!reqParam.isNull("pageSize")){
				pageSize = reqParam.optInt("pageSize");
			}
			if(!isHavePermission(reqParam.optString("key"))){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_NOPERMISSION, "没有访问权限！",result, this);
			}
			VideoReviewService reviewService = SystemInitialization
					.getApplicationContext().getBean(VideoReviewService.class);
			List<Album> unReviewAlbums = reviewService.getUnReviewAlbumList(page,pageSize);
			//构建未审核剧集
			List<AlbumVO> unReviewAlbumVOs = buildUnReviewAlbumList(unReviewAlbums);
			result.put("unReviewList", unReviewAlbumVOs);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取未审核剧集成功",result, this);
	}
	
	/**
	* <p>功能描述:构建未审核的剧集</p>
	* <p>参数：@param unReviewList
	* <p>参数：@return</p>
	* <p>返回类型：List<AlbumVO></p>
	 */
	private List<AlbumVO> buildUnReviewAlbumList(List<Album> unReviewList){
		List<AlbumVO> unReviewAlb = new ArrayList<AlbumVO>();
		if(unReviewList == null || unReviewList.size() <=0){
			return unReviewAlb;
		}
		for (Album alb : unReviewList) {
			unReviewAlb.add(buildAlbumVO(alb));
		}
		return unReviewAlb;
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
		vo.setSnapshot(Util.getFullImageURL(ablum.getCover()));
		vo.setId(ablum.getId());
		vo.setName(ablum.getName());
		vo.setType(ablum.getType().getValue());
		vo.setTotalCount(NumericUtil.parseInt(ablum.getTotalCount()));
		vo.setNowCount(NumericUtil.parseInt(ablum.getNowCount()));
		
		return vo;
	}
}
