/* 
 * $Id: StringBidDecimalAdapter.java 1208 2011-09-25 08:25:55Z jeff $ * 
 * Copyright (C) CoolMind Network Technology. visit http://www.cool-mind.com
 * All rights reserved 
 */

package com.ytsp.entrance.recharge.alipay.model.adapter;

import java.math.BigDecimal;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.log4j.Logger;

public class StringBidDecimalAdapter extends XmlAdapter<String, BigDecimal> {

    private static final Logger log = Logger.getLogger(StringBidDecimalAdapter.class);

    @Override
    public BigDecimal unmarshal(final String v) throws Exception {
        if (v == null) {
            return BigDecimal.ZERO;
        }

        try {
            return new BigDecimal(v);
        } catch (NumberFormatException e) {
            log.error("parse failed", e);
            return BigDecimal.ZERO;
        }
    }

    @Override
    public String marshal(final BigDecimal v) throws Exception {
        if (v == null) {
            return null;
        }

        return String.valueOf(v);
    }

}
