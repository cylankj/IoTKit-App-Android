package com.cylan.jiafeigou.base.module;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

/**
 * Created by hds on 17-4-11.
 */

public class Base {


    @Message
    public static class ForewordSedHeader extends ForewordMsgHeader {
        //1.如果是客户端发起，则为设备CID数组；
        //2.如果是设备端发起：
        //dst为空数组时，则服务器查询主账号，再查询sessid，填充后转发给客户端；
        //dst为账号数组时，服务器查询sessid，填充后转发给客户端； --- 第三方账号，绑定关系不在加菲狗平台。 2016.4.8
        @Index(4)
        public String[] cidArray;
        @Index(5)
        public int isAck;//非零需要对端响应，零不需要对端响应
        @Index(6)
        public int msgId;//消息类型
        @Index(7)
        public byte[] msgByte;//数据
    }

    @Message
    public static class ForewordMsgHeader {
        @Index(0)
        public int mId = 20006;//默认20006
        @Index(1)
        public String mCaller = "";
        @Index(2)
        public String mCallee = "";
        @Index(3)
        public long mSeq;

        @Override
        public String toString() {
            return "ForewordMsgHeader{" +
                    "mId=" + mId +
                    ", mCaller='" + mCaller + '\'' +
                    ", mCallee='" + mCallee + '\'' +
                    ", mSeq=" + mSeq +
                    '}';
        }
    }

}
