package com.cylan.jiafeigou.n.mvp.model;

/**
 * Created by cylan-hunt on 16-8-2.
 */
public class CamInfoBean {
    public String ssid;
    public boolean isMobileNet;
    public Safe safe;
    public String safeSummary;

    //待机
    public boolean isStandby;
    //设备指示灯
    public boolean isDeviceIndicator;

    public boolean isRotatable;
    //支持 市电 110V
    public boolean isSupport110V;

    //安全防护
    public static class Safe {
        public boolean isMotionDetect;

        public int sensitivity;
        //0xff00 高4位表示选项，低4位表示时间。
        public int warnNotification;
        public long startTime;
        public long endTime;
        //0x0111,1111表示 周日，周六，周五，周四，周三，周二，周一
        public long repeatMode;
    }

    public static class Detail {
        public String nickName;
        public int timeZone;
        public int sdCardState;
        public int netMobile;
        public String netWifi;
        public String cid;
        public String mac;
        public String sysVersion;
        public String packVersion;
        public int batterLevel;
        private int freeMomory;
    }

}
