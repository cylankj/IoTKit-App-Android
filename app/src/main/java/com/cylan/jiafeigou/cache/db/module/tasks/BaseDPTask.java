package com.cylan.jiafeigou.cache.db.module.tasks;

import com.cylan.entity.jniCall.JFGMsgHttpResult;
import com.cylan.entity.jniCall.RobotoGetDataRsp;
import com.cylan.jiafeigou.cache.db.impl.BaseDBHelper;
import com.cylan.jiafeigou.cache.db.view.IDPEntity;
import com.cylan.jiafeigou.cache.db.view.IDBHelper;
import com.cylan.jiafeigou.cache.db.view.IDPMultiTask;
import com.cylan.jiafeigou.cache.db.view.IDPSingleTask;
import com.cylan.jiafeigou.cache.db.view.IDPTaskResult;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;

/**
 * Created by yanzhendong on 2017/3/2.
 */

public abstract class BaseDPTask<T extends IDPTaskResult> implements IDPSingleTask<T>, IDPMultiTask<T> {
    protected IDPEntity singleEntity;
    protected List<IDPEntity> multiEntity;
    protected IDBHelper mDPHelper;

    @Override
    public <R extends IDPMultiTask<T>> R init(List<IDPEntity> cache) {
        this.mDPHelper = BaseDBHelper.getInstance();
        this.multiEntity = cache;
        this.singleEntity = cache.get(0);
        return (R) this;
    }

    @Override
    public <R extends IDPSingleTask<T>> R init(IDPEntity cache) {
        this.mDPHelper = BaseDBHelper.getInstance();
        this.singleEntity = cache;
        return (R) this;
    }


    //以 make 开头的是和 DP 打交道的,因此是有网操作
    protected Observable<RobotoGetDataRsp> makeGetDataRspResponse(long seq) {
        return RxBus.getCacheInstance().toObservable(RobotoGetDataRsp.class)
                .filter(rsp -> rsp.seq == seq).first().timeout(30, TimeUnit.SECONDS);
    }

    protected Observable<RxEvent.SetDataRsp> makeSetDataRspResponse(int seq) {
        return RxBus.getCacheInstance().toObservable(RxEvent.SetDataRsp.class)
                .filter(rsp -> ((int) rsp.seq) == seq).first().timeout(30, TimeUnit.SECONDS);
    }

    protected Observable makeGetDataRequest() {
        return null;
    }

    protected Observable<RxEvent.DeleteDataRsp> makeDeleteDataRspResponse(long seq) {
        return RxBus.getCacheInstance().toObservable(RxEvent.DeleteDataRsp.class)
                .filter(rsp -> rsp.seq == seq).first().timeout(30, TimeUnit.SECONDS);
    }

    protected Observable<JFGMsgHttpResult> makeHttpDoneResultResponse(long seq) {
        return RxBus.getCacheInstance().toObservable(JFGMsgHttpResult.class)
                .filter(ret -> ret.requestId == seq).first().timeout(30, TimeUnit.SECONDS);
    }

}
