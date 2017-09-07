package com.ytsp.entrance.singleton;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.ytsp.entrance.listener.InVmLogoutListener;
import com.ytsp.entrance.listener.SessionListener;
import com.ytsp.entrance.system.IConstants;
import com.ytsp.entrance.system.SessionCustomer;

public class CustomerLogoutListener implements InVmLogoutListener {
	private static final Logger logger = Logger.getLogger(CustomerLogoutListener.class);

	private String sessionid;
	
	public CustomerLogoutListener(String sessionid) {
		this.sessionid = sessionid;
	}
	

	@Override
	public void execute(String id, String key) throws Exception{
		HttpSession session = SessionListener.getSession(sessionid);
		if(session != null){
			SessionCustomer customer = (SessionCustomer)session.getAttribute(IConstants.SESSION_CUSTOMER);
			if(customer != null){
				logger.info(String.format("用户[%s]在其它地点登录，移动设备端将被强制下线！", customer.getCustomer().getAccount()));
				session.removeAttribute(IConstants.SESSION_CUSTOMER);//销毁会话中的用户信息
				session.setAttribute(IConstants.SINGLEENDPOINT_KICKOUT, Boolean.TRUE);
			}
		}

	}

}
