package com.cylan.jiafeigou.cache.db.module.tasks;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.cache.db.impl.BaseDPHelper;
import com.cylan.jiafeigou.cache.db.impl.BaseDPTaskResult;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.util.ArrayList;

import rx.Observable;
import rx.schedulers.Schedulers;

import static com.cylan.jiafeigou.misc.JfgCmdInsurance.getCmd;

/**
 * Created by yanzhendong on 2017/3/2.
 */

public class DPQueryTask extends BaseDPTask<BaseDPTaskResult> {
    protected boolean asc;
    protected int limit = 20;

    public DPQueryTask() {
    }

    public DPQueryTask(boolean asc, int limit) {
        this.asc = asc;
        this.limit = limit;
    }

    public DPQueryTask setAsc(boolean asc) {
        this.asc = asc;
        return this;
    }

    public DPQueryTask setLimit(int limit) {
        this.limit = limit;
        return this;
    }


    @Override
    public Observable<BaseDPTaskResult> execute() {
        return null;
    }


    private class LocalDPQueryTask extends DPQueryTask {
        @Override
        public Observable<BaseDPTaskResult> execute() {
            return BaseDPHelper.getInstance().queryDPMsg(entity.getUuid(), entity.getVersion(), entity.getMsgId(), asc, limit)
                    .map(items -> new BaseDPTaskResult()
                            .setResultCode(0)
                            .setResultResponse(items));
        }
    }

    private class ServerDPQueryTask extends DPQueryTask {

        @Override
        public Observable<BaseDPTaskResult> execute() {
            return Observable.create((Observable.OnSubscribe<Long>) subscriber -> {
                try {
                    AppLogger.d("正在发送查询请求,version:" + entity.getVersion() + "count:" + limit + "acs:" + asc);
                    ArrayList<JFGDPMsg> params = new ArrayList<>();
                    JFGDPMsg msg = new JFGDPMsg(entity.getMsgId(), entity.getVersion());
                    params.add(msg);
                    long seq = getCmd().robotGetData(entity.getUuid(), params, limit, asc, 0);//多请求一条数据,用来判断是否是一天最后一条
                    subscriber.onNext(seq);
                    subscriber.onCompleted();
                } catch (JfgException e) {
                    AppLogger.e(e.getMessage());
                    subscriber.onError(e);
                }
            })
                    .subscribeOn(Schedulers.io())
                    .filter(seq -> seq > 0)
                    .flatMap(this::makeGetDataRspResponse)
                    .map(rsp -> {
                        AppLogger.d("收到从服务器返回数据!!!");
                        DpMsgDefine.DPSet<DpMsgDefine.DPWonderItem> result = null;//.getValue(mUUID, DpMsgMap.ID_602_ACCOUNT_WONDERFUL_MSG, rsp.seq);
                        return null;
                    });
        }
    }
}
