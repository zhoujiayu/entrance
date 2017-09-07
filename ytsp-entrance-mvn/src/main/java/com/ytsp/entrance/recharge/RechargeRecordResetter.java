/* 
 * $Id: RechargeRecordResetter.java 1078 2011-09-22 07:49:20Z jeff $ * 
 * Copyright (C) CoolMind Network Technology. visit http://www.cool-mind.com
 * All rights reserved 
 */

package com.ytsp.entrance.recharge;

import java.util.Date;

import org.apache.log4j.Logger;

import com.ytsp.db.dao.MonthlyDao;
import com.ytsp.db.exception.SqlException;

public class RechargeRecordResetter {

    private static final Logger log = Logger.getLogger(RechargeRecordResetter.class);

    private MonthlyDao dao;
    public void setDao(MonthlyDao dao) {
        this.dao = dao;
    }

    public void process() {
        System.out.println("process");
        Date now = new Date();
        try {
            dao.deleteExpiriedRecords(now);
        } catch (SqlException e) {
            log.error("expired recharge record reset FAILED", e);
        }
    }
}
