package com.cylan.jiafeigou.n.mvp.impl.cam;

import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.db.impl.BaseDPTaskDispatcher;
import com.cylan.jiafeigou.cache.db.impl.BaseDPTaskResult;
import com.cylan.jiafeigou.cache.db.module.DPEntity;
import com.cylan.jiafeigou.cache.db.module.tasks.DPCamDateQueryTask;
import com.cylan.jiafeigou.cache.db.module.tasks.DPCamMultiQueryTask;
import com.cylan.jiafeigou.cache.db.view.DBAction;
import com.cylan.jiafeigou.cache.db.view.DBOption;
import com.cylan.jiafeigou.cache.db.view.IDPEntity;
import com.cylan.jiafeigou.cache.db.view.IDPTaskResult;
import com.cylan.jiafeigou.dp.DataPoint;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamMessageListContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.CamMessageBean;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ListUtils;
import com.cylan.jiafeigou.utils.TimeUtils;
import com.cylan.jiafeigou.widget.wheel.WonderIndicatorWheelView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by cylan-hunt on 16-7-13.
 */
public class CamMessageListPresenterImpl extends AbstractPresenter<CamMessageListContract.View>
        implements CamMessageListContract.Presenter {

    private List<WonderIndicatorWheelView.WheelItem> dateItemList = new ArrayList<>();

    public CamMessageListPresenterImpl(CamMessageListContract.View view, String uuid) {
        super(view, uuid);
        view.setPresenter(this);
    }

    @Override
    protected Subscription[] register() {
        return new Subscription[]{sdcardStatusSub()};
    }

    @Override
    protected boolean registerTimeTick() {
        return true;
    }

    @Override
    protected void onTimeTick() {

    }

    /**
     * sd卡状态更新
     *
     * @return
     */
    private Subscription sdcardStatusSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.DeviceSyncRsp.class)
                .filter((RxEvent.DeviceSyncRsp data) -> (getView() != null && TextUtils.equals(uuid, data.uuid)))
                .filter(ret -> ret.dpList != null)
                .flatMap(ret -> Observable.from(ret.dpList))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(msg -> {
                    try {
                        getView().deviceInfoChanged((int) msg.id, msg);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    AppLogger.d("收到,属性同步了");
                });
    }


    private List<IDPEntity> buildEntity(long timeStart, long timeEnd, boolean asc) {
        List<IDPEntity> list = new ArrayList<>();
        list.add(new DPEntity()
                .setMsgId(222)
                .setUuid(uuid)
                .setOption(new DBOption.MultiQueryOption(timeStart, timeEnd, asc))
                .setAccount(DataSourceManager.getInstance().getJFGAccount().getAccount()));
        list.add(new DPEntity()
                .setMsgId(505)
                .setUuid(uuid)
                .setOption(new DBOption.MultiQueryOption(timeStart, timeEnd, asc))
                .setAccount(DataSourceManager.getInstance().getJFGAccount().getAccount()));
        list.add(new DPEntity()
                .setMsgId(512)
                .setUuid(uuid)
                .setOption(new DBOption.MultiQueryOption(timeStart, timeEnd, asc))
                .setAccount(DataSourceManager.getInstance().getJFGAccount().getAccount()));
        return list;
    }

    /**
     * 消息列表请求
     *
     * @param timeStart
     * @param asc:true:从timeStart开始，往前查，直到timeEnd.,false:从timeStart开始，到现在。 false:向后查。true:向前查。
     * @return
     */
    private Observable<BaseDPTaskResult> getMessageListQuery(long timeStart, boolean asc) {
        Log.d("getMessageListQuery", "getMessageListQuery:" + timeStart + ",asc: " + asc);
        try {
            long timeEnd = asc ? System.currentTimeMillis() : TimeUtils.getSpecificDayStartTime(timeStart);
            //需要注意这个timeEnd,他可以小于timeStart.可以大于timeStart.
            //查询本地的时候，不用asc.只用timeStart,timeEnd
            //查询服务器的时候，用timeStart中大的一个，加上asc.
            if (DataSourceManager.getInstance().isOnline()) {
                return new DPCamMultiQueryTask()
                        .init(buildEntity(timeStart, timeEnd, asc))//服务器查询不理会timeEnd
                        .performServer();
            } else {
                return new DPCamMultiQueryTask()
                        .init(buildEntity(timeStart, timeEnd, asc))//本地查询不理会asc
                        .performLocal();
            }
        } catch (Exception e) {
            return Observable.just(BaseDPTaskResult.ERROR);
        }
    }

    /**
     * 初次进入页面，先要确定第一天的时间。
     */
    private void loadDataListFirst() {
        refreshDateList();
    }

    @Override
    public void fetchMessageList(long timeStart, boolean asc) {
        //1.timeStart==0->服务器，本地
        //服务器：1.日历。2.偏移到最靠近有数据的一天。开始查。以后，点击开始查。
        //本地，查出日历。
        if (timeStart == 0) {
            loadDataListFirst();
            return;
        }
        Subscription subscription = getMessageListQuery(timeStart, asc)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap(new Func1<BaseDPTaskResult, Observable<ArrayList<CamMessageBean>>>() {
                    @Override
                    public Observable<ArrayList<CamMessageBean>> call(BaseDPTaskResult baseDPTaskResult) {
                        List<DataPoint> result = baseDPTaskResult.getResultResponse();
                        AppLogger.d("fetchLocalList: " + ListUtils.getSize(result));
                        if (ListUtils.getSize(result) == 0)
                            return Observable.just(new ArrayList<>());
                        ArrayList<CamMessageBean> list = new ArrayList<>();
                        for (DataPoint dataPoint : result) {
                            CamMessageBean bean = new CamMessageBean();
                            bean.id = dataPoint.msgId;
                            bean.time = dataPoint.version;
                            if (bean.id == 222) {
                                bean.sdcardSummary = (DpMsgDefine.DPSdcardSummary) dataPoint;
                            }
                            if (bean.id == 512 || bean.id == 505) {
                                bean.alarmMsg = (DpMsgDefine.DPAlarm) dataPoint;
                            }
                            list.add(bean);
                        }
                        return Observable.just(list);
                    }
                })
                .filter(result -> mView != null && result != null)
                .flatMap(new Func1<ArrayList<CamMessageBean>, Observable<Pair<ArrayList<CamMessageBean>, Boolean>>>() {
                    @Override
                    public Observable<Pair<ArrayList<CamMessageBean>, Boolean>> call(ArrayList<CamMessageBean> camList) {
                        //需要和列表里面的items 融合
                        ArrayList<CamMessageBean> list = new ArrayList<>(mView.getList());
                        camList.removeAll(list);
                        AppLogger.d("uiList: " + ListUtils.getSize(list) + ",newList: " + ListUtils.getSize(camList));
                        if (camList.size() > 0) {
                            //检查是否 append 或者insert
                            if (ListUtils.getSize(list) == 0) {
                                return Observable.just(new Pair<>(camList, true));
                            }
                            if (camList.get(camList.size() - 1).time >= list.get(0).time) {
                                return Observable.just(new Pair<>(camList, false));
                            }
                            return Observable.just(new Pair<>(camList, true));
                        }
                        return Observable.just(new Pair<>(camList, true));
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .filter(ret -> mView != null && ret != null)
                .map(result -> {
                    if (result.second) {
                        mView.onListAppend(result.first);
                    } else mView.onListInsert(result.first, 0);
                    return null;
                })
                .subscribeOn(Schedulers.newThread())
                .delay(500, TimeUnit.MILLISECONDS)
                .filter(ret -> mView != null)
                .doOnError(throwable -> AppLogger.e("err: " + throwable.getLocalizedMessage()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ret -> mView.loadingDismiss(),
                        throwable -> {
                            AppLogger.e("err: " + throwable.getLocalizedMessage());
                            mView.onErr();
                        });
        addSubscription(subscription, "DPCamMultiQueryTask");
    }

    private List<DPEntity> buildMultiEntities(ArrayList<CamMessageBean> beanList) {
        List<DPEntity> entities = new ArrayList<>();
        for (CamMessageBean bean : beanList) {
            DPEntity dpEntity = new DPEntity();
            dpEntity.setUuid(uuid);
            dpEntity.setAccount(DataSourceManager.getInstance().getJFGAccount().getAccount());
            dpEntity.setMsgId((int) bean.id);
            dpEntity.setVersion(bean.time);
            dpEntity.setAction(DBAction.DELETED);
            entities.add(dpEntity);
        }
        return entities;
    }

    public Observable<IDPTaskResult> perform(List<? extends IDPEntity> entity) {
        return BaseDPTaskDispatcher.getInstance().perform(entity);
    }

    @Override
    public void removeItems(ArrayList<CamMessageBean> beanList) {
        List<DPEntity> list = buildMultiEntities(beanList);
        Subscription subscription = BaseDPTaskDispatcher.getInstance().perform(list)
                .subscribeOn(Schedulers.io())
                .filter(result -> mView != null)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(idpTaskResult -> {
                    if (idpTaskResult.getResultCode() == 0) {
                        //good
                        mView.onMessageDeleteSuc();
                    }
                }, throwable -> {
                    AppLogger.e("err:" + throwable.getLocalizedMessage());
                    mView.onErr();
                });
        addSubscription(subscription, "removeItems");
    }


    @Override
    public List<WonderIndicatorWheelView.WheelItem> getDateList() {
        return dateItemList;
    }

    /**
     * 日历列表请求
     *
     * @return
     */
    private Observable<BaseDPTaskResult> getDateListQuery() {
        DPEntity entity = new DPEntity();
        entity.setAccount(DataSourceManager.getInstance().getJFGAccount().getAccount());
        entity.setUuid(uuid);
        try {
            if (DataSourceManager.getInstance().isOnline()) {
                return new DPCamDateQueryTask()
                        .init(entity)
                        .performServer();
            } else {
                return new DPCamDateQueryTask()
                        .init(entity)
                        .performLocal();
            }
        } catch (Exception e) {
            return Observable.just(BaseDPTaskResult.ERROR);
        }
    }

    @Override
    public void refreshDateList() {
        Subscription subscription = getDateListQuery()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(throwable -> AppLogger.e("err: " + throwable.getLocalizedMessage()))
                .filter(ret -> mView != null && ret != null && ret.getResultResponse() != null)
                .map(result -> {
                    if (result.getResultCode() == 0) {
                        mView.onDateMapRsp(dateItemList = result.getResultResponse());
                    }
                    return dateItemList;
                })
                .subscribeOn(Schedulers.computation())
                .subscribe(wheelItems -> {
                    long timeHit = 0;
                    for (WonderIndicatorWheelView.WheelItem item : wheelItems) {
                        if (item.wonderful) {
                            timeHit = item.time;
                            break;
                        }
                    }
                    if (timeHit != 0) {
                        //需要保证这个timeHit是当天的最大的一个
                        fetchMessageList(timeHit, false);
                        AppLogger.d("Max dateList timeHit:" + timeHit);
                    }
                }, throwable -> AppLogger.e("err: " + throwable.getLocalizedMessage()));
        addSubscription(subscription, "DPCamDateQueryTask");
    }

    @Override
    public void stop() {
        super.stop();
    }
}
