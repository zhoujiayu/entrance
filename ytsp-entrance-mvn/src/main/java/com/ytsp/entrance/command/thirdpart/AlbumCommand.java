package com.ytsp.entrance.command.thirdpart;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.ytsp.db.domain.Video;
import com.ytsp.entrance.command.VersionCommand;
import com.ytsp.entrance.command.base.AbstractCommand;
import com.ytsp.entrance.command.base.CommandList;
import com.ytsp.entrance.command.base.ExecuteResult;
import com.ytsp.entrance.command.base.HeadInfo;
import com.ytsp.entrance.service.AlbumService;
import com.ytsp.entrance.service.MemberService;
import com.ytsp.entrance.system.SystemInitialization;
import com.ytsp.entrance.system.SystemManager;
import com.ytsp.entrance.util.URLParse;

/**
 * @author GENE
 * @description 专辑列表
 * 
 */
public class AlbumCommand extends AbstractCommand {

	@Override
	public boolean canExecute() {
		int code = getContext().getHead().getCommandCode();
		return CommandList.CMD_ALBUM_LIST_THIRDPART == code || 
		CommandList.CMD_ALBUM_THIRDPART == code ;
	}

	@Override
	public ExecuteResult execute() {
		try {
			int code = getContext().getHead().getCommandCode();
			if (CommandList.CMD_ALBUM_LIST_THIRDPART == code) {
				return listAlbum();

			} else if (CommandList.CMD_ALBUM_THIRDPART == code) {
				return oneAlbum();

			}  
		} catch (Exception e) {
			logger.error("execute() error," +
					" HeadInfo :"+getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}
		return null;
	}

	private ExecuteResult listAlbum() throws Exception {
		try {
			HeadInfo head = getContext().getHead();
			String platform = head.getPlatform();
			String version = head.getVersion();
			JSONObject jsonObj = getContext().getBody().getBodyObject();
			int cid =0;
			if(!jsonObj.isNull("cid"))
				cid = jsonObj.getInt("cid");
			String mode = "";
			//妈妈网的
			if(!jsonObj.isNull("thirdpart")&&
					jsonObj.getString("thirdpart").equals("mamanet")){
				mode = "hot";
				platform = "ipad";
				version = "4.3.9";
			}
			int start = jsonObj.getInt("start");
			int limit = jsonObj.getInt("limit");
			AlbumService rs = SystemInitialization.getApplicationContext().getBean(AlbumService.class);
			JSONArray array = rs.getAlbumArray(platform, cid, "", start, limit, mode,version);
			JSONArray ret = new JSONArray();
			for (int i = 0; i < array.length(); i++) {
				JSONObject jo = array.getJSONObject(i);
				if(!jo.isNull("vip")&&!jo.getBoolean("vip"))
					ret.put(jo);
			}
			JSONObject obj = new JSONObject();
			obj.put("albumList", ret);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取专辑列表成功", obj, this);
		} catch (Exception e) {
			logger.error("listAlbum() error," +
					" HeadInfo :"+getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}
	}
	
	private ExecuteResult oneAlbum() throws Exception {
		try {
			JSONObject jsonObj = getContext().getBody().getBodyObject();
			if(jsonObj.isNull("aid"))
			{
				return new ExecuteResult(CommandList.RESPONSE_STATUS_BODY_JSON_ERROR, "请求体异常,aid为空", null, this);
			}
			int _version = VersionCommand.convert2Num(getContext().getHead().getVersion());
			
			int aid = jsonObj.getInt("aid");
			//妈妈网的
			if(!jsonObj.isNull("thirdpart")&&
					jsonObj.getString("thirdpart").equals("mamanet")){
				_version = 400000;
			}
			MemberService memberService = SystemInitialization.getApplicationContext().getBean(MemberService.class);
			List<Video> videos = memberService.getVideoList(aid, 0, 10000);
			JSONArray array = new JSONArray();
			for (int i =0;i<videos.size();i++) {
				Video video = videos.get(i);
				JSONObject obj = new JSONObject();
				obj.put("vid", video.getId());
				obj.put("name", video.getName() == null ? "" : video.getName());
				obj.put("snapshot", SystemManager.getInstance().getSystemConfig().getImgServerUrl() + video.getCover());
				String saveDir = video.getSaveDir() == null ? "" : video.getSaveDir();
				saveDir = saveDir.endsWith("/") ? saveDir : (saveDir + "/");
				if(_version>=400000){
					obj.put("v720", URLParse.makeURL(saveDir + video.getNumber() + "-720p.mp4"));
				}
				array.put(obj);
			}
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取专辑信息成功", array, this);
		} catch (Exception e) {
			logger.error("mamanet oneAlbum() error," +
					" HeadInfo :"+getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}
		
	}
	
}
