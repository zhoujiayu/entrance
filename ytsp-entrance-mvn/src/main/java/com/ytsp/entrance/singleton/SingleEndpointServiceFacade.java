package com.ytsp.entrance.singleton;

import java.net.URI;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.ytsp.common.jmx.Service;
import com.ytsp.common.util.StringUtil;
import com.ytsp.entrance.listener.InVmLogoutListener;

public class SingleEndpointServiceFacade extends Service implements SingleEndpointServiceMBean {

    private static final Logger log = Logger.getLogger(SingleEndpointServiceFacade.class);
    private SingleEndpointService seService;

    public void setSeService(SingleEndpointService seService) {
        this.seService = seService;
    }

    @Override
    public void ping(final String id, final String key, final Map<String, Object> params) {
        assert seService != null;

        if (StringUtil.isNullOrEmpty(id) || StringUtil.isNullOrEmpty(key)) {
            log.error(String.format("INSUFFICIENT info [id=%s, key=%s]", id, key));
            return;
        }
        if (params == null) {
            log.error(String.format("INSUFFICIENT parameters for the param-map is NULL"));
            return;
        }
        final Object o1 = params.get(SingleEndpointServiceMBean.PARAM_NAME_CALLBACK_TYPE);
        final Object o2 = params.get(SingleEndpointServiceMBean.PARAM_NAME_CALLBACK_PARAM);

        if (o1 == null || o2 == null) {
            log.error(String.format("INSUFFICIENT parameters " + map2string(params)));
            return;
        }

        if (SingleEndpointServiceMBean.CALLBACK_TYPE_HTTP_GET.equals(o1)) {
        	if (!(o2 instanceof URI)) {
        		log.error(String.format("ILLEGAL uri: %s" + o2));
        		return;
        	}
        	
        	seService.ping(id, key, new HttpRequestCallback((URI) o2));
        	
        } else if (SingleEndpointServiceMBean.CALLBACK_TYPE_INVM.equals(o1)) {
        	if (!(o2 instanceof InVmLogoutListener)) {
        		log.error(String.format("ILLEGAL InVmLogoutListener: %s" + o2));
        		return;
        	}
        	
        	seService.ping(id, key, new InVmCallback((InVmLogoutListener) o2));
        	
        } else {
        	log.error(String.format("UNSUPPORTED callback type: %s" + o1));
        	return;
        }
    }
    
    public void unregister(String id){
    	assert seService != null;
    	seService.unregister(id);
    }

    private static String map2string(Map<String, Object> m) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (Entry<String, Object> entry : m.entrySet()) {
            sb.append("[").append(entry.getKey()).append("=").append(entry.getValue()).append("]");
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    public void pingWithHttpCallback(String id, String key, final URI uri) {
        assert seService != null;

        if (StringUtil.isNullOrEmpty(id) || StringUtil.isNullOrEmpty(key) || uri == null) {
            log.error(String.format("INSUFFICIENT info [id=%s, key=%s, uri=%s]", id, key, uri));
            return;
        }

        seService.ping(id, key, new HttpRequestCallback(uri));
    }
}
