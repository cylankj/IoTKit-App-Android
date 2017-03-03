package com.cylan.jiafeigou.cache.db.module.tasks;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.db.impl.BaseDPHelper;
import com.cylan.jiafeigou.cache.db.impl.BaseDPTaskResult;
import com.cylan.jiafeigou.cache.db.module.DPByteParser;
import com.cylan.jiafeigou.cache.db.module.DPEntity;
import com.cylan.jiafeigou.cache.db.view.IDPAction;
import com.cylan.jiafeigou.cache.db.view.IDPEntity;
import com.cylan.jiafeigou.cache.db.view.IDPSingleTask;
import com.cylan.jiafeigou.dp.DataPoint;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.schedulers.Schedulers;

import static com.cylan.jiafeigou.misc.JfgCmdInsurance.getCmd;

/**
 * Created by yanzhendong on 2017/3/2.
 */

public class DPSingleQueryTask extends BaseDPTask<BaseDPTaskResult> {
    protected IDPAction.DPQueryAction action;

    public DPSingleQueryTask() {
    }

    @Override
    public <R extends IDPSingleTask<BaseDPTaskResult>> R init(IDPEntity cache) {
        action = IDPAction.BaseDPAction.$(cache.getAction(), IDPAction.DPQueryAction.class);
        return super.init(cache);

    }

    @Override
    public Observable<BaseDPTaskResult> performLocal() {
        return BaseDPHelper.getInstance().queryDPMsg(singleEntity.getUuid(), singleEntity.getVersion() == 0 ? Long.MAX_VALUE : singleEntity.getVersion(), singleEntity.getMsgId(), action.asc, action.limit)
                .map(items -> {
                    List<DataPoint> result = new ArrayList<>();
                    for (DPEntity item : items) {
                        DataPoint parse = DPByteParser.parse(item);
                        if (parse != null) {
                            parse.version = item.getVersion();
                            parse.id = item.getMsgId();
                        }
                        result.add(parse);
                    }
                    return new BaseDPTaskResult()
                            .setResultCode(0)
                            .setResultResponse(result);
                });
    }

    @Override
    public Observable<BaseDPTaskResult> performServer() {
        return Observable.create((Observable.OnSubscribe<Long>) subscriber -> {
            try {
                AppLogger.d("正在发送查询请求,version:" + singleEntity.getVersion() + "count:" + action.asc + "acs:" + action.limit);
                ArrayList<JFGDPMsg> params = new ArrayList<>();
                JFGDPMsg msg = new JFGDPMsg(singleEntity.getMsgId(), singleEntity.getVersion());
                params.add(msg);
                long seq = getCmd().robotGetData(singleEntity.getUuid() == null ? "" : singleEntity.getUuid(), params, action.limit, action.asc, 0);//多请求一条数据,用来判断是否是一天最后一条
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
                .flatMap(rsp -> mDPHelper.markDPMsgWithConfirm(singleEntity.getUuid(), null, singleEntity.getMsgId(), IDPAction.SAVED).map(entity -> rsp))
                .map(rsp -> {
                    AppLogger.d("收到从服务器返回数据!!!");
                    DpMsgDefine.DPSet<DpMsgDefine.DPWonderItem> result = DataSourceManager.getInstance().getValue(singleEntity.getUuid(), singleEntity.getMsgId(), rsp.seq);
                    return new BaseDPTaskResult().setResultResponse(result.list()).setResultCode(0);
                });
    }
}
