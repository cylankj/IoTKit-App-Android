package com.cylan.jiafeigou.cache.db.module.tasks;

import com.cylan.entity.jniCall.JFGMsgHttpResult;
import com.cylan.entity.jniCall.JFGResult;
import com.cylan.entity.jniCall.RobotoGetDataRsp;
import com.cylan.jiafeigou.base.module.BasePropertyParser;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.base.view.IPropertyParser;
import com.cylan.jiafeigou.base.view.JFGSourceManager;
import com.cylan.jiafeigou.cache.db.impl.BaseDBHelper;
import com.cylan.jiafeigou.cache.db.view.IDBHelper;
import com.cylan.jiafeigou.cache.db.view.IDPEntity;
import com.cylan.jiafeigou.cache.db.view.IDPMultiTask;
import com.cylan.jiafeigou.cache.db.view.IDPSingleTask;
import com.cylan.jiafeigou.cache.db.view.IDPTaskResult;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.google.gson.Gson;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;

/**
 * Created by yanzhendong on 2017/3/2.
 */

public abstract class BaseDPTask<T extends IDPTaskResult> implements IDPSingleTask<T>, IDPMultiTask<T> {
    protected IDPEntity entity;
    protected List<IDPEntity> multiEntity;
    protected IDBHelper mDPHelper;
    protected JFGSourceManager sourceManager;
    protected IPropertyParser propertyParser;
    protected static Gson parser = new Gson();
    public static final long GLOBAL_NET_OPERATION_TIME_OUT = 10;

    @Override
    public <R extends IDPMultiTask<T>> R init(List<IDPEntity> cache) throws Exception {
        this.mDPHelper = BaseDBHelper.getInstance();
        this.sourceManager = DataSourceManager.getInstance();
        this.propertyParser = BasePropertyParser.getInstance();
        this.multiEntity = cache;
        this.entity = cache.get(0);
        return (R) this;
    }

    @Override
    public <R extends IDPSingleTask<T>> R init(IDPEntity cache) throws Exception {
        this.mDPHelper = BaseDBHelper.getInstance();
        this.sourceManager = DataSourceManager.getInstance();
        this.propertyParser = BasePropertyParser.getInstance();
        this.entity = cache;
        return (R) this;
    }


    //以 make 开头的是和 DP 打交道的,因此是有网操作
    protected Observable<RobotoGetDataRsp> makeGetDataRspResponse(long seq) {
        return RxBus.getCacheInstance().toObservable(RobotoGetDataRsp.class)
                .filter(rsp -> rsp.seq == seq).first().timeout(GLOBAL_NET_OPERATION_TIME_OUT, TimeUnit.SECONDS);
    }

    protected Observable<RxEvent.SetDataRsp> makeSetDataRspResponse(int seq) {
        return RxBus.getCacheInstance().toObservable(RxEvent.SetDataRsp.class)
                .filter(rsp -> ((int) rsp.seq) == seq).first().timeout(GLOBAL_NET_OPERATION_TIME_OUT, TimeUnit.SECONDS);
    }

    protected Observable makeGetDataRequest() {
        return null;
    }

    protected Observable<RxEvent.DeleteDataRsp> makeDeleteDataRspResponse(long seq) {
        return RxBus.getCacheInstance().toObservable(RxEvent.DeleteDataRsp.class)
                .filter(rsp -> rsp.seq == seq).first().timeout(GLOBAL_NET_OPERATION_TIME_OUT, TimeUnit.SECONDS);
    }

    protected Observable<JFGMsgHttpResult> makeHttpDoneResultResponse(long seq) {
        return RxBus.getCacheInstance().toObservable(JFGMsgHttpResult.class)
                .filter(ret -> ret.requestId == seq).first().timeout(GLOBAL_NET_OPERATION_TIME_OUT, TimeUnit.SECONDS);
    }

    protected Observable<JFGResult> makeGetJFGResultResponse(long seq) {
        return RxBus.getCacheInstance().toObservable(JFGResult.class)
                .filter(ret -> ret.seq == seq).first().timeout(GLOBAL_NET_OPERATION_TIME_OUT, TimeUnit.SECONDS);
    }
}
