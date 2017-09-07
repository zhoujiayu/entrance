/* 
 * $Id: AlipayService.java 1208 2011-09-25 08:25:55Z jeff $ * 
 * Copyright (C) CoolMind Network Technology. visit http://www.cool-mind.com
 * All rights reserved 
 */

package com.ytsp.entrance.recharge.alipay;

public interface AlipayService {

    <T> T getPayChannel(Class<T> t, String outUser) throws AlipayException, Exception;

    String trade(String tradeId, String cashierCode, String outUser, String subject, String price) throws AlipayTradeException, Exception;
}
