package com.ytsp.entrance.command.v3_1;

import org.json.JSONObject;

import com.ytsp.entrance.command.base.AbstractCommand;
import com.ytsp.entrance.command.base.CommandList;
import com.ytsp.entrance.command.base.ExecuteResult;
import com.ytsp.entrance.service.v3_1.MemberServiceV31;
import com.ytsp.entrance.service.v5_0.AlbumServiceV5_0;
import com.ytsp.entrance.system.SystemInitialization;

public class MemberCommandV31 extends AbstractCommand {

	@Override
	public boolean canExecute() {
		int code = getContext().getHead().getCommandCode();
		return  CommandList.CMD_MEMBER_VIDEO_PLAY_V3_1 == code;
	}

	@Override
	public ExecuteResult execute() {
		try {
			int code = getContext().getHead().getCommandCode();
			if (CommandList.CMD_MEMBER_VIDEO_PLAY_V3_1 == code) {
				return memberPlayVideo();
			} 
		} catch (Exception e) {
			logger.error("execute() error," +
					" HeadInfo :"+getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}
		return null;
	}

	/**
	 * 会员播放视频
	 * 
	 * @return
	 */
	public ExecuteResult memberPlayVideo() {
		try {
			JSONObject jsonObj = getContext().getBody().getBodyObject();
			if (jsonObj.isNull("vid") || jsonObj.isNull("aid")) {
				return new ExecuteResult(CommandList.RESPONSE_STATUS_BODY_JSON_ERROR, "请求体错误",null, this);
			}
			int userId = getContext().getHead().getUid();
			String platform = getContext().getHead().getPlatform();
			String version = getContext().getHead().getVersion();
			String terminalNumber = getContext().getHead().getUniqueId();
			String ip = getContext().getHead().getIp();
			int videoId = jsonObj.getInt("vid");
			int albumId = jsonObj.getInt("aid");
			//playerType使用播放器类型：0为本地播放器，1为乐视播放器
			int playerType = 0;
			if(jsonObj.has("playerType")){
				playerType = jsonObj.optInt("playerType");
			}
			MemberServiceV31 memberService = SystemInitialization.getApplicationContext().getBean(MemberServiceV31.class);
			JSONObject obj = memberService.savePlayVideo(userId, videoId, albumId, platform, version, terminalNumber, ip,getContext(),playerType);
			obj.put("videoId", videoId);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "播放视频成功！", obj, this);
		} catch (Exception e) {
			logger.error("memberPlayVideo() error , " +
					"HeadInfo:"+ getContext().getHead().toString(),e);
			return getExceptionExecuteResult(e);
		}
	}
	
}
