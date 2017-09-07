package com.ytsp.entrance.command.v4_0;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ytsp.db.domain.Customer;
import com.ytsp.db.enums.MobileTypeEnum;
import com.ytsp.db.enums.RecommendTypeEnum;
import com.ytsp.db.exception.SqlException;
import com.ytsp.entrance.command.base.AbstractCommand;
import com.ytsp.entrance.command.base.CommandList;
import com.ytsp.entrance.command.base.ExecuteResult;
import com.ytsp.entrance.command.base.HeadInfo;
import com.ytsp.entrance.service.AlbumService;
import com.ytsp.entrance.service.EbProductService;
import com.ytsp.entrance.service.RecommendService;
import com.ytsp.entrance.service.v3_0.ActivityService;
import com.ytsp.entrance.system.SessionCustomer;
import com.ytsp.entrance.system.SystemInitialization;

public class RecommendCommandV4 extends AbstractCommand {
	@Override
	public boolean canExecute() {
		int code = getContext().getHead().getCommandCode();
		// TODO Auto-generated method stub
		return code == CommandList.CMD_RECOMMEND_LIST_V4
				|| code == CommandList.CMD_TOP_ALBUM
				|| code == CommandList.CMD_COUNT_TOP_ALBUM
				|| code == CommandList.CMD_TOP_ACTIVITY
				|| code == CommandList.CMD_COUNT_TOP_ACTIVITY
				|| code == CommandList.CMD_TOP_EB_PRODUCT
				|| code == CommandList.CMD_COUNT_TOP_EB_PRODUCT;
	}

	@Override
	public ExecuteResult execute() {
		int code = getContext().getHead().getCommandCode();
		// TODO Auto-generated method stub
		try {
			if (code == CommandList.CMD_RECOMMEND_LIST_V4) {
				return getRecommendList();
			} else if (code == CommandList.CMD_TOP_ALBUM) {
				return getTopAlbumList();
			} else if (code == CommandList.CMD_COUNT_TOP_ALBUM) {
				return getCountTopAlbumList();
			} else if (code == CommandList.CMD_TOP_ACTIVITY) {
				return getTopActivityList();
			} else if (code == CommandList.CMD_COUNT_TOP_ACTIVITY) {
				return getCountTopActivityList();
			} else if (code == CommandList.CMD_TOP_EB_PRODUCT) {
				return getTopEbProductList();
			} else if (code == CommandList.CMD_COUNT_TOP_EB_PRODUCT) {
				return getCountTopEbProductList();
			}
		} catch (Exception e) {
			logger.error("execute() error," + " HeadInfo :"
					+ getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}
		return null;
	}

	private ExecuteResult getRecommendList() throws Exception {
		RecommendService rs = SystemInitialization.getApplicationContext()
				.getBean(RecommendService.class);
		String plat = getContext().getHead().getPlatform();
		JSONObject jsonObj = getContext().getBody().getBodyObject();
		int recommendType = jsonObj.getInt("recommendType");
		JSONArray array = null;
		SessionCustomer sc = getSessionCustomer();
		Customer customer = sc == null ? null
				: sc.getCustomer();
		JSONObject obj = new JSONObject();
		//对付苹果审核
//		if(sc!=null&&sc.getCustomer()!=null&&sc.getCustomer().getAccount()!=null){
//			if(sc.getCustomer().getAccount().equals("appletest")&&
//					!getContext().getHead().getVersion().equals("4.5")){
//				obj.put("recommendList", "notfound");
//				return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取推荐列表成功！",
//						obj, this);
//			}
//		}
		getContext().getHead().setUid(customer == null ? 0 : customer.getId());
		array = rs.getRecommendArrayByRecommendType(
				MobileTypeEnum.valueOf(plat), getContext().getHead(),
				RecommendTypeEnum.valueOf(recommendType));
		obj.put("recommendList", array);
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取推荐列表成功！",
				obj, this);
	}

