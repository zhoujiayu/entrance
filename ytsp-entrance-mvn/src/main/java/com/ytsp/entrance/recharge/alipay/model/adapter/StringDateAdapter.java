/* 
 * $Id: StringDateAdapter.java 1208 2011-09-25 08:25:55Z jeff $ * 
 * Copyright (C) CoolMind Network Technology. visit http://www.cool-mind.com
 * All rights reserved 
 */

package com.ytsp.entrance.recharge.alipay.model.adapter;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.ytsp.entrance.recharge.alipay.Constants;

public class StringDateAdapter extends XmlAdapter<String, Date>{

    @Override
    public Date unmarshal(final String v) throws Exception {
        if (v == null) {
            return null;
        }
        return new SimpleDateFormat(Constants.ALIPAY_DATE_FORMAT).parse(v);
    }

    @Override
    public String marshal(final Date v) throws Exception {
        if (v == null) {
            return null;
        }
        return new SimpleDateFormat(Constants.ALIPAY_DATE_FORMAT).format(v);
    }

}
