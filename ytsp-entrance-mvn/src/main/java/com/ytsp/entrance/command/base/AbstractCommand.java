package com.ytsp.entrance.command.base;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.ytsp.db.domain.Customer;
import com.ytsp.entrance.service.CustomerService;
import com.ytsp.entrance.system.IConstants;
import com.ytsp.entrance.system.SessionCustomer;
import com.ytsp.entrance.system.SystemInitialization;
import com.ytsp.session.service.SessionService;

public abstract class AbstractCommand implements Command {
	protected static final Logger logger = Logger.getLogger(AbstractCommand.class);
	protected CommandContext context;

	protected SessionService sessionService =  SystemInitialization.getApplicationContext().getBean(SessionService.class);
	
	@Override
	public void setContext(CommandContext context) {
		this.context = context;
	}

	public CommandContext getContext() {
		return context;
	}

	protected HttpServletRequest getRequest() {
		return context.getRequest();
	}

	protected HttpServletResponse getResponse() {
		return context.getResponse();
	}

	protected HttpSession getSession() {
		return context.getRequest().getSession();
	}

//	protected SessionCustomer getSessionCustomer() {
//		HttpSession s =  getSession();
//		if(s != null)
//		{
//			s.setAttribute(IConstants.SESSION_Uid, getContext().getHead().getUid());
//			s.setAttribute(IConstants.SESSION_OP, getContext().getHead().getCommandCode());
//			s.setAttribute(IConstants.SESSION_Platform, getContext().getHead().getPlatform());
//			s.setAttribute(IConstants.SESSION_Version, getContext().getHead().getVersion());
//			s.setAttribute(IConstants.SESSION_UniqueId, getContext().getHead().getUniqueId());
//		}
//		return (SessionCustomer) s.getAttribute(IConstants.SESSION_CUSTOMER);
//	}

	protected SessionCustomer getSessionCustomer() {
		try {
			HttpSession s =  getSession();
			SessionCustomer sc =  (SessionCustomer) s.getAttribute(IConstants.SESSION_CUSTOMER);
			if (sc != null
					&& sc.getCustomer() != null
					&& sc.getCustomer().getId() == getContext().getHead()
							.getUid())
				return sc;
			int userId = getContext().getHead().getUid();//UID由客户端传递过来,与当前用户的session中的用户ID做比对
			String sessionId = getContext().getHead().getSessionId();
			if(sessionId==null||sessionId.trim().equals(""))
				return null;
			if(userId<=0)
				return null;
			if(sessionService.isSignIn(sessionId, userId)){
				Customer customer  = SystemInitialization.getApplicationContext().getBean(
						CustomerService.class).findCustomerById(userId);
				sc = new SessionCustomer(customer);
				s.setAttribute(IConstants.SESSION_CUSTOMER, sc);
			}
			return sc;
		} catch (Exception e) {
		}
		return null;
	}

	protected ExecuteResult getNoPermissionExecuteResult(){
		HttpSession session = getSession();
		if(session != null){
			if(Boolean.TRUE.equals(session.getAttribute(IConstants.SINGLEENDPOINT_KICKOUT))){
				return new ExecuteResult(CommandList.RESPONSE_STATUS_LOGIN_IN_OTHER_SIDE, "您的帐号在其它地点登录，移动设备端将被强制下线！！", null, this);
			}
		}
		return new ExecuteResult(CommandList.RESPONSE_STATUS_NOLOGIN, "您还未登录系统！", null, this);
	}
	
	protected ExecuteResult getExceptionExecuteResult(Exception e){
		if(e != null){
			String str = "CommandCode:"+getContext().getHead().getCommandCode()+"_Platform:"+getContext().getHead().getPlatform()+"_Version:"+getContext().getHead().getVersion();
			logger.error("服务端执行命令异常！"+str, e);
		}
		return new ExecuteResult(CommandList.RESPONSE_STATUS_FAIL, "服务端执行命令异常！", null, this);
	}
	
}
