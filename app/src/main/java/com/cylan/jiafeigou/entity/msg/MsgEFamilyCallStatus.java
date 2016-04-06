package com.cylan.jiafeigou.entity.msg;

import com.cylan.publicApi.MsgpackMsg;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

/**
 *
 * Created by yangc on 2016/3/11.
 */
@Message
public class MsgEFamilyCallStatus extends MsgpackMsg.MsgHeader{

    /**
     * @param caller
     * @param callee  位置填充为绑定的account
     */
    public MsgEFamilyCallStatus(String caller, String callee) {
        this.callee = callee;
        this.caller = caller;
        msgId = MsgpackMsg.CLIENT_CALL_EFAML_STATUS ;
    }

    @Index(3)
    public int isOk;  //True为接通，否则未通
    @Index(4)
    public long time;   //门铃的呼叫时间，ios和android客户端可以根据此时间判断是否收到了重复的呼叫，根据此时间可以找到对应的截图url
    @Index(5)
    public int timeDuration;  //通话时长
    @Index(6)
    public int type;  //区分海内外地址。由登录时，服务器推送的128消息下发。0:不发送截图 1:CN地址 2:US地址
    @Index(7)
    public int callType;   //2-efaml call 1-client call  //对客户端消息中type==0为留言，修改一下type的值方便处理
}
