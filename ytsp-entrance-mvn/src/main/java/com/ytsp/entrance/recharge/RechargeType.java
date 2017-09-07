/* 
 * $Id: RechargeType.java 1078 2011-09-22 07:49:20Z jeff $ * 
 * Copyright (C) CoolMind Network Technology. visit http://www.cool-mind.com
 * All rights reserved 
 */

package com.ytsp.entrance.recharge;

public enum RechargeType {

    PREPAID_CARD("充值卡"), ALIPAY("支付宝");

    private String desc;

    private RechargeType(String desc) {
        this.desc = desc;
    }

    String getDesc() {
        return desc;
    }

}
