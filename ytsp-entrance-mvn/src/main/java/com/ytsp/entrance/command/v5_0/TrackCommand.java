package com.ytsp.entrance.command.v5_0;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.ytsp.db.domain.Customer;
import com.ytsp.db.domain.EbTrack;
import com.ytsp.db.enums.EbTrackTypeEnum;
import com.ytsp.entrance.command.base.AbstractCommand;
import com.ytsp.entrance.command.base.CommandList;
import com.ytsp.entrance.command.base.ExecuteResult;
import com.ytsp.entrance.service.v5_0.TrackService;
import com.ytsp.entrance.system.SessionCustomer;
import com.ytsp.entrance.system.SystemInitialization;
import com.ytsp.entrance.util.Util;

public class TrackCommand extends AbstractCommand {

	@Override
	public boolean canExecute() {
		int code = getContext().getHead().getCommandCode();
		return code == CommandList.CMD_TRACK_QUERY
				|| code == CommandList.CMD_TRACK_SAVE
				|| code == CommandList.CMD_TRACK_DELETE
				|| code == CommandList.CMD_TRACK_DELETEALL;
	}

	@Override
	public ExecuteResult execute() {
		// 验证权限.
		int userId = getContext().getHead().getUid();// UID由客户端传递过来,与当前用户的session中的用户ID做比对
		SessionCustomer sc = getSessionCustomer();
		if (sc == null || sc.getCustomer() == null) {
			return getNoPermissionExecuteResult();
		}
		// 判断操作的用户与当前的session中用户是否一致.
		Customer customer = sc.getCustomer();
		if (userId == 0 || customer.getId().intValue() != userId) {
			return getNoPermissionExecuteResult();
		}
		int code = getContext().getHead().getCommandCode();
		if (code == CommandList.CMD_TRACK_QUERY) {
			return queryTrackByPage();
		} else if (code == CommandList.CMD_TRACK_DELETE) {
			return deleteTrack();
		} else if(code == CommandList.CMD_TRACK_DELETEALL){
			return deleteAllTrack();
		}
		return null;
	}
	
	/**
	 * 入参：trackIds,所有选中足迹的id数组
	* <p>功能描述:</p>
	* <p>参数：@return</p>
	* <p>返回类型：ExecuteResult</p>
	 */
	private ExecuteResult deleteAllTrack() {
		try {
			JSONObject result = new JSONObject();
			int userId = getContext().getHead().getUid();
			TrackService trackServ = SystemInitialization.getApplicationContext().getBean(TrackService.class);
			trackServ.deleteAllTracks(userId);
			Util.addStatistics(getContext(), userId);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "清空成功",result, this);
		} catch (Exception e) {
			logger.error("deleteAllTrack() error," + " HeadInfo :"
					+ getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}
	}
	
