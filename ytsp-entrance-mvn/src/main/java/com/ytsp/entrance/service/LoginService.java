package com.ytsp.entrance.service;

import java.util.Map;

import javax.servlet.http.HttpSession;

import com.ytsp.db.domain.Customer;
import com.ytsp.entrance.listener.SessionListener;
import com.ytsp.entrance.system.IConstants;
import com.ytsp.entrance.system.SessionCustomer;

public class LoginService {
	
	public static void singleRegister(HttpSession session, Customer customer){
//		SingleEndpointServiceFacade single = SystemInitialization.getApplicationContext().getBean(SingleEndpointServiceFacade.class);
//		Map<String, Object> params = new HashMap<String, Object>();
//		params.put(SingleEndpointServiceMBean.PARAM_NAME_CALLBACK_TYPE, SingleEndpointServiceMBean.CALLBACK_TYPE_INVM);
//		params.put(SingleEndpointServiceMBean.PARAM_NAME_CALLBACK_PARAM, new CustomerLogoutListener(session.getId()));
//		single.ping(String.valueOf(customer.getId()), IConstants.MOBILE_SESSIONID + "=" + session.getId(), params);
		session.setAttribute(IConstants.SINGLEENDPOINT_KICKOUT, Boolean.FALSE);
	}
	
	public static void singleUnregister(HttpSession session, String id){
//		SingleEndpointServiceFacade single = SystemInitialization.getApplicationContext().getBean(SingleEndpointServiceFacade.class);
//		single.unregister(id);
		session.removeAttribute(IConstants.SINGLEENDPOINT_KICKOUT);
	}
	
	
	public static void inValidateSameSession(Customer customer,String terminal_number,String sessionId)
	{
		if(terminal_number == null || sessionId == null)
		{
			return;
		}
		HttpSession ops = null;
		Map<String, HttpSession> sessions = SessionListener.getSessions();
		if(sessions != null){
			for(String key : sessions.keySet())
			{
				HttpSession session = sessions.get(key);
				SessionCustomer oldCustomer = (SessionCustomer)session.getAttribute(IConstants.SESSION_CUSTOMER);
				if(oldCustomer != null)
				{
					Customer c = oldCustomer.getCustomer();
					if(terminal_number.equals(c.getTerminalNumber()) 
							&& c.getId().equals(customer.getId())
							&& !sessionId.equals(session.getId()))
					{
						ops = session;
						break;
					}
				}
			}
		}
		
		if(ops != null)
		{
			ops.removeAttribute(IConstants.SESSION_CUSTOMER);
		}
	}
	
}
