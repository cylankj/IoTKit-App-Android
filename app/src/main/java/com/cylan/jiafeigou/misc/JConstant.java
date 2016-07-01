package com.cylan.jiafeigou.misc;

import java.util.regex.Pattern;

/**
 * Created by cylan-hunt on 16-6-30.
 */
public class JConstant {
    public static final int TYPE_INVALID = -1;
    public static final int TYPE_PHONE = 0;
    public static final int TYPE_EMAIL = 1;
    public final static Pattern PHONE_REG = Pattern.compile("^1[3|4|5|7|8]\\d{9}$");

    public static final int VALID_VERIFICATION_CODE_LEN = 6;
    public static final int PWD_LEN_MIN = 6;
    public static final int PWD_LEN_MAX = 12;
    public static final int REGISTER_BY_PHONE = 0;
    public static final int REGISTER_BY_EMAIL = 1;
    /**
     * 最外层layoutId，添加Fragment使用。
     */
    public static final String KEY_ACTIVITY_FRAGMENT_CONTAINER_ID = "activityFragmentContainerId";

    public static final String KEY_LOCALE = "key_locale";
    public static final int LOCALE_CN = 0;
    public static final long VERIFICATION_CODE_DEADLINE = 90 * 1000L;
    /**
     * 注册，登陆模块，携带账号
     */
    public static final String KEY_ACCOUNT_TO_SEND = "key_to_send_account";
    public static final String KEY_PWD_TO_SEND = "key_to_send_pwd";
    /**
     * verification code
     */
    public static final String KEY_VCODE_TO_SEND = "key_to_send_pwd";
    /**
     * fragment与宿主activity之间的切换关系，{1:finishActivity,2:just popFragment}
     */
    public static final String KEY_FRAGMENT_ACTION_1 = "key_fragment_activity_0";

}
