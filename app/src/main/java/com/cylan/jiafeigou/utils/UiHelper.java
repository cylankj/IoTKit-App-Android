package com.cylan.jiafeigou.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by chen on 5/25/16.
 */
public class UiHelper {


    public static String TAG_LOGING_STATUS = "the cout has logined";

    /**
     * 相关权限
     */
    //摄像头 照相机
    public static final int REQUEST_SHOWCAMERA = 0;
    public static final String[] PERMISSION_SHOWCAMERA = new String[]{
            "android.permission.CAMERA",
    };
    //联系人
    public static final int REQUEST_SHOWCONTACTS = 1;
    public static final String[] PERMISSION_SHOWCONTACTS = new String[]{
            "android.permission.READ_CONTACTS",
            "android.permission.WRITE_CONTACTS"
    };
    //存储
    public static final int REQUEST_SHOWWRITE_STORAGE = 3;
    public static final String[] PERMISSION_SHOWWRITE_STORAGE = new String[]{
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.READ_EXTERNAL_STORAGE"
    };
    //位置
    public static final int REQUEST_SHOWLOCATION = 4;
    public static final String[] PERMISSION_SHOWLOCATION = new String[]{
            "android.permission.ACCESS_FINE_LOCATION"
    };
    //电话
    public static final int REQUEST_SHOWCALLPHONE = 5;
    public static final String[] PERMISSION_SHOWCALLPHONE = new String[]{
            "android.permission.CALL_PHONE"
    };
    //短信
    public static final int REQUEST_SHOWSMS = 6;
    public static final String[] PERMISSION_SHOWSMS = new String[]{
            "android.permission.RECEIVE_SMS",
            "android.permission.READ_SMS"
    };
    //麦克风
    public static final int REQUEST_SHOWAUDIO = 7;
    public static final String[] PERMISSION_SHOWSAUDIO = new String[]{
            "android.permission.MODIFY_AUDIO_SETTINGS",
            "android.permission.RECORD_AUDIO"
    };
    // 权限子项....

    //所有权限
    public static final int REQUEST_SHOWMULTIS = 25;
    public static final String[] PERMISSION_SHOWMULTIS = new String[]{
            "android.permission.CAMERA",
            "android.permission.READ_CONTACTS",
            "android.permission.WRITE_CONTACTS",
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.CALL_PHONE",
            "android.permission.RECEIVE_SMS",
            "android.permission.READ_SMS",
            "android.permission.MODIFY_AUDIO_SETTINGS",
            "android.permission.RECORD_AUDIO"
    };

    public static final int WONDELFUL_REFRESH_DELAY = 2500;


    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager
                    .getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }
}
