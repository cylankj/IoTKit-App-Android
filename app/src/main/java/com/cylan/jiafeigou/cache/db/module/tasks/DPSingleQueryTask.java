package com.cylan.jiafeigou.cache.db.module.tasks;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.cache.db.impl.BaseDPTaskResult;
import com.cylan.jiafeigou.cache.db.module.DPEntity;
import com.cylan.jiafeigou.cache.db.view.DBAction;
import com.cylan.jiafeigou.cache.db.view.DBOption;
import com.cylan.jiafeigou.cache.db.view.IDPEntity;
import com.cylan.jiafeigou.cache.db.view.IDPSingleTask;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.util.ArrayList;

import rx.Observable;
import rx.schedulers.Schedulers;

import static com.cylan.jiafeigou.misc.JfgCmdInsurance.getCmd;

/**
 * Created by yanzhendong on 2017/3/2.
 */

public class DPSingleQueryTask extends BaseDPTask<BaseDPTaskResult> {
    private DBOption.SingleQueryOption option;

    public DPSingleQueryTask() {
    }

    @Override
    public <R extends IDPSingleTask<BaseDPTaskResult>> R init(IDPEntity cache) throws Exception {
        this.option = cache.option(DBOption.SingleQueryOption.class);
        return super.init(cache);

    }

    @Override
    public Observable<BaseDPTaskResult> performLocal() {
        if (sourceManager.isOnline()) {//如果在线则直接返回
            return Observable.just(BaseDPTaskResult.SUCCESS);
        }
        if (option.type == 1) {//one by one 精准查询
            return mDPHelper.findDPMsg(entity.getUuid(), entity.getVersion(), entity.getMsgId())
                    .map(ret -> {
                        Object result = null;
                        if (DBAction.AVAILABLE.accept(ret.action())) {
                            sourceManager.clearValue(ret.getUuid(), ret.getMsgId());
                            sourceManager.setValue(ret.getUuid(), ret.getMsgId(), ret.getBytes(), ret.getVersion(), -1);
                            result = sourceManager.getValue(ret.getUuid(), ret.getMsgId());
                            if (result instanceof DpMsgDefine.DPSet) {
                                result = ((DpMsgDefine.DPSet) result).list();
                            }
                        }
                        return new BaseDPTaskResult().setResultCode(0).setResultResponse(result);
                    });
        } else if (option.type == 0) {
            return mDPHelper.queryDPMsg(entity.getUuid(), entity.getVersion() == 0 ? Long.MAX_VALUE : entity.getVersion(), entity.getMsgId(), option.asc, option.limit)
                    .map(items -> {
                        sourceManager.clearValue(entity.getUuid(), entity.getMsgId());
                        for (DPEntity item : items) {
                            sourceManager.setValue(item.getUuid(), item.getMsgId(), item.getBytes(), item.getVersion(), -1);
                        }
                        Object result = sourceManager.getValue(entity.getUuid(), entity.getMsgId(), -1);
                        if (result instanceof DpMsgDefine.DPSet) {
                            result = ((DpMsgDefine.DPSet) result).list();
                        }
                        return new BaseDPTaskResult().setResultResponse(result).setResultCode(0);
                    });
        }
        return Observable.just(BaseDPTaskResult.SUCCESS);
    }

    @Override
    public Observable<BaseDPTaskResult> performServer() {
        return Observable.create((Observable.OnSubscribe<Long>) subscriber -> {
            try {
                AppLogger.d("正在发送查询请求,uuid:" + entity.getUuid() + ",dpMsgVersion:" + entity.getVersion() + ",type" + option.type + "count:" + option.limit + "acs:" + option.asc);
                ArrayList<JFGDPMsg> params = new ArrayList<>();
                JFGDPMsg msg = new JFGDPMsg(entity.getMsgId(), entity.getVersion());
                params.add(msg);
                long seq = -1;
                if (option.type == 0) {
                    seq = getCmd().robotGetData(entity.getUuid() == null ? "" : entity.getUuid(), params, option.limit, option.asc, 0);//多请求一条数据,用来判断是否是一天最后一条
                } else if (option.type == 1) {
                    seq = getCmd().robotGetDataByTime(entity.getUuid() == null ? "" : entity.getUuid(), params, 0);
                }
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
                    Object result = sourceManager.getValue(entity.getUuid(), entity.getMsgId(), rsp.seq);
                    if (result instanceof DpMsgDefine.DPSet) {
                        result = ((DpMsgDefine.DPSet) result).list();
                    }
                    return new BaseDPTaskResult().setResultResponse(result).setResultCode(0);
                });
    }
}
