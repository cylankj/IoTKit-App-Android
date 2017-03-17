package com.cylan.jiafeigou.base.module;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

import java.util.List;

/**
 * Created by yanzhendong on 2017/3/17.
 */

public interface PanoramaEvent {

    int MIDRobotForwardDataV2 = 20006;
    int MIDRobotForwardDataV2Ack = 20007;

    int TYPE_FILE_DOWNLOAD_REQ = 1;//	下载请求	图片和视频文件管理	DOG_5W
    int TYPE_FILE_DOWNLOAD_RSP = 2;//	下载响应	图片和视频文件管理	DOG_5W
    int TYPE_FILE_DELETE_REQ = 3;//	删除请求	图片和视频文件管理	DOG_5W
    int TYPE_FILE_DELETE_RSP = 4;//	删除响应	图片和视频文件管理	DOG_5W
    int TYPE_FILE_LIST_REQ = 5;//	列表请求	图片和视频文件管理	DOG_5W
    int TYPE_FILE_LIST_RSP = 6;//	列表响应	图片和视频文件管理	DOG_5W
    int TYPE_TAKE_PICTURE_REQ = 7;//	拍照请求	图片和视频文件管理	DOG_5W
    int TYPE_TAKE_PICTURE_RSP = 8;//	拍照响应	图片和视频文件管理	DOG_5W
    int TYPE_VIDEO_BEGIN_REQ = 9;//	开始录像请求	图片和视频文件管理	DOG_5W
    int TYPE_VIDEO_BEGIN_RSP = 10;//	开始录像响应	图片和视频文件管理	DOG_5W
    int TYPE_VIDEO_END_REQ = 11;//	停止录像请求	图片和视频文件管理	DOG_5W
    int TYPE_VIDEO_END_RSP = 12;//	停止录像响应	图片和视频文件管理	DOG_5W
    int TYPE_VIDEO_STATUS_REQ = 13;//	查询录像状态请求	图片和视频文件管理	DOG_5W
    int TYPE_VIDEO_STATUS_RSP = 14;//	查询录像状态响应	图片和视频文件管理	DOG_5W


    @Message
    class RawMsgHeader {
        @Index(0)
        public int mId;
        @Index(1)
        public String mCaller;
        @Index(2)
        public String mCallee;
        @Index(3)
        public long mSeq;
    }

    @Message
    class RawReqMsg extends RawMsgHeader {
        @Index(4)
        public List<String> dst;
        @Index(5)
        public int isAck;
        @Index(6)
        public int type;
        @Index(7)
        public byte[] msg;
    }

    @Message
    class MSG_TYPE_FILE_DOWNLOAD_REQ {
        @Index(0)
        public String fileName;// 文件名, 注：根据后缀区分是图片或视频
        @Index(1)
        public byte[] md5;//      文件的md5值
        @Index(2)
        public int begin;//    起始位置
        @Index(3)
        public int offset;//   偏移量
    }

    @Message
    class MSG_TYPE_FILE_DOWNLOAD_RSP {
        @Index(0)
        public int ret;//       错误码
        @Index(1)
        public String fileName;//  文件名, 注：根据后缀区分是图片或视频
        @Index(2)
        public int begin;//     起始位置
        @Index(3)
        public int offset;//    偏移量
        @Index(4)
        public byte[] buffer;//    文件内容
    }

    @Message
    class MSG_TYPE_FILE_DELETE_REQ {
        @Index(0)
        public String fileName;//  文件名, 注：根据后缀区分是图片或视频
    }

    @Message
    @Deprecated
    class MSG_TYPE_FILE_DELETE_RSP {//直接用 Integer 类,不要用这个
        @Index(0)
        public int ret;// 错误码
    }

    @Message
    class MSG_TYPE_FILE_LIST_REQ {
        @Index(0)
        public int beginTime;// 查询开始时间，public   int timestamp;//单位秒。
        @Index(1)
        public int endTime;// 查询截止时间，unix timestamp 单位秒。
        @Index(2)
        public int limit;// 查询条数
    }

    @Message
    class MSG_TYPE_FILE_LIST_RSP {
        @Index(0)
        public String fileName;// 文件名，命名格式[timestamp].jpg 或[timestamp]_[secends].avi，timestamp是文件生成时间的unix时间戳，secends是视频录制的时长,单位秒。根据后缀区分是图片或视频。
        @Index(1)
        public int fileSize;// 文件大小, bit。
        @Index(2)
        public byte[] md5;// 文件的md5值
    }

    @Message
    class MSG_TYPE_TAKE_PICTURE_REQ {
    }

    @Message
    class MSG_TYPE_TAKE_PICTURE_RSP {
        @Index(0)
        public int ret;//       错误码
        @Index(1)
        public String fileName;//  文件名， 命名格式[timestamp].jpg 或 [timestamp]_[secends].avi， timestamp是文件生成时间的unix时间戳，secends是视频录制的时长,单位秒。根据后缀区分是图片或视频。
        @Index(2)
        public int fileSize;//  文件大小, bit。
        @Index(3)
        public byte[] md5;//  文件的md5值
    }

    @Message
    @Deprecated
    class MSG_TYPE_VIDEO_BEGIN_REQ {//直接用 Integer 类,不要用这个
        @Index(0)
        public int videoType;// 特征值定义： videoTypeShort = 1 8s短视频； videoTypeLong = 2 长视频；

        public MSG_TYPE_VIDEO_BEGIN_REQ(int videoType) {
            this.videoType = videoType;
        }
    }

    @Message
    @Deprecated
    class MSG_TYPE_VIDEO_BEGIN_RSP {//直接用Integer类,不要用这个
        @Index(0)
        public int ret;//       错误码
    }

    @Message
    @Deprecated
    class MSG_TYPE_VIDEO_END_REQ {//直接用 Integer 类,不要用这个
        @Index(0)
        public int videoType;// 特征值定义：videoTypeShort =1 8s短视频；videoTypeLong =2长视频；
    }

    @Message
    class MSG_TYPE_VIDEO_END_RSP {
        @Index(0)
        public int ret;//       错误码
        @Index(1)
        public String fileName;//  文件名， 命名格式[timestamp].jpg 或 [timestamp]_[secends].avi， timestamp是文件生成时间的unix时间戳，secends是视频录制的时长,单位秒。根据后缀区分是图片或视频。
        @Index(2)
        public int fileSize;//  文件大小, bit。
        @Index(3)
        public byte[] md5;//  文件的md5值
    }

    @Message
    class MSG_TYPE_VIDEO_STATUS_REQ {
    }

    @Message
    class MSG_TYPE_VIDEO_STATUS_RSP {
        @Index(0)
        public int ret;//       错误码
        @Index(1)
        public int secends;//   视频录制的时长,单位秒
        @Index(2)
        public int videoType;// 特征值定义：videoTypeShort =1 8s短视频；videoTypeLong =2长视频；
    }

    // type	功能	DP数组	设备型号
//        TYPE_DPID_REQ = 51;//	专用于查询DP类消息，APP与设备直连时使用	array(REQ)	DOG_5W
//        TYPE_DPID_RSP = 52;//	专用于响应DP类消息，APP与设备直连时使用	array(RSP)	DOG_5W
//        REQ RSP DPIDBaseSDStatus = 204;// DPIDBaseBattery = 206 DPIDVideoMic = 301 DPIDVideoSpeaker = 302
    @Message
    class RawRspMsg extends RawMsgHeader {
        @Index(4)
        public int type;// 功能定义
        @Index(5)
        public byte[] msg;// 最大长度64K
    }

}
