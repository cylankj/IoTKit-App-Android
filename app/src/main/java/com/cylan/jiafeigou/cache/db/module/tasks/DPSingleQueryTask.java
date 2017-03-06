package com.cylan.jiafeigou.cache.db.module.tasks;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.db.impl.BaseDBHelper;
import com.cylan.jiafeigou.cache.db.impl.BaseDPTaskResult;
import com.cylan.jiafeigou.cache.db.module.DPByteParser;
import com.cylan.jiafeigou.cache.db.module.DPEntity;
import com.cylan.jiafeigou.cache.db.view.DBAction;
import com.cylan.jiafeigou.cache.db.view.DBOption;
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
    private DBOption.SingleQueryOption option;


    public DPSingleQueryTask() {
    }

    @Override
    public <R extends IDPSingleTask<BaseDPTaskResult>> R init(IDPEntity cache) {
        this.option = cache.option(DBOption.SingleQueryOption.class);
        return super.init(cache);

    }

    @Override
    public Observable<BaseDPTaskResult> performLocal() {
        return BaseDBHelper.getInstance().queryDPMsg(entity.getUuid(), entity.getVersion() == 0 ? Long.MAX_VALUE : entity.getVersion(), entity.getMsgId(), option.asc, option.limit)
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
                AppLogger.d("正在发送查询请求,version:" + entity.getVersion() + "count:" + option.asc + "acs:" + option.limit);
                ArrayList<JFGDPMsg> params = new ArrayList<>();
                JFGDPMsg msg = new JFGDPMsg(entity.getMsgId(), entity.getVersion());
                params.add(msg);
                long seq = getCmd().robotGetData(entity.getUuid() == null ? "" : entity.getUuid(), params, option.limit, option.asc, 0);//多请求一条数据,用来判断是否是一天最后一条
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
                .flatMap(rsp -> mDPHelper.markDPMsgWithConfirm(entity.getUuid(), null, entity.getMsgId(), DBAction.SAVED,null).map(entity -> rsp))
                .map(rsp -> {
                    AppLogger.d("收到从服务器返回数据!!!");
                    DpMsgDefine.DPSet<DpMsgDefine.DPWonderItem> result = DataSourceManager.getInstance().getValue(entity.getUuid(), entity.getMsgId(), rsp.seq);
                    return new BaseDPTaskResult().setResultResponse(result.list()).setResultCode(0);
                });
    }
}
