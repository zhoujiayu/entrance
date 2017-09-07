/* 
 * $Id: RechargeProcessorPrepaidCardImpl.java 1589 2011-10-13 02:42:03Z jeff $ * 
 * Copyright (C) CoolMind Network Technology. visit http://www.cool-mind.com
 * All rights reserved 
 */

package com.ytsp.entrance.recharge.prepaid;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import cn.dongman.util.DongmanCore;
import cn.dongman.util.DongmanMd5Encrypt;
import cn.dongman.util.DongmanNotify;

import com.ytsp.common.util.StringUtil;
import com.ytsp.entrance.recharge.RechargeException;
import com.ytsp.entrance.recharge.RechargeProcessor;
import com.ytsp.entrance.recharge.RechargeType;

public class RechargeProcessorPrepaidCardImpl implements RechargeProcessor {

    @Override
    public RechargeType support() {
        return RechargeType.PREPAID_CARD;
    }

    @Override
    public void process(int id, Map<String, Object> param) throws RechargeException {

        Object o1 = param.get(com.ytsp.entrance.recharge.Constants.PARAMS_PREPAID_CARD_CODE);
        Object o2 = param.get(com.ytsp.entrance.recharge.Constants.PARAMS_PREPAID_CARD_PASSWORD);
        if (o1 == null || o2 == null || !(o1 instanceof String) || !(o2 instanceof String)
            || StringUtil.isNullOrEmpty((String) o1) || StringUtil.isNullOrEmpty((String) o2)) {
            throw new IllegalArgumentException(String.format("cid=%s, code=%s, psw=%s", id, o1, o2));
        }

        String code = (String) o1;
        String psw = (String) o2;
        String cid = String.valueOf(id);

        HttpPost post = null;
        try {
            post = createHttpPost(cid, code, psw);
        } catch (URISyntaxException e) {
            throw new RechargeException(e, TradeStatus.ERROR);
        }

        final DefaultHttpClient client = new DefaultHttpClient();
        try {
            HttpResponse response = client.execute(post);
            JSONObject json = resolveResponse(response, "ISO-8859-1");
            TradeStatus status;
            try {
                String statusStr = json.getString(Constants.KEY_TRADE_STATUS);
                int i = Integer.parseInt(statusStr);
                status = TradeStatus.getStatus(i);
            } catch (NumberFormatException e) {
                throw new RechargeException(e, TradeStatus.ERROR);
            }
            if (!varify(json)) {
                throw new RechargeException(status);
            } else {
                if (!TradeStatus.SUCESSEED.equals(status)) {
                    throw new RechargeException(status);
                }
            }
        } catch (ClientProtocolException e) {
            throw new RechargeException(e, TradeStatus.ERROR);
        } catch (IOException e) {
            throw new RechargeException(e, TradeStatus.ERROR);
        } catch (ParseException e) {
            throw new RechargeException(e, TradeStatus.ERROR);
        } catch (JSONException e) {
            throw new RechargeException(e, TradeStatus.ERROR);
        } finally {
            client.getConnectionManager().shutdown();
        }
    }

    private boolean varify(JSONObject json) throws JSONException {
        Map<String, String> map = new HashMap<String, String>();
        map.put(Constants.KEY_CARD_CODE,       json.getString(Constants.KEY_CARD_CODE));
        map.put(Constants.KEY_TRADE_STATUS,    json.getString(Constants.KEY_TRADE_STATUS));
        map.put(Constants.KEY_CARD_PRICE,      json.getString(Constants.KEY_CARD_PRICE));
        map.put(Constants.KEY_USER,            json.getString(Constants.KEY_USER));
        map.put(Constants.KEY_SIGN,            json.getString(Constants.KEY_SIGN));
        map.put(Constants.KEY_MONTHS,          json.getString(Constants.KEY_MONTHS));
    
        return DongmanNotify.verifyClient(map);
    }

    private JSONObject resolveResponse(HttpResponse response, String encoding) throws ParseException, IOException, JSONException {
        HttpEntity resEntity = response.getEntity();
        String responseString = EntityUtils.toString(resEntity, encoding);
        return new JSONObject(responseString);
        
    }

    private HttpPost createHttpPost(String cid, String code, String psw) throws URISyntaxException {
        URI uri = URIUtils.createURI(Constants.SERVER_PROTOCOL,
                                     Constants.SERVER_LOCATION,
                                     Constants.SERVER_PORT,
                                     Constants.SERVER_PATH,
                                     null,
                                     null);

        HttpPost post = new HttpPost(uri);
        String signString = generateSignString(cid, code, psw);

        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair(Constants.KEY_CARD_CODE,  code));
        formparams.add(new BasicNameValuePair(Constants.KEY_CARD_PSW,   DongmanMd5Encrypt.md5(psw)));
        formparams.add(new BasicNameValuePair(Constants.KEY_SIGN,       signString));
        formparams.add(new BasicNameValuePair(Constants.KEY_USER,       cid));
        UrlEncodedFormEntity entity = null;
        try {
            entity = new UrlEncodedFormEntity(formparams, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UNSUPPORTED encoding: UTF-8");
        }
        post.setEntity(entity);

        return post;
    }

    private String generateSignString(String cid, String code, String psw) {
        Map<String, String> signParam = new HashMap<String, String>();
        signParam.put(Constants.KEY_CARD_CODE,  code);
        signParam.put(Constants.KEY_CARD_PSW,   DongmanMd5Encrypt.md5(psw));
        signParam.put(Constants.KEY_USER,       cid);
        return DongmanCore.buildMysign(signParam);
    }
}
