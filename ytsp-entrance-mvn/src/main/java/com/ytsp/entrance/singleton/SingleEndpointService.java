package com.ytsp.entrance.singleton;

public interface SingleEndpointService {

    void ping(String id, String key, KeyConflictCallback callback);
    void unregister(String id);
}
