package com.ytsp.entrance.command.v5_0;

import java.util.List;

import org.json.JSONObject;

import com.ytsp.db.domain.ExceptionLog;
import com.ytsp.entrance.command.base.AbstractCommand;
import com.ytsp.entrance.command.base.CommandList;
import com.ytsp.entrance.command.base.ExecuteResult;
import com.ytsp.entrance.handleResponse.RestResponse;
import com.ytsp.entrance.service.v5_0.ExceptionLogService;
import com.ytsp.entrance.system.SystemInitialization;

public class ExceptionLogCommand extends AbstractCommand {

	@Override
	public boolean canExecute() {
		int code = getContext().getHead().getCommandCode();
		return (code == CommandList.CMD_EXCEPTION_LIST);
	}

	@Override
	public ExecuteResult execute() {
		int code = getContext().getHead().getCommandCode();
		try{
			if(code == CommandList.CMD_EXCEPTION_LIST){
				return getExceptionLogList();
			}
			return null;
		} catch (Exception e) {
			logger.error("ADCommandV5_0," + " HeadInfo :"
					+ getContext().getHead().toString(), e);
			return getExceptionExecuteResult(e);
		}
	}

	private ExecuteResult getExceptionLogList() throws Exception {
	
		JSONObject jsonObj = getContext().getBody().getBodyObject();
		RestResponse<List<ExceptionLog>> res  = new RestResponse<List<ExceptionLog>>();
		try {
			ExceptionLogService exceptionLogService = SystemInitialization.getApplicationContext().getBean(ExceptionLogService.class);
			res.setVo(exceptionLogService.getExceptionLog());
			
		} catch (Exception e) {
			// TODO: handle exception
		}
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "执行成功", res.convertJSONObject(), this);
	}
	
}
