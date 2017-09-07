package com.ytsp.entrance.singleton;

public interface SingleEndpointRegistry {

    void register(String id, String key);

    void unregister(String id);

    boolean existed(String id, String key) throws KeyConflictException;
}
