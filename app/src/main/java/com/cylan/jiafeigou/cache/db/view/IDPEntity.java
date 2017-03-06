package com.cylan.jiafeigou.cache.db.view;

/**
 * Created by yanzhendong on 2017/3/1.
 */

public interface IDPEntity extends IEntity<IDPEntity> {
    //这是表属性
    IDPEntity setMsgId(Integer msgId);

    Integer getMsgId();

    IDPEntity setVersion(Long version);

    Long getVersion();

    IDPEntity setUuid(String uuid);

    String getUuid();

    byte[] getBytes();

    IDPEntity setBytes(byte[] bytes);

}
