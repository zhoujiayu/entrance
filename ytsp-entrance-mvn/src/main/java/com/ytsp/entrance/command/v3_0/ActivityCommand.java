package com.ytsp.entrance.command.v3_0;

import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

import com.ytsp.db.domain.Activity;
import com.ytsp.db.domain.Customer;
import com.ytsp.db.domain.EbSku;
import com.ytsp.entrance.command.base.AbstractCommand;
import com.ytsp.entrance.command.base.CommandList;
import com.ytsp.entrance.command.base.ExecuteResult;
import com.ytsp.entrance.service.EbSkillOrderService;
import com.ytsp.entrance.service.EbSkuService;
import com.ytsp.entrance.service.MemberService;
import com.ytsp.entrance.service.v3_0.ActivityService;
import com.ytsp.entrance.system.SessionCustomer;
import com.ytsp.entrance.system.SystemInitialization;
import com.ytsp.entrance.util.DateFormatter;

public class ActivityCommand extends AbstractCommand {

	@Override
	public boolean canExecute() {
		int code = getContext().getHead().getCommandCode();
		return CommandList.CMD_ACTIVITY_LIST == code
				|| CommandList.CMD_ACTIVITY_COUNT == code
				|| CommandList.CMD_RECENTLY_IMAGE == code
				|| CommandList.CMD_ACTIVITY_NOTIFICATION_LIST == code
						|| CommandList.CMD_GETACTIVITYBYID == code||
						//因为要修改老版本entrance所以就直接用数字了；是否有资格领券，是否缺货
								CommandList.CMD_YUEBING_CHECK== code||//点选区域时验证
										CommandList.CMD_YUEBING_OBTAIN== code||//领取
												CommandList.CMD_YUEBING_INIT== code;//初始状态验证
	}

	@Override
	public ExecuteResult execute() {
		try {
			int code = getContext().getHead().getCommandCode();
			if (CommandList.CMD_ACTIVITY_LIST == code) {
				return getActivityList();
			} else if (CommandList.CMD_ACTIVITY_COUNT == code) {
				return countActivity();
			} else if (CommandList.CMD_RECENTLY_IMAGE == code) {
				return getRecentlyImage();
			} else if (CommandList.CMD_ACTIVITY_NOTIFICATION_LIST == code) {
				return getActivityNotificationList();
			}else if (CommandList.CMD_GETACTIVITYBYID == code) {
				return getActivityById();
			}else if (CommandList.CMD_YUEBING_CHECK == code) {
				return checkVIPYUEBING();
			}else if (CommandList.CMD_YUEBING_INIT == code) {
				return callVIPYUEBING();
			}else if (CommandList.CMD_YUEBING_OBTAIN == code) {
				return obtainVIPYUEBING();
			}
		} catch (Exception e) {
			logger.error("ActivityCommand execute() error," + " HeadInfo :"
					+ getContext().getHead().toString(), e);
			e.printStackTrace();
			return getExceptionExecuteResult(e);
		}
		return null;
	}

