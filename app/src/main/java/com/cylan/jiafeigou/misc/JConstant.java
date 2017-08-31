package com.cylan.jiafeigou.misc;

import android.os.Environment;
import android.util.Patterns;

import com.cylan.jiafeigou.NewHomeActivity;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.view.bell.BellLiveActivity;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;

import java.io.File;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Created by cylan-hunt on 16-6-30.
 */
public class JConstant {
    /**
     * {@link NewHomeActivity}底部menu对应的FrameLayout的id。用来存放每日精彩的时间控件
     */

    public static final String CYLAN_TAG = "CYLAN_TAG";


    public static final String KEY_NEW_HOME_ACTIVITY_BOTTOM_MENU_CONTAINER_ID = "new_home_menu_id";
    public static final int AUTHORIZE_PHONE_SMS = 0x111110;
    public static final int AUTHORIZE_MAIL = 0x111111;
    public static final int GET_SMS_BACK = 0x111112;
    public static final int CHECK_TIMEOUT = 0x111113;
    public static final int CHECK_ACCOUNT = 0x111114;
    public static final int THIS_ACCOUNT_NOT_REGISTERED = -1;

    public static final String KEY_TIME_TICK_ = "key_time_tick";

    public static final int TYPE_INVALID = -1;
    public static final int TYPE_PHONE = 0;
    public static final int TYPE_EMAIL = 1;
    public static final int TYPE_EMAIL_VERIFY = 2;
    public final static Pattern PHONE_REG = Pattern.compile("^1[3|4|5|7|8]\\d{9}$");
    public final static Pattern EMAIL_REG = Patterns.EMAIL_ADDRESS;

    public static final Pattern MAC_REG = Pattern.compile("([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$");
    public static final String EFAMILY_URL_PREFIX = "http://www.jfgou.com/app/download.html?";
//    public static final Pattern QR_CODE_REG_WITH_SN = Pattern.compile(
//            "Vid=[0-9a-zA-Z]{0,12}" +
//                    "&pid=\\d{0,12}" +
//                    "&sn=[0-9a-zA-Z]{0,64}");
//    public static final Pattern QR_CODE_REG = Pattern.compile(
//            "Vid=[0-9a-zA-Z]{0,12}" +
//                    "&pid=\\d{0,12}");


    //看JConstantTest单元测试

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

    public static final String OPEN_LOGIN_TO_BIND_PHONE = "open_login_to_bind_phone";
    public static final String OPEN_LOGIN_USER_ICON = "open_login_user_icon";
    public static final String OPEN_LOGIN_USER_ALIAS = "open_login_user_alias";
    public static final String AUTO_SIGNIN_KEY = "auto_sign_in";
    public static final String TWITTER_INIT_KEY = "twitter_init_key";
    public static final String FACEBOOK_INIT_KEY = "facebook_init_key";
    public static final String FROM_LOG_OUT = "from_log_out";
    public static final String AUTO_lOGIN_PWD_ERR = "auto_login_pwd_err";
    public static final String REG_SWITCH_BOX = "reg_switch_box";
    public static final String THIRD_RE_SHOW = "third_re_show";
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

    public static final String KEY_HELP_GUIDE = "need_help_guide";
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
    public static final String BELL_HOME_LAST_ENTER_TIME = "BELL_HOME_LAST_ENTER_TIME";

    public static final String KEY_COMPONENT_NAME = "key_component";
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
    public static final String KEY_DEVICE_TIME_ZONE = "key_device_time_zone";
    public static final java.lang.String SHOW_PASSWORD_CHANGED = "SHOW_PASSWORD_CHANGED";

