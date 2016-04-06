package com.cylan.jiafeigou.entity.msg;

import com.cylan.jiafeigou.base.MyApp;
import com.cylan.jiafeigou.utils.PathGetter;
import com.cylan.jiafeigou.utils.PreferenceUtil;

import org.msgpack.annotation.Ignore;
import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

import java.io.Serializable;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-01-08
 * Time: 10:32
 */
@Message
public class EfamlMsg implements Serializable, Comparable<EfamlMsg> {
    @Ignore
    public static final int MSG_WORD = 0x00;
    @Ignore
    public static final int ACTIVE_CALL = 0x01;
    @Ignore
    public static final int PASSIVE_CALL = 0x02;
    @Ignore
    public int send_state;
    @Ignore
    public static final int SENDSUC = 0x00;
    @Ignore
    public static final int SENDFAIL = 0x01;
    @Ignore
    public static final int SENDING = 0x02;
    @Ignore
    public boolean isPlay;//是否在播放

    @Index(0)
    public int msgType;  //0-留言消息 1-主动呼叫 2-被动接听
    @Index(1)
    public long timeBegin;
    @Index(2)
    public int timeDuration;  //时长
    @Index(3)
    public int isRead; //是否已读
    @Index(4)
    public String url;

    @Ignore
    public String downUrl(String cid) {
        return String.format(PreferenceUtil.getOssUrl(MyApp.getContext()), cid, url);
    }

    @Ignore
    public String filePath(String cid) {
        return PathGetter.getRecordAudioPath(MyApp.getContext(), cid, "" + timeBegin);
    }


    @Override
    public int compareTo(EfamlMsg another) {
        return this.timeBegin < another.timeBegin ? -1 : 1;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        return this.timeBegin == ((EfamlMsg) o).timeBegin;
    }

    @Override
    public String toString() {
        return "EfamlMsg{" +
                "send_state=" + send_state +
                ", isPlay=" + isPlay +
                ", msgType=" + msgType +
                ", timeBegin=" + timeBegin +
                ", timeDuration=" + timeDuration +
                ", isRead=" + isRead +
                ", url='" + url + '\'' +
                '}';
    }
}
