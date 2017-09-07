package com.ytsp.entrance.command;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.ytsp.db.domain.Channel;
import com.ytsp.db.enums.MobileTypeEnum;
import com.ytsp.entrance.command.base.AbstractCommand;
import com.ytsp.entrance.command.base.CommandList;
import com.ytsp.entrance.command.base.ExecuteResult;
import com.ytsp.entrance.service.ChannelService;
import com.ytsp.entrance.system.SystemInitialization;

/**
 * @author GENE
 * @description 栏目列表
 * 
 */
public class ChannelCommand extends AbstractCommand {

	@Override
	public boolean canExecute() {
		int code = getContext().getHead().getCommandCode();
		return CommandList.CMD_CHANNEL_LIST == code;
	}

	@Override
	public ExecuteResult execute() {
		try {
			int code = getContext().getHead().getCommandCode();
			if (CommandList.CMD_CHANNEL_LIST == code) {
				return channelList();

			} 
		} catch (Exception e) {
			logger.error("execute() error," +
					" HeadInfo :"+getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}

		return null;
	}


	private ExecuteResult channelList() throws Exception {
//		SessionCustomer sc = getSessionCustomer();
//		if (sc == null || sc.getCustomer() == null) {
//			return getNoPermissionExecuteResult();
//		}
		String platform = getContext().getHead().getPlatform();
		String otherInfo = getContext().getHead().getOtherInfo();
		
		ChannelService rs = SystemInitialization.getApplicationContext().getBean(ChannelService.class);
		List<Channel> channels = rs.getAllChannelsOrderByWeight();
		JSONArray array = new JSONArray();
		
		if(MobileTypeEnum.valueOf(platform) == MobileTypeEnum.gpadtv )
		{
			for(Channel channel : channels){
				if(channel.getParent() == null || channel.getParent() <= 0){
					continue;//过滤根节点
				}
				if(channel.getId() == 3 || channel.getId() == 9 || channel.getId() == 11){//国产经典老片,电视少儿节目去掉,儿童益智早教,世界童话名著
					continue;//过滤根节点
				}
				JSONObject obj = new JSONObject();
				obj.put("name", channel.getName());
				obj.put("cid", channel.getId());
				array.put(obj);
			}
		}
		else
		{
			for(Channel channel : channels){
				if(channel.getParent() == null || channel.getParent() <= 0){
					continue;//过滤根节点
				}
				
				JSONObject obj = new JSONObject();
				obj.put("name", channel.getName());
				obj.put("cid", channel.getId());
				array.put(obj);
			}
		}
	
		
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取栏目列表成功", array, this);
	}
	
	

}