    /**
     * 有区别于直接绑定
     */
    public static final String KEY_CONFIG_FREE_CAM = "config_free_cam";
    public static final java.lang.String PANORAMA_THUMB_PICTURE = "panorama_thumb_picture";
    public static final java.lang.String SWITCH_MODE_POP = "switch_mode_pop";
    public static final String ALERT_MOBILE = "alert_mobile";
    public static final java.lang.String SHOW_VR_MODE_TIPS = "show_vr_mode_tips";
    public static final java.lang.String KEY_SHOW_TIME_ZONE = "key_show_time_zone";
    public static final java.lang.String FRIEND_LAST_VISABLE_TIME = "friend_last_visiable_time";
    public static final String KEY_DEVICE_ITEM_IS_BELL = "key_device_item_is_bell";
    public static final java.lang.String NEED_SHOW_BIND_ANIMATION = "need_show_bind_animation";
    public static final java.lang.String KEY_SHOW_HISTORY_WHEEL_CASE = "key_show_history_wheel_case";
    public static final String KEY_CAM_LIVE_PAGE_PLAY_HISTORY_INIT_WHEEL = "key_cam_live_page_play_history_init_wheel";
    public static final String KEY_BIND_BACK_ACTIVITY = "KEY_BIND_BACK_ACTIVITY";

    //人形检测的物体
    public static final int OBJECT_MAN = 1;
    public static final int OBJECT_CAT = 2;
    public static final int OBJECT_DOG = 3;
    public static final int OBJECT_CAR = 4;
    public static final String IS_IN_BACKGROUND = "IS_IN_BACKGROUND";


    public static String getAIText(int[] objects) {
        StringBuilder result = new StringBuilder();

        if (objects != null) {
            int length = objects.length;
            for (int i = 0; i < length; i++) {
                int obj = objects[i];
                switch (obj) {
                    case JConstant.OBJECT_MAN: {
                        result.append(ContextUtils.getContext().getString(R.string.AI_HUMAN));
//                        result.append("人形");
                        break;
                    }
                    case JConstant.OBJECT_DOG: {
                        result.append(ContextUtils.getContext().getString(R.string.AI_DOG));
//                        result.append("狗");
                        break;
                    }
                    case JConstant.OBJECT_CAR: {
                        result.append(ContextUtils.getContext().getString(R.string.AI_VEHICLE));
//                        result.append("车");
                        break;
                    }
                    case JConstant.OBJECT_CAT: {
                        result.append(ContextUtils.getContext().getString(R.string.AI_CAT));
//                        result.append("猫");
                        break;
                    }
                }
//                if (i < length - 1) {
                result.append(" ");
//                }
            }
        }

        return result.toString();
    }
    //人形检测的物体

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
    public static final String KEY_SHARED_ELEMENT_BELL_LIST = "key_shared_element_bell_list";
    public static final String KEY_SHARED_ELEMENT_STARTED_POSITION = "key_shared_element_pos";
    public static final String EXTRA_STARTING_ALBUM_POSITION = "key_start_position";
    public static final String EXTRA_CURRENT_ALBUM_POSITION = "key_current_position";

    public static final String KEY_SHARED_ELEMENT_TRANSITION_NAME_SUFFIX = "_image";

    public static final String KEY_DEVICE_NEW_VERSION = "key_DEVICE_NEW_VERSION";

    public static String getRoot() {
        return ContextUtils.getContext().getString(R.string.log);
    }

    /**
     * 程序文件存放目录
     */
    public static final String ROOT_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + getRoot();
    public static final String UPDATE_FILE_PATH = ROOT_DIR;
    public static final String BLOCK_LOG_PATH = ROOT_DIR + File.separator + "block";
    public static final String CRASH_PATH = ROOT_DIR + File.separator + "crash";
    public static final String DAEMON_DIR = ROOT_DIR + File.separator + "daemon";
    //升级包的目录也需要放在这里
    public static final String WORKER_PATH = ROOT_DIR + File.separator + "log";
    public static final String MEDIA_PATH = ROOT_DIR + File.separator + "media";
    //    public static final String PANORAMA_MEDIA_PATH = ROOT_DIR + File.separator + "Panorama";
//    public static final String PANORAMA_MEDIA_THUMB_PATH = PANORAMA_MEDIA_PATH + File.separator + "thumb";
    public static final String SYSTEM_PHOTO_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "DCIM" + File.separator + "Camera";
    public static final String MISC_PATH = ROOT_DIR + File.separator + "misc";
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
    public static final String KEY_REGISTER_SMS_TOKEN_TIME = "key_token_time";
    public static final String KEY_FORGET_PWD_FRIST_GET_SMS = "key_frist_get_time";
    public static final String KEY_FORGET_PWD_GET_SMS_COUNT = "key_get_sms_count";
    public static final String KEY_REG_FRIST_GET_SMS = "key_reg_frist_get_time";
    public static final String KEY_REG_GET_SMS_COUNT = "key_reg_get_sms_count";

