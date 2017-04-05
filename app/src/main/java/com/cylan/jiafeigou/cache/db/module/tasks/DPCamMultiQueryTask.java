package com.cylan.jiafeigou.cache.db.module.tasks;

import android.text.TextUtils;

import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.db.impl.BaseDBHelper;
import com.cylan.jiafeigou.cache.db.impl.BaseDPTaskResult;
import com.cylan.jiafeigou.cache.db.module.DPEntity;
import com.cylan.jiafeigou.cache.db.view.DBOption;
import com.cylan.jiafeigou.cache.db.view.DBState;
import com.cylan.jiafeigou.cache.db.view.IDPEntity;
import com.cylan.jiafeigou.cache.db.view.IDPMultiTask;
import com.cylan.jiafeigou.dp.DataPoint;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by hds on 2017/3/2.
 */

public class DPCamMultiQueryTask extends BaseDPTask<BaseDPTaskResult> {
    private DBOption.MultiQueryOption option;

    public DPCamMultiQueryTask() {
    }

    @Override
    public <R extends IDPMultiTask<BaseDPTaskResult>> R init(List<IDPEntity> cache) throws Exception {
        this.option = cache.get(0).option(DBOption.MultiQueryOption.class);
        return super.init(cache);
    }

    @Override
    public Observable<BaseDPTaskResult> performLocal() {
        List<Integer> list = new ArrayList<>();
        IDPEntity one = multiEntity.get(0);
        for (IDPEntity entity : multiEntity) {
            list.add(entity.getMsgId());
        }
        if (option.timeStart > option.timeEnd) {
            long tmp = option.timeStart;
            option.timeStart = option.timeEnd;
            option.timeEnd = tmp;
        }
        AppLogger.d("let's go for local cache:" + option);
        return BaseDBHelper.getInstance().queryMultiDpMsg(one.getAccount(), null, one.getUuid(),
                option.timeStart, option.timeEnd, list, 1000, null, DBState.SUCCESS, null)
                .flatMap(new Func1<List<DPEntity>, Observable<BaseDPTaskResult>>() {
                    @Override
                    public Observable<BaseDPTaskResult> call(List<DPEntity> items) {
                        List<DataPoint> result = new ArrayList<>();
                        for (DPEntity item : items) {
                            DataPoint parse = propertyParser.parser(item.getMsgId(), item.getBytes(), item.getVersion());
                            if (parse != null) {
                                parse.version = item.getVersion();
                                parse.msgId = item.getMsgId();
                            }
                            result.add(parse);
                        }
                        return Observable.just(new BaseDPTaskResult()
                                .setResultCode(0)
                                .setResultResponse(result));
                    }
                });

    }

    @Override
    public Observable<BaseDPTaskResult> performServer() {
        return Observable.create((Observable.OnSubscribe<Long>) subscriber -> {
            try {
                AppLogger.d("let's go for server cache:" + option);
                long seq = DataSourceManager.getInstance().syncJFGCameraWarn(entity.getUuid() == null ? "" : entity.getUuid(), option.timeStart, option.asc, 100);
                subscriber.onNext(seq);
                subscriber.onCompleted();
            } catch (Exception e) {
                AppLogger.e(e.getMessage());
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.io())
                .flatMap(this::makeGetDataRspResponse)
                .doOnError(throwable -> {
                    if (throwable != null && throwable instanceof TimeoutException) {
                        performLocal();
                    }
                })
                .filter(ret -> TextUtils.equals(ret.identity, multiEntity.get(0).getUuid()))
                .flatMap(robotoGetDataRsp -> performLocal());//数据回来了，并且已经存到db中。
    }
}
