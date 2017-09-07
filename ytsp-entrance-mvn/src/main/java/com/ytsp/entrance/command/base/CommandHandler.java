package com.ytsp.entrance.command.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * @author GENE
 * @description 命令处理器，注册各种Command，接收外部命令后分发给注册的Command处理
 */
public class CommandHandler {
	private static final Logger logger = Logger.getLogger(CommandHandler.class);

	private static final byte[] LOCKER = new byte[0];
	private static CommandHandler handler;
	private List<Class> commands = new ArrayList<Class>();
	//存放Command的Class
	private Map<Integer,Class> commandMap = new HashMap<Integer,Class>();
	
	private CommandHandler() {
	}

	public static CommandHandler getInstance() {
		if (handler == null) {
			synchronized (LOCKER) {
				if (handler == null) {
					handler = new CommandHandler();
				}
			}
		}
		return handler;
	}

	public void registCommand(Class command) {
		if (command != null && Command.class.isAssignableFrom(command)) {
			if (!commands.contains(command)) {
				commands.add(command);
			}
		}
	}

	public void removeCommand(Class command) {
		commands.remove(command);
	}

	public void clearCommand() {
		commands.clear();
	}

	public List<ExecuteResult> execute(CommandContext context) {
		List<ExecuteResult> ers = new ArrayList<ExecuteResult>();

		try {
			int commandCode = context.getHead().getCommandCode();
			if (commandMap.containsKey(commandCode)) {
				Command cmd = (Command) commandMap.get(commandCode)
						.newInstance();
				cmd.setContext(context);
				if (cmd.canExecute()) {
					ExecuteResult er = cmd.execute();
					if (er != null) {
						ers.add(er);
					}
				}
			} else {
				for (Class command : commands) {
					Command cmd = (Command) command.newInstance();
					cmd.setContext(context);
					if (cmd.canExecute()) {
						ExecuteResult er = cmd.execute();
						if (er != null) {
							ers.add(er);
						}
						commandMap.put(commandCode, command);
					}
				}
			}
		} catch (Exception e) {
			logger.error("execute command error!", e);
		}
		return ers;
	}

}
