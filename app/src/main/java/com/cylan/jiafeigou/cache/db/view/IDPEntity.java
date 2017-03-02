package com.cylan.jiafeigou.cache.db.view;

/**
 * Created by yanzhendong on 2017/3/1.
 */

public interface IDPEntity {
    IDPEntity setMsgId(Integer msgId);

    Integer getMsgId();

    IDPEntity setVersion(Long version);

    Long getVersion();

    IDPEntity setUuid(String uuid);

    String getUuid();

    IDPEntity setTag(String tag);

    String getTag();

    IDPEntity setState(String state);

    String getState();

}
