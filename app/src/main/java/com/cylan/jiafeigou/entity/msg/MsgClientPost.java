package com.cylan.jiafeigou.entity.msg;

import com.cylan.publicApi.MsgpackMsg;

import org.msgpack.annotation.Ignore;
import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

import java.util.List;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 10:30
 */
@Message
public  class MsgClientPost extends MsgpackMsg.MsgHeader {

    @Ignore
    public static final int BIND = 0;
    @Ignore
    public static final int SETWIFI = 1;

    public MsgClientPost(String callee) {
        this.callee=callee;
        msgId = MsgpackMsg.CLIENT_POST;
    }

    @Index(3)
    public int postType;//0-绑定，1-设置WiFi
    @Index(4)
    public List<String> array;
//        string,   SSID
//        string,   密码
//        string,   加密方式
//        string,   频段
//        string,   IP


}
