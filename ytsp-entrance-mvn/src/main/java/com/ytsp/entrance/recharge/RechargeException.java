/* 
 * $Id: RechargeException.java 1092 2011-09-22 10:34:54Z jeff $ * 
 * Copyright (C) CoolMind Network Technology. visit http://www.cool-mind.com
 * All rights reserved 
 */

package com.ytsp.entrance.recharge;

import com.ytsp.entrance.recharge.prepaid.TradeStatus;

public class RechargeException extends Exception {

    private static final long serialVersionUID = 1L;
    private TradeStatus status;

    public RechargeException(TradeStatus status) {
        this.status = status;
    }

    TradeStatus getStatus() {
        return status;
    }

    public RechargeException(String msg, TradeStatus status) {
        super(msg);
        this.status = status;
    }

    public RechargeException(Throwable t, TradeStatus status) {
        super(t);
        this.status = status;
    }

    public RechargeException(String msg, Throwable t, TradeStatus status) {
        super(msg, t);
        this.status = status;
    }
}
