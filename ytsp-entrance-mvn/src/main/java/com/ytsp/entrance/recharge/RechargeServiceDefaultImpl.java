/* 
 * $Id: RechargeServiceDefaultImpl.java 1078 2011-09-22 07:49:20Z jeff $ * 
 * Copyright (C) CoolMind Network Technology. visit http://www.cool-mind.com
 * All rights reserved 
 */

package com.ytsp.entrance.recharge;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RechargeServiceDefaultImpl implements RechargeService {

    private Map<RechargeType, RechargeProcessor> processors = new HashMap<RechargeType, RechargeProcessor>();

    public void setProcessors(final List<RechargeProcessor> processors) {
        if (processors != null && !processors.isEmpty()) {
            for (RechargeProcessor processor : processors) {
                enmap(processor.support(), processor);
            }
        }
    }

    private void enmap(final RechargeType type, final RechargeProcessor p) {
        if (type == null || p == null) {
            throw new IllegalArgumentException(String.format("invalid parameters[type=%s, processor=%s]", type, p));
        }
        this.processors.put(type, p);
    }

    private RechargeProcessor getProcessor(RechargeType type) {
        final RechargeProcessor p = this.processors.get(type);
        if (p == null) {
            throw new RuntimeException("UNSUPPORTED recharge type: " + type);
        }
        return p;
    }

    @Override
    public void recharge(int cid, RechargeType type, Map<String, Object> params) throws RechargeException {
        if (type == null) {
            throw new IllegalArgumentException(String.format("cid=%s, type=%s", cid, type));
        }

        getProcessor(type).process(cid, params);
    }

}
