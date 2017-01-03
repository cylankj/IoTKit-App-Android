package com.cylan.jiafeigou.misc;

import android.os.Environment;
import android.util.Patterns;

import com.cylan.jiafeigou.NewHomeActivity;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.view.bell.BellLiveActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public final static Pattern EMAIL_REG = Patterns.EMAIL_ADDRESS;

    public static final Pattern MAC_REG = Pattern.compile("([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$");
    public static final String EFAMILY_URL_PREFIX = "http://www.jfgou.com/app/download.html?";
    public static final Pattern EFAMILY_QR_CODE_REG = Pattern.compile(
            "cid=7\\d{11}" +
                    "&" +
                    "mac=([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$");


    //看JConstantTest单元测试
    public static final Pattern JFG_DOG_DEVICE_REG = Pattern.compile("DOG-[a-zA-Z0-9]{6}");
    public static final Pattern JFG_BELL_DEVICE_REG = Pattern.compile("DOG-ML-[a-zA-Z0-9]{6}");
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

    public static final String KEY_DELAY_RECORD_GUIDE = "delay_record_guide";
    public static final String BELL_CALL_WAY = "bell_call_way";
    public static final String BELL_CALL_WAY_VIEWER = "BELL_CALL_WAY_VIEWER";
    public static final String BELL_CALL_WAY_LISTEN = "BELL_CALL_WAY_LISTEN";
    public static final String BELL_CALL_WAY_EXTRA = "BELL_CALL_WAY_EXTRA";

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
    public static final String MEDIA_PATH = ROOT_DIR + File.separator + "media";
    public static final String USER_IMAGE_HEAD_URL = "";                  //用户头像
    public static final String MEDIA_DETAIL_PICTURE_DOWNLOAD_DIR = ROOT_DIR + File.separator + "images";
    public static final String MEDIA_DETAIL_VIDEO_DOWNLOAD_DIR = ROOT_DIR + File.separator + "videos";

    public static final String RECEIVE_MESSAGE_NOTIFICATION = "receive_message_notification";      //接收消息通知
    public static final String OPEN_VOICE = "open_voice";                       //开启声音提示
    public static final String OPEN_SHAKE = "open_shake";                       //开启震动提示
    public static final String OPEN_DOOR_NOTIFY = "open_door_notify";           //门磁开关提示

    public static final String KEY_REGISTER_SMS_TOKEN = "key_token";

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
    public static final int OS_CAMERA_CC3200 = 17;    //乐视狗使用门铃包DOG-CAM-CC3200
    public static final int OS_CAMERA_PANORAMA_HAISI = 18;    //海思全景摄像头
    public static final int OS_CAMERA_PANORAMA_QIAOAN = 19;    //乔安全景摄像头
    public static final int OS_CAMERA_PANORAMA_GUOKE = 20;    //国科全景摄像头

    public static final int OS_MAX_COUNT = OS_CAMERA_PANORAMA_GUOKE;
    public static Map<Integer, Integer> onLineIconMap = new HashMap<>();
    public static Map<Integer, Integer> offLineIconMap = new HashMap<>();
    public static Map<Integer, Integer> NET_TYPE_RES = new HashMap<>();


    static {
        NET_TYPE_RES.put(-1, -1);
        NET_TYPE_RES.put(0, -1);
        NET_TYPE_RES.put(1, R.drawable.icon_home_net_wifi);
        NET_TYPE_RES.put(2, R.drawable.icon_home_net_2g);
        NET_TYPE_RES.put(3, R.drawable.icon_home_net_3g);
        NET_TYPE_RES.put(4, -1);
        NET_TYPE_RES.put(5, -1);
    }

    static {
        //bell
        onLineIconMap.put(JConstant.OS_DOOR_BELL, R.drawable.icon_home_doorbell_online);
        //camera
        onLineIconMap.put(JConstant.OS_CAMARA_ANDROID_SERVICE, R.drawable.icon_home_camera_online);
        onLineIconMap.put(JConstant.OS_CAMERA_ANDROID, R.drawable.icon_home_camera_online);
        onLineIconMap.put(JConstant.OS_CAMERA_ANDROID_4G, R.drawable.icon_home_camera_online);
        onLineIconMap.put(JConstant.OS_CAMERA_CC3200, R.drawable.icon_home_camera_online);
        onLineIconMap.put(JConstant.OS_CAMERA_PANORAMA_GUOKE, R.drawable.icon_home_camera_online);
        onLineIconMap.put(JConstant.OS_CAMERA_PANORAMA_HAISI, R.drawable.icon_home_camera_online);
        onLineIconMap.put(JConstant.OS_CAMERA_PANORAMA_QIAOAN, R.drawable.icon_home_camera_online);
        onLineIconMap.put(JConstant.OS_CAMERA_UCOS_V3, R.drawable.icon_home_camera_online);
        onLineIconMap.put(JConstant.OS_CAMERA_UCOS_V2, R.drawable.icon_home_camera_online);
        onLineIconMap.put(JConstant.OS_CAMERA_UCOS, R.drawable.icon_home_camera_online);

        //MAG
        onLineIconMap.put(JConstant.OS_MAGNET, R.drawable.icon_home_magnetic_online);
        //E_FAMILY
        onLineIconMap.put(JConstant.OS_EFAML, R.drawable.icon_home_album_online);
        for (int i = 0; i < OS_MAX_COUNT; i++) {
            if (onLineIconMap.get(i) == null) {
                onLineIconMap.put(i, R.mipmap.ic_launcher);
            }
        }
    }

    static {
        //offline
        //bell
        offLineIconMap.put(JConstant.OS_DOOR_BELL, R.drawable.icon_home_doorbell_offline);
        //camera
        offLineIconMap.put(JConstant.OS_CAMARA_ANDROID_SERVICE, R.drawable.icon_home_camera_offline);
        offLineIconMap.put(JConstant.OS_CAMERA_ANDROID, R.drawable.icon_home_camera_offline);
        offLineIconMap.put(JConstant.OS_CAMERA_ANDROID_4G, R.drawable.icon_home_camera_offline);
        offLineIconMap.put(JConstant.OS_CAMERA_CC3200, R.drawable.icon_home_camera_offline);
        offLineIconMap.put(JConstant.OS_CAMERA_PANORAMA_GUOKE, R.drawable.icon_home_camera_offline);
        offLineIconMap.put(JConstant.OS_CAMERA_PANORAMA_HAISI, R.drawable.icon_home_camera_offline);
        offLineIconMap.put(JConstant.OS_CAMERA_PANORAMA_QIAOAN, R.drawable.icon_home_camera_offline);
        offLineIconMap.put(JConstant.OS_CAMERA_UCOS_V3, R.drawable.icon_home_camera_offline);
        offLineIconMap.put(JConstant.OS_CAMERA_UCOS_V2, R.drawable.icon_home_camera_offline);
        offLineIconMap.put(JConstant.OS_CAMERA_UCOS, R.drawable.icon_home_camera_offline);

        //MAG
        offLineIconMap.put(JConstant.OS_MAGNET, R.drawable.icon_home_magnetic_offline);
        //E_FAMILY
        offLineIconMap.put(JConstant.OS_EFAML, R.drawable.icon_home_album_offline);
        for (int i = 0; i < OS_MAX_COUNT; i++) {
            if (offLineIconMap.get(i) == null) {
                offLineIconMap.put(i, R.mipmap.ic_launcher);
            }
        }
    }

    public static List<Integer> CAMERA_OS_LIST = new ArrayList<>();

    static {
        CAMERA_OS_LIST.add(OS_CAMARA_ANDROID_SERVICE);
        CAMERA_OS_LIST.add(OS_CAMERA_ANDROID);
        CAMERA_OS_LIST.add(OS_CAMERA_ANDROID_4G);
        CAMERA_OS_LIST.add(OS_CAMERA_CC3200);
        CAMERA_OS_LIST.add(OS_CAMERA_PANORAMA_GUOKE);
        CAMERA_OS_LIST.add(OS_CAMERA_PANORAMA_HAISI);
        CAMERA_OS_LIST.add(OS_CAMERA_PANORAMA_QIAOAN);
        CAMERA_OS_LIST.add(OS_CAMERA_UCOS_V3);
        CAMERA_OS_LIST.add(OS_CAMERA_UCOS_V2);
        CAMERA_OS_LIST.add(OS_CAMERA_UCOS);
    }

    public static boolean isCamera(int pid) {
        return CAMERA_OS_LIST.contains(pid);
    }

    public static boolean isBell(int pid) {
        return OS_DOOR_BELL == pid;
    }

    public static boolean isEFamily(int pid) {
        return OS_EFAML == pid;
    }

    public static boolean isMag(int pid) {
        return OS_MAGNET == pid;
    }

    public static final String KEY_BIND_DEVICE = "BIND_DEVICE";
    public static final String BIND_DEVICE_CAM = "cam";
    public static final String BIND_DEVICE_BELL = "bell";
    public static final String BIND_DEVICE_CLOUD = "cloud";
    public static final String BIND_DEVICE_MAG = "mag";


    public static final int PLAY_STATE_IDLE = 1;
    public static final int PLAY_STATE_PREPARE = 2;
    public static final int PLAY_STATE_PLAYING = 3;
}
