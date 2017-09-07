package com.ytsp.entrance.command.base;

public class ExecuteResult {
	private int status;
	private String statusMsg;
	private Object result;
	private Command command; // 生成该结果的命令

	public ExecuteResult(int status, String statusMsg, Object result, Command command) {
		this.status = status;
		this.statusMsg = statusMsg;
		this.result = result;
		this.command = command;
	}

	public int getStatus() {
		return status;
	}	

	public void setStatus(int status) {
		this.status = status;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

	public String getStatusMsg() {
		return statusMsg;
	}

	public void setStatusMsg(String statusMsg) {
		this.statusMsg = statusMsg;
	}

	public Command getCommand() {
		return command;
	}

	public void setCommand(Command command) {
		this.command = command;
	}

}
