package com.ytsp.entrance.command.v3_1;

import org.json.JSONArray;

import com.ytsp.entrance.command.base.AbstractCommand;
import com.ytsp.entrance.command.base.CommandList;
import com.ytsp.entrance.command.base.ExecuteResult;
import com.ytsp.entrance.service.v3_1.MemberServiceV31;
import com.ytsp.entrance.system.SystemInitialization;
import com.ytsp.entrance.util.Util;

public class AdVideoCommand extends AbstractCommand {

	/**
	 * 在应用启动时会请求前贴片广告列表
	 */
	@Override
	public boolean canExecute() {
		int code = getContext().getHead().getCommandCode();
		return  CommandList.CMD_AD_VIDEO == code;
	}

	@Override
	public ExecuteResult execute() {
		try {
			int code = getContext().getHead().getCommandCode();
			if (CommandList.CMD_AD_VIDEO == code) {
				return getAdVideos();
			}
		} catch (Exception e) {
			logger.error("execute() error," +
					" HeadInfo :"+getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}
		return null;
	}

	private ExecuteResult getAdVideos() {
		try {
			String platform = getContext().getHead().getPlatform();
			String ip = getContext().getHead().getIp();
			MemberServiceV31 memberService = SystemInitialization.getApplicationContext().getBean(MemberServiceV31.class);
			JSONArray array = memberService.getAdVideos(platform, ip);
//			if("ipad".equals(platform))
//				array = new JSONArray();//空表，暂时把pad屏蔽了
			//添加统计内容
			Util.addStatistics(getContext(), array);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取广告前贴片成功！", array, this);
		} catch (Exception e) {
			logger.error("getAdVideos() error , " +
					"HeadInfo:"+ getContext().getHead().toString(),e);
			return getExceptionExecuteResult(e);
		}
	}
}
