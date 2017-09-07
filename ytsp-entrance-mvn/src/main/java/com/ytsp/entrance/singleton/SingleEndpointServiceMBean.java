package com.ytsp.entrance.singleton;

import java.net.URI;
import java.util.Map;

public interface SingleEndpointServiceMBean {

    public static final String PARAM_NAME_CALLBACK_TYPE = "x.singleton.callbackType";
    public static final String PARAM_NAME_CALLBACK_PARAM = "x.singleton.callbackParam";

    public static final String CALLBACK_TYPE_HTTP_GET = "httpget";
    public static final String CALLBACK_TYPE_INVM = "invm";

    void ping(String id, String key, Map<String, Object> params);

    void pingWithHttpCallback(String id, String key, URI uri);

    void unregister(String id);
}
