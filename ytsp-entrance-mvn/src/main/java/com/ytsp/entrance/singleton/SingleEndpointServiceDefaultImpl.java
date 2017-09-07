package com.ytsp.entrance.singleton;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class SingleEndpointServiceDefaultImpl implements SingleEndpointService {

	private static final Logger log = Logger.getLogger(SingleEndpointServiceDefaultImpl.class);
	private SingleEndpointRegistry registry;
	private Map<String, KeyConflictCallback> callbacks = new HashMap<String, KeyConflictCallback>();

	public void setRegistry(SingleEndpointRegistry registry) {
		this.registry = registry;
	}

	public void ping(String id, String key, KeyConflictCallback callback) {
		assert registry != null;

		boolean existed = false;
		try {
			existed = registry.existed(id, key);
		} catch (KeyConflictException e) {
			log.debug(String.format("key conflict for [id=%s, orginalKey=%s, newKey=%s]", id, e.getOriginalKey(), e.getNewKey()));
			final KeyConflictCallback orginalCallback = callbacks.get(id);
			assert orginalCallback != null;
			try {
				//此处调用上次登录的退出函数，然后退出，取消单点登录的限制。不踢上次的登录用户
				/*2012.3.12 daiyu delete by daiyu start */
//				orginalCallback.execute(id, key);
				/*2012.3.12 daiyu delete by daiyu end */
			} catch (Exception ex) {
				log.debug("call FAILED", ex);
			}

			callbacks.put(id, callback);
		}

		if (!existed) {
			registry.unregister(id);
			registry.register(id, key);
			callbacks.put(id, callback);
		}
	}

	@Override
	public void unregister(String id) {
		assert registry != null;
		registry.unregister(id);
		callbacks.remove(id);
	}

}
