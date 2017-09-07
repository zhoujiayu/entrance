package com.ytsp.entrance.command.v4_0;

import java.util.Date;

import org.json.JSONObject;

import com.ytsp.db.domain.FeedbackRecord;
import com.ytsp.entrance.command.base.AbstractCommand;
import com.ytsp.entrance.command.base.CommandList;
import com.ytsp.entrance.command.base.ExecuteResult;
import com.ytsp.entrance.service.v4_0.FeedbackService;
import com.ytsp.entrance.system.SystemInitialization;

public class FeedbackCommand extends AbstractCommand {

	@Override
	public boolean canExecute() {
		int code = getContext().getHead().getCommandCode();
		return CommandList.CMD_SAVE_FEEDBACK == code;
	}

	@Override
	public ExecuteResult execute() {
		return saveFeedBack();
	}

	/**
	 * 保存反馈
	 * @return
	 * @throws Exception
	 */
	private ExecuteResult saveFeedBack() {
		try {
			FeedbackService feedbackService = SystemInitialization.getApplicationContext().getBean(FeedbackService.class);
			int userId = getContext().getHead().getUid();
			JSONObject jsonObj = getContext().getBody().getBodyObject();
			String content = jsonObj.getString("content");
			String mobile="";
			int feedbackType = 0;
			if(!jsonObj.isNull("mobile"))
				mobile= jsonObj.getString("mobile");
			String email="";
			if(!jsonObj.isNull("email"))
				email= jsonObj.getString("email");
			if(!jsonObj.isNull("feedbackType"))
				feedbackType = jsonObj.getInt("feedbackType");
			String ip = getContext().getHead().getIp();
			String plat = getContext().getHead().getPlatform();
			String version = getContext().getHead().getVersion();
			FeedbackRecord feedbackRecord = new FeedbackRecord();
			feedbackRecord.setContent(content);
			feedbackRecord.setMobile(mobile);
			feedbackRecord.setEmail(email);
			feedbackRecord.setCreateTime(new Date());
			feedbackRecord.setIp(ip);
			feedbackRecord.setFeedbackType(feedbackType);
			feedbackRecord.setPlatform(plat);
			feedbackRecord.setVersion(version);
			feedbackService.saveFeedBack(userId,feedbackRecord);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "保存反馈成功！", null, this);
		} catch (Exception e) {
			return getExceptionExecuteResult(e);
		}
	}
	
}