	private ExecuteResult getCountTopAlbumList() throws Exception {
		AlbumService rs = SystemInitialization.getApplicationContext().getBean(
				AlbumService.class);
		String plat = getContext().getHead().getPlatform();
		String version = getContext().getHead().getVersion();
		int count = rs.getAlbumTopListCount(plat, version);
		JSONObject obj = new JSONObject();
		if(count>15)
			count=15;
		obj.put("topAlbumCount", count);
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取推荐专辑列表成功！",
				obj, this);
	}

	private ExecuteResult getCountTopActivityList() throws SqlException,
			JSONException {
		ActivityService rs = SystemInitialization.getApplicationContext()
				.getBean(ActivityService.class);
		String plat = getContext().getHead().getPlatform();
		String version = getContext().getHead().getVersion();
		int count = rs.getActivityTopListCount(plat, version);
		JSONObject obj = new JSONObject();
		obj.put("topActivityCount", count);
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取推荐活动列表成功！",
				obj, this);
	}

	private ExecuteResult getCountTopEbProductList() throws JSONException {
		EbProductService rs = SystemInitialization.getApplicationContext()
				.getBean(EbProductService.class);
		String plat = getContext().getHead().getPlatform();
		String version = getContext().getHead().getVersion();
		int count = rs.getEbProductTopListCount(plat, version);
		JSONObject obj = new JSONObject();
		obj.put("topEbProductCount", count);
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取推荐商品列表成功！",
				obj, this);
	}

	private ExecuteResult getTopAlbumList() throws Exception {
		HeadInfo head = getContext().getHead();
		String platform = head.getPlatform();
		String version = head.getVersion();
		JSONObject jsonObj = getContext().getBody().getBodyObject();
		int start = 0;
		int limit = -1;
		if (!jsonObj.isNull("start")) {
			start = jsonObj.getInt("start");
		}
		if (!jsonObj.isNull("limit")) {
			limit = jsonObj.getInt("limit");
		}
		AlbumService rs = SystemInitialization.getApplicationContext().getBean(
				AlbumService.class);
		JSONArray array = rs.getAlbumTopListArray(platform, start, limit,
				version);
		JSONObject obj = new JSONObject();
		obj.put("albumList", array);
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取专辑排序列表成功",
				obj, this);
	}

	private ExecuteResult getTopActivityList() throws Exception {
		ActivityService rs = SystemInitialization.getApplicationContext()
				.getBean(ActivityService.class);
		HeadInfo head = getContext().getHead();
		String platform = head.getPlatform();
		String version = head.getVersion();
		JSONObject jsonObj = getContext().getBody().getBodyObject();
		int start = 0;
		int limit = -1;
		if (!jsonObj.isNull("start")) {
			start = jsonObj.getInt("start");
		}
		if (!jsonObj.isNull("limit")) {
			limit = jsonObj.getInt("limit");
		}
		JSONArray array = rs.getTopActivities(platform, start, limit, version);
		JSONObject obj = new JSONObject();
		obj.put("activityList", array);
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
				"获取活动toplist成功", obj, this);
	}

	private ExecuteResult getTopEbProductList() throws Exception {
		EbProductService rs = SystemInitialization.getApplicationContext()
				.getBean(EbProductService.class);
		HeadInfo head = getContext().getHead();
		String platform = head.getPlatform();
		String version = head.getVersion();
		JSONObject jsonObj = getContext().getBody().getBodyObject();
		int start = 0;
		int limit = -1;
		if (!jsonObj.isNull("start")) {
			start = jsonObj.getInt("start");
		}
		if (!jsonObj.isNull("limit")) {
			limit = jsonObj.getInt("limit");
		}
		JSONArray array = rs.getTopEbProducts(platform, start, limit, version);
		JSONObject obj = new JSONObject();
		obj.put("ebproductList", array);
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
				"获取商品toplist成功", obj, this);
	}
}
