/* 
 * $Id: Constants.java 1810 2011-10-17 13:33:54Z jeff $ * 
 * Copyright (C) CoolMind Network Technology. visit http://www.cool-mind.com
 * All rights reserved 
 */

package com.ytsp.entrance.recharge.alipay;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.coolmind.util.IOUtil;

public class Constants {

    private static final String KEY_ALI_NOTIFY = "notify.url";
    private static final String KEY_ALI_MACHANT = "machant.url";
    private static final String KEY_ADD_REDIRECT = "add.redirect.url";
    private static final String KEY_ADD_IMAGE = "add.image.url";

    public static final String NOTIFY_URL;
    public static final String MACHANT_URL;
    public static final String ADD_REDIRECT_URL;
    public static final String ADD_IMAGE_URL;

    static {
        Properties props = new Properties();
        final InputStream is = Constants.class.getResourceAsStream("/ali.properties");
        try {
            if (is == null) {
                throw new RuntimeException("resource '/ali.properties' NOT found");
            }
            props.load(is);
            NOTIFY_URL = props.getProperty(KEY_ALI_NOTIFY);
            MACHANT_URL = props.getProperty(KEY_ALI_MACHANT);
            ADD_REDIRECT_URL = props.getProperty(KEY_ADD_REDIRECT);
            ADD_IMAGE_URL = props.getProperty(KEY_ADD_IMAGE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (is != null) {
                IOUtil.closeQuietly(is);
            }
        }
    }

    private Constants() {
    }

    public static final String TRADE_STATUS_SUCCESS = "TRADE_FINISHED";
    public static final String TRADE_STATUS_SUCCESS2 = "TRADE_SUCCESS";
    public static final String TRADE_STATUS_WAIT_TO_PAY = "WAIT_BUYER_PAY";

    public static final String TRADE_RESPOSNE_SUCCESS = "success";
    public static final String TRADE_RESPOSNE_FAIL = "fail";

    public static final String PARTNER_ID = "2088701162312122";
    public static final String PARTNER_SELLER = "kandongman@yeah.net";
    public static final String PARTNER_KEY="84d91lyr2uybkwk3n4xwttu0lp8w9gts";
    public static final String ALI_COMMUNICATE_CHARSET = "GBK";
    public static final String ALI_COMMUNICATE_SIGN_TYPE = "MD5";

    public static final String CHANNEL_SCHEME = "https";
    public static final String CHANNEL_HOST = "mapi.alipay.com";
    public static final String CHANNEL_PATH = "/gateway.do";
    public static final String CHANNEL_SERVICE = "mobile.merchant.paychannel";

    public static final String TRADE_SCHEME = "http";
    public static final String TRADE_HOST = "wappaygw.alipay.com";
    public static final String TRADE_PATH = "/service/rest.htm";
    public static final String TRADE_SERVICE = "alipay.wap.trade.create.direct";

    public static final String AUTH_EXEC_SERVICE = "alipay.wap.auth.authAndExecute";

    public static final String CALLBACK_URL = "";
    public static final String TRADE_FORMAT = "xml";
    public static final String TRADE_VERSION = "2.0";
    
    public static final String KEY_REQ_DATA = "req_data";
    public static final String KEY_REQ_ID = "req_id";
    public static final String KEY_VERSION = "v";
    public static final String KEY_SEC_ID = "sec_id";
    public static final String KEY_CALLBACK_URL = "call_back_url";
    public static final String KEY_FORMAT = "format";
    public static final String KEY_RES_DATA = "res_data";
    public static final String KEY_RES_ERROR = "res_error";
    public static final String KEY_NOTIFY_DATA = "notify_data";

    public static final String KEY_SERVICE = "service";
    public static final String KEY_PARTNER = "partner";
    public static final String KEY_SIGN_TYPE = "sign_type";
    public static final String KEY_SIGN = "sign";
    public static final String KEY_INPUT_CHARSET = "_input_charset";
    public static final String KEY_OUT_USER = "out_user";

    public static final String IS_SUCCESS = "T";
    public static final String TAG_IS_SUCCESS = "is_success";
    public static final String TAG_ERROR = "error";
    public static final String TAG_RESULT = "result";
    public static final String TAG_SIGN = "sign";
    public static final String TAG_XPATH_RESULT = "response/alipay/result";

    /**
     * 支付宝的人拼错了(channel而不是channle)，我们只能跟着错
     */
    public static final String JSONKEY_PAY_CHANNLE_RESULT = "payChannleResult";
    public static final String JSONKEY_LATEST_PAY_CHANNEL = "latestPayChannel";
    public static final String JSONKEY_SUPPORT_TOP_PAY_CHANNEL = "supportTopPayChannel";
    public static final String JSONKEY_SUPPORT_SEC_PAY_CHANNEL = "supportSecPayChannel";

    public static final String ALIPAY_DATE_FORMAT = "yyyy-MM-dd kk:mm:ss";
}
