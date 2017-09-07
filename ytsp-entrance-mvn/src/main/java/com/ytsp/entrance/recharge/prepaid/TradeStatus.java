/* 
 * $Id: TradeStatus.java 1495 2011-10-10 10:47:07Z jeff $ * 
 * Copyright (C) CoolMind Network Technology. visit http://www.cool-mind.com
 * All rights reserved 
 */

package com.ytsp.entrance.recharge.prepaid;

public enum TradeStatus {

    SUCESSEED(1, "验证成功并充值"),
    INVALID_PARAMETERS(2, "验证失败，参数不正确"),
    CARD_USED(3, "验证失败，此卡已经使用"),
    CARD_EXPIRED(4, "验证失败，此卡已经过期"),
    ERROR(5, "异常"),
    PASSWORD_ERROR(6, "密码输入出错"),
    INVALID_REQUEST(7, "无效请求"),
    REQUEST_TIME_OUT(8, "请求超时");

    static TradeStatus[] statuses;
    static {
        statuses = new TradeStatus[] {
            SUCESSEED, INVALID_PARAMETERS, CARD_USED, CARD_EXPIRED, ERROR, PASSWORD_ERROR, INVALID_REQUEST, REQUEST_TIME_OUT
        };
    }

    private TradeStatus(int status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public static TradeStatus getStatus(final int status) throws NumberFormatException{
        for (TradeStatus s : statuses) {
            if (s.getStatus() == status) {
                return s;
            }
        }
        throw new IllegalArgumentException("UNKNOW status: " + status);
    }

    private int status;
    private String desc;

    public int getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }

}
