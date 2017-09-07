/* 
 * $Id: DataConverter.java 1208 2011-09-25 08:25:55Z jeff $ * 
 * Copyright (C) CoolMind Network Technology. visit http://www.cool-mind.com
 * All rights reserved 
 */

package com.ytsp.entrance.recharge.alipay.util;

public class DataConverter {

    public static boolean tBoolean2native(String str) {

        if ("Y".equals(str)) {
            return true;
        } else if ("N".equals(str)) {
            return false;
        }

        throw new IllegalArgumentException("the value could only be 'Y' or 'N', but now it is: " + str);
    }

    public static String native2TBoolean(boolean b) {
        return b ? "Y" : "N";
    }
}
