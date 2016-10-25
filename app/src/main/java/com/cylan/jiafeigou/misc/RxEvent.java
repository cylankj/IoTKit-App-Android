package com.cylan.jiafeigou.misc;

import android.os.Bundle;

import com.cylan.entity.jniCall.JFGFriendAccount;

import java.util.ArrayList;


/**
 * Created by cylan-hunt on 16-7-6.
 */
public class RxEvent {

    public static class NeedLoginEvent {
        public static final String KEY = "show_register";
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

    public static class CloundLiveDelete{

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

    /**
     * The type Sms code result.
     */
    public static class SmsCodeResult {

        /**
         * The Error.
         */
        public int error;
        /**
         * The Token.
         */
        public String token;

        /**
         * Instantiates a new Sms code result.
         *
         * @param error the error
         * @param token the token
         */
        public SmsCodeResult(int error, String token) {
            this.error = error;
            this.token = token;
        }

        @Override
        public String toString() {
            return "SmsCodeResult{" +
                    "error=" + error +
                    ", token='" + token + '\'' +
                    '}';
        }
    }

    /**
     * {@link com.cylan.jfgapp.jni.JfgAppCallBack#OnSendSMSResult(int, String)}
     */
    public static final class ResultVerifyCode {
        public int code;

        public ResultVerifyCode(int code) {
            this.code = code;
        }
    }

    /**
     * 调用 {@link com.cylan.jfgapp.jni.JfgAppCmd#register(String, String, int, String)}
     */
    public static final class ResultRegister {
        public int code;

        public ResultRegister(int code) {
            this.code = code;
        }
    }

    public static final class ResultLogin {
        public int code;

        public ResultLogin(int code) {
            this.code = code;
        }
    }

    public static final class ResultBind {
        public int code;

        public ResultBind(int code) {
            this.code = code;
        }
    }


    public static final class ResultUnBind {
        public int code;
    }

    public static final class SwitchBox {
//        public String account;

//        public SwitchBox(String account) {
//            this.account = account;
//        }
    }

    public static final class GetFriendList{

        public int i;
        public ArrayList<JFGFriendAccount> arrayList;

        public GetFriendList(int i, ArrayList<JFGFriendAccount> arrayList) {
            this.i = i;
            this.arrayList = arrayList;
        }
    }

}
