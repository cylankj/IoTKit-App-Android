package com.cylan.jiafeigou.cache.db;

import rx.Observable;

/**
 * Created by yanzhendong on 2017/2/27.
 */

public interface DPHelperInterface {

    Observable saveDPByte(String uuid, long version, int msgId, byte[] bytes);

    Observable queryDPMsg(String uuid, long version, int msgId, int limit);
}
