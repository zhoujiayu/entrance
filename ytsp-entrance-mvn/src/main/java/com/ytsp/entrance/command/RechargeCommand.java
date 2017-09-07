package com.ytsp.entrance.command;

import org.json.JSONObject;

import com.ytsp.entrance.command.base.AbstractCommand;
import com.ytsp.entrance.command.base.CommandList;
import com.ytsp.entrance.command.base.ExecuteResult;
import com.ytsp.entrance.service.RechargeHistoryService;
import com.ytsp.entrance.system.SessionCustomer;
import com.ytsp.entrance.system.SystemInitialization;
import com.ytsp.entrance.util.WebUtils;

/**
 * @author GENE
 * @description 充值列表
 * 
 */
public class RechargeCommand extends AbstractCommand {

	@Override
	public boolean canExecute() {
		int code = getContext().getHead().getCommandCode();
		return CommandList.CMD_RECHARGE_COUNT == code || CommandList.CMD_RECHARGE_LIST == code;
	}

	@Override
	public ExecuteResult execute() {
		try {
			int code = getContext().getHead().getCommandCode();
			if (CommandList.CMD_RECHARGE_COUNT == code) {
				return countRecharge();

			} else if (CommandList.CMD_RECHARGE_LIST == code) {
				return listRecharge();

			} 
		} catch (Exception e) {
			logger.error("execute() error," +
					" HeadInfo :"+getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}

		return null;

	}

	private ExecuteResult countRecharge() throws Exception {
		SessionCustomer sc = getSessionCustomer();
		if (sc == null || sc.getCustomer() == null) {
			return getNoPermissionExecuteResult();
		}

		RechargeHistoryService rs = SystemInitialization.getApplicationContext().getBean(RechargeHistoryService.class);
		int count = rs.getRechargeHistoryCount(sc.getCustomer().getId());
		JSONObject obj = new JSONObject();
		obj.put("count", count);

		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取充值列表记录数成功", obj, this);
	}

	private ExecuteResult listRecharge() throws Exception {
		SessionCustomer sc = getSessionCustomer();
		if (sc == null || sc.getCustomer() == null) {
			return getNoPermissionExecuteResult();
		}

//		JSONObject jsonObj = getContext().getBody().getBodyObject();
//		int start = jsonObj.getInt("start");
//		int limit = jsonObj.getInt("limit");

		String hardwareNumber = getContext().getHead().getUniqueId();
		RechargeHistoryService rs = SystemInitialization.getApplicationContext().getBean(RechargeHistoryService.class);
		JSONObject obj = rs.getRechargeHistoryArray(sc.getCustomer(), WebUtils.getBasePath(this.getRequest()), hardwareNumber);

		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "获取充值列表成功", obj, this);
	}
}
