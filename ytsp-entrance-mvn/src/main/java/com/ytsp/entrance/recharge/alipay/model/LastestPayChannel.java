package com.ytsp.entrance.recharge.alipay.model;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class LastestPayChannel {

    private String name;

    private String cashierCode;

    public LastestPayChannel() {

    }

    public LastestPayChannel(String name, String cashierCode) {
        this.name = name;
        this.cashierCode = cashierCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCashierCode() {
        return cashierCode;
    }

    public void setCashierCode(String cashierCode) {
        this.cashierCode = cashierCode;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SIMPLE_STYLE);
    }

}
