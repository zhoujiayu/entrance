package com.ytsp.entrance.command.v5_0;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.ytsp.db.exception.SqlException;
import com.ytsp.db.vo.ActivityVO;
import com.ytsp.entrance.command.base.AbstractCommand;
import com.ytsp.entrance.command.base.CommandList;
import com.ytsp.entrance.command.base.ExecuteResult;
import com.ytsp.entrance.service.v5_0.ActivityServiceV5_0;
import com.ytsp.entrance.system.SystemInitialization;
import com.ytsp.entrance.util.Util;

public class ActivityCommandV5_0 extends AbstractCommand{

	@Override
	public boolean canExecute() {
		int code = getContext().getHead().getCommandCode();
		return (code == CommandList.CMD_ACTIVITY_LIST_V5
				||code == CommandList.CMD_EB_ACTIVITY_GET);
	}

	@Override
	public ExecuteResult execute() {
		try {
			int code = getContext().getHead().getCommandCode();
			if(code == CommandList.CMD_ACTIVITY_LIST_V5){
				return getActivityList();
			}
		}  catch (Exception e) {
			logger.error("ActivityCommandV5_0," + " HeadInfo :"
					+ getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}
		return null;
	}
	
	/**
	* <p>功能描述:获取活动列表</p>
	* <p>参数：@return</p>
	* <p>返回类型：ExecuteResult</p>
	 * @throws JSONException 
	 * @throws SqlException 
	 */
	private ExecuteResult getActivityList() throws JSONException, SqlException{
		JSONObject reqBody = getContext().getBody().getBodyObject();
		int page = 0;
		int pageSize = -1;
		if(!reqBody.isNull("page")){
			page = reqBody.getInt("page");
		}
		if(!reqBody.isNull("pageSize")){
			pageSize = reqBody.getInt("pageSize");
		}
		
		String platform = getContext().getHead().getPlatform();
		int userId = getContext().getHead().getUid();
		String version =  getContext().getHead().getVersion();
		String uniqueId = getContext().getHead().getUniqueId();
		ActivityServiceV5_0 actServ = SystemInitialization.getApplicationContext().getBean(ActivityServiceV5_0.class);
		List<ActivityVO> actVos = actServ.getActivityList(page*pageSize, pageSize, platform, version, userId, uniqueId);
		ActivityInfoVO info = new ActivityInfoVO();
		info.setActivityList(actVos);
		Gson gson = new Gson();
		JSONObject result = new JSONObject(gson.toJson(info));
		Util.addStatistics(getContext(), info);
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取活动列表成功", result, this);
	}
	
	class ActivityInfoVO{
		private List<ActivityVO> activityList;

		public List<ActivityVO> getActivityList() {
			return activityList;
		}

		public void setActivityList(List<ActivityVO> activityList) {
			this.activityList = activityList;
		}
		
	}
	
}
