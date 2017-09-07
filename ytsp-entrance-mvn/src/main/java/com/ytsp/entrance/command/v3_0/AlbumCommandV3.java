package com.ytsp.entrance.command.v3_0;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.ytsp.entrance.command.base.AbstractCommand;
import com.ytsp.entrance.command.base.CommandList;
import com.ytsp.entrance.command.base.ExecuteResult;
import com.ytsp.entrance.command.base.HeadInfo;
import com.ytsp.entrance.service.AlbumService;
import com.ytsp.entrance.service.v3_0.AlbumServiceV3;
import com.ytsp.entrance.system.SystemInitialization;

/**
 * 在之前版本的基础上增加了对专辑的tag功能
 * @author GENE
 * @description 专辑列表
 * 
 */
public class AlbumCommandV3 extends AbstractCommand {

	@Override
	public boolean canExecute() {
		int code = getContext().getHead().getCommandCode();
		return CommandList.CMD_TAG_ALBUM_LIST == code || 
				CommandList.CMD_TAG_ALBUM_COUNT == code ||
				CommandList.CMD_ALBUM_TOPLIST_LIST_V3 == code;
	}

	@Override
	public ExecuteResult execute() {
		try {
			int code = getContext().getHead().getCommandCode();
			if (CommandList.CMD_TAG_ALBUM_COUNT == code) {
				return countTagedAlbum();
			} else if (CommandList.CMD_TAG_ALBUM_LIST == code) {
				return listTagedAlbum();
			} else if (CommandList.CMD_ALBUM_TOPLIST_LIST_V3 == code) {
				return topListAlbum();
			}
		} catch (Exception e) {
			logger.error("execute() error," +
					" HeadInfo :"+getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}
		return null;
	}

	private ExecuteResult countTagedAlbum() throws Exception {
		try {
			HeadInfo head = getContext().getHead();
			String platform = head.getPlatform();
			JSONObject jsonObj = getContext().getBody().getBodyObject();
			
			String tag = jsonObj.getString("tag_id");
			JSONArray tagIds = new JSONArray(tag);
			
			List<String> lst = new ArrayList<String>();
			for (int i = 0; i < tagIds.length(); i++) {
				if(tagIds.getString(i)!=null&&!tagIds.getString(i).equals(""))
					lst.add(tagIds.getString(i));
			}
			int cid = jsonObj.getInt("cid");
			AlbumServiceV3 rs = SystemInitialization.getApplicationContext().getBean(AlbumServiceV3.class);
			int count = rs.getTagedAlbumCount(platform, cid, lst);
			JSONObject obj = new JSONObject();
			obj.put("count", count);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取taged专辑列表记录数成功", obj, this);
		} catch (Exception e) {
			logger.error("countTagedAlbum() error," +
					" HeadInfo :"+getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}
		
	}

	private ExecuteResult listTagedAlbum() throws Exception {
		try {
			HeadInfo head = getContext().getHead();
			String platform = head.getPlatform();
			JSONObject jsonObj = getContext().getBody().getBodyObject();
			String tag = jsonObj.getString("tag_id");
			
			JSONArray tagIds = new JSONArray(tag);
			List<String> lst = new ArrayList<String>();
			for (int i = 0; i < tagIds.length(); i++) {
				if(tagIds.getString(i)!=null&&!tagIds.getString(i).equals(""))
					lst.add(tagIds.getString(i));
			}
			int cid = jsonObj.getInt("cid");
			String mode = "";
			if(!jsonObj.isNull("mode"))
			{
				mode = jsonObj.getString("mode");
			}
			int start = jsonObj.getInt("start");
			int limit = jsonObj.getInt("limit");
			AlbumServiceV3 rs = SystemInitialization.getApplicationContext().getBean(AlbumServiceV3.class);
			JSONArray array = rs.getTagedAlbumArray(platform, cid, lst, start, limit, mode);
			JSONObject obj = new JSONObject();
			obj.put("albumList", array);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取taged专辑列表成功", obj, this);
		} catch (Exception e) {
			logger.error("listTagedAlbum() error," +
					" HeadInfo :"+getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}
	}
	
	private ExecuteResult topListAlbum() throws Exception {
		HeadInfo head = getContext().getHead();
		String platform = head.getPlatform();
		String version = head.getVersion();
		AlbumService rs = SystemInitialization.getApplicationContext().getBean(AlbumService.class);
		JSONArray array = rs.getAlbumTopListArray(platform, 0,20,version);
		JSONObject obj = new JSONObject();
		obj.put("albumList", array);
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取专辑排序列表成功", obj, this);
	}
}
