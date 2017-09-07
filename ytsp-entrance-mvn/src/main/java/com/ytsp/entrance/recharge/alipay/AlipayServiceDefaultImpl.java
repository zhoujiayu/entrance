/* 
 * $Id: AlipayServiceDefaultImpl.java 1589 2011-10-13 02:42:03Z jeff $ * 
 * Copyright (C) CoolMind Network Technology. visit http://www.cool-mind.com
 * All rights reserved 
 */

package com.ytsp.entrance.recharge.alipay;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.ytsp.common.util.StringUtil;
import com.ytsp.entrance.recharge.alipay.channel.PayChannelRequestProcessor;
import com.ytsp.entrance.recharge.alipay.model.DirectTradeCreateRes;
import com.ytsp.entrance.recharge.alipay.model.ErrorCode;
import com.ytsp.entrance.recharge.alipay.security.MD5Signature;
import com.ytsp.entrance.recharge.alipay.util.ParameterUtil;
import com.ytsp.entrance.recharge.alipay.util.XMapUtil;

@SuppressWarnings({"rawtypes", "unchecked"})
public class AlipayServiceDefaultImpl implements AlipayService {

    private Map<Class<?>, PayChannelRequestProcessor<?>> processors = new HashMap<Class<?>, PayChannelRequestProcessor<?>>();

    public void setProcessors(List<PayChannelRequestProcessor<?>> processors) {
        if (processors != null) {
            for (PayChannelRequestProcessor<?> p : processors) {
                this.processors.put(p.support(), p);
            }
        }
    }
    
    public static void main(String[] args) throws AlipayTradeException, Exception {
        
        String sss =  "http://wappaygw.alipay.com/service/rest.htm?sign=4a74aabf997ee0c8b2f186bd4b29f52e&sec_id=MD5&v=2.0&call_back_url=http%3A%2F%2F192.168.1.145%3A8080%2FWapPayChannelDemo%2Fservlet%2FCallBack&req_data=%3Cauth_and_execute_req%3E%3Crequest_token%3E20110924f8555b67df0c730447dbe885d2831ddf%3C%2Frequest_token%3E%3C%2Fauth_and_execute_req%3E&service=alipay.wap.auth.authAndExecute&partner=2088701162312122&format=xml";
        String s = new AlipayServiceDefaultImpl().trade(String.valueOf(System.currentTimeMillis()), "CREDITCARD_CCB", null, "啊钊专用充气娃娃", "333.33");
        System.out.println(s);
        System.out.println(URLDecoder.decode(s, "UTF-8"));
        System.out.println(URLDecoder.decode(sss, "UTF-8"));
        
//        Calendar cal = Calendar.getInstance();
//        cal.setTime(new Date());
//        cal.set(Calendar.MONTH, Calendar.FEBRUARY);
//        System.out.println(cal.getActualMaximum(Calendar.DAY_OF_MONTH));
//        cal.add(Calendar.MONTH, 1);
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
//        System.out.println(sdf.format(cal.getTime()));
    }

    @Override
    public <T> T getPayChannel(Class<T> t, String outUser) throws AlipayException, Exception {

        final PayChannelRequestProcessor p = processors.get(t);
        if (p == null) {
            throw new IllegalArgumentException("unsupported type: " + t);
        }
        return (T) p.getPayChannel(outUser);
    }

    private static final String DIRECT_TRADE_CREATE_REQ = 
        "<direct_trade_create_req>"+
            "<subject>%s</subject>"+
            "<out_trade_no>%s</out_trade_no>"+
            "<total_fee>%s</total_fee>"+
            "<seller_account_name>%s</seller_account_name>" +
            "%s"+
            "<notify_url>%s</notify_url>"+
            "<out_user>%s</out_user>"+
            "<merchant_url>%s</merchant_url>"+
        "</direct_trade_create_req>";

    private static final String AUTH_EXEC_REQ = 
        "<auth_and_execute_req>" +
            "<request_token>%s</request_token>" +
        "</auth_and_execute_req>";

    public String trade(String tradeId, String cashierCode, String outUser, String subject, String price) throws AlipayTradeException, Exception {

        String tradeResult = getTradeResult(tradeId, cashierCode, outUser, subject, price);
        DirectTradeCreateRes directTradeCreateRes = null;
        XMapUtil.register(DirectTradeCreateRes.class);
        try {
            directTradeCreateRes = (DirectTradeCreateRes) XMapUtil.load(new ByteArrayInputStream(tradeResult.getBytes("UTF-8")));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("unsupported encoding: UTF-8");
        }

        String requestToken = directTradeCreateRes.getRequestToken();
        Map<String, String> authParams = generateAuthParamsMap(requestToken);
        String authSign = sign(authParams);
        authParams.put(Constants.KEY_SIGN, authSign);
        return getAuthExecRedirectUrl(authParams);
    }

    private String getAuthExecRedirectUrl(Map<String, String> reqParams) throws Exception {
        URI uri = URIUtils.createURI(Constants.TRADE_SCHEME,
                                     Constants.TRADE_HOST,
                                     -1,
                                     Constants.TRADE_PATH,
                                     ParameterUtil.formatQuerys(reqParams, "UTF-8"),
                                     null);
        return uri.toURL().toString();
    }

