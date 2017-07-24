package com.cylan.jiafeigou.cache.db.module.tasks;

import android.text.TextUtils;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.RobotoGetDataRsp;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.cache.db.impl.BaseDPTaskResult;
import com.cylan.jiafeigou.cache.db.module.DPEntity;
import com.cylan.jiafeigou.cache.db.module.DPEntityDao;
import com.cylan.jiafeigou.cache.db.view.DBOption;
import com.cylan.jiafeigou.cache.db.view.DBState;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ListUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.TimeUtils;
import com.cylan.jiafeigou.widget.wheel.WonderIndicatorWheelView;

import org.greenrobot.greendao.query.QueryBuilder;
import org.greenrobot.greendao.query.WhereCondition;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by yanzhendong on 2017/3/2.
 */

public class DPCamDateQueryTask extends BaseDPTask<BaseDPTaskResult> {

    private static final int DAYS = 7;

    public DPCamDateQueryTask() {
        if (sourceManager == null)
            sourceManager = BaseApplication.getAppComponent().getSourceManager();
    }

    @Override
    public Observable<BaseDPTaskResult> performLocal() {
        long performanceTime = System.currentTimeMillis();
        return Observable.just("go")
                .subscribeOn(Schedulers.io())
                .flatMap(s -> {
                    long startTime = TimeUtils.getTodayStartTime();//当前时间
                    DBOption.CamMultiDateOption option = entity.option(DBOption.CamMultiDateOption.class);
                    int queryDays = DAYS;
                    if (option != null) {
                        queryDays = option.days;
                    }
                    List<Long> will = new ArrayList<>();
                    for (int i = 0; i < queryDays; i++) {
                        will.add(startTime - i * 24 * 3600 * 1000L);
                    }
                    return Observable.from(will);
                })
                .flatMap(aLong -> {
                    QueryBuilder<DPEntity> builder = dpHelper.getDpEntityQueryBuilder();
                    String account = entity.getAccount();
                    if (!TextUtils.isEmpty(account)) {
                        builder.where(DPEntityDao.Properties.Account.eq(account));//设置 dpAccount 约束
                    }
                    String uuid = entity.getUuid();
                    if (!TextUtils.isEmpty(uuid)) {
                        builder.where(DPEntityDao.Properties.Uuid.eq(uuid));//设置 UUID 约束
                    }
                    builder.where(DPEntityDao.Properties.State.eq(DBState.SUCCESS));
                    builder.where(DPEntityDao.Properties.Version.ge(aLong),
                            DPEntityDao.Properties.Version.lt(aLong + 24 * 3600 * 1000L));//设置 version 约束
                    WhereCondition condition1 = DPEntityDao.Properties.MsgId.eq(222);
                    WhereCondition condition2 = DPEntityDao.Properties.MsgId.eq(505);
                    WhereCondition conditions3 = DPEntityDao.Properties.MsgId.eq(512);
                    WhereCondition conditions4 = DPEntityDao.Properties.MsgId.eq(401);
                    builder.whereOr(condition1, condition2, conditions3, conditions4);
                    builder.orderDesc(DPEntityDao.Properties.Version);//降序排列，得到最大的一个值。
                    builder.limit(1);
                    DPEntity entity = builder.unique();
                    return Observable.just(entity);
                })
                .toList()
                .flatMap(result -> {
                    //已经是降序
                    long startTime = TimeUtils.getTodayStartTime();//今天凌晨时间戳
                    DBOption.CamMultiDateOption option = entity.option(DBOption.CamMultiDateOption.class);
                    int queryDays = DAYS;
                    if (option != null) {
                        queryDays = option.days;
                    }
                    ArrayList<WonderIndicatorWheelView.WheelItem> finalList = new ArrayList<>(queryDays);
                    if (result != null) {
                        for (int i = 0; i < queryDays; i++) {
                            DPEntity entity = result.get(i);
                            WonderIndicatorWheelView.WheelItem item = new WonderIndicatorWheelView.WheelItem();
                            item.time = entity == null ? (startTime - i * 3600 * 24 * 1000L) : entity.getVersion();
                            item.wonderful = entity != null;
                            finalList.add(item);
                        }
                    }
                    AppLogger.d("localDateList: " + ListUtils.getSize(finalList) + "," + (System.currentTimeMillis() - performanceTime) + "ms");
                    AppLogger.d("localDateList: " + finalList);
                    BaseDPTaskResult good = new BaseDPTaskResult();
                    good.setResultCode(0);
                    good.setResultResponse(finalList);
                    return Observable.just(good);
                });
    }

    @Override
    public Observable<BaseDPTaskResult> performServer() {
        return Observable.just("go to get and assemble Date List")
                .subscribeOn(Schedulers.io())
                .flatMap(s -> {
                    //今天凌晨时间戳。
                    long todayTimeStamp = TimeUtils.getTodayStartTime();
                    DBOption.CamMultiDateOption option = entity.option(DBOption.CamMultiDateOption.class);
                    int queryDays = DAYS;
                    if (option != null) {
                        queryDays = option.days;
                    }
                    ArrayList<JFGDPMsg> list = (ArrayList<JFGDPMsg>) MiscUtils.getCamDateVersionList(todayTimeStamp, queryDays);
                    try {
                        long ret = appCmd.robotGetData(entity.getUuid(), list, 1, true, 0);
                        return Observable.just(ret);
                    } catch (JfgException e) {
                        AppLogger.e("err: " + e.getLocalizedMessage());
                        return Observable.just(-1L);
                    }
                })
                .filter(aLong -> aLong != -1)
                .flatMap(aLong ->//到这里，表明已经存到db了
                        RxBus.getCacheInstance().toObservable(RobotoGetDataRsp.class)
                                .subscribeOn(Schedulers.computation())
                                .filter(result -> result != null && TextUtils.equals(result.identity, entity.getUuid()) && result.seq == aLong))
                .flatMap(robotoGetDataRsp -> performLocal());//再次回调数据库

    }
}
