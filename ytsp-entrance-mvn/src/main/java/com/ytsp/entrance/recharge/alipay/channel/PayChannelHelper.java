/* 
 * $Id: PayChannelHelper.java 1210 2011-09-25 08:26:38Z jeff $ * 
 * Copyright (C) CoolMind Network Technology. visit http://www.cool-mind.com
 * All rights reserved 
 */

package com.ytsp.entrance.recharge.alipay.channel;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;

import com.ytsp.common.util.StringUtil;
import com.ytsp.common.util.W3CDomUtil;
import com.ytsp.entrance.recharge.alipay.Constants;
import com.ytsp.entrance.recharge.alipay.security.MD5Signature;
import com.ytsp.entrance.recharge.alipay.util.ParameterUtil;

public class PayChannelHelper {

    private PayChannelHelper() {
    }
    
    private static URI createPayChannelRequestUri(String outUser) throws Exception {
        Map<String, String> reqParams = new HashMap<String, String>();
        reqParams.put(Constants.KEY_SERVICE, Constants.CHANNEL_SERVICE);
        reqParams.put(Constants.KEY_PARTNER, Constants.PARTNER_ID);
        reqParams.put(Constants.KEY_SIGN_TYPE, Constants.ALI_COMMUNICATE_SIGN_TYPE);
        reqParams.put(Constants.KEY_INPUT_CHARSET, Constants.ALI_COMMUNICATE_CHARSET);
        if (!StringUtil.isNullOrEmpty(outUser)) {
            reqParams.put(Constants.KEY_OUT_USER, outUser);
        }
        String signData = ParameterUtil.getSignData(reqParams); // 待签名数据
        String sign = MD5Signature.sign(signData, Constants.PARTNER_KEY); // 签名
        reqParams.put(Constants.KEY_SIGN, sign);

        String querys = ParameterUtil.formatQuerys(reqParams, "UTF-8");
        return URIUtils.createURI(Constants.CHANNEL_SCHEME, Constants.CHANNEL_HOST, -1, Constants.CHANNEL_PATH, querys, null);
    }

    public static Document getPayChannelsDocument(String outUser) throws Exception {
        return W3CDomUtil.loadDocument(getPayChannels(outUser), false, Constants.ALI_COMMUNICATE_CHARSET);
    }

    public static String getPayChannels(String outUser) throws Exception {

        URI uri = createPayChannelRequestUri(outUser);
        HttpClient client = new DefaultHttpClient();
        try {
            HttpGet get = new HttpGet(uri);
            HttpResponse response = client.execute(get);
            HttpEntity resEntity = response.getEntity();
            return EntityUtils.toString(resEntity, Constants.ALI_COMMUNICATE_CHARSET);
        } finally {
            client.getConnectionManager().shutdown();
        }
    }



}
