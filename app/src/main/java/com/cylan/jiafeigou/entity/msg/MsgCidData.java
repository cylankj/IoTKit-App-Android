package com.cylan.jiafeigou.entity.msg;

import android.text.TextUtils;

import com.cylan.jiafeigou.base.MyApp;
import com.cylan.jiafeigou.utils.PathGetter;

import org.msgpack.annotation.Ignore;
import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

import java.io.Serializable;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 09:37
 */
@Message
public class MsgCidData implements Serializable {

    @Ignore
    public final static int CID_NET_OFFLINE = 0;
    @Ignore
    public final static int CID_NET_WIFI = 1;
    @Ignore
    public final static int CID_NET_3G = 2;
    @Ignore
    public final static int CID_NET_CONNECT = 3;

    @Index(0)
    public String cid;
    @Index(1)
    public String alias;
    @Index(2)
    public int magstate;
    @Index(3)
    public int is_recv_warn;
    @Index(4)
    public String share_account;
    @Index(5)
    public int os;
    @Index(6)
    public int net;
    @Index(7)
    public String name;
    @Index(8)
    public String version;
    @Index(9)
    public int sdcard;
    @Index(10)
    public int err;
    @Index(11)
    public int battery;
    @Index(12)
    public float tmp;
    @Index(13)
    public int humi;
    @Index(14)
    public long sync_time;
    @Index(15)
    public int vid;
    @Index(16)
    public long relayMask;
    @Index(17)
    public byte natType;
    @Index(18)
    public int noAnswerBC;

    @Ignore
    public String mName() {
        return TextUtils.isEmpty(alias) ? cid : alias;
    }

    @Ignore
    public String url;

    @Ignore
    public String mThumbPath() {
        if (url != null) {
            return url;
        }
        return PathGetter.getCoverPath(MyApp.getContext(), cid);
    }

}
