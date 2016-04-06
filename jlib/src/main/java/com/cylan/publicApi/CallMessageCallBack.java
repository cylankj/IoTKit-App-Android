package com.cylan.publicApi;

public interface CallMessageCallBack {
    enum MSG_TO_UI {
        // 解析服务器地址失败, param 为空
        RESOLVE_SERVER_FAILED,
        // 连接服务器失败, param 为空
        CONNECT_SERVER_FAILED,
        // 连接服务器成功, param 为空
        CONNECT_SERVER_SUCCESS,
        // 连接已断开, param 为空
        SERVER_DISCONNECTED,
        // msgapck消息，, param 为msgpack buffer
        MSGPACK_MESSAGE,
        // 收到呼叫， param 为 对端名称
        RECV_CALL,
        // 收到呼叫断开消息， param为空
        RECV_DISCONN,
        //  分辨率已解析, param格式如"720x576"
        NOTIFY_RESOLUTION,
        // 接收数据汇报，param格式如"10x40x1000x12324535345", 含义分别为"framerate bitrate videoRecvedBytes timestamp"
        NOTIFY_RTCP,
        // 传输已经准备好，可以开始启动视频, param为空
        TRANSPORT_READY,
        // 传输失败，一般需要断开连接, param为空
        TRANSPORT_FAILED,
        // Http请求完成 ,param为msgpack buffer，消息定义在smartCall/trunk/cylan/test/smartcall_jni.cc
        HTTP_DONE,
        RELAY_MASK_REPORT,
        //移动侦测报警
        MD_ALARM,
        //无图像输出报警
        NO_PIC,
        SINGAL_NOT_CONNECTED, //如果发送数据时服务器没连接上，刚会上报此数据。
        NTP_UPDATE //字符串时间戳
    }


    void handleMsg(int msgId, byte[] bytes);
}
