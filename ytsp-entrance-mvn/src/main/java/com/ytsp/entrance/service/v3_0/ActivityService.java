package com.ytsp.entrance.service.v3_0;

import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ytsp.db.dao.ActivityDao;
import com.ytsp.db.domain.Activity;
import com.ytsp.db.exception.SqlException;
import com.ytsp.entrance.util.DateFormatter;

public class ActivityService {

	ActivityDao activityDao;
	
	public int countActivity(String platform) throws SqlException {
		if ("ipad".equals(platform) || "iphone".equals(platform))
			return activityDao.getRecordCount(
					" where activity_id>39 and platform in(?,?,?,?)",
					new Object[] { 2, 3, 6, 7 });
		else if ("gpad".equals(platform) || "gphone".equals(platform))
			return activityDao.getRecordCount(
					" where activity_id>39 and platform in(?,?,?,?)",
					new Object[] { 1, 3, 5, 7 });
		else
			return activityDao.getRecordCount("where activity_id>39");
	}

	public JSONArray getActivityArray(int start, int limit, String platform,
			String version, int uid, String uniqueId) throws SqlException,
			Exception {
		// 只获取开始有图片以后的，也就是id>39的
		List<Activity> ls = null;
		if ("ipad".equals(platform)) {
			ls = activityDao
					.findAllByHql(
							"where activity_id>39 and platform in(?,?,?,?) order by activity_end_time desc",
							start, limit, new Object[] { 2, 3, 6, 7 });
		}
		else {
			ls = activityDao
					.findAllByHql(
							"where activity_id>39 and platform  in(?,?,?,?) order by activity_end_time desc",
							start, limit, new Object[] { 1, 3, 5, 7 });
		}
		JSONArray array = new JSONArray();
		if (ls != null)
			for (Activity activity : ls) {
				JSONObject obj = new JSONObject();
				obj.put("activityId", activity.getActivity_id());
				obj.put("title", activity.getTitle());
				obj.put("activityBeginTime", DateFormatter.date2String(activity
						.getActivity_begin_time()));
				obj.put("activityEndTime", DateFormatter.date2String(activity
						.getActivity_end_time()));
				obj.put("clientImg",
						"http://images.ikan.cn/" + activity.getClient_activity_img());
				obj.put("location", activity.getLocation());
				JSONObject jo = new JSONObject();
				jo.put("userid", uid);
				jo.put("terminal", uniqueId);
				jo.put("via", "ACT");
				if(activity.getUrl()!=null&&!activity.getUrl().equals(""))
					obj.put("url",activity.getUrl());
				else
					obj.put("url",
							"http://m.ikan.cn/activity_client.action?activity_id="
									+ activity.getActivity_id() + "&from="
									+ platform);
				array.put(obj);
			}
		return array;
	}

	public JSONArray getActivityNotificationList(int activityId,
			String platform, String version, int uid, String uniqueId)
			throws SqlException, JSONException {
		// 只获取activityId>39的
		if (activityId < 39)
			activityId = 39;
		List<Activity> ls = null;

		if (("ipad".equals(platform) || "iphone".equals(platform))) {
			ls = activityDao
					.findAllByHql(
							"where CURDATE()<=activity_end_time and activity_id>? and platform in(?,?,?,?) order by activity_id desc",
							new Object[] { activityId, 2, 3, 6, 7 });
		}
		if (("gpad".equals(platform) || "gphone".equals(platform))) {
			ls = activityDao
					.findAllByHql(
							"where CURDATE()<=activity_end_time and activity_id>? and platform  in(?,?,?,?) order by activity_id desc",
							new Object[] { activityId, 1, 3, 5, 7 });
		}

		JSONArray array = new JSONArray();
		if (ls != null)
			for (Activity activity : ls) {
				JSONObject obj = new JSONObject();
				obj.put("activityId", activity.getActivity_id());
				array.put(obj);
			}
		return array;
	}

	public String getRecentlyImage() throws Exception {
		return "http://m.ikan.cn/nilLast/nilLast.png";
	}

	public ActivityDao getActivityDao() {
		return activityDao;
	}

	public void setActivityDao(ActivityDao activityDao) {
		this.activityDao = activityDao;
	}

	public JSONArray getTopActivities(String platform, int start, int limit,
			String version) throws Exception {
    	JSONArray array = new JSONArray();
    	String plat="";
		if("ipad".equals(platform))
			plat="2,3,6,7";
		else
			plat="1,3,5,7";
    	List<Activity> ls = activityDao.findAllByHql(" where activity_end_time>? and platform in("+plat+") order by activity_id desc" , 
    			start, limit, new Object[]{new Date()});
    	for (int i = 0; i < ls.size(); i++) {
    		Activity foo = ls.get(i);
    		JSONObject jo = new JSONObject();
    		jo.put("title", foo.getTitle());
    		jo.put("activityBeginTime", foo.getActivity_begin_time());
    		jo.put("activityEndTime", foo.getActivity_end_time());
    		jo.put("activityId", foo.getActivity_id());
    		jo.put("clientImg", "http://images.ikan.cn/"+foo.getClient_activity_img());
    		jo.put("location", foo.getLocation());
    		if(foo.getUrl()!=null&&!foo.getUrl().trim().equals(""))
    			jo.put("url",foo.getUrl());
    		else
    			jo.put("url","http://m.ikan.cn/activity_client.action?activity_id="
							+ foo.getActivity_id() + "&from="+ platform);
    		array.put(jo);
		}
    	return array;
	}

	public int getActivityTopListCount(String plat, String version) throws SqlException {
		//1,2,4分别是手机、Pad、网站
		String date = DateFormatter.date2String(new Date());
		if(plat.equals("ipad"))
			plat="2,3,6,7";
		else
			plat="1,3,5,7";
		return activityDao.sqlCount("select count(1) from  where activity_end_time>'"+date+"' and platform in("+plat+")"); 
	}

	public Activity getActivityById(int activityId) throws SqlException {
		return activityDao.findById(activityId);
	}
}
