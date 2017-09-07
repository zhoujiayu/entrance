/* 
 * $Id: IntegerStringAdapter.java 1208 2011-09-25 08:25:55Z jeff $ * 
 * Copyright (C) CoolMind Network Technology. visit http://www.cool-mind.com
 * All rights reserved 
 */

package com.ytsp.entrance.recharge.alipay.model.adapter;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.log4j.Logger;

public class IntegerStringAdapter extends XmlAdapter<String, Integer> {

    private static final Logger log = Logger.getLogger(IntegerStringAdapter.class);

    @Override
    public Integer unmarshal(final String v) throws Exception {
        if (v == null) {
            return null;
        }

        try {
            return Integer.parseInt(v);
        } catch (NumberFormatException e) {
            log.error("parse failed", e);
            return null;
        }
    }

    @Override
    public String marshal(final Integer v) throws Exception {
        if (v == null) {
            return null;
        }

        return String.valueOf(v);
    }

}
