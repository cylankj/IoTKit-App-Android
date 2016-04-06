package com.cylan.jiafeigou.entity.msg.rsp;

import com.cylan.jiafeigou.entity.msg.EfamilyData;
import com.cylan.jiafeigou.entity.msg.RspMsgHeader;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

import java.io.Serializable;
import java.util.List;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 10:19
 */
@Message
public class MsgEfamilyListRsp extends RspMsgHeader implements Serializable {
    @Index(5)
    public List<EfamilyData> data;// 列表
    @Index(6)
    public List<EfamilyData> data1;//温湿度
    @Index(7)
    public List<EfamilyData> data2;//空气质量，粉尘
}

