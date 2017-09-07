package com.ytsp.entrance.command;

import org.json.JSONArray;
import org.json.JSONObject;

import com.ytsp.entrance.command.base.AbstractCommand;
import com.ytsp.entrance.command.base.CommandList;
import com.ytsp.entrance.command.base.ExecuteResult;
import com.ytsp.entrance.command.base.HeadInfo;
import com.ytsp.entrance.service.AlbumService;
import com.ytsp.entrance.system.SystemInitialization;

/**
 * @author GENE
 * @description 专辑排序列表
 * 
 */
public class AlbumTopListCommand extends AbstractCommand {

	@Override
	public boolean canExecute() {
		int code = getContext().getHead().getCommandCode();
		return CommandList.CMD_ALBUM_TOPLIST_LIST == code || CommandList.CMD_ALBUM_TOPLIST_COUNT == code;
	}

	@Override
	public ExecuteResult execute() {
		try {
			int code = getContext().getHead().getCommandCode();
			if (CommandList.CMD_ALBUM_TOPLIST_COUNT == code) {
				return countAlbum();
			} else if (CommandList.CMD_ALBUM_TOPLIST_LIST == code) {
				return listAlbum();
			} 
		} catch (Exception e) {
			logger.error("execute() error," +
					" HeadInfo :"+getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}
		return null;
	}

	private ExecuteResult countAlbum() throws Exception {
//		SessionCustomer sc = getSessionCustomer();
//		if (sc == null || sc.getCustomer() == null) {
//			return getNoPermissionExecuteResult();
//		}
		
		HeadInfo head = getContext().getHead();
		String platform = head.getPlatform();
		String version = head.getVersion();
		AlbumService rs = SystemInitialization.getApplicationContext().getBean(AlbumService.class);
		int count = rs.getAlbumTopListCount(platform,version);
		JSONObject obj = new JSONObject();
		obj.put("count", count);

		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取专辑排序列表记录数成功", obj, this);
	}

	private ExecuteResult listAlbum() throws Exception {
		HeadInfo head = getContext().getHead();
		String platform = head.getPlatform();
		String version = head.getVersion();
		JSONObject jsonObj = getContext().getBody().getBodyObject();
		int start = 0;
		int limit = -1;
		if(!jsonObj.isNull("start"))
		{
			start = jsonObj.getInt("start");
		}
		if(!jsonObj.isNull("limit"))
		{
			limit = jsonObj.getInt("limit");
		}
		AlbumService rs = SystemInitialization.getApplicationContext().getBean(AlbumService.class);
		JSONArray array = rs.getAlbumTopListArray(platform, start, limit,version);
		JSONObject obj = new JSONObject();
		obj.put("albumList", array);

		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取专辑排序列表成功", obj, this);
	}
	
}
