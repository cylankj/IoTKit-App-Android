package com.cylan.utils;

public class Constants {
    //    public static String ADDR = Config.RELAY_ADDR;
    /**
     * ************WIFI 加密方式*****************
     */
    public static final int JFG_WIFI_OPEN = 0;
    public static final int JFG_WIFI_WEP = 1;
    public static final int JFG_WIFI_WPA = 2;
    public static final int JFG_WIFI_WPA2 = 3;

    public static String ADDR = Config.ADDR;
    public static String WEB_ADDR = Config.ADDR;
    public static int CONFERENCE_PORT = Config.CONFERENCE_PORT;
    public static int WEB_PORT = Config.WEB_PORT;

    public static String RELAY_ADDR = Config.RELAY_ADDR;
    public static int RELAY_PORT = Config.RELAY_PORT;

    public static final int WS_POST = 8888;
    public static final int HTTPS_PORT = 443;

    public static final String MOD = "mod";
    public static final String ACT = "act";
    public static final String NRET = "nret";
    public static final String CLIENT = "client";
    public static final String RET = "ret";
    public static final String MSG = "msg";
    public static final String OS = "os";
    public static final String VERSION = "version";
    public static final String PHONE = "phone";
    public static final String PASS = "pass";
    public static final String OLDPASS = "oldpass";
    public static final String NEWPASS = "newpass";
    public static final String SESSID = "sessid";
    public static final String TYPE = "type";
    public static final String CID = "cid";
    public static final String URL = "url";
    public static final String NAME = "name";
    public static final String LAST_MODIFY = "lastModified";
    public static final String ACCOUNT = "account";
    public static final String ACCOUNT_LIST = "accountlist";
    public static final String UNDO_LIST = "undo_accountlist";
    public static final String SHARE_LIST = "share_account";

    public static final int PHONEUSER_TYPE_REGISTER = 0;
    public static final int PHONEUSER_TYPE_SHARE = 1;

    // for thumbs
    public static final String PHOTO_PATH_SDCARD = "/mnt/sdcard/DCIM/Camera/";
    public static final String PHOTO_PATH_DEFAULT = "/DCIM/Camera/";

    public static final String THUMB_NAME = "thumb.png";

    public static final int CLIENT_GETCODE_TYPE_REGISTER = 0;
    public static final int CLIENT_GETCODE_TYPE_FORGETPASS = 1;
    public static final int CLIENT_GETCODE_TYPE_EDIT_USERINFO = 2;

    // for version
    public static final int HAS_NEW_VERSION_NO = 0;
    public static final int HAS_NEW_VERSION_YES = 1;

    // server
    public static final int BINDCID_UNEXIST = 2; //
    public static final int BINDCID_IN_BINDING = 1; //
    public static final int RETOK = 0; //
    public static final int RETFALSE = -1; //
    public static final int RETFALSE_SESSION = -2; // session
    public static final int RETFALSE_DATABASE = -3; //
    public static final int HTTP_RETOK = 200; //
    public static final int DEFAULT_VALUE = -1; //

    // CID status
    // public static final int CID_STATUS_NOT_ONLINE = 0;
    // public static final int CID_STATUS_ONLINE = 1;
    // public static final int CID_STATUS_IN_USING = 2;
    // public static final int CID_STATUS_EXPIRED = 3;


    public static boolean IS_PUSH_CAMERA = false;

    // os:
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
    public static final int OS_CAMERA_4G = 16;

    public static final String RegPhone = "[1-9]\\d{10}$";
    public static final String RegPSW = "^\\d{6}$";
    // public static final String RegText = "[a-zA-Z0-9_\u4e00-\u9fa5]+$";
    public static final String RegText = "[\\s]+";

    public static final String RegTrim = "^\\s*|\\s*$";

    public static final String RegBlank = "\\s";

    public static final String RegCode = "\\d{6}$";


    public static final int APKeyCode = 80;
//    public static final int APKeyCode = android.os.Build.MODEL.equals("DOG-3G72") ? 80
//            : KeyEvent.KEYCODE_VOLUME_UP;

    public static String VERSION_DATE = "";

    public static String PUSH_SERVER_IP = "yun.jfgou.com";
    public static int PUSH_SERVER_PORT = 2001;

    public static int DIRECTION = 0;
    public static int TIMEZONE = 8;


    public static final int REGISTER_TYPE_PHONE = 0;
    public static final int REGISTER_TYPE_EMAIL = 1;
    public static final int REGISTER_TYPE_NOACCOUNT = 2;
    public static final int REGISTER_TYPE_QQ = 3;
    public static final int REGISTER_TYPE_SINA_WEIBO = 4;

    public static final int LANGUAGE_TYPE_CHINESE = 0;
    public static final int LANGUAGE_TYPE_ENGLISH = 1;
    public static final int LANGUAGE_TYPE_RU = 2;

    // for upgrading light
//    public static final String UPGRADING_ACTION = "com.cylan.camera.android.UPGRADING";

    public static class URL_DATA {
        public static final String SESSID = "&sessid";
        public static final String ACT = "&act";
        public static final String CID = "&cid";
        public static final String TYPE = "&type";
        public static final String FILE = "&file";
        public static final String WARNPICID = "&id";
        public static final String TIME = "&time";
        public static final String RECORD_STATE = "&is_record";

    }

    public final static String PREFIX0 = "server ping state:";
    public final static String PREFIX1 = "server ping content:";
    public final static String PREFIX2 = "ping state:";
    public final static String PREFIX3 = "ping content:";
    public final static String HTTP_QQ = "http://www.qq.com";
    public final static String _QQ = "www.qq.com";
    /**
     * 程序文件存放目录
     */
    public static final String ROOT_DIR = "Smarthome";
    public static final String BLOCK_FOLDER = "block";

    /**
     * 通过os判断是ucos的狗
     */
    public static boolean isUcosByOS(int os) {
        return os == Constants.OS_CAMERA_UCOS || os == Constants.OS_CAMERA_UCOS_V2;
    }


    /**
     * 通过os判断是门铃
     */
    public static boolean isDoorbellByOS(int os) {
        return os == Constants.OS_DOOR_BELL || os == Constants.OS_DOOR_BELL_V2;
    }
}
