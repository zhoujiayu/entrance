package com.ytsp.entrance.command.v5_0;

import com.ytsp.entrance.command.base.AbstractCommand;
import com.ytsp.entrance.command.base.CommandList;
import com.ytsp.entrance.command.base.ExecuteResult;

public class StatisticsCommandV5_0 extends AbstractCommand {

	@Override
	public boolean canExecute() {
		int code = getContext().getHead().getCommandCode();
		return code == CommandList.CMD_STATISTICS_ADD;
	}

	@Override
	public ExecuteResult execute() {
		int code = getContext().getHead().getCommandCode();
		if (code == CommandList.CMD_STATISTICS_ADD) {
			return addStatistics();
		} 
		return null;
	}
	
	/**
	* <p>功能描述:统计接口：入参：pageType</p>
	* <p>参数：@return</p>
	* <p>返回类型：ExecuteResult</p>
	 */
	private ExecuteResult addStatistics() {
		getContext().getHead();
		getContext().getBody().getBodyObject();
//		System.out.println(getContext().getBody().getBodyObject() == null?"":getContext().getBody().getBodyObject().toString());
		return new ExecuteResult(CommandList.RESPONSE_STATUS_OK, "操作成功",
				null, this);
	}

}
