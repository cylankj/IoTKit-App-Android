package com.cylan.jiafeigou.misc;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Patterns;

import com.cylan.jiafeigou.NewHomeActivity;
import com.cylan.jiafeigou.R;
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

    public static final String KEY_TIME_TICK_ = "key_time_tick";

    public static final int TYPE_INVALID = -1;
    public static final int TYPE_PHONE = 0;
    public static final int TYPE_EMAIL = 1;
    public final static Pattern PHONE_REG = Pattern.compile("^1[3|4|5|7|8]\\d{9}$");
    public final static Pattern EMAIL_REG = Patterns.EMAIL_ADDRESS;

    public static final Pattern MAC_REG = Pattern.compile("([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$");
    public static final String EFAMILY_URL_PREFIX = "http://www.jfgou.com/app/download.html?";
    public static final Pattern QR_CODE_REG_WITH_SN = Pattern.compile(
            "vid=[0-9a-zA-Z]{0,12}" +
                    "&pid=\\d{0,12}" +
                    "&sn=[0-9a-zA-Z]{0,64}");
    public static final Pattern QR_CODE_REG = Pattern.compile(
            "vid=[0-9a-zA-Z]{0,12}" +
                    "&pid=\\d{0,12}");


    //看JConstantTest单元测试
    public static final Pattern JFG_DOG_DEVICE_REG = Pattern.compile("DOG-[a-zA-Z0-9]{6}");
    public static final Pattern JFG_BELL_DEVICE_REG = Pattern.compile("DOG-ML-[a-zA-Z0-9]{6}");
    public static final Pattern JFG_PAN_DEVICE_REG = Pattern.compile("DOG-5W-[a-zA-Z0-9]{6}");
    public static final int VALID_VERIFICATION_CODE_LEN = 6;
    public static final int PWD_LEN_MIN = 6;
    public static final int PWD_LEN_MAX = 12;
    public static final int USER_INPUT_LEN = 64;
    public static final int REGISTER_BY_PHONE = 0;
    public static final int REGISTER_BY_EMAIL = 1;
    /**
     * 最外层layoutId，添加Fragment使用。
     */
    public static final String KEY_ACTIVITY_FRAGMENT_CONTAINER_ID = "activityFragmentContainerId";

    public static final String KEY_LOCALE = "key_locale";
    public static final int LOCALE_SIMPLE_CN = 0;
    public static final int LOCALE_T_CN = 10;
    public static final long VERIFICATION_CODE_DEADLINE = 90 * 1000L;
    /**
     * 注册，登陆模块，携带账号
     */
    public static final String KEY_ACCOUNT_TO_SEND = "key_to_send_account";
    public static final String KEY_PWD_TO_SEND = "key_to_send_pwd";

    public static final String AUTO_LOGIN_ACCOUNT = "auto_login_account";
    public static final String AUTO_LOGIN_PWD = "auto_login_pwd";

    public static final String SAVE_TEMP_ACCOUNT = "save_temp_account";
    public static final String SAVE_TEMP_CODE = "save_temp_code";
    public static final String OPEN_LOGIN_TO_BIND_PHONE = "open_login_to_bind_phone";
    public static final String OPEN_LOGIN_USER_ICON = "open_login_user_icon";
    public static final String OPEN_LOGIN_USER_ALIAS = "open_login_user_alias";
    public static final String AUTO_SIGNIN_KEY = "auto_sign_in";
    public static final String TWITTER_INIT_KEY = "twitter_init_key";
    public static final String FACEBOOK_INIT_KEY = "facebook_init_key";
    public static final String AUTO_SIGNIN_TAB = "auto_signin_tab";
    public static final String FROM_LOG_OUT = "from_log_out";
    public static final String IS_lOGINED = "is_logined";   //是否登录过
    public static final String AUTO_lOGIN_PWD_ERR = "auto_login_pwd_err";
    /**
     * verification code
     */
    public static final String KEY_VCODE_TO_SEND = "key_to_send_pwd";
    public static final String KEY_SET_UP_PWD_TYPE = "key_set_up_type";
    /**
     * fragment与宿主activity之间的切换关系，{1:finishActivity,2:just popFragment}
     */
    public static final String KEY_SHOW_LOGIN_FRAGMENT = "key_show_login_fragment";
    /**
     * 作为一个key:{@link com.cylan.jiafeigou.n.view.login.LoginFragment}
     * ，是否需要调用addToBackStack
     */
    public static final String KEY_SHOW_LOGIN_FRAGMENT_EXTRA = "key_show_login_fragment_extra";


    public static final String KEY_FRESH = "is_you_fresh";

    public static final String KEY_DELAY_RECORD_GUIDE = "KEY_DELAY_RECORD_GUIDE";
    public static final String VIEW_CALL_WAY = "VIEW_CALL_WAY";
    public static final String VIEW_CALL_WAY_VIEWER = "VIEW_CALL_WAY_VIEWER";
    public static final String VIEW_CALL_WAY_LISTEN = "VIEW_CALL_WAY_LISTEN";
    public static final String VIEW_CALL_WAY_EXTRA = "VIEW_CALL_WAY_EXTRA";
    public static final String KEY_WONDERFUL_GUIDE = "KEY_WONDERFUL_GUIDE";
    public static final String VIEW_CALL_WAY_TIME = "VIEW_CALL_WAY_TIME";
    public static final String KEY_SIMPLE_STRING_ITEM = "KEY_SIMPLE_STRING_ITEM";

    public static final String KEY_CAM_SIGHT_SETTING = "cam_sight_setting";
    public static final java.lang.String LAST_ENTER_TIME = "LAST_ENTER_TIME";
    public static final String KEY_BIND_DEVICE_ALIAS = "KEY_BIND_DEVICE_ALIAS";
    public static final java.lang.String KEY_PANORAMA_POP_HINT = "KEY_PANORAMA_POP_HINT";

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

    public static final String KEY_DEVICE_ITEM_UUID = "key_device_uuid";

    /**
     * 保存了 {@link BellLiveActivity}的进程id
     */
    public static String KEY_BELL_CALL_PROCESS_ID = "key_bell_call_process_id";

    public static String KEY_BELL_CALL_PROCESS_IS_FOREGROUND = "key_is_foreground";

    public static final int INVALID_PROCESS = -1;

    public static class LOG_TAG {
        public static final String PERMISSION = "permission";
    }

    public static final String KEY_SHARE_ELEMENT_BYTE = "key_share_element_byte";
    public static final String KEY_SHARED_ELEMENT_LIST = "key_shared_element_url";
    public static final String KEY_SHARED_ELEMENT_STARTED_POSITION = "key_shared_element_pos";
    public static final String EXTRA_STARTING_ALBUM_POSITION = "key_start_position";
    public static final String EXTRA_CURRENT_ALBUM_POSITION = "key_current_position";

    public static final String KEY_SHARED_ELEMENT_TRANSITION_NAME_SUFFIX = "_image";

    public static String getRoot() {
        String content = JFGRules.getTrimPackageName();
        if (TextUtils.equals(content, "cell_c") || TextUtils.equals(content, "zhongxing"))
            return content;
        return "Smarthome";
    }

    /**
     * 程序文件存放目录
     */
    public static final String ROOT_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + getRoot();
    public static final String BLOCK_LOG_PATH = ROOT_DIR + File.separator + "block";
    public static final String CRASH_PATH = ROOT_DIR + File.separator + "crash";
    public static final String DAEMON_DIR = ROOT_DIR + File.separator + "daemon";
    public static final String LOG_PATH = ROOT_DIR + File.separator + "log";
    public static final String MEDIA_PATH = ROOT_DIR + File.separator + "media";
    public static final String USER_IMAGE_HEAD_URL = "";                  //用户头像
    public static final String MEDIA_DETAIL_PICTURE_DOWNLOAD_DIR = ROOT_DIR + File.separator + "images";
    public static final String MEDIA_DETAIL_VIDEO_DOWNLOAD_DIR = ROOT_DIR + File.separator + "videos";
    public static final String PREVIEW_CACHE = ROOT_DIR + File.separator + "cache" + File.separator;
    public static final String RECEIVE_MESSAGE_NOTIFICATION = "receive_message_notification";      //接收消息通知
    public static final String OPEN_VOICE = "open_voice";                       //开启声音提示
    public static final String OPEN_SHAKE = "open_shake";                       //开启震动提示
    public static final String OPEN_DOOR_NOTIFY = "open_door_notify";           //门磁开关提示

    public static final String PAN_PATH = ROOT_DIR + File.separator + "view";
    public static final String KEY_REGISTER_SMS_TOKEN = "key_token";
    public static final String KEY_FORGET_PWD_FRIST_GET_SMS = "key_frist_get_time";
    public static final String KEY_FORGET_PWD_GET_SMS_COUNT = "key_get_sms_count";
    public static final String KEY_REG_FRIST_GET_SMS = "key_reg_frist_get_time";
    public static final String KEY_REG_GET_SMS_COUNT = "key_reg_get_sms_count";

    public static final String KEY_BELL_LAST_ENTER_TIME_PREFIX = "bell_last_time";
    public static final int OS_SERVER = -1; //system message
    public static final int OS_IOS_PHONE = 0;
    public static final int OS_PC = 1;
    public static final int OS_ANDROID_PHONE = 2;
    public static final int OS_CAMARA_ANDROID_SERVICE = 3;
    public static final int OS_CAMERA_ANDROID = 4;
    public static final int OS_CAMERA_UCOS = 5;
    public static final int OS_DOOR_BELL = 6;
    public static final int OS_CAMERA_UCOS_V2 = 7;
    public static final int OS_EFAML = 8;
    public static final int OS_TEMP_HUMI = 9;//EHOME 温湿度
    public static final int OS_IR = 10; //EHOME 红外
    public static final int OS_MAGNET = 11;//EHOME 门窗磁
    public static final int OS_AIR_DETECTOR = 12;////EHOME 空气检测
    public static final int OS_CAMERA_UCOS_V3 = 13; //define("OS_CAMERA_UCOS_V3", 13); //DOG-1W-V3
    public static final int OS_DOOR_BELL_CAM = 14; //摄像头主板
    public static final int OS_DOOR_BELL_V2 = 15; //wifi狗主板
    public static final int OS_CAMERA_ANDROID_4G = 16;  //DOG_82
    public static final int OS_CAMERA_CC3200 = 17;    //乐视狗使用门铃包DOG-CAM-CC3200 freeCam
    public static final int OS_CAMERA_PANORAMA_HAISI = 18;    //海思全景摄像头  2W
    public static final int OS_CAMERA_PANORAMA_QIAOAN = 19;    //乔安全景摄像头  3W
    public static final int OS_CAMERA_PANORAMA_GUOKE = 20;    //国科全景摄像头   4W
    public static final int PID_CAMERA_ANDROID_3_0 = 1071;    //3g狗

    public static final int OS_CAMERA_FXXX_LESHI = 27;//门铃
    public static final int PID_CAMERA_FXXX_LESHI_PID = 1160;

    public static final int PID_CAMERA_VR_720 = 1080;
    public static final int PID_CAMERA_CLOUD = 1088;
    public static final int PID_CAMERA_WIFI_G1 = 1090;//wifi狗

    public static final int PID_CAMERA_PANORAMA_HAISI_1080 = 1092;
    public static final int PID_CAMERA_PANORAMA_HAISI_960 = 1091;

    public static final int PID_BELL_G_1 = 1093;//门铃1代
    public static final int PID_BELL_G_2 = 1094;//门铃2代


    public static int getNetTypeRes(int net) {
        switch (net) {
            case 1:
                return R.drawable.icon_home_net_wifi;
            case 2:
                return R.drawable.icon_home_net_2g;
            case 3:
                return R.drawable.icon_home_net_3g;
            default:
                return -1;
        }
    }

    public static int getOnlineIcon(int pid) {
        if (JFGRules.isBell(pid))
            return R.drawable.icon_home_doorbell_online;
        if (JFGRules.isCamera(pid))
            return R.drawable.icon_home_camera_online;
        if (JFGRules.isVRCam(pid))
            return R.drawable.home_icon_720camera_online;
        return R.mipmap.ic_launcher;
    }

    public static int getOfflineIcon(int pid) {
        if (JFGRules.isBell(pid))
            return R.drawable.icon_home_doorbell_offline;
        if (JFGRules.isCamera(pid))
            return R.drawable.icon_home_camera_offline;
        if (JFGRules.isVRCam(pid))
            return R.drawable.home_icon_720camera_offline;
        return R.mipmap.ic_launcher;
    }

    public static final String KEY_BIND_DEVICE = "BIND_DEVICE";
    public static final String BIND_DEVICE_CAM = "cam";
    public static final String BIND_DEVICE_BELL = "bell";
    public static final String BIND_DEVICE_CLOUD = "cloud";
    public static final String BIND_DEVICE_MAG = "mag";

    /**
     * 无账号:0
     * 有账号,未登录:1
     * 有账号,已登录:2
     *
     * @return
     */
    public final static String KEY_ACCOUNT = "key_account";

    public final static String KEY_ACCOUNT_LOG_STATE = "key_log_state";

    public final static String KEY_NTP_INTERVAL = "ntp_key";

    public static final int PLAY_STATE_IDLE = 1;
    public static final int PLAY_STATE_PREPARE = 2;
    public static final int PLAY_STATE_PLAYING = 3;

    public static final int CLOUD_OUT_CONNECT_TIME_OUT = 0;
    public static final int CLOUD_IN_CONNECT_TIME_OUT = 2531;
    public static final int CLOUD_IN_CONNECT_OK = 1;
    public static final int CLOUD_IN_CONNECT_FAILED = 2;
    public static final int CLOUD_OUT_CONNECT_OK = 3;
    public static final int CLOUD_OUT_CONNECT_FAILED = 4;

    public static final String JUST_SEND_INFO = "just_send_info";


    public static final String KEY_CAM_LIVE_PAGE_PLAY_TYPE = "page_play_type";
    public static final String KEY_CAM_LIVE_PAGE_PLAY_HISTORY_TIME = "page_play_history_time";
    public static final String KEY_JUMP_TO_CAM_DETAIL = "jump_to_cam_detail";

    public static final String KEY_UUID_RESOLUTION = "uuid_resolution";
    public static final String KEY_UUID_PREVIEW_THUMBNAIL_TOKEN = "uuid_preview_token";
    public static final String KEY_DEVICE_SETTING_SHOW_RED = "device_setting_show";


    public static final String BINDING_DEVICE = "key_need_bind";//
    public static final String CHECK_HARDWARE_TIME = "check_hardware_time";
    public static final String PREF_NAME = "config_pref";      //2.x Sp 的key
    public static final String KEY_PHONE = "PhoneNum";//2.x account key
    public static final String SESSIONID = "sessid";// 2.x sessid key
    public static final String KEY_PSW = "PSW";     //2.x pwd key
    public static final String UPDATAE_AUTO_LOGIN = "update_auto_login";
}
