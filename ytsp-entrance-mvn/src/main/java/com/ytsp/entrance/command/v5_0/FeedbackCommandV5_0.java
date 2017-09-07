package com.ytsp.entrance.command.v5_0;

import java.util.Date;

import org.json.JSONObject;

import com.ytsp.db.domain.FeedbackRecord;
import com.ytsp.entrance.command.base.AbstractCommand;
import com.ytsp.entrance.command.base.CommandList;
import com.ytsp.entrance.command.base.ExecuteResult;
import com.ytsp.entrance.service.v4_0.FeedbackService;
import com.ytsp.entrance.system.SystemInitialization;
import com.ytsp.entrance.util.Util;
import com.ytsp.entrance.util.VerifyClientCustomer;

public class FeedbackCommandV5_0 extends AbstractCommand {

	@Override
	public boolean canExecute() {
		int code = getContext().getHead().getCommandCode();
		return CommandList.CMD_FEEDBACK_SAVE == code;
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
			JSONObject reqBody = getContext().getBody().getBodyObject();
			String content = reqBody.getString("content");
			String mobile="";
			int feedbackType = 0;
			if(!reqBody.isNull("mobile")){
				mobile= reqBody.getString("mobile");
				//校验11位手机号是否正确
				if(!VerifyClientCustomer.validateCellphone(mobile)){
					return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "手机号格式不正确！", null, this);
				}
			}
			String email="";
			if(!reqBody.isNull("email")){
				email= reqBody.getString("email");
				if(!VerifyClientCustomer.emailValidate(email)){
					return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "不是有效的电子邮箱格式！", null, this);
				}
			}
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
			Util.addStatistics(getContext(), feedbackRecord);
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "保存反馈成功！", null, this);
		} catch (Exception e) {
			return getExceptionExecuteResult(e);
		}
	}
	
}
