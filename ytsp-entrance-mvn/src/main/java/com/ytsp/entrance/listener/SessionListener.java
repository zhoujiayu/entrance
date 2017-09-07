package com.ytsp.entrance.listener;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

public class SessionListener implements HttpSessionListener {
	private static Map<String, HttpSession> sessions = new HashMap<String, HttpSession>();

	@Override
	public void sessionCreated(HttpSessionEvent event) {
		HttpSession session = event.getSession();
		sessions.put(session.getId(), session);
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent event) {
		HttpSession session = event.getSession();
		sessions.remove(session.getId());
	}

	public static HttpSession getSession(String sessionId) {
		return sessions.get(sessionId);
	}
	
	public static Map<String, HttpSession> getSessions() {
		return sessions;
	}
	
	public static void removeSession(String sessionId) {
		if(sessions.containsKey(sessionId))
		{
			sessions.remove(sessionId);
		}
	}


}
