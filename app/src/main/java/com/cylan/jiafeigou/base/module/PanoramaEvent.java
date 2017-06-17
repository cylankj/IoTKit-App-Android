package com.cylan.jiafeigou.base.module;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.udpMsgPack.JfgUdpMsg;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

import java.util.List;

/**
 * Created by yanzhendong on 2017/3/17.
 */

public interface PanoramaEvent {

    int ERROR_CODE_HTTP_NOT_AVAILABLE = -20000;


    @Message
    class ReportMsg extends JfgUdpMsg.UdpRecvHeard {
        @Index(2)
        public byte[] bytes;
    }

    @Message
    class ReportMsgList {
        @Index(0)
        public List<DpMsgForward> msgForwards;
    }

    @Message
    class DpMsgForward {
        @Index(0)
        public long id;
        @Index(1)
        public long version;
        @Index(2)
        public byte[] packValue;

        public JFGDPMsg msg() {
            return new JFGDPMsg(id, version, packValue);
        }
    }

    @Message
    class MsgForward {
        @Index(0)
        public int mId = 20006;//默认20006
        @Index(1)
        public String mCaller = "mCaller";
        @Index(2)
        public String mCallee = "mCallee";
        @Index(3)
        public long mSeq;
        // 1.如果是客户端发起，则为设备CID数组；
        // 2.如果是设备端发起：
        //    a. 服务器查询主账号，再查询sessid，填充后转发给客户端；
        //    b. dst为账号数组时，服务器查询sessid，填充后转发给客户端（暂未支持）； --- 第三方账号，绑定关系不在加菲狗平台。
        @Index(4)
        public List<String> dst;
        @Index(5)
        public int isAck;//非零需要对端响应，零不需要对端响应
        @Index(6)
        public int type;// 功能定义。见下表定义
        @Index(7)
        public byte[] msg = new byte[0];// 最大长度64K
    }

    @Message
    class MsgFileListReq {
        @Index(0)
        int beginTime;//  查询开始时间， unix timestamp 单位秒。填充 0 时， 设备端从本地最早的文件开始查询。
        @Index(1)
        int endTime;//    查询截止时间， unix timestamp 单位秒。
        @Index(2)
        int limit;//      查询条数

        public MsgFileListReq(int beginTime, int endTime, int limit) {
            this.beginTime = beginTime;
            this.endTime = endTime;
            this.limit = limit;
        }
    }

    @Message
    class MsgFileListRsp {
        @Index(0)
        public List<String> files;
    }


    @Message
    class MsgFileReq {
        @Index(0)
        public List<String> fileName;
        @Index(1)
        public int deleteType;

        public MsgFileReq(List<String> fileName, int deleteType) {
            this.fileName = fileName;
            this.deleteType = deleteType;
        }

    }

    @Message
    class MsgFileRsp {
        @Index(0)
        public int ret;
        @Index(1)
        public List<String> files;
    }

    @Message
    class MsgVideoStatusRsp {
        @Index(0)
        public int ret;//错误码 -1是没有录像，0是正在录像
        @Index(1)
        public int seconds;// 视频录制的时长, 单位秒
        @Index(2)
        public int videoType;// 特征值定义：videoTypeShort =1 8s短视频；videoTypeLong =2长视频；
    }

    @Message
    class MsgSdInfoRsp {
        @Index(0)
        public int sdIsExist;//sd卡是否存在
        @Index(1)
        public int sdcard_recogntion;//错误号。0 正常； 非0错误，需要格式化
        @Index(2)
        public long storage;//卡容量 单位byte
        @Index(3)
        public long storage_used;//已用空间 单位byte
    }

    @Message
    class MsgResolutionRsp {
        @Index(0)
        public int ret;
        @Index(1)
        public int resolution;
    }
    @Message
    class MsgUpgradeStatusRsp {
        @Index(0)
        public int resolution;
    }


    @Message
    class MsgReq {
        @Index(0)
        public int req;

        public MsgReq(int req) {
            this.req = req;
        }
    }

    @Message
    class MsgLogoRsp {
        @Index(0)
        public int ret;//":0,//0为成功，-1是失败
        @Index(1)
        public int logtype;//:1//可以是1/2/3/4
    }

    @Message
    class MsgRsp {
        @Index(0)
        public int ret;

        public MsgRsp() {
        }

        public MsgRsp(int ret) {
            this.ret = ret;
        }
    }

    @Message
    class MsgBatteryRsp {
        @Index(0)
        public int battery;
    }

    @Message
    class MsgPowerLineRsp {
        @Index(0)
        public int powerline;
    }

    @Message
    class TP {
        @Index(0)
        public int ret;
        @Index(1)
        public String pitcure;
    }

    @Message
    class Msg {
    }
}
