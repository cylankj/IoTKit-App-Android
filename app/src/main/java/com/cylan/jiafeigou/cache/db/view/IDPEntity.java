package com.cylan.jiafeigou.cache.db.view;

/**
 * Created by yanzhendong on 2017/3/1.
 */

public interface IDPEntity {

    //这是表属性
    IDPEntity setMsgId(Integer msgId);

    Integer getMsgId();

    IDPEntity setVersion(Long version);

    Long getVersion();

    IDPEntity setUuid(String uuid);

    String getUuid();

    IDPEntity setAction(IDPAction action);

    String ACTION();

    IDPEntity setAction(String action);

    String getAction();

    IDPEntity setState(String state);

    String getState();

    byte[] getBytes();

    IDPEntity setBytes(byte[] bytes);

}
