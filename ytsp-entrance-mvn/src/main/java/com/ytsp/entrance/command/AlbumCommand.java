package com.ytsp.entrance.command;

import org.json.JSONArray;
import org.json.JSONObject;

import com.ytsp.db.domain.Album;
import com.ytsp.entrance.command.base.AbstractCommand;
import com.ytsp.entrance.command.base.CommandList;
import com.ytsp.entrance.command.base.ExecuteResult;
import com.ytsp.entrance.command.base.HeadInfo;
import com.ytsp.entrance.service.AlbumService;
import com.ytsp.entrance.system.SystemInitialization;

/**
 * @author GENE
 * @description 专辑列表
 * 
 */
public class AlbumCommand extends AbstractCommand {

	@Override
	public boolean canExecute() {
		int code = getContext().getHead().getCommandCode();
		return CommandList.CMD_ALBUM_LIST == code || CommandList.CMD_ALBUM_COUNT == code || 
		CommandList.CMD_ALBUM == code || CommandList.CMD_ALBUM_SAVE_SCORE == code;
	}

	@Override
	public ExecuteResult execute() {
		try {
			int code = getContext().getHead().getCommandCode();
			if (CommandList.CMD_ALBUM_COUNT == code) {
				return countAlbum();

			} else if (CommandList.CMD_ALBUM_LIST == code) {
				return listAlbum();

			} else if (CommandList.CMD_ALBUM == code) {
				return oneAlbum();

			} else if (CommandList.CMD_ALBUM_SAVE_SCORE == code) {
				return saveAlbumScore();
			}
		} catch (Exception e) {
			logger.error("execute() error," +
					" HeadInfo :"+getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}
		return null;
	}

	private ExecuteResult countAlbum() throws Exception {
		try {
			HeadInfo head = getContext().getHead();
			String platform = head.getPlatform();
			String version = head.getVersion();
			JSONObject jsonObj = getContext().getBody().getBodyObject();
			int cid =0;
			if(!jsonObj.isNull("cid"))
				cid = jsonObj.optInt("cid");
			String searchName = jsonObj.optString("searchName");
			AlbumService rs = SystemInitialization.getApplicationContext().getBean(AlbumService.class);
			int count = rs.getAlbumCount(platform, cid, searchName,version);
			JSONObject obj = new JSONObject();
			obj.put("count", count);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取专辑列表记录数成功", obj, this);
		} catch (Exception e) {
			logger.error("countAlbum() error," +
					" HeadInfo :"+getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}
		
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
			if(!jsonObj.isNull("mode"))
			{
				mode = jsonObj.getString("mode");
			}
			String searchName = null;
			if(!jsonObj.isNull("searchName"))
			{
				searchName = jsonObj.getString("searchName");
			}
			int start = jsonObj.optInt("start",0);
			int limit = jsonObj.optInt("limit");
			AlbumService rs = SystemInitialization.getApplicationContext().getBean(AlbumService.class);
			JSONArray array = rs.getAlbumArray(platform, cid, searchName, start, limit, mode,version);
			JSONObject obj = new JSONObject();
			obj.put("albumList", array);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取专辑列表成功", obj, this);
		} catch (Exception e) {
			logger.error("listAlbum() error," +
					" HeadInfo :"+getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}
	}
	
	private ExecuteResult oneAlbum() throws Exception {
		try {
//			String plat = getContext().getHead().getPlatform();
			JSONObject jsonObj = getContext().getBody().getBodyObject();
			if(jsonObj.isNull("aid"))
			{
				return new ExecuteResult(CommandList.RESPONSE_STATUS_BODY_JSON_ERROR, "请求体异常,aid为空", null, this);
			}
			int aid = jsonObj.getInt("aid");
			AlbumService rs = SystemInitialization.getApplicationContext().getBean(AlbumService.class);
			JSONObject obj = rs.getAlbumJson(aid);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取专辑信息成功", obj, this);
		} catch (Exception e) {
			logger.error("oneAlbum() error," +
					" HeadInfo :"+getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}
		
	}
	
	private ExecuteResult saveAlbumScore() throws Exception {
		try {
			JSONObject jsonObj = getContext().getBody().getBodyObject();
			int aid = jsonObj.getInt("aid");
			double score = jsonObj.getDouble("score");
			AlbumService rs = SystemInitialization.getApplicationContext().getBean(AlbumService.class);
			Album album = rs.findAlbumById(aid);
			if(album != null){
				double _score = album.getScore() == null ? 0 : album.getScore();
				int _count = album.getScoreCount();
				double _all = _score * _count + score;
				_count++;
				int new_score = Integer.valueOf(String.valueOf(Math.round(_all / _count)));
				new_score = new_score > 10 ? 10 : new_score;
				album.setScore(new_score/1.0);
				album.setScoreCount(_count);
				rs.updateAlbum(album);
				return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "专辑评分成功", null, this);
			}else{
				return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "该专辑不存在", null, this);
			}
		} catch (Exception e) {
			logger.error("saveAlbumScore() error," +
					" HeadInfo :"+getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}
	}
}
