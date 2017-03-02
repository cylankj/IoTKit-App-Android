package com.cylan.jiafeigou.cache.db.module.tasks;

import com.cylan.entity.jniCall.RobotoGetDataRsp;
import com.cylan.jiafeigou.cache.db.view.IDPEntity;
import com.cylan.jiafeigou.cache.db.view.IDPTask;
import com.cylan.jiafeigou.cache.db.view.IDPTaskResult;
import com.cylan.jiafeigou.rx.RxBus;

import java.util.concurrent.TimeUnit;

import rx.Observable;

/**
 * Created by yanzhendong on 2017/3/2.
 */

public abstract class BaseDPTask<T extends IDPTaskResult> implements IDPTask<T> {
    protected IDPEntity entity;

    @Override
    public <R extends IDPTask<T>> R init(IDPEntity cache) {
        this.entity = cache;
        return (R) this;
    }

    //以 make 开头的是和 DP 打交道的,因此是有网操作
    protected Observable<RobotoGetDataRsp> makeGetDataRspResponse(long seq) {
        return RxBus.getCacheInstance().toObservable(RobotoGetDataRsp.class)
                .filter(rsp -> rsp.seq == seq).first().timeout(10, TimeUnit.SECONDS);
    }

    protected Observable makeSetDataRspResponse(long seq) {
        return null;
    }

    protected Observable makeGetDataRequest() {
        return null;
    }
}
