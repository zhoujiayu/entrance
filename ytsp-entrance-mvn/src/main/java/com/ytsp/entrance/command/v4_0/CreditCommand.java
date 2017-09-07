package com.ytsp.entrance.command.v4_0;

import org.json.JSONException;
import org.json.JSONObject;

import com.ytsp.db.domain.Customer;
import com.ytsp.db.exception.SqlException;
import com.ytsp.entrance.command.base.AbstractCommand;
import com.ytsp.entrance.command.base.CommandList;
import com.ytsp.entrance.command.base.ExecuteResult;
import com.ytsp.entrance.service.v4_0.CreditService;
import com.ytsp.entrance.system.SessionCustomer;
import com.ytsp.entrance.system.SystemInitialization;
import com.ytsp.entrance.util.Util;

public class CreditCommand extends AbstractCommand{

	@Override
	public boolean canExecute() {
		int code = getContext().getHead().getCommandCode();
		return (code == CommandList.CMD_CREDIT_POLICY||
				code == CommandList.CMD_CREDIT_BYUID||
				code == CommandList.CMD_CREDIT_ADD);
	}

	@Override
	public ExecuteResult execute() {
		int code = getContext().getHead().getCommandCode();
		try {
			if(code == CommandList.CMD_CREDIT_POLICY)
				return getCreditPolicy();
			if(code == CommandList.CMD_CREDIT_BYUID)
				return getCreditByUser();
			if(code == CommandList.CMD_CREDIT_ADD)
				return creditAdd();
		} catch (Exception e) {
			logger.error("execute() error," +
					" HeadInfo :"+getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}
		return null;
	}

	private ExecuteResult creditAdd() throws JSONException, SqlException {
		CreditService cs = SystemInitialization.getApplicationContext().getBean(CreditService.class);
		int uid = getContext().getHead().getUid();//UID由客户端传递过来,与当前用户的session中的用户ID做比对
		SessionCustomer sc = getSessionCustomer();
		if (sc == null || sc.getCustomer() == null) {
			return getNoPermissionExecuteResult();
		}
//		int num = getContext().getBody().getBodyObject().getInt("creditsAdd");
		int num = getContext().getBody().getBodyObject().optInt("creditsAdd",0);
		String action = getContext().getBody().getBodyObject().optString("action");
		//判断操作的用户与当前的session中用户是否一致.
		Customer customer = sc.getCustomer();
		if (uid == 0 || customer.getId().intValue() != uid) {
			return getNoPermissionExecuteResult();
		}
		cs.transactionCreditAdd(uid, num,action);
		return  new ExecuteResult(CommandList.RESPONSE_STATUS_OK, 
				"用户增加积分成功",null, this);
	}

	private ExecuteResult getCreditByUser() throws SqlException, JSONException {
		CreditService cs = SystemInitialization.getApplicationContext().getBean(CreditService.class);
		int uid = getContext().getHead().getUid();//UID由客户端传递过来,与当前用户的session中的用户ID做比对
		SessionCustomer sc = getSessionCustomer();
		if (sc == null || sc.getCustomer() == null) {
			return getNoPermissionExecuteResult();
		}
		//判断操作的用户与当前的session中用户是否一致.
		Customer customer = sc.getCustomer();
		if (uid == 0 || customer.getId().intValue() != uid) {
			return getNoPermissionExecuteResult();
		}
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, 
				"获取用户积分成功", new JSONObject().put("credits", cs.getCreditByUser(uid)), this); 
	}

	private ExecuteResult getCreditPolicy() throws Exception {
		CreditService cs = SystemInitialization.getApplicationContext().getBean(CreditService.class);
		JSONObject result = cs.getCreditPolicy();
		//添加统计内容
		Util.addStatistics(getContext(), result.toString());
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取积分策略列表成功", result, this); 
	}

}
