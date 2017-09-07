package com.ytsp.entrance.command.v4_0;

import org.json.JSONArray;
import org.json.JSONObject;

import com.ytsp.entrance.command.base.AbstractCommand;
import com.ytsp.entrance.command.base.CommandList;
import com.ytsp.entrance.command.base.ExecuteResult;
import com.ytsp.entrance.service.AlbumService;
import com.ytsp.entrance.service.MemberService;
import com.ytsp.entrance.system.SessionCustomer;
import com.ytsp.entrance.system.SystemInitialization;

/**
 * @author GENE
 * @description 专辑列表
 * 
 */
public class AlbumCommandV4 extends AbstractCommand {

	@Override
	public boolean canExecute() {
		int code = getContext().getHead().getCommandCode();
		return  CommandList.CMD_ALBUM_DETAIL == code;
	}

	@Override
	public ExecuteResult execute() {
		try {
			int code = getContext().getHead().getCommandCode();
			if (CommandList.CMD_ALBUM_DETAIL == code) {
				return oneAlbum();
			}
		} catch (Exception e) {
			logger.error("execute() error," +
					" HeadInfo :"+getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}
		return null;
	}

	
	private ExecuteResult oneAlbum() throws Exception {
		try {
			String plat = getContext().getHead().getPlatform();
			String version = getContext().getHead().getVersion();
			JSONObject jsonObj = getContext().getBody().getBodyObject();
			if(jsonObj.isNull("aid"))
			{
				return new ExecuteResult(CommandList.RESPONSE_STATUS_BODY_JSON_ERROR, "请求体异常,aid为空", null, this);
			}
			int aid = jsonObj.getInt("aid");
			AlbumService rs = SystemInitialization.getApplicationContext().getBean(AlbumService.class);
			MemberService ms = SystemInitialization.getApplicationContext().getBean(MemberService.class);
			JSONObject obj = rs.getAlbumJson(aid);
			int uid = getContext().getHead().getUid();//UID由客户端传递过来,与当前用户的session中的用户ID做比对
			SessionCustomer sc = getSessionCustomer();
			if (sc!=null && sc.getCustomer()!=null
					&&uid!=0&&sc.getCustomer().getId().intValue() == uid) {
				obj.put("credits", sc.getCustomer().getCredits()) ;
				JSONObject foo = ms.memberCheck(uid);
				obj.put("memberType",foo.get("memberType"));
				obj.put("endTime", foo.get("endTime"));
			}
			int start = 0;
			int limit = -1;
			JSONArray array = ms.getMemberVideos(uid, aid, plat, version, start, limit);
			obj.put("videoList", array);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取专辑信息成功", obj, this);
		} catch (Exception e) {
			logger.error("oneAlbum() error," +
					" HeadInfo :"+getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}
		
	}
	
}
