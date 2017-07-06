package com.cylan.jiafeigou.cache.db.module.tasks;

import android.text.TextUtils;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.RobotoGetDataRsp;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.db.impl.BaseDPTaskResult;
import com.cylan.jiafeigou.cache.db.module.DPEntity;
import com.cylan.jiafeigou.cache.db.view.DBAction;
import com.cylan.jiafeigou.cache.db.view.DBOption;
import com.cylan.jiafeigou.cache.db.view.DBState;
import com.cylan.jiafeigou.cache.db.view.IDPEntity;
import com.cylan.jiafeigou.cache.db.view.IDPMultiTask;
import com.cylan.jiafeigou.dp.DataPoint;
import com.cylan.jiafeigou.support.block.log.PerformanceUtils;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ListUtils;
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
 * 现在服务器一次回复有N条,定义为集合A{},db中可能有>N条数据,定义为集合B.由于加上了拦截器,处理掉了
 * 集合B中的[A[0],A[N-1]],之后再插入db中,生成集合C.此时集合C包含集合A,C与A的差集,
 * 可能包含一些废弃消息(其他端删除,没有通知同步.),执行完成performServer后,
 * 执行performLocal.performLocal中的timeEnd需要与A[N-1]的时间戳比较.Math.min(A[N-1],timeEnd),
 * 因为A[N-1]可以是隔天的数据.
 */

public class DPCamMultiQueryTask extends BaseDPTask<BaseDPTaskResult> {
    private DBOption.MultiQueryOption option;
    private long timeMax = -1;//服务返回的数据中的最大值.
    private long timeMin = -1;//服务返回的数据中的最大值.

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
        if (timeMax != -1) {
            //向后查timeMax可能是隔天的数据.
            versionMax = Math.min(timeMax, versionMax);
        }
        if (timeMin != -1) {//向前查询,timeMin可能是隔天的数据了.
            versionMin = Math.max(timeMin, versionMin);
        }
        AppLogger.d("let'account go for local cache:" + option);
        AppLogger.d("let'account go for local versionMin:" + versionMin);
        AppLogger.d("let'account go for local versionMax:" + versionMax);
        return dpHelper.queryMultiDpMsg(one.getAccount(), null, one.getUuid(),
                versionMin, versionMax, list, 1000, DBAction.SAVED, DBState.SUCCESS, null)
                .flatMap(new Func1<List<DPEntity>, Observable<BaseDPTaskResult>>() {
                    @Override
                    public Observable<BaseDPTaskResult> call(List<DPEntity> items) {
                        List<DataPoint> result = new ArrayList<>();
                        for (DPEntity item : items) {
                            DataPoint parse = propertyParser.parser(item.getMsgId(), item.getBytes(), item.getVersion());
                            if (parse != null) {
                                parse.setVersion(item.getVersion());
                                parse.setMsgId(item.getMsgId());
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
                AppLogger.d("let'account go for server cache:" + option);
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
                                    String account = getAppComponent().getSourceManager().getAccount().getAccount();
                                    String uuid = data.identity;
                                    if (data.map.size() == 0) {
                                        AppLogger.d("没有数据");
                                        //需要根据option,的逻辑来删除本地数据.
                                        if (option.asc) {//向前查询.
                                            long timeEnd = TimeUtils.getSpecificDayEndTime(option.timeStart);
                                            dpHelper.deleteDpSync(account, uuid, 505, option.timeStart, timeEnd);
                                            dpHelper.deleteDpSync(account, uuid, 512, option.timeStart, timeEnd);
                                            dpHelper.deleteDpSync(account, uuid, 222, option.timeStart, timeEnd);
                                            dpHelper.deleteDpSync(account, uuid, 401, option.timeStart, timeEnd);
                                        } else {
                                            long timeStart = TimeUtils.getSpecificDayStartTime(option.timeStart);
                                            dpHelper.deleteDpSync(account, uuid, 505, timeStart, option.timeStart);
                                            dpHelper.deleteDpSync(account, uuid, 512, timeStart, option.timeStart);
                                            dpHelper.deleteDpSync(account, uuid, 222, timeStart, option.timeStart);
                                            dpHelper.deleteDpSync(account, uuid, 401, timeStart, option.timeStart);
                                        }
                                        return;
                                    }
                                    ArrayList<JFGDPMsg> list = new ArrayList<>();
                                    for (Integer integer : data.map.keySet()) {
                                        if (data.map.get(integer) != null) {
                                            list.addAll(data.map.get(integer));
                                        }
                                    }
                                    if (ListUtils.isEmpty(list)) return;
                                    Collections.sort(list, (JFGDPMsg lhs, JFGDPMsg rhs) ->
                                            (int) (lhs.version - rhs.version));
                                    timeMax = Math.max(list.get(0).version, list.get(list.size() - 1).version);
                                    timeMin = Math.min(list.get(0).version, list.get(list.size() - 1).version);
                                    PerformanceUtils.startTrace("deleteDpSync");
                                    try {
                                        if (timeMax == timeMin) {
                                            //只有一条数据,需要考虑之前的查询方向.
                                            if (option.asc) {
                                                //向前查,但是只有一条.所以清空这个时间戳以后的数据.
                                                timeMax = Long.MAX_VALUE;
                                            } else {
                                                //向前查,但是只有一条.所以清空这个时间戳以前的数据.
                                                timeMin = 0;
                                            }
                                            AppLogger.d("只有一条数据");
                                        }
                                        //有多条数据,先清空本地这个时间段内的数据.再插入.
                                        dpHelper.deleteDpSync(account, uuid, 505, timeMax, timeMin);
                                        dpHelper.deleteDpSync(account, uuid, 512, timeMax, timeMin);
                                        dpHelper.deleteDpSync(account, uuid, 222, timeMax, timeMin);
                                        dpHelper.deleteDpSync(account, uuid, 401, timeMax, timeMin);
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
                .filter(ret -> {
                    AppLogger.d("uuid?" + multiEntity.get(0).getUuid() + " ");
                    return TextUtils.equals(ret.identity, multiEntity.get(0).getUuid());
                })
                .flatMap(robotoGetDataRsp -> performLocal());//数据回来了，并且已经存到db中。
    }
}
