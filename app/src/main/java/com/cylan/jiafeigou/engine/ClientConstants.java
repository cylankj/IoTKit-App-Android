package com.cylan.jiafeigou.engine;

import com.cylan.jiafeigou.R;

/**
 * author：hebin on 2015/10/21 15:36
 * email：hebin@cylan.com.cn
 */
public class ClientConstants {

    //LoginActivity
    public static final int DURATION_OVER_TIME = 30000;
    public static final String EDIT_LOGIN_ACCOUNT = "EDIT_LOGIN_ACCOUNT";

    //MyVideo
    public static final String CIDINFO = "CIDINFO";
    public static final String SCENCINFO = "SCENCINFO";
    public static final String TITLE_BAR = "TITLEBAR";
    public static final String ACCOUNT_VID = "ACCOUNT_VID";
    public static final String LOW_POWER = "LOW_POWER";
    public static final String ACTION = "com.cylan.MyVideos.NewRecord";
    public static final int ENABLE_SCENE = 1;
    public static int covers[] = {R.drawable.cover0, R.drawable.cover1, R.drawable.cover2, R.drawable.cover3, R.drawable.cover4, R.drawable.cover5,
            R.drawable.cover6, R.drawable.cover7, R.drawable.cover8};
    public static int home_covers[] = {R.drawable.home_cover0, R.drawable.home_cover1, R.drawable.home_cover2, R.drawable.home_cover3, R.drawable.home_cover4,
            R.drawable.home_cover5, R.drawable.home_cover6, R.drawable.home_cover7, R.drawable.home_cover8};
    public static int MENU_LAYOUT_WIDTH = 306;

    //AddVideoActivity
    public static int CONNECT_AP_OVERTIME = 20000;
    public static final int EBINDCID_IN_BINDING = 8;
    public static final int CUSTOM_BIND = 0;
    public static final int COVER_BIND = 1;
    //判断软件版本号，低于2.4.2时，局域网传输升级包
    public static final String UPGRADE_VERSION = "2.4.4.17";
    public static final String UPGRADE_FILE_V1 = "v1.bin";
    public static final String UPGRADE_FILE_V2 = "v2.bin";
    //ChooseDeviceFragment
    public static final String PARAM_SCAN_LIST = "SCAN_LIST_PARAM";
    //SubmitWifiInfoFragment
    public static final String PARAM_BIND_CID = "PARAM_BIND_CID";


    //支持局域网查看的起始版本
    public static final String SUPPORT_LAN_CHAECK_VERSION = "2.4.5.10";
    //RECV_DISCONN
    public final static int ERROR_SERVER_ERROR_START = 100;
    public final static int CAUSE_PEERNOTEXIST = ERROR_SERVER_ERROR_START;
    public final static int CAUSE_PEERDISCONNECT = 101;
    public final static int CAUSE_PEERINCONNECT = 102;
    public final static int CAUSE_CALLER_NOTLOGIN = 103;
    public final static int CAUSE_BELL_CONNECTED = 104;

    //FaceTime
    public final static String IS_CALLED_FROM_FACETIME = "IS_CALLED_FROM_FACETIME";


    /**
     * the flag for device setting week
     */
    public static String ALARM_WEEKS = "ALARM_WEEKS";


    public static String SELECT_INDEX = "SELECT_INDEX";
    public static String SELECT_SOUND_INDEX = "SELECT_SOUNDswLogN_INDEX";
    public static String SELECT_SENS_INDEX = "SELECT_SENS_INDEX";


    /**
     * 自动录像*
     */
    public static final int AUTO_RECORD1 = 0;
    public static final int AUTO_RECORD2 = 1;
    public static final int AUTO_RECORD3 = 2;


    /**
     * 设备设置*
     */
    public static final String K_LED = "led";
    public static final String K_DIRECTION = "direction";
    public static final String K_TIMEZONE = "timezone";
    public static final String K_TIMEZONESTR = "timezonestr";
    public static final String K_VIDEO_MODEL = "auto_record";
    public static final String K_BEGIN_TIME = "warn_begin_time";
    public static final String K_END_TIME = "warn_end_time";
    public static final String K_WEEKS = "warn_week";
    public static final String K_ENABLE = "warn_enable";
    public static final String K_CIDLIST = "cidlist";
    public static final String K_SOUND = "sound";
    public static final String K_SOUND_LONG = "sound_long";
    public static final String VIDEOINFO = "videoinfo";


