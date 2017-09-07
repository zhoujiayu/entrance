/* 
 * $Id: AlipayTradeException.java 1208 2011-09-25 08:25:55Z jeff $ * 
 * Copyright (C) CoolMind Network Technology. visit http://www.cool-mind.com
 * All rights reserved 
 */

package com.ytsp.entrance.recharge.alipay;

import com.ytsp.entrance.recharge.alipay.model.ErrorCode;

public class AlipayTradeException extends Exception {

    private static final long serialVersionUID = 1L;
    private ErrorCode error;

    public ErrorCode getError() {
        return error;
    }

    public AlipayTradeException(ErrorCode error) {
        this.error = error;
    }

    public AlipayTradeException(String msg) {
        super(msg);
    }
}
