package com.cylan.jiafeigou.cache.db.view;

/**
 * Created by yanzhendong on 2017/3/1.
 */

public interface IDPEntity {
    void setMsgId(Integer msgId);

    Integer getMsgId();

    void setVersion(Long version);

    Long getVersion();

    void setUuid(String uuid);

    String getUuid();

    void setTag(String tag);

    String getTag();

    void setState(String state);

    String getState();

}
