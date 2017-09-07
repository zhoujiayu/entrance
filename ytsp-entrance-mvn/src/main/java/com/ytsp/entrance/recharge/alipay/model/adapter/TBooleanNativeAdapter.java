/* 
 * $Id: TBooleanNativeAdapter.java 1208 2011-09-25 08:25:55Z jeff $ * 
 * Copyright (C) CoolMind Network Technology. visit http://www.cool-mind.com
 * All rights reserved 
 */

package com.ytsp.entrance.recharge.alipay.model.adapter;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.ytsp.entrance.recharge.alipay.util.DataConverter;

public class TBooleanNativeAdapter extends XmlAdapter<String, Boolean> {

    public Boolean unmarshal(String value) {
        if (value == null) {
            return null;
        }
        return DataConverter.tBoolean2native(value);
    }

    public String marshal(Boolean value) {
        if (value == null) {
            return null;
        }
        return DataConverter.native2TBoolean(value);
    }

}
