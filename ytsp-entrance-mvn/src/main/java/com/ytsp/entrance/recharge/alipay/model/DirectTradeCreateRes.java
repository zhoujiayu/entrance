package com.ytsp.entrance.recharge.alipay.model;

import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XObject;

@XObject("direct_trade_create_res")
public class DirectTradeCreateRes {

    @XNode("request_token")
    private String requestToken;

    public String getRequestToken() {
        return requestToken;
    }

}
