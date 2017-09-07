/* 
 * $Id: Constants.java 2147 2011-10-31 05:29:31Z jeff $ * 
 * Copyright (C) CoolMind Network Technology. visit http://www.cool-mind.com
 * All rights reserved 
 */

package com.ytsp.entrance.recharge.prepaid;

class Constants {

    private Constants() {
    }

    static final String SERVER_LOCATION = "www.kandongman.com.cn";
    static final int    SERVER_PORT = 8080;
    static final String SERVER_PROTOCOL = "http";
    static final String SERVER_PATH = "/dongman/cardService.action";

    static final String KEY_CARD_CODE = "card_code";
    static final String KEY_CARD_PSW = "card_password";
    static final String KEY_CARD_PRICE = "card_price";
    static final String KEY_MONTHS = "months";
    static final String KEY_SERVICE = "service";
    static final String KEY_METHODS = "methods";
    static final String KEY_USER = "user_id";
    static final String KEY_TRADE_STATUS = "trade_status";
    static final String KEY_SIGN = "sign";

    static final String VALUE_SERVICE = "validateCard";

}
