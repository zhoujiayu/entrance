package com.ytsp.entrance.command;

import java.util.Date;

import org.json.JSONObject;

import com.ytsp.db.dao.EbActivityDao;
import com.ytsp.db.dao.PushMessageDao;
import com.ytsp.db.domain.EbActivity;
import com.ytsp.db.domain.PushMessage;
import com.ytsp.db.enums.MessageTypeEnum;
import com.ytsp.db.exception.SqlException;
import com.ytsp.entrance.command.base.AbstractCommand;
import com.ytsp.entrance.command.base.CommandList;
import com.ytsp.entrance.command.base.ExecuteResult;
import com.ytsp.entrance.system.SystemInitialization;
import com.ytsp.entrance.util.Util;

/**
 * @author GENE
 * @description 推送消息命令
 * 
 */
public class PushMessageCommand extends AbstractCommand {

	@Override
	public boolean canExecute() {
		int code = getContext().getHead().getCommandCode();
		return CommandList.CMD_PUSHMESSAGE_LAST == code
				|| CommandList.CMD_PUSHMESSAGE_LAST_V5 == code;
	}

	@Override
	public ExecuteResult execute() {
		try {
			int code = getContext().getHead().getCommandCode();
			if (CommandList.CMD_PUSHMESSAGE_LAST == code)
				return lastMeg();
			else if (CommandList.CMD_PUSHMESSAGE_LAST_V5 == code) {
				return getPushMessageV5_0();
			}
		} catch (Exception e) {
			logger.error("execute() error," + " HeadInfo :"
					+ getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}
		return null;
	}

	/**
	* <p>功能描述:获取推送消息</p>
	* <p>参数：@return
	* <p>参数：@throws Exception</p>
	* <p>返回类型：ExecuteResult</p>
	 */
	private ExecuteResult getPushMessageV5_0() throws Exception {
		String platform = getContext().getHead().getPlatform();
		String version = getContext().getHead().getVersion();
		if (platform.equals("gphone")
				&& VersionCommand.convert2Num(version) <= 205000) {
			JSONObject obj = new JSONObject();
			obj.put("id", 0);
			obj.put("content", "您的版本已过期，请更新版本以观看影片，更有折扣品牌玩具和各种折扣精品玩具在等着您！");
			obj.put("redirect", "http://images.ikan.cn/download/phone/ikan.apk");
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
					"获取最新推送信息成功！", obj, this);
		}
		if (platform.equals("gpad")
				&& VersionCommand.convert2Num(version) <= 205000) {
			JSONObject obj = new JSONObject();
			obj.put("id", 0);
			obj.put("content", "您的版本已过期，请更新版本以观看影片，更有折扣品牌玩具和各种折扣精品玩具在等着您！");
			obj.put("redirect", "http://images.ikan.cn/download/pad/ikan.apk");
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
					"获取最新推送信息成功！", obj, this);
		}
		JSONObject reqBody = getContext().getBody().getBodyObject();
		if (reqBody.isNull("lastId")) {
			return new ExecuteResult(
					CommandList.RESPONSE_STATUS_BODY_JSON_ERROR, "请求体错误！",
					null, this);
		}
		int id = reqBody.getInt("lastId");
		PushMessage message = getNextPushMessage(id);
		JSONObject result = new JSONObject();
		if (message != null) {
			result.put("id", message.getId());
			result.put("redirect", message.getRedirect());
			result.put("content", message.getContent());
			result.put("title", message.getTitle());
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
					"获取最新推送信息成功！", result, this);
		}
		Util.addStatistics(getContext(), message);
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "目前无最新推送信息！",
				null, this);
	}


	/**
	 * <p>
	 * 功能描述:获取下一个推送消息
	 * </p>
	 * <p>
	 * 参数：@param id
	 * <p>
	 * 参数：@return
	 * <p>
	 * 参数：@throws SqlException
	 * </p>
	 * <p>
	 * 返回类型：PushMessage
	 * </p>
	 */
	private PushMessage getNextPushMessage(int id) throws SqlException {
		PushMessageDao pmd = SystemInitialization.getApplicationContext()
				.getBean(PushMessageDao.class);
		Date now = new Date();
		PushMessage message = pmd
				.findOneByHql(
						" WHERE id > ? and sendTime < ? and exceedTime > ? ORDER BY id DESC",
						new Object[] { id, now, now });
		return message;
	}

	private ExecuteResult lastMeg() throws Exception {
		// SessionCustomer sc = getSessionCustomer();
		// if (sc == null || sc.getCustomer() == null) {
		// return getNoPermissionExecuteResult();
		// }
		String platform = getContext().getHead().getPlatform();
		String version = getContext().getHead().getVersion();
		if (platform.equals("gphone")
				&& VersionCommand.convert2Num(version) <= 205000) {
			JSONObject obj = new JSONObject();
			obj.put("id", 0);
			obj.put("content", "您的版本已过期，请更新版本以观看影片，更有折扣品牌玩具和各种折扣精品玩具在等着您！");
			obj.put("redirect", "http://images.ikan.cn/download/phone/ikan.apk");
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
					"获取最新推送信息成功！", obj, this);
		}
		if (platform.equals("gpad")
				&& VersionCommand.convert2Num(version) <= 205000) {
			JSONObject obj = new JSONObject();
			obj.put("id", 0);
			obj.put("content", "您的版本已过期，请更新版本以观看影片，更有折扣品牌玩具和各种折扣精品玩具在等着您！");
			obj.put("redirect", "http://images.ikan.cn/download/pad/ikan.apk");
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
					"获取最新推送信息成功！", obj, this);
		}
		JSONObject jsonObj = getContext().getBody().getBodyObject();
		if (jsonObj.isNull("lastid")) {
			return new ExecuteResult(
					CommandList.RESPONSE_STATUS_BODY_JSON_ERROR, "请求体错误！",
					null, this);
		}
		int id = jsonObj.getInt("lastid");
		Date now = new Date();
		PushMessageDao pmd = SystemInitialization.getApplicationContext()
				.getBean(PushMessageDao.class);
		PushMessage message = null;
		message = pmd
				.findOneByHql(
						" WHERE id>? and sendTime < ? and exceedTime > ? ORDER BY id DESC",
						new Object[] { id, now, now });
		JSONObject obj = new JSONObject();
		if (message != null) {
			obj.put("id", message.getId());
			obj.put("type", message.getType().getValue());
			obj.put("title", message.getTitle());
			obj.put("content", message.getContent());
			obj.put("params", message.getParams());
			if (message.getType().equals(MessageTypeEnum.ebactivity)) {
				EbActivityDao ebActivityDao = SystemInitialization
						.getApplicationContext().getBean(EbActivityDao.class);
				EbActivity ebActivity = ebActivityDao.findOneByHql(
						" WHERE activityId=? ",
						new Object[] { Integer.valueOf(message.getParams()) });
				obj.put("ebActivityName", ebActivity.getActivityName());
			}
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK,
					"获取最新推送信息成功！", obj, this);
		}
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "目前无最新推送信息！",
				null, this);
	}
}
