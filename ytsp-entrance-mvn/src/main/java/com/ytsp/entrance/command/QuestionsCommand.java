package com.ytsp.entrance.command;

import org.json.JSONArray;
import org.json.JSONObject;

import com.ytsp.entrance.command.base.AbstractCommand;
import com.ytsp.entrance.command.base.CommandList;
import com.ytsp.entrance.command.base.ExecuteResult;
import com.ytsp.entrance.service.QuestionsService;
import com.ytsp.entrance.system.SystemInitialization;

/**
 * @author GENE
 * @description 试题命令
 * 
 */
public class QuestionsCommand extends AbstractCommand {

	@Override
	public boolean canExecute() {
		return CommandList.CMD_QUESTIONS_RANDOM == getContext().getHead().getCommandCode();
	}

	@Override
	public ExecuteResult execute() {
		try {
			return randomQuestions();
		} catch (Exception e) {
			logger.error("execute() error," +
					" HeadInfo :"+getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}
	}

	private ExecuteResult randomQuestions() throws Exception {
//		SessionCustomer sc = getSessionCustomer();
//		if (sc == null || sc.getCustomer() == null) {
//			return getNoPermissionExecuteResult();
//		}

		JSONObject jsonObj = getContext().getBody().getBodyObject();
		int age = jsonObj.getInt("age");
		int count = jsonObj.getInt("count");

		QuestionsService qs = SystemInitialization.getApplicationContext().getBean(QuestionsService.class);
		JSONArray array = qs.getRandomQuestions(age, count);
		JSONObject obj = new JSONObject();
		obj.put("questionsList", array);
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取随机试题成功！", obj, this);
	}

}
