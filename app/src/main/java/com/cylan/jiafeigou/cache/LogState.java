package com.cylan.jiafeigou.cache;

/**
 * Created by cylan-hunt on 17-2-16.
 */

public class LogState {
    public static int STATE_NONE = 0;
    public static int STATE_ACCOUNT_OFF = 1;
    public static int STATE_ACCOUNT_ON = 2;
    public int state;

    public LogState(int state) {
        this.state = state;
    }
}
