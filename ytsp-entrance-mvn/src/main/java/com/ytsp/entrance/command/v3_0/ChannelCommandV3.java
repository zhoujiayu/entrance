package com.ytsp.entrance.command.v3_0;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.ytsp.db.domain.Channel;
import com.ytsp.entrance.command.base.AbstractCommand;
import com.ytsp.entrance.command.base.CommandList;
import com.ytsp.entrance.command.base.ExecuteResult;
import com.ytsp.entrance.service.v3_0.ChannelServiceV3;
import com.ytsp.entrance.system.SystemInitialization;

public class ChannelCommandV3  extends AbstractCommand {

	@Override
	public boolean canExecute() {
		int code = getContext().getHead().getCommandCode();
		return CommandList.CMD_FORUM_LIST == code;
	}

	@Override
	public ExecuteResult execute() {
		try {
			ChannelServiceV3 rs = SystemInitialization.getApplicationContext().getBean(ChannelServiceV3.class);
			List<Channel> channels = rs.getAllChannelsOrderByWeight();
			JSONArray array = new JSONArray();
			for(Channel channel : channels){
				if(channel.getParent() == null || channel.getParent() <= 0){
					continue;//过滤根节点
				}
				JSONObject obj = new JSONObject();
				obj.put("name", channel.getName());
				obj.put("cid", channel.getId());
				array.put(obj);
			}
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
					"获取channel列表成功", array, this);
		} catch (Exception e) {
			logger.error("execute() error," +
					" HeadInfo :"+getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}
	}
}
