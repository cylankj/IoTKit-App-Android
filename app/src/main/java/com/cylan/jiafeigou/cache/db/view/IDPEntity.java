package com.cylan.jiafeigou.cache.db.view;

/**
 * Created by yanzhendong on 2017/3/1.
 */

public interface IDPEntity extends IEntity<IDPEntity> {

    IDPEntity setMsgId(Integer msgId);

    Integer getMsgId();

    IDPEntity setVersion(Long version);

    Long getVersion();

    IDPEntity setUuid(String uuid);

    IDPEntity setAccount(String account);

    String getAccount();

    String getUuid();

    byte[] getBytes();

    IDPEntity setBytes(byte[] bytes);

}
