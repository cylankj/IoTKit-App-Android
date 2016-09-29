package com.cylan.jiafeigou.misc;

import android.os.Bundle;


/**
 * Created by cylan-hunt on 16-7-6.
 */
public class RxEvent {

    public static class NeedLoginEvent {
        public Bundle bundle;

        public NeedLoginEvent(Bundle bundle) {
            this.bundle = bundle;
        }
    }

    /**
     * 系统TimeTick广播
     */
    public static class TimeTickEvent {

    }

    public static class LoginRsp {

    }

    public static class ActivityResult {
        public Bundle bundle;
    }

    /**
     * The type Result event.
     */
    public static class ResultEvent {
        /**
         * The constant JFG_RESULT_VERIFY_SMS.
         */
        public static final int JFG_RESULT_VERIFY_SMS = 0;
        /**
         * The constant JFG_RESULT_REGISTER.
         */
        public static final int JFG_RESULT_REGISTER = 1;
        /**
         * The constant JFG_RESULT_LOGIN.
         */
        public static final int JFG_RESULT_LOGIN = 2;
        /**
         * The constant JFG_RESULT_BINDDEV.
         */
        public static final int JFG_RESULT_BINDDEV = 3;

        /**
         * The constant JFG_RESULT_UNBINDDEV.
         */
        public static final int JFG_RESULT_UNBINDDEV = 4;


        /**
         * The constant JFG_RESULT_UPDATE_ACCOUNT.
         */
        public static final int JFG_RESULT_UPDATE_ACCOUNT = 5;

        /**
         * 删除好友的结果
         */
        public static final int JFG_RESULT_DEL_FRIEND = 6;
        /**
         * 同意添加好友的结果
         */
        public static final int JFG_RESULT_CONSENT_ADD_FRIEND = 7;

        /**
         * 设置好友备注名
         */
        public static final int JFG_RESULT_SET_FRIEND_MARKNAME = 8;
    }

}
