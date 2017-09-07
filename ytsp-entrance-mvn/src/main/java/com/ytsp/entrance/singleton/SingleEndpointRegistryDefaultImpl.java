package com.ytsp.entrance.singleton;

import java.util.HashMap;
import java.util.Map;

import com.ytsp.common.util.StringUtil;

public class SingleEndpointRegistryDefaultImpl implements SingleEndpointRegistry {

    private Map<String, String> store = new HashMap<String, String>();

    public synchronized void register(String id, String key) {
        if (StringUtil.isNullOrEmpty(id) || StringUtil.isNullOrEmpty(key)) {
            throw new IllegalArgumentException(String.format("ILLEGAL parameters [id=%s, key=%s]", id, key));
        }

        store.put(id, key);
    }

    public synchronized void unregister(String id) {
        if (StringUtil.isNullOrEmpty(id)) {
            throw new IllegalArgumentException(String.format("ILLEGAL parameters [id=%s]", id));
        }

        store.remove(id);
    }

    public synchronized boolean existed(String id, String key) throws KeyConflictException {
        if (StringUtil.isNullOrEmpty(id) || StringUtil.isNullOrEmpty(key)) {
            throw new IllegalArgumentException(String.format("ILLEGAL parameters [id=%s, key=%s]", id, key));
        }

        final String k = store.get(id);
        if (k != null) {
            if (k.equals(key)) {
                return true;
            }
            throw new KeyConflictException(id, k, key);
        }

        return false;
    }

}
