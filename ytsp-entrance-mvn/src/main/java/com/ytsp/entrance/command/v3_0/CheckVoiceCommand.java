package com.ytsp.entrance.command.v3_0;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.ytsp.db.domain.Voice;
import com.ytsp.entrance.command.base.AbstractCommand;
import com.ytsp.entrance.command.base.CommandList;
import com.ytsp.entrance.command.base.ExecuteResult;
import com.ytsp.entrance.service.v3_0.VoiceService;
import com.ytsp.entrance.system.SystemInitialization;

public class CheckVoiceCommand extends AbstractCommand {

	@Override
	public boolean canExecute() {
		int code = getContext().getHead().getCommandCode();
		return CommandList.CMD_CHECK_VOICE == code;
	}

	@Override
	public ExecuteResult execute() {
		VoiceService vs = SystemInitialization.getApplicationContext().getBean(VoiceService.class);
		try {
			List<Voice> ls = vs.getAllVoice();
			JSONArray array = new JSONArray();
			for(Voice voice : ls){
				JSONObject obj = new JSONObject();
				obj.put("id", voice.getId());
				obj.put("addr", voice.getAddr());
				obj.put("md5", voice.getMd5());
				array.put(obj);
			}
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, 
					"获取voice列表成功", array, this);
		} catch (Exception e) {
			logger.error("execute() error," +
					" HeadInfo :"+getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}
	}

}
