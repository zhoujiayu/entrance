package com.ytsp.entrance.command.v5_0;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.ytsp.db.domain.LaunchAd;
import com.ytsp.db.enums.MobileTypeEnum;
import com.ytsp.db.exception.SqlException;
import com.ytsp.entrance.command.base.AbstractCommand;
import com.ytsp.entrance.command.base.CommandList;
import com.ytsp.entrance.command.base.ExecuteResult;
import com.ytsp.entrance.service.v4_0.LaunchAdService;
import com.ytsp.entrance.system.SystemInitialization;
import com.ytsp.entrance.util.Util;

public class ADCommandV5_0 extends AbstractCommand {

	@Override
	public boolean canExecute() {
		int code = getContext().getHead().getCommandCode();
		return (code == CommandList.CMD_AD_LAUNCH_V5);
	}

	@Override
	public ExecuteResult execute() {
		int code = getContext().getHead().getCommandCode();
		try{
			if(code == CommandList.CMD_AD_LAUNCH_V5){
				return getLaunchAd();
			}
			return null;
		} catch (Exception e) {
			logger.error("ADCommandV5_0," + " HeadInfo :"
					+ getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}
	}

	private ExecuteResult getLaunchAd() throws Exception {
		List<LaunchAd> ret = getLaunchAdList();
		JSONArray array = new JSONArray();
		// 正常状况下最多只能有两个返回值，一个是启动图，一个是启动后
		int flag = 0;
		int plat = MobileTypeEnum.valueOf(getContext().getHead().getPlatform())
				.getValue();
		String pad = "";
		if (plat == 1)// ipad
			pad = "pad";
		else
			pad = "phone";
		if (ret != null && ret.size() > 0) {
			for (LaunchAd ad : ret) {
				if ((flag & 1) > 0 && ad.getAfterLaunch() == 0)
					continue;
				if ((flag & 2) > 0 && ad.getAfterLaunch() == 1)
					continue;
				if (!ad.getPlatType().equals(pad))
					continue;
				JSONObject jo = new JSONObject();
				jo.put("afterLaunch", ad.getAfterLaunch());
				jo.put("id", ad.getId());
//				jo.put("img", Util.getFullImageURL(ad.getImg()));
				jo.put("img", Util.getFullImageURLByVersion(ad.getImg(),getContext().getHead().getVersion(),getContext().getHead().getPlatform()));
				jo.put("type", ad.getType().getValue());
				jo.put("millis", ad.getMillis());
				jo.put("url", ad.getUrl());
				array.put(jo);
				if (ad.getAfterLaunch() == 0)
					flag++;
				else
					flag += 2;
			}
		}
		//添加统计内容
		Util.addStatistics(getContext(), array);
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取启动广告成功",
				array, this);
	}
	
	/**
	* <p>功能描述:获取启动广告</p>
	* <p>参数：@return
	* <p>参数：@throws SqlException</p>
	* <p>返回类型：List<LaunchAd></p>
	 */
	private List<LaunchAd> getLaunchAdList() throws SqlException{
		LaunchAdService ls = SystemInitialization.getApplicationContext()
				.getBean(LaunchAdService.class);
		List<LaunchAd> ret = ls.findLaunchAd();
		return ret;
	}
}
