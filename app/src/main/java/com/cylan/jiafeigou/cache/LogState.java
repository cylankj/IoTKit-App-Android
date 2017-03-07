package com.cylan.jiafeigou.cache;

/**
 * Created by cylan-hunt on 17-2-16.
 */

public class LogState {
    public static int STATE_GUEST = -1;
    public static int STATE_NONE = 0;//无账号,游客身份,初次进入
    public static int STATE_ACCOUNT_OFF = 1;//账号离线,退出登陆
    public static int STATE_ACCOUNT_ON = 2;//账号在线
    public int state;

    public LogState(int state) {
        this.state = state;
    }
}