	private ExecuteResult obtainVIPYUEBING() throws Exception {
		int userId = getContext().getHead().getUid();// UID由客户端传递过来,与当前用户的session中的用户ID做比对
		SessionCustomer sc = getSessionCustomer();
		JSONObject params = getContext().getBody().getBodyObject();
		if (sc == null || sc.getCustomer() == null) {
			return getNoPermissionExecuteResult();
		}
		// 判断操作的用户与当前的session中用户是否一致.
		Customer customer = sc.getCustomer();
		if (userId == 0 || customer.getId().intValue() != userId) {
			return getNoPermissionExecuteResult();
		}
		MemberService memberService = SystemInitialization.getApplicationContext().getBean(MemberService.class);
		JSONObject ret = new JSONObject();
		ret.put("command", CommandList.CMD_YUEBING_OBTAIN);
		JSONObject json = memberService.memberCheck_v2_5(userId);
		if(json.isNull("endTime")){
			ret.put("status", -1);
			return  new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
					"到期时间在一年以上的vip用户才能领取", ret, this);
		}else{
			Date end = DateFormatter.string2Date(json.getString("endTime"));
			if(end.before(new Date(System.currentTimeMillis()+1000*86400*360l))){
				ret.put("status", -1);
				return  new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
						"到期时间在一年以上的vip用户才能领取", ret, this);
			}
		}
		int skuCode = Integer.parseInt(String.valueOf(params.get("yuebingSkuCode")));
		EbSkuService skuService = SystemInitialization.getApplicationContext().getBean(EbSkuService.class);
		EbSku sku = skuService.retrieveEbSkuBySkuCode(skuCode);
		if(sku.getStorage().getAvailable()<=0){
			ret.put("status", -2);
			return  new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
					"已经领完", ret, this);
		}
		EbSkillOrderService skillOrderService = SystemInitialization.getApplicationContext().getBean(EbSkillOrderService.class);
		if(!skillOrderService.checkYuebing(userId)){
			ret.put("status", -3);
			return  new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
					"已经领过月饼了，不能再领", ret, this);
		}
		String addr = params.getString("addr");
		String cellphone = String.valueOf(params.get("cellphone"));
		String username = String.valueOf(params.get("username"));
		skillOrderService.saveYuebing(userId, username,skuCode, addr, cellphone);
		ret.put("status",1);
		return  new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
				"成功领取", ret, this);
	}

	private ExecuteResult checkVIPYUEBING() throws Exception {
		int userId = getContext().getHead().getUid();// UID由客户端传递过来,与当前用户的session中的用户ID做比对
		SessionCustomer sc = getSessionCustomer();
		JSONObject params = getContext().getBody().getBodyObject();
		if (sc == null || sc.getCustomer() == null) {
			return getNoPermissionExecuteResult();
		}
		// 判断操作的用户与当前的session中用户是否一致.
		Customer customer = sc.getCustomer();
		if (userId == 0 || customer.getId().intValue() != userId) {
			return getNoPermissionExecuteResult();
		}
		MemberService memberService = SystemInitialization.getApplicationContext().getBean(MemberService.class);
		JSONObject ret = new JSONObject();
		ret.put("command", CommandList.CMD_YUEBING_CHECK);
		JSONObject json = memberService.memberCheck_v2_5(userId);
		if(json.isNull("endTime")){
			ret.put("status", -1);
			return  new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
					"到期时间在一年以上的vip用户才能领取", ret, this);
		}else{
			Date end = DateFormatter.string2Date(json.getString("endTime"));
			if(end.before(new Date(System.currentTimeMillis()+1000*86400*360l))){
				ret.put("status", -1);
				return  new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
						"到期时间在一年以上的vip用户才能领取", ret, this);
			}
		}
		int skuCode = Integer.parseInt(String.valueOf(params.get("yuebingSkuCode")));
		EbSkuService skuService = SystemInitialization.getApplicationContext().getBean(EbSkuService.class);
		EbSku sku = skuService.retrieveEbSkuBySkuCode(skuCode);
		if(sku.getStorage().getAvailable()<=0){
			ret.put("status", -2);
			return  new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
					"已经领完", ret, this);
		}
		ret.put("status", 1);
		return  new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
				"准备领取", ret, this);
	}
	

	private ExecuteResult callVIPYUEBING() throws Exception {
		JSONObject ret = new JSONObject();
		ret.put("command", CommandList.CMD_YUEBING_INIT);
		EbSkuService skuService = SystemInitialization.getApplicationContext().getBean(
				EbSkuService.class);
		int skuCode= 990001101;
		for (int i = 0; i < 3; i++) {
			EbSku sku = skuService.retrieveEbSkuBySkuCode(skuCode);
			if(sku!=null)
				ret.put(String.valueOf(skuCode), sku.getStorage().getAvailable());
			skuCode++;
		}
		ret.put("status", 1);
		return  new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
				"初始化", ret, this);
	}
	

	private ExecuteResult getActivityById() {
		try {
			JSONObject jsonObj = getContext().getBody().getBodyObject();
			ActivityService rs = SystemInitialization.getApplicationContext().getBean(ActivityService.class);
			int activityId = jsonObj.getInt("activityId");
			JSONObject obj = new JSONObject();
			Activity activity = rs.getActivityById(activityId);
			obj.put("title", activity.getTitle());
			obj.put("type", activity.getType());
			obj.put("clientActivityImg", activity.getClient_activity_img());
			obj.put("activityBeginTime", activity.getActivity_begin_time());
			obj.put("activityEndTime", activity.getActivity_end_time());
			obj.put("location", activity.getLocation());
			if(activity.getUrl()!=null&&!activity.getUrl().equals(""))
				obj.put("url",activity.getUrl());
			else
				obj.put("url","http://m.ikan.cn/activity_client.action?activity_id="
							+ activity.getActivity_id() + "&from="
							+  getContext().getHead().getPlatform());
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
					"获取单个活动成功", obj, this);
		} catch (Exception e) {
			logger.error("countActivity() error," + " HeadInfo :"
					+ getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}
	}

	private ExecuteResult countActivity() {
		try {
			ActivityService rs = SystemInitialization.getApplicationContext().getBean(ActivityService.class);
			int count = rs.countActivity(getContext().getHead().getPlatform());
			JSONObject obj = new JSONObject();
			obj.put("count", count);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
					"获取活动列表记录数成功", obj, this);
		} catch (Exception e) {
			logger.error("countActivity() error," + " HeadInfo :"
					+ getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}
	}

	private ExecuteResult getActivityList() {
		try {
			JSONObject jsonObj = getContext().getBody().getBodyObject();
			int limit = 12;
			if(!jsonObj.isNull("limit")){
				limit = jsonObj.optInt("limit");
			}
			int start = jsonObj.optInt("start");
			ActivityService rs = SystemInitialization.getApplicationContext()
					.getBean(ActivityService.class);
			JSONArray ret;
			Customer customer = getSessionCustomer()==null? null:getSessionCustomer().getCustomer();
			//limit参数暂时不用
			ret = rs.getActivityArray(start, limit, getContext().getHead()
					.getPlatform(), getContext().getHead().getVersion(),
					customer==null?0:customer.getId(), getContext().getHead()
							.getUniqueId());
			JSONObject obj = new JSONObject();
			obj.put("activityList", ret); 
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
					"获取活动列表成功", obj, this);
		} catch (Exception e) {
			logger.error("getActivityList() error," + " HeadInfo :"
					+ getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}
	}

	private ExecuteResult getActivityNotificationList() {
		try {
			JSONObject jsonObj = getContext().getBody().getBodyObject();
			int activityId = 0;
			try {
				activityId = jsonObj.getInt("activityId");
			} catch (Exception e) {
				activityId = 39;
			}
			ActivityService rs = SystemInitialization.getApplicationContext()
					.getBean(ActivityService.class);
			JSONArray ret;
			ret = rs.getActivityNotificationList(activityId, getContext().getHead()
					.getPlatform(), getContext().getHead().getVersion(),
					getContext().getHead().getUid(), getContext().getHead()
							.getUniqueId());

			JSONObject obj = new JSONObject();
			obj.put("activityList", ret);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
					"获取活动列表成功", obj, this);
		} catch (Exception e) {
			logger.error("getActivityAllList() error," + " HeadInfo :"
					+ getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}
	}

	private ExecuteResult getRecentlyImage() {
		try {
			ActivityService rs = SystemInitialization.getApplicationContext()
					.getBean(ActivityService.class);
			String imageUrl = rs.getRecentlyImage();
			JSONObject obj = new JSONObject();
			obj.put("imageUrl", imageUrl);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
					"获取观看记录图片成功", obj, this);
		} catch (Exception e) {
			logger.error("getRecentlyImage() error," + " HeadInfo :"
					+ getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}
	}
}
