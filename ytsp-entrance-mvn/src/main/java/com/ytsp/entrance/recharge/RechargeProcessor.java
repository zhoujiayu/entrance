/* 
 * $Id: RechargeProcessor.java 1078 2011-09-22 07:49:20Z jeff $ * 
 * Copyright (C) CoolMind Network Technology. visit http://www.cool-mind.com
 * All rights reserved 
 */

package com.ytsp.entrance.recharge;

import java.util.Map;

public interface RechargeProcessor {

    RechargeType support();

    void process(int cid, Map<String, Object> param) throws RechargeException;
}