    private Map<String, String> generateAuthParamsMap(String requestToken) {
        Map<String, String> requestParams = new HashMap<String, String>();

        String reqData = String.format(AUTH_EXEC_REQ, requestToken);
        requestParams.put(Constants.KEY_REQ_DATA, reqData);
        addCommonParameters(requestParams);
//        requestParams.put(Constants.KEY_CALLBACK_URL, Constants.CALLBACK_URL);
        requestParams.put(Constants.KEY_SERVICE, Constants.AUTH_EXEC_SERVICE);
        return requestParams;
    }

    private String getTradeResult(String tradeId, String cashierCode, String outUser, String subject, String price) throws AlipayTradeException, Exception {
        Map<String, String> paramMap = new HashMap<String, String>();
        String reqData = String.format(DIRECT_TRADE_CREATE_REQ,
                                           subject,
                                           tradeId,
                                           price,
                                           Constants.PARTNER_SELLER,
                                           StringUtil.isNullOrEmpty(cashierCode) ? "" : "<cashier_code>" + cashierCode + "</cashier_code>",
                                           Constants.NOTIFY_URL,
                                           outUser == null ? "" : outUser,
                                           Constants.MACHANT_URL + "?uid=" + outUser);

        paramMap.put(Constants.KEY_REQ_DATA, reqData);
        paramMap.put(Constants.KEY_REQ_ID, String.valueOf(new Date().getTime()));
        addCommonParameters(paramMap);
        paramMap.put(Constants.KEY_SIGN, sign(paramMap));

        DefaultHttpClient client = new DefaultHttpClient();
        HttpPost post = createHttpPost(paramMap);
        try {
            HttpResponse response = client.execute(post);
            HttpEntity resEntity = response.getEntity();
            String responseString = EntityUtils.toString(resEntity, "UTF-8");
            responseString = URLDecoder.decode(responseString, "UTF-8");
            return resolve(responseString);
        } finally {
            client.getConnectionManager().shutdown();
        }
    }

    private String resolve(String response) throws AlipayTradeException, Exception {
        HashMap<String, String> resMap = new HashMap<String, String>();
        String resError = ParameterUtil.getParameter(response, Constants.KEY_RES_ERROR);

        if (resError != null) {
            String businessResult = ParameterUtil.getParameter(response, Constants.KEY_RES_ERROR);

            XMapUtil.register(ErrorCode.class);
            try {
                ErrorCode errorCode = (ErrorCode) XMapUtil.load(new ByteArrayInputStream(businessResult.getBytes("UTF-8")));
                throw new AlipayTradeException(errorCode);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("unsupported encoding: UTF-8", e);
            }

        } else {
            String businessResult = ParameterUtil.getParameter(response, Constants.KEY_RES_DATA);
            String sign = ParameterUtil.getParameter(response, Constants.KEY_SIGN);

            resMap.put(Constants.KEY_VERSION, ParameterUtil.getParameter(response, Constants.KEY_VERSION));
            resMap.put(Constants.KEY_SERVICE, ParameterUtil.getParameter(response, Constants.KEY_SERVICE));
            resMap.put(Constants.KEY_PARTNER, ParameterUtil.getParameter(response, Constants.KEY_PARTNER));
            resMap.put(Constants.KEY_SEC_ID, Constants.ALI_COMMUNICATE_SIGN_TYPE);
            resMap.put(Constants.KEY_REQ_ID, ParameterUtil.getParameter(response, Constants.KEY_REQ_ID));
            resMap.put(Constants.KEY_RES_DATA, businessResult);

            String verifyData = ParameterUtil.getSignData(resMap);
            if (!MD5Signature.verify(verifyData, sign, Constants.PARTNER_KEY)) {
                throw new AlipayTradeException("签名验证失败");
            }

            return businessResult;
        }

    }

//    private HttpPost createHttpPost2(Map<String, String> paramMap) throws URISyntaxException {
//        URI uri = URIUtils.createURI(Constants.TRADE_SCHEME,
//                                     Constants.TRADE_HOST,
//                                     -1,
//                                     Constants.TRADE_PATH,
//                                     ParameterUtil.formatQuerys(paramMap, "UTF-8"),
//                                     null);
//
//        return new HttpPost(uri);
//    }

    private HttpPost createHttpPost(Map<String, String> paramMap) throws URISyntaxException {
        URI uri = URIUtils.createURI(Constants.TRADE_SCHEME,
                                     Constants.TRADE_HOST,
                                     -1,
                                     Constants.TRADE_PATH,
                                     null,
                                     null);

        HttpPost post = new HttpPost(uri);

        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        for (Entry<String, String> entry : paramMap.entrySet()) {
            formparams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }

        UrlEncodedFormEntity entity = null;
        try {
            entity = new UrlEncodedFormEntity(formparams, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UNSUPPORTED encoding: UTF-8");
        }
        post.setEntity(entity);

        return post;
    }

    private String sign(Map<String, String> reqParams) throws Exception {
        String signData = ParameterUtil.getSignData(reqParams);
        return MD5Signature.sign(signData, Constants.PARTNER_KEY);
    }

    private void addCommonParameters(Map<String, String> paramMap) {
        paramMap.put(Constants.KEY_SERVICE, Constants.TRADE_SERVICE);
        paramMap.put(Constants.KEY_SEC_ID, Constants.ALI_COMMUNICATE_SIGN_TYPE);
        paramMap.put(Constants.KEY_PARTNER, Constants.PARTNER_ID);
//        paramMap.put(Constants.KEY_CALLBACK_URL, Constants.CALLBACK_URL);
        paramMap.put(Constants.KEY_FORMAT, Constants.TRADE_FORMAT);
        paramMap.put(Constants.KEY_VERSION, Constants.TRADE_VERSION);
    }

}