	/**
	 * 入参：trackIds,所有选中足迹的id数组
	* <p>功能描述:</p>
	* <p>参数：@return</p>
	* <p>返回类型：ExecuteResult</p>
	 */
	private ExecuteResult deleteTrack() {
		try {
			JSONObject result = new JSONObject();
			JSONObject body = getContext().getBody().getBodyObject();
			JSONArray trackArr = body.optJSONArray("trackIds");
			if(trackArr == null || trackArr.length() == 0){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "删除成功",
						result, this);
			}
			List<Integer> ids = new ArrayList<Integer>();
			for (int i = 0; i < trackArr.length(); i++) {
				Integer id = trackArr.getInt(i);
				ids.add(id);
			}
			TrackService trackServ = SystemInitialization.getApplicationContext().getBean(TrackService.class);
			trackServ.deleteTrackByIds(ids);
			Util.addStatistics(getContext(), ids);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "删除成功",
					result, this);
		} catch (Exception e) {
			logger.error("deleteTrack() error," + " HeadInfo :"
					+ getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}
	}
	
	/**
	 * <p>
	 * 入参：pageSize 显示足迹数量，trackId为最后一个足迹id，若没有不用传该 参数
	 * </p>
	 * <p>
	 * 功能描述:分页查询足迹
	 * </p>
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：ExecuteResult
	 * </p>
	 */
	private ExecuteResult queryTrackByPage() {
		try {
			JSONObject body = getContext().getBody().getBodyObject();
			JSONObject result = new JSONObject();
			int userId = getContext().getHead().getUid();
			int trackId = 0;
			if (body.isNull("pageSize")) {
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL,
						"显示足迹数量不能为空", result, this);
			}
			if (!body.isNull("trackId")) {
				trackId = body.getInt("trackId");
			}
			if (userId == 0) {
				return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
						"操作成功", result, this);
			}
			int pageSize = body.getInt("pageSize");
			TrackService trackService = SystemInitialization
					.getApplicationContext().getBean(TrackService.class);
			//删除重复的数据
			trackService.deleteRepeatTracks(userId);
			List<EbTrack> tracks = trackService.getTrackByPage(userId, trackId,
					pageSize,getContext().getHead().getPlatform());
			// 构建足迹VO
			TrackList trackList = buildTrackVOs(tracks);
			Gson gson = new Gson();
			result = new JSONObject(gson.toJson(trackList));
			Util.addStatistics(getContext(), trackList);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "操作成功",
					result, this);
		} catch (Exception e) {
			logger.error("queryTrackByPage() error," + " HeadInfo :"
					+ getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}
	}
	
	/**
	* <p>功能描述:构建足迹VO</p>
	* <p>参数：@param tracks
	* <p>参数：@return
	* <p>参数：@throws JSONException</p>
	* <p>返回类型：TrackList</p>
	 */
	private TrackList buildTrackVOs(List<EbTrack> tracks)
			throws JSONException {
		Map<String, TrackInfoVO> trackInfoVOMap = new HashMap<String, TrackInfoVO>();
		//排序变量
		int sortNum = 0;
		for (EbTrack track : tracks) {
			//构建足迹VO
			TrackVO trackVO = buildTrackVO(track);
			//将足迹按时间分类
			if (trackInfoVOMap.containsKey(trackVO.getCreatTime())) {
				if(isTrackExists(trackInfoVOMap.get(trackVO.getCreatTime()).getTracks(), trackVO)){
					continue;
				}
				trackInfoVOMap.get(trackVO.getCreatTime()).getTracks().add(trackVO);
			} else {
				TrackInfoVO trackInfo = new TrackInfoVO();
				List<TrackVO> list = new ArrayList<TrackVO>();
				list.add(trackVO);
				trackInfo.setTrackDate(trackVO.getCreatTime());
				trackInfo.setTracks(list);
				trackInfo.setSortNum(sortNum);
				sortNum ++;
				trackInfoVOMap.put(trackVO.getCreatTime(), trackInfo);
			}
		}
		//按时间降序排序
		List<TrackInfoVO> trackInfoList = Arrays.asList(trackInfoVOMap.values().toArray(new TrackInfoVO[0]));
		Collections.sort(trackInfoList, new Comparator<TrackInfoVO>() {  
			@Override
			public int compare(TrackInfoVO o1, TrackInfoVO o2) { 
				int ret = 0;
				if(o1.getSortNum() > o2.getSortNum()){
					return 1;
				}else if(o1.getSortNum() < o2.getSortNum()){
					return -1;
				}
				return ret;  
			}  
		});  
		TrackList trackList = new TrackList();
		trackList.setTrackList(trackInfoList);
		return trackList;
	}

	/**
	* 功能描述:
	* 参数：@param list
	* 参数：@param trackVO
	* 参数：@return
	* 返回类型:boolean
	 */
	private boolean isTrackExists(List<TrackVO> list,TrackVO trackVO){
		if(list == null || list.isEmpty()){
			return false;
		}
		int type = trackVO.getTrackType();
		for (TrackVO track : list) {
			if(type != track.getTrackType()){
				continue;
			}
			if (type == EbTrackTypeEnum.PRODUCT.getValue().intValue()) {
				if (trackVO.getProductCode().intValue() == track.getProductCode()) {
					return true;
				}
			} else {
				if (trackVO.getAlbumId().intValue() == track.getAlbumId()) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	class TrackList {
		private List<TrackInfoVO> trackList;

		public List<TrackInfoVO> getTrackList() {
			return trackList;
		}

		public void setTrackList(List<TrackInfoVO> trackList) {
			this.trackList = trackList;
		}

	}

	/**
	 * 足迹信息VO
	 */
	class TrackInfoVO {
		private String trackDate;

		private List<TrackVO> tracks;
		//排序字段
		private int sortNum;
		
		public int getSortNum() {
			return sortNum;
		}

		public void setSortNum(int sortNum) {
			this.sortNum = sortNum;
		}

		public String getTrackDate() {
			return trackDate;
		}

		public void setTrackDate(String trackDate) {
			this.trackDate = trackDate;
		}

		public List<TrackVO> getTracks() {
			return tracks;
		}

		public void setTracks(List<TrackVO> tracks) {
			this.tracks = tracks;
		}

	}

	/**
	 * <p>
	 * 功能描述:构建trackVO
	 * </p>
	 * <p>
	 * 参数：@param track
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：TrackVO
	 * </p>
	 */
	private TrackVO buildTrackVO(EbTrack track) {
		TrackVO trackVo = new TrackVO();
		trackVo.setAge(track.getAge());
		trackVo.setAlbumCount(track.getAlbumCount());
		trackVo.setAlbumId(track.getAlbumId());
		trackVo.setCreatTime(formateTrackDate(track.getCreateTime()));
		trackVo.setVideoId(track.getVideoId());
		trackVo.setIkanPrice(track.getIkanPrice() == null? 0:track.getIkanPrice());
		trackVo.setImageSrc(Util.getFullImageURLByVersion(track.getImageSrc(),
				getContext().getHead().getVersion(), getContext().getHead()
						.getPlatform()));
		trackVo.setProductCode(track.getProductCode());
		trackVo.setSize(track.getSize());
		trackVo.setTrackId(track.getId());
		trackVo.setTrackName(track.getTrackName());
		trackVo.setTrackType(track.getTrackType().getValue());
		trackVo.setUserId(track.getUserId());
		trackVo.setVprice(track.getVprice() == null? 0 : track.getVprice());
		trackVo.setVideoId(track.getVideoId());
		trackVo.setProdTypeName(track.getTypeName());
		trackVo.setVip(track.getVip()==null?false:track.getVip());
		if (track.getNowCount() != null) {
			trackVo.setNowCount(track.getNowCount());
		}
		return trackVo;
	}

	private String formateTrackDate(String createDate) {
		if (createDate == null || createDate.length() <= 0) {
			return "";
		}
		String[] dateArr = createDate.split("-");
		return dateArr[0] + "年" + trimZero(dateArr[1]) + "月"
				+ trimZero(dateArr[2]) + "日";
	}

	private String trimZero(String str) {
		if (StringUtils.isEmpty(str)) {
			return "";
		}
		if(str.startsWith("0")){
			return str.substring(1);
		}
		return str;
	}

	class TrackVO {
		// 足迹id
		private Integer trackId;
		// 用户id
		private Integer userId;
		// 商品id
		private Integer productCode;
		// 专辑id
		private Integer albumId;
		// 足迹类型：1：商品类足迹 2：动漫类足迹 3：知识类足迹
		private Integer trackType;
		// 动漫知识类型名称
		private String trackName;
		// 图片
		private String imageSrc;
		// 创建时间
		private String creatTime;
		// 浏览次数
		private Integer viewCount;
		// 爱看价格
		private double ikanPrice;
		// vip价格
		private double vprice;
		// 产品款式
		private String size;
		// 适用年龄
		private String age;
		// 专辑总数量
		private Integer albumCount;
		// 当前播放专辑
		private Integer videoId;
		// 商品类型
		private String prodTypeName;
		// 更新至
		private int nowCount;
		//视频或者知识vip标识
		private boolean vip;
		
		public boolean isVip() {
			return vip;
		}

		public void setVip(boolean vip) {
			this.vip = vip;
		}

		public int getNowCount() {
			return nowCount;
		}

		public void setNowCount(int nowCount) {
			this.nowCount = nowCount;
		}

		public String getProdTypeName() {
			return prodTypeName;
		}

		public void setProdTypeName(String prodTypeName) {
			this.prodTypeName = prodTypeName;
		}

		public Integer getTrackId() {
			return trackId;
		}

		public void setTrackId(Integer trackId) {
			this.trackId = trackId;
		}

		public Integer getUserId() {
			return userId;
		}

		public void setUserId(Integer userId) {
			this.userId = userId;
		}

		public Integer getProductCode() {
			return productCode;
		}

		public void setProductCode(Integer productCode) {
			this.productCode = productCode;
		}

		public Integer getAlbumId() {
			return albumId;
		}

		public void setAlbumId(Integer albumId) {
			this.albumId = albumId;
		}

		public Integer getTrackType() {
			return trackType;
		}

		public void setTrackType(Integer trackType) {
			this.trackType = trackType;
		}

		public String getTrackName() {
			return trackName;
		}

		public void setTrackName(String trackName) {
			this.trackName = trackName;
		}

		public String getImageSrc() {
			return imageSrc;
		}

		public void setImageSrc(String imageSrc) {
			this.imageSrc = imageSrc;
		}

		public String getCreatTime() {
			return creatTime;
		}

		public void setCreatTime(String creatTime) {
			this.creatTime = creatTime;
		}

		public Integer getViewCount() {
			return viewCount;
		}

		public void setViewCount(Integer viewCount) {
			this.viewCount = viewCount;
		}

		public double getIkanPrice() {
			return ikanPrice;
		}

		public void setIkanPrice(double ikanPrice) {
			this.ikanPrice = ikanPrice;
		}

		public double getVprice() {
			return vprice;
		}

		public void setVprice(double vprice) {
			this.vprice = vprice;
		}

		public String getSize() {
			return size;
		}

		public void setSize(String size) {
			this.size = size;
		}

		public String getAge() {
			return age;
		}

		public void setAge(String age) {
			this.age = age;
		}

		public Integer getAlbumCount() {
			return albumCount;
		}

		public void setAlbumCount(Integer albumCount) {
			this.albumCount = albumCount;
		}

		public Integer getVideoId() {
			return videoId;
		}

		public void setVideoId(Integer videoId) {
			this.videoId = videoId;
		}

	}

}
