package com.ytsp.entrance.singleton;

import org.apache.log4j.Logger;

import com.ytsp.entrance.listener.InVmLogoutListener;

public class InVmCallback implements KeyConflictCallback {

    private static final Logger log = Logger.getLogger(InVmCallback.class);
    private InVmLogoutListener listener;
    public InVmCallback(InVmLogoutListener listener) {
    	this.listener = listener;
    }

    @Override
    public void execute(String id, String key) {
        try {
            this.listener.execute(id, key);
        } catch (Exception e) {
            log.error("call back FAILED", e);
        } 
    }

}
