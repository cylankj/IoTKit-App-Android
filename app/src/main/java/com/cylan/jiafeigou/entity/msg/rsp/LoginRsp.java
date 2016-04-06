package com.cylan.jiafeigou.entity.msg.rsp;

import com.cylan.jiafeigou.entity.msg.MsgSceneData;
import com.cylan.jiafeigou.entity.msg.MsgServer;
import com.cylan.jiafeigou.entity.msg.RspMsgHeader;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

import java.io.Serializable;
import java.util.List;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 09:38
 */
@Message
public class LoginRsp extends RspMsgHeader implements Serializable {

    @Index(5)
    public String sessid;
    @Index(6)
    public int os;
    @Index(7)
    public String version;
    @Index(8)
    public int language_type;
    @Index(9)
    public String account;
    @Index(10)
    public String url;
    @Index(11)
    public int is_upgrade;
    @Index(12)
    public List<MsgServer> server;
    @Index(13)
    public int msg_count;
    @Index(14)
    public int is_safe;
    @Index(15)
    public int vid;
    @Index(16)
    public List<MsgSceneData> data;
}
