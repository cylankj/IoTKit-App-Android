package com.cylan.jiafeigou.misc;

import android.os.Environment;

import com.cylan.jiafeigou.NewHomeActivity;
import com.cylan.jiafeigou.n.view.bell.BellLiveActivity;

import java.io.File;
import java.util.regex.Pattern;

/**
 * Created by cylan-hunt on 16-6-30.
 */
public class JConstant {
    /**
     * {@link NewHomeActivity}底部menu对应的FrameLayout的id。用来存放每日精彩的时间控件
     */
    public static final String KEY_NEW_HOME_ACTIVITY_BOTTOM_MENU_CONTAINER_ID = "new_home_menu_id";
    public static final int AUTHORIZE_PHONE = 0;
    public static final int AUTHORIZE_MAIL = 1;
    public static final int THIS_ACCOUNT_NOT_REGISTERED = -1;

    public static final int TYPE_INVALID = -1;
    public static final int TYPE_PHONE = 0;
    public static final int TYPE_EMAIL = 1;
    public final static Pattern PHONE_REG = Pattern.compile("^1[3|4|5|7|8]\\d{9}$");

    public static final int JFG_DEVICE_BELL = 0;
    public static final int JFG_DEVICE_CAMERA = 1;
    public static final int JFG_DEVICE_ALBUM = 2;
    public static final int JFG_DEVICE_MAG = 3;

    public static final Pattern JFG_DEVICE_REG = Pattern.compile("DOG-\\d{6}");
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


    public static final String KEY_FRESH = "is_you_fresh";


    public static int ConfigApState = 0;


    public static final int REQ_CODE_ACTIVITY = 1;
    public static final int RESULT_CODE_FINISH = 1;
    public static final int RESULT_CODE_REMOVE_ITEM = 2;

    public static final String KEY_REMOVE_DEVICE = "rm_device";
    public static final String KEY_REMOVE_ITEM_CID = "key_remove_cid";
    public static final String KEY_ACTIVITY_RESULT_CODE = "key_result_code";

    /**
     * 主页的item传递给各个Activity的key.
     */
    public static final String KEY_DEVICE_ITEM_BUNDLE = "key_bundle_item";


    /**
     * 保存了 {@link BellLiveActivity}的进程id
     */
    public static String KEY_BELL_CALL_PROCESS_ID = "key_bell_call_process_id";

    public static String KEY_BELL_CALL_PROCESS_IS_FOREGROUND = "key_is_foreground";

    public static final int INVALID_PROCESS = -1;

    public static class LOG_TAG {
        public static final String PERMISSION = "permission";
    }

    public static final String KEY_SHARED_ELEMENT_LIST = "key_shared_element_url";
    public static final String KEY_SHARED_ELEMENT_STARTED_POSITION = "key_shared_element_pos";
    public static final String EXTRA_STARTING_ALBUM_POSITION = "key_start_position";
    public static final String EXTRA_CURRENT_ALBUM_POSITION = "key_current_position";

    public static final String KEY_SHARED_ELEMENT_TRANSITION_NAME_SUFFIX = "_image";


    /**
     * 程序文件存放目录
     */
    public static final String ROOT_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Smarthome";
    public static final String BLOCK_LOG_PATH = ROOT_DIR + File.separator + "block";
    public static final String CRASH_PATH = ROOT_DIR + File.separator + "crash";
    public static final String DAEMON_DIR = ROOT_DIR + File.separator + "daemon";
    public static final String LOG_PATH = ROOT_DIR + File.separator + "log";
    public static final String USER_IMAGE_HEAD_URL = "";                  //用户头像

    public static final String RECEIVE_MESSAGE_NOTIFICATION = "receive_message_notification";      //接收消息通知
    public static final String OPEN_VOICE = "open_voice";                       //开启声音提示
    public static final String OPEN_SHAKE = "open_shake";                       //开启震动提示
    public static final String OPEN_DOOR_NOTIFI = "open_door_notify";                       //门磁开关提示


}
