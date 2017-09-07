package com.ytsp.entrance.service.v5_0;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ytsp.db.dao.ActivityDao;
import com.ytsp.db.domain.Activity;
import com.ytsp.db.exception.SqlException;
import com.ytsp.db.vo.ActivityVO;
import com.ytsp.entrance.util.DateFormatter;
import com.ytsp.entrance.util.Util;

@Service("activityServiceV5_0")
@Transactional
public class ActivityServiceV5_0 {

	@Resource(name = "activityDao")
	ActivityDao activityDao;

	/**
	 * <p>
	 * 功能描述:获取活动列表
	 * </p>
	 * <p>
	 * 参数：@param start
	 * <p>
	 * 参数：@param limit
	 * <p>
	 * 参数：@param platform
	 * <p>
	 * 参数：@param version
	 * <p>
	 * 参数：@param uid
	 * <p>
	 * 参数：@param uniqueId
	 * <p>
	 * 参数：@return
	 * <p>
	 * 参数：@throws SqlException
	 * </p>
	 * <p>
	 * 返回类型：List<ActivityVO>
	 * </p>
	 */
	public List<ActivityVO> getActivityList(int start, int limit,
			String platform, String version, int uid, String uniqueId)
			throws SqlException {
		// 只获取开始有图片以后的，也就是id>39的
		List<Activity> ls = null;
		if ("ipad".equals(platform)) {
			//苹果审核期间将vip去掉
			if(Util.isIOSInReview(platform, version)){
				ls = activityDao
						.findAllByHql(
								"where activity_id>39 and platform in(?,?,?,?) and activity_id not in (94,95) order by activity_end_time desc",
								start, limit, new Object[] { 2, 3, 6, 7 });
			}else{
				ls = activityDao
						.findAllByHql(
								"where activity_id>39 and platform in(?,?,?,?) order by activity_end_time desc",
								start, limit, new Object[] { 2, 3, 6, 7 });
			}
		} else {
			//苹果审核期间将vip去掉
			if(Util.isIOSInReview(platform, version)){
				ls = activityDao
						.findAllByHql(
								"where activity_id >39 and platform  in(?,?,?,?)  and activity_id not in (94,95) order by activity_end_time desc",
								start, limit, new Object[] { 1, 3, 5, 7 });
			}else{
				ls = activityDao
						.findAllByHql(
								"where activity_id >39 and platform  in(?,?,?,?) order by activity_end_time desc",
								start, limit, new Object[] { 1, 3, 5, 7 });
			}
		}
		return buildActivityVO(ls, uid, uniqueId,version, platform);
	}

	/**
	 * <p>
	 * 功能描述:构建活动VO
	 * </p>
	 * <p>
	 * 参数：@param activityList
	 * <p>
	 * 参数：@param userId
	 * <p>
	 * 参数：@param uniqueId
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：List<ActivityVO>
	 * </p>
	 */
	private List<ActivityVO> buildActivityVO(List<Activity> activityList,
			int userId, String uniqueId,String version, String platform) {
		if (activityList == null || activityList.size() <= 0) {
			return null;
		}
		List<ActivityVO> actVOs = new ArrayList<ActivityVO>();
		String via = "ACT";
//		String imgHost = SystemManager.getInstance().getSystemConfig()
//				.getImgServerUrl();
		for (Activity activity : activityList) {
			ActivityVO actVo = new ActivityVO();
			actVo.setActivityId(activity.getActivity_id());
			actVo.setBeginTime(DateFormatter.date2String(activity
					.getActivity_begin_time(),"yyyy.MM.dd HH:mm"));
			String imgUrl = activity.getClient_activity_img();
			if (imgUrl.startsWith("/images/upload/")) {
				actVo.setClientImg("http://m.ikan.cn" + imgUrl);
			} else {
//				actVo.setClientImg(Util.getFullImageURL(imgUrl));
				actVo.setClientImg(Util.getFullImageURLByVersion(imgUrl,version,platform));
			}
			actVo.setEndTime(DateFormatter.date2String(activity
					.getActivity_end_time(),"yyyy.MM.dd HH:mm"));
			actVo.setLocation(activity.getLocation());
			actVo.setOver(isActivityOver(activity.getActivity_begin_time(),
					activity.getActivity_end_time()));
			actVo.setTerminal(uniqueId);
			actVo.setTitle(activity.getTitle());
//			actVo.setUrl(activity.getUrl());
			actVo.setUrl(Util.replaceURLByVersion(activity.getUrl(),version,platform));
			actVo.setUserId(userId);
			actVo.setVia(via);
			actVOs.add(actVo);
		}
		return actVOs;
	}

	/**
	 * <p>
	 * 功能描述:
	 * </p>
	 * <p>
	 * 参数：@param activity
	 * <p>
	 * 参数：@param platform
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：String
	 * </p>
	 */
	private String getDefaultUrl(int activity, String platform) {
		String defaultUrl = "http://m.ikan.cn/activity_client.action?activity_id="
				+ activity + "&from=" + platform;
		return defaultUrl;
	}

	/**
	 * <p>
	 * 功能描述:判断活动是否结束
	 * </p>
	 * <p>
	 * 参数：@param startTime
	 * <p>
	 * 参数：@param endTime
	 * <p>
	 * 参数：@return
	 * </p>
	 * <p>
	 * 返回类型：boolean
	 * </p>
	 */
	private boolean isActivityOver(Date startTime, Date endTime) {
		Date now = new Date();
		if(startTime == null){
			return false;
		}
		if(endTime == null){
			return false;
		}
		return endTime.before(now);
	}

}
