/* 
 * $Id: AlipayException.java 1208 2011-09-25 08:25:55Z jeff $ * 
 * Copyright (C) CoolMind Network Technology. visit http://www.cool-mind.com
 * All rights reserved 
 */

package com.ytsp.entrance.recharge.alipay;


public class AlipayException extends Exception{

    private static final long serialVersionUID = 1L;

    private AlipayError error;
    public AlipayException(String msg, AlipayError error) {
        super(msg);
        this.error = error;
    }

    public AlipayException(String msg, Throwable t, AlipayError error) {
        super(msg, t);
        this.error = error;
    }

    public AlipayException(Throwable t, AlipayError error) {
        super(t);
        this.error = error;
    }

    public AlipayException(AlipayError error) {
        this.error = error;
    }

    public AlipayError getError() {
        return error;
    }

}
