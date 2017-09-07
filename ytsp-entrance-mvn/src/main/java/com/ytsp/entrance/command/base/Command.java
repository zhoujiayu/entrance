package com.ytsp.entrance.command.base;

public interface Command {
	
	/**
	 * 是否接受执行该命令
	 * @return
	 */
	public boolean canExecute();
	
	/**
	 * 设置上下文
	 * @param context
	 */
	public void setContext(CommandContext context);
	
	/**
	 * 执行结果
	 * @return
	 */
	public ExecuteResult execute();
	
}
