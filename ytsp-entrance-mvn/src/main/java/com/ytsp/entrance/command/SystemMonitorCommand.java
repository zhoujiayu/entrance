package com.ytsp.entrance.command;

import java.util.List;

import com.ytsp.entrance.command.base.AbstractCommand;
import com.ytsp.entrance.command.base.BodyInfo;
import com.ytsp.entrance.command.base.CommandContext;
import com.ytsp.entrance.command.base.CommandHandler;
import com.ytsp.entrance.command.base.CommandList;
import com.ytsp.entrance.command.base.ExecuteResult;
import com.ytsp.entrance.command.base.HeadInfo;

public class SystemMonitorCommand extends AbstractCommand {

	@Override
	public boolean canExecute() {
		int code = getContext().getHead().getCommandCode();
		return CommandList.SYSTEM_MONITOR == code;
	}

	@Override
	public ExecuteResult execute() {
		int code = getContext().getHead().getCommandCode();
		if(CommandList.SYSTEM_MONITOR == code)
			return systemMonitor();
		return null;
	}

	private ExecuteResult systemMonitor() {
		ExecuteResult er = null;
		CommandHandler handler = CommandHandler.getInstance();
		CommandContext context = new CommandContext();
		context.setRequest(getContext().getRequest());
		context.setResponse(getContext().getResponse());
		context.setHead(TEST_CMD_VERSION_LAST);
		context.setBody(emptyBody);
		List<ExecuteResult> ers = handler.execute(context);
		//只返回一个命令结果
		if(ers.size() > 0){
			er = ers.get(0);
		}
		if(er.getStatus()==200)
			return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "alibaba test success", null, this);
		return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "alibaba test error", null, this);
	}

	private static HeadInfo TEST_CMD_VERSION_LAST= new HeadInfo(){
		{
			commandCode = 111; // 指令
			platform = "gphone";// 平台
			uniqueId = "ALITEST";// 设备唯一码
			otherInfo = "ALITEST";// 其他信息
			version="9.9";
			uid=21;
		}
	};
	private BodyInfo emptyBody = new BodyInfo();
}
