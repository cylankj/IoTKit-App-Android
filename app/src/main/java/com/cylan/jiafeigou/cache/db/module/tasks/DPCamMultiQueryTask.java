package com.cylan.jiafeigou.cache.db.module.tasks;

import android.text.TextUtils;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.RobotoGetDataRsp;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.db.impl.BaseDPTaskResult;
import com.cylan.jiafeigou.cache.db.module.DPEntity;
import com.cylan.jiafeigou.cache.db.view.DBOption;
import com.cylan.jiafeigou.cache.db.view.DBState;
import com.cylan.jiafeigou.cache.db.view.IDPEntity;
import com.cylan.jiafeigou.cache.db.view.IDPMultiTask;
import com.cylan.jiafeigou.dp.DataPoint;
import com.cylan.jiafeigou.support.block.log.PerformanceUtils;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.TimeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;

import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static com.cylan.jiafeigou.n.base.BaseApplication.getAppComponent;

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
        if (sourceManager == null)
            sourceManager = getAppComponent().getSourceManager();
        return super.init(cache);
    }

    @Override
    public Observable<BaseDPTaskResult> performLocal() {
        List<Integer> list = new ArrayList<>();
        IDPEntity one = multiEntity.get(0);
        for (IDPEntity entity : multiEntity) {
            list.add(entity.getMsgId());
        }
        long todayStart = TimeUtils.getSpecificDayStartTime(option.timeStart);
        long todayEnd = TimeUtils.getSpecificDayEndTime(option.timeStart);
        long versionMin = option.asc ? option.timeStart : todayStart;
        long versionMax = option.asc ? todayEnd : option.timeStart;
        AppLogger.d("let's go for local cache:" + option);
        AppLogger.d("let's go for local versionMin:" + versionMin);
        AppLogger.d("let's go for local versionMax:" + versionMax);
        return dpHelper.queryMultiDpMsg(one.getAccount(), null, one.getUuid(),
                versionMin, versionMax, list, 1000, null, DBState.SUCCESS, null)
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
                            if (!result.contains(parse))
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
                //需要先清空这一天的.
                AppLogger.d("let's go for server cache:" + option);
                long seq = sourceManager.syncJFGCameraWarn(entity.getUuid() == null ? "" : entity.getUuid(), option.timeStart, option.asc, 100);
                subscriber.onNext(seq);
                subscriber.onCompleted();
                getAppComponent().getSourceManager()
                        .addInterceptor(seq, new DataSourceManager.Interceptors<RobotoGetDataRsp>() {
                            @Override
                            public void handleInterception(RobotoGetDataRsp data) {
                                //麻痹的.没有diff,很麻烦,这里肯定是  505,512,222消息了.
                                AppLogger.d("开始处理 拦截器");
                                if (data != null && data.map != null) {
                                    ArrayList<JFGDPMsg> list = new ArrayList<>();
                                    for (Integer integer : data.map.keySet()) {
                                        if (data.map.get(integer) != null) {
                                            list.addAll(data.map.get(integer));
                                        }
                                    }
                                    Collections.sort(list, (JFGDPMsg lhs, JFGDPMsg rhs) ->
                                            (int) (lhs.version - rhs.version));
                                    long max = Math.max(list.get(0).version, list.get(list.size() - 1).version);
                                    long min = Math.min(list.get(0).version, list.get(list.size() - 1).version);
                                    String account = getAppComponent().getSourceManager().getAccount().getAccount() == null ? null :
                                            getAppComponent().getSourceManager().getAccount().getAccount();
                                    String uuid = data.identity;
                                    PerformanceUtils.startTrace("deleteDpSync");
                                    try {
                                        if (max == min) {
                                            //只有一条数据?
                                            dpHelper.deleteDpSync(account, uuid, 505);
                                            dpHelper.deleteDpSync(account, uuid, 512);
                                            dpHelper.deleteDpSync(account, uuid, 222);
                                            return;
                                        }
                                        dpHelper.deleteDpSync(account, uuid, max, min);
                                        PerformanceUtils.stopTrace("deleteDpSync");
                                    } catch (Exception e) {
                                        AppLogger.e("err:" + MiscUtils.getErr(e));
                                    }
                                }
                            }
                        });
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
