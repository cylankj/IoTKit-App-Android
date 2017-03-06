package com.cylan.jiafeigou.cache.db.view;

/**
 * Created by yanzhendong on 2017/3/6.
 */

public enum DBState {
    SUCCESS, NOT_CONFIRM, ACTIVE;

    public String state() {
        return name();
    }
}