    /**
     * cid_set  flag
     */
    public static final int FLAG_AUTO_RECORD = 0x01;
    public static final int FLAG_VIDEO_DIRECTION = 0x02;
    public static final int FLAG_TIMEZONE = 0x03;
    public static final int FLAG_LED = 0x04;
    public static final int FLAG_LOCATION = 0x05;
    public static final int FLAG_ISMOBILE = 0x06;
    public static final int FLAG_ISNTSC = 0x07;

    /**
     * K_ENABLE Value
     */
    public static final int WARN_ENABLE = 1;
    public static final int WARN_UNENABLE = 0;


    public static String ALARMINFO = "ALARMINFO";
    public static String ALARM_VOICE_LONG = "ALARM_VOICE_LONG";


    public static final String TIMEZONE_DATA = "TIMEZONE_DATA";
    public static final String TIMEZONE_SET_POS = "TIMEZONE_SET_POS";

    /***
     * MessageService
     ***/
    public static final int WARM_NOTIFY_FLAG = 0x01;
    public static final String MESSAGE_ID = "MESSAGEID";
    public static final String MESSAGE_DATA = "MESSAGE_DATA";

    /**
     * MyApp
     **/
    public static final String MESSAGE_ACTION = "COM.CYLAN.JIAFEIGOU.MESSAGE_ACTION";
    /**
     * push type
     */
    public static final int PUSH_TYPE_MSGCENTER = -1;
    public static final int PUSH_TYPE_OFFLINE = 0;//未使用
    public static final int PUSH_TYPE_ONLINE = 1;//未使用
    public static final int PUSH_TYPE_WARN_ON = 2;// 开启警报
    public static final int PUSH_TYPE_WARN = 3;
    public static final int PUSH_TYPE_WARN_OFF = 4;// 关闭警报
    public static final int PUSH_TYPE_LOW_BATTERY = 5;
    public static final int PUSH_TYPE_TEMP_HUMI = 6;// 温湿度
    public static final int PUSH_TYPE_SDCARD_OFF = 7;
    public static final int PUSH_TYPE_UNHELLO = 8;// 解除绑定
    public static final int PUSH_TYPE_HELLO = 9;// 绑定
    public static final int PUSH_TYPE_SYSTEM = 10;// 1 系统消息,用于计数
    public static final int PUSH_TYPE_NEW_VERSION = 11; // 1版本升级
    public static final int PUSH_TYPE_WARN_REPORT = 12; // 1 统计报告
    public static final int PUSH_TYPE_SDCARD_ON = 13;// SD卡接入
    public static final int PUSH_TYPE_REBIND = 14;// 重复绑定
    public static final int PUSH_TYPE_REGISTER = 15;// 新用户注册
    public static final int PUSH_TYPE_SHARE = 16;// 分享
    public static final int PUSH_TYPE_UNSHARE = 17;// 取消分享
    public static final int PUSH_TYPE_MAGNET_ON = 18;   //1 门磁打开
    public static final int PUSH_TYPE_MAGNET_OFF = 19;   //1 门磁关闭
    public static final int PUSH_TYPE_IR = 20;   //1 红外感应
    public static final int PUSH_TYPE_AIR_DETECTOR = 21;   //1 空气检测

    public static final String DOOR_BELL_TIME = "DOOR_BELL_TIME";
    public static final String FACE_TIME_TIME = "FACE_TIME_TIME";
    //支持局域网播放的版本
    public static final String SUPPORT_LAN_PLAY_VERSION = "2.4.4.18";
    //门铃分辨率
    public static final String DOORBELL_RESOLUTION = "720x576";

    public static final String FACETIME_RESOLUTION = "240x320";

    /**
     * simple notice push
     */
    public static final int PUSH_TYPE_NO_ANSWER = 1;
    public static final int FACE_TIME_NOTIFY_FLAG = 0x03;
    public static final int MAG_WARN_NOTIFY_FLAG= 0x04;
    public static final String ACTION_PULL_SERVICE = "ACTION_PULL_SERVICE";

    //支持局域网播放的门铃版本
    public static final String SUPPORT_LAN_PLAY_BELL_VERSION = "1.3.9.7";

    //是否主动查看门铃
    public static final String IS_ACTIVE_CHECK_BELL = "IS_ACTIVE_CHECK_BELL";

    //yun addr
    public static final String YUN_ADDR = "yun.jfgou.com";

}
