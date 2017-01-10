package com.cylan.jiafeigou.misc;

/**
 * Created by cylan-hunt on 16-11-12.
 */

/**
 * The type Result event.
 */
public class JResultEvent {
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
     * 绑定结果
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
     * 重置密码回调
     */
    public static final int JFG_RESULT_RESET_PASS = 6;

    /**
     * 修改密码的回调
     */
    public static final int JFG_RESULT_CHANGE_PASS = 7;

    /**
     * 添加好友的回调
     */
    public static final int JFG_RESULT_ADD_FRIEND = 8;

    /**
     * 删除好友的结果
     */
    public static final int JFG_RESULT_DEL_FRIEND = 9;
    /**
     * 同意添加好友的结果
     */
    public static final int JFG_RESULT_CONSENT_ADD_FRIEND = 10;

    /**
     * 设置好友备注名
     */
    public static final int JFG_RESULT_SET_FRIEND_MARKNAME = 11;

    /**
     * 分享设备的回调
     */
    public static final int JFG_RESULT_SHARE_DEVICE = 12;

    /**
     * 取消分享的回调
     */
    public static final int JFG_RESULT_UNSHARE_DEVICE = 13;

    /**
     * 设置设备别名的回调
     */
    public static final int JFG_RESULT_SET_DEVICE_ALIAS = 14;

    /**
     * 发送送反馈的结果
     */
    public static final int JFG_RESULT_SEND_FEEDBACK = 15;

    /**
     * 设置DeviceToken的返回结果
     */
    public static final int JFG_RESULT_SET_DEVICE_TOKEN = 16;

    /**
     * 第三方绑定账号时设置密码的回复
     */
    public static final int JFG_RESULT_SETPWD_WITH_BINDACCOUNT = 17;

    /**
     * 删除好友添加请求
     */
    public static final int JFG_RESULT_DEL_FRIEND_ADD_REQ = 19;

}