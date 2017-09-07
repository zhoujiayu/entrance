package com.ytsp.entrance.singleton;

public class KeyConflictException extends Exception {

    private static final long serialVersionUID = 1L;
    private String id;
    private String originalKey;
    private String newKey;
    public KeyConflictException(String id, String originalKey, String newKey) {
        this.id = id;
    }

    public String getOriginalKey() {
        return originalKey;
    }

    public String getNewKey() {
        return newKey;
    }

    public String getConflitId() {
        return id;
    }
}