    public static final String KEY_BELL_LAST_ENTER_TIME_PREFIX = "bell_last_time";
    public static final String KEY_BELL_LAST_LISTEN_TIME_PREFIX = "bell_last_listen_time";
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
            case 4:
            case 5:
                return R.drawable.icon_home_net_3g;
            default:
                return -1;
        }
    }

    public static int getMessageIcon(int pid) {
        if (JFGRules.isRS(pid) && !JFGRules.isBell(pid))
            return R.drawable.me_icon_head_camera_ruishi;
        if (JFGRules.isCatEeyBell(pid)) return R.drawable.me_icon_intelligent_eye;
        if (JFGRules.isBell(pid)) return R.drawable.me_icon_head_ring;
        if (JFGRules.isPan720(pid)) return R.drawable.me_icon_head_720camera;
        if (JFGRules.isCamera(pid)) return R.drawable.me_icon_head_camera;
        AppLogger.e("bad pid: " + pid);
        return R.mipmap.ic_launcher;
    }

    public static int getOnlineIcon(int pid) {
        if (JFGRules.isRS(pid) && !JFGRules.isBell(pid)) return R.drawable.home_icon_rs_online;
        if (JFGRules.isBell(pid) && !JFGRules.isCatEeyBell(pid))
            return R.drawable.icon_home_doorbell_online;
        if (JFGRules.isCatEeyBell(pid))
            return R.drawable.home_icon_intelligent_eye;
        if (JFGRules.isPan720(pid))
            return R.drawable.home_icon_720camera_online;
        if (JFGRules.isCamera(pid))
            return R.drawable.icon_home_camera_online;
        AppLogger.e("bad pid: " + pid);
        return R.mipmap.ic_launcher;
    }

    public static int getOfflineIcon(int pid) {
        if (JFGRules.isRS(pid) && !JFGRules.isBell(pid)) return R.drawable.home_icon_rs_offline;
        if (JFGRules.isBell(pid) && !JFGRules.isCatEeyBell(pid))
            return R.drawable.icon_home_doorbell_offline;
        if (JFGRules.isCatEeyBell(pid))
            return R.drawable.home_icon_intelligent_eye_disable;
        if (JFGRules.isPan720(pid))
            return R.drawable.home_icon_720camera_offline;
        if (JFGRules.isCamera(pid))
            return R.drawable.icon_home_camera_offline;
        AppLogger.e("bad pid: " + pid);
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
    //登录状态,应该保存在内存中
    public final static String KEY_ACCOUNT_LOG_STATE = "key_log_state";

    public final static String KEY_NTP_INTERVAL = "ntp_key";

    public static final int PLAY_STATE_LOADING_FAILED = -111;
    public static final int PLAY_STATE_IDLE = 111;
    public static final int PLAY_STATE_PREPARE = 222;//loading
    public static final int PLAY_STATE_PLAYING = 333;
    public static final int PLAY_STATE_STOP = 444;
    public static final int PLAY_STATE_NET_CHANGED = 555;//网络变化过度

    public static final int CLOUD_OUT_CONNECT_TIME_OUT = 0;
    public static final int CLOUD_IN_CONNECT_TIME_OUT = 2531;
    public static final int CLOUD_IN_CONNECT_OK = 1;
    public static final int CLOUD_IN_CONNECT_FAILED = 2;
    public static final int CLOUD_OUT_CONNECT_OK = 3;
    public static final int CLOUD_OUT_CONNECT_FAILED = 4;

    public static final String JUST_SEND_INFO = "just_send_info";


    public static final String KEY_CAM_LIVE_PAGE_PLAY_HISTORY_TIME = "page_play_history_time";
    public static final String KEY_JUMP_TO_CAM_DETAIL = "jump_to_cam_detail";

    public static final String KEY_UUID_RESOLUTION = "uuid_resolution";
    public static final String KEY_UUID_PREVIEW_THUMBNAIL_TOKEN = "uuid_preview_token";

    public static final String BINDING_DEVICE = "key_need_bind";//
    public static final String KEY_FIRMWARE_CONTENT = "firmware_content";
    public static final String KEY_FIRMWARE_POP_DIALOG_TIME = "firmware_check";
    public static final String KEY_CLIENT_NEW_VERSION_DIALOG = "new_version_pop";
    public static final String PREF_NAME = "config_pref";      //2.x Sp 的key
    public static final String KEY_PHONE = "PhoneNum";//2.x account key
    public static final String SESSIONID = "sessid";// 2.x sessid key
    public static final String KEY_PSW = "PSW";     //2.x pwd key
    public static final String UPDATAE_AUTO_LOGIN = "update_auto_login";
    public static final String CLIENT_UPDATAE_TAB = "client_update_tab";
    public static final String CLIENT_UPDATAE_TIME_TAB = "client_update_time_tab";

    public static final String NEED_SHOW_COLLECT_USE_CASE = "show_collect_use_case";
    public static final String NEED_SHOW_BIND_USE_CASE = "show_bind_use_case";
    public static final String IS_FIRST_PAGE_VIS = "is_first_page_vis";
    public static final String KEY_JUMP_TO_MESSAGE = "jump_to_message";
    public static final String KEY_CLIENT_CHECK_VERSION_ID = "client_id";

    public static final String KEY_DEVICE_MAC = "KEY_MAC";
    public static final String KEY_ADD_DESC = "key_ads";
    public static final int CODE_AD_FINISH = 222;
    public static final String KEY_NEED_LOGIN = "needLogin";
    private static final String VERSION_URL = "http://yun.app8h.com/app?act=check_version&id=%s&platform=androidPhone&appid=%s";

    public static String assembleUrl(String id, String packageName) {
        return String.format(Locale.getDefault(), VERSION_URL, id, packageName);
    }

    public static final String KEY_CLIENT_UPDATE_DESC = "update_desc";

    public static final String KEY_LAST_TIME_CHECK_VERSION = "last_check_version";

    public static final String KEY_JUMP_TO_WONDER = "key_jump_to_home_wonder";

    public static final String SHOW_GCM_DIALOG = "gcm_check";

    public static final String KEY_SHOW_SUGGESTION = "key_show_suggestion";

    public static final class D {
        public static int FAILED = -1;
        public static int IDLE = 0;
        public static int DOWNLOADING = 1;
        public static int SUCCESS = 2;
    }

    public static final class U {
        public static int FAILED_DEVICE_FAILED = -4;//设备返回非0
        public static int FAILED_FPING_ERR = -3;
        public static int FAILED_90S = -2;
        public static int FAILED_120S = -1;
        public static int IDLE = 0;
        public static int UPDATING = 1;
        public static int SUCCESS = 2;
    }

    public static String KEY_CURRENT_PLAY_VIEW = "";

    public static final String KEY_ANIM_GIF = "ANIM_GIF";
    public static final String KEY_CONNECT_AP_GIF = "CONNECT_AP_GIF";
    public static final String KEY_SSID_PREFIX = "SSID_PREFIX";
    public static final String KEY_ANIM_TITLE = "KEY_ANIM_TITLE";
    public static final String KEY_ANIM_SUB_TITLE = "KEY_ANIM_SUB_TITLE";
    public static final String KEY_NEXT_STEP = "NEXT_STEP";

}
