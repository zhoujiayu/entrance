/* 
 * $Id: RechargeService.java 1078 2011-09-22 07:49:20Z jeff $ * 
 * Copyright (C) CoolMind Network Technology. visit http://www.cool-mind.com
 * All rights reserved 
 */

package com.ytsp.entrance.recharge;

import java.util.Map;

public interface RechargeService {

    void recharge(int cid, RechargeType type, Map<String, Object> params) throws RechargeException;

}
