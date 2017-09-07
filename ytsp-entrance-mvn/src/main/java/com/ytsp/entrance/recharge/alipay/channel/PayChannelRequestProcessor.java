/* 
 * $Id: PayChannelRequestProcessor.java 1210 2011-09-25 08:26:38Z jeff $ * 
 * Copyright (C) CoolMind Network Technology. visit http://www.cool-mind.com
 * All rights reserved 
 */

package com.ytsp.entrance.recharge.alipay.channel;

import com.ytsp.entrance.recharge.alipay.AlipayException;

public interface PayChannelRequestProcessor<T> {

    T getPayChannel(String outUser) throws AlipayException, Exception;

    Class<T> support();
}