package com.cylan.jiafeigou.base.module;

import java.util.List;

/**
 * Created by yanzhendong on 2017/3/17.
 */

public interface PanoramaEvent {

    class MsgFileListRsp {
        public List<String> files;
    }

    class MsgFileRsp {
        public int ret;
        public List<String> files;
    }

    class MsgVideoStatusRsp {
        public int ret;//错误码 -1是没有录像，0是正在录像
        public int seconds;// 视频录制的时长, 单位秒
        public int videoType;// 特征值定义：videoTypeShort =1 8s短视频；videoTypeLong =2长视频；
    }

    class MsgSdInfoRsp {
        public int sdIsExist;//sd卡是否存在
        public int sdcard_recogntion;//错误号。0 正常； 非0错误，需要格式化
        public long storage;//卡容量 单位byte
        public long storage_used;//已用空间 单位byte
    }

    class MsgResolutionRsp {
        public int ret;
        public int resolution;
    }

    class MsgLogoRsp {
        public int ret;//":0,//0为成功，-1是失败
        public int logtype;//:1//可以是1/2/3/4
    }

    class MsgRsp {
        public int ret;
    }

    class MsgBatteryRsp {
        public int battery;
    }

    class MsgPowerLineRsp {
        public int powerline;
    }
}
