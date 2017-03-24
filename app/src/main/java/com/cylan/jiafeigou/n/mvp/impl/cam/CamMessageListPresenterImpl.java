package com.cylan.jiafeigou.n.mvp.impl.cam;

import android.text.TextUtils;
import android.util.Log;

import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.db.impl.BaseDPTaskResult;
import com.cylan.jiafeigou.cache.db.module.DPEntity;
import com.cylan.jiafeigou.cache.db.module.tasks.DPCamDateQueryTask;
import com.cylan.jiafeigou.cache.db.module.tasks.DPCamMultiQueryTask;
import com.cylan.jiafeigou.cache.db.view.DBOption;
import com.cylan.jiafeigou.cache.db.view.IDPEntity;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                    AppLogger.e("收到,属性同步了");
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
     * @param loadMore
     * @return
     */
    private Observable<BaseDPTaskResult> getMessageListQuery(long timeStart, boolean loadMore) {
        Log.d("getMessageListQuery", "getMessageListQuery:" + timeStart + ",loadMore: " + loadMore);
        if (DataSourceManager.getInstance().isOnline()) {
            return new DPCamMultiQueryTask()
                    .init(buildEntity(timeStart, TimeUtils.getSpecificDayStartTime(timeStart) + 24 * 3600 * 1000L, loadMore))
                    .performServer(null);
        } else {
            return new DPCamMultiQueryTask()
                    .init(buildEntity(timeStart, TimeUtils.getSpecificDayStartTime(timeStart) + 24 * 3600 * 1000L, loadMore))
                    .performLocal();
        }
    }

    @Override
    public void fetchMessageList(long timeStart, boolean loadMore) {
        //1.timeStart==0->服务器，本地
        //服务器：1.日历。2.偏移到最靠近有数据的一天。开始查。以后，点击开始查。
        //本地，查出日历。

        Subscription subscription = getMessageListQuery(timeStart, loadMore)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap(new Func1<BaseDPTaskResult, Observable<ArrayList<CamMessageBean>>>() {
                    @Override
                    public Observable<ArrayList<CamMessageBean>> call(BaseDPTaskResult baseDPTaskResult) {
                        List<DataPoint> result = baseDPTaskResult.getResultResponse();
                        AppLogger.d("fetchLocalList: " + ListUtils.getSize(result));
                        if (ListUtils.getSize(result) == 0)
                            return Observable.just(null);
                        ArrayList<CamMessageBean> list = new ArrayList<>();
                        for (DataPoint dataPoint : result) {
                            CamMessageBean bean = new CamMessageBean();
                            bean.id = dataPoint.dpMsgId;
                            bean.time = dataPoint.dpMsgVersion;
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
                .filter(result -> mView != null && result != null && result.size() > 0)
                .flatMap(new Func1<ArrayList<CamMessageBean>, Observable<ArrayList<CamMessageBean>>>() {
                    @Override
                    public Observable<ArrayList<CamMessageBean>> call(ArrayList<CamMessageBean> camMessageBeen) {
                        //需要和列表里面的items 融合
                        ArrayList<CamMessageBean> list = mView.getList();
                        AppLogger.d("uiList: " + ListUtils.getSize(list) + ",newList: " + ListUtils.getSize(camMessageBeen));
                        return Observable.just(camMessageBeen);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .filter(ret -> mView != null && ret != null && ret.size() > 0)
                .doOnError(throwable -> AppLogger.e("err: " + throwable.getLocalizedMessage()))
                .subscribe(list -> mView.onListAppend(list),
                        throwable -> AppLogger.e("err: " + throwable.getLocalizedMessage()), () -> mView.onErr());
        addSubscription(subscription, "DPCamMultiQueryTask");
    }

    @Override
    public void removeItems(ArrayList<CamMessageBean> beanList) {
        Observable.just(beanList)
                .subscribeOn(Schedulers.computation())
                .subscribe((ArrayList<CamMessageBean> list) -> {
                    Map<Long, ArrayList<Long>> map = new HashMap<>();
                    for (CamMessageBean bean : list) {
                        ArrayList<Long> arrayList = map.get(bean.id);
                        if (arrayList == null) {
                            arrayList = new ArrayList<>();
                            map.put(bean.id, arrayList);
                        }
                        arrayList.add(bean.time);
                    }
                    for (long id : map.keySet()) {
                        boolean result = DataSourceManager.getInstance().deleteByVersions(uuid, id, map.get(id));
                        AppLogger.i("delete: " + result + " dpMsgId:" + id);
                    }
                }, (Throwable throwable) -> {
                    AppLogger.e(":" + throwable.getLocalizedMessage());
                });
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
        if (DataSourceManager.getInstance().isOnline()) {
            return new DPCamDateQueryTask()
                    .init(entity)
                    .performServer(null);
        } else {
            return new DPCamDateQueryTask()
                    .init(entity)
                    .performLocal();
        }
    }

    @Override
    public void refreshDateList() {
        Subscription subscription = getDateListQuery()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(throwable -> AppLogger.e("err: " + throwable.getLocalizedMessage()))
                .filter(ret -> mView != null && ret != null && ret.getResultResponse() != null)
                .subscribe(baseDPTaskResult ->
                                mView.onDateMapRsp(baseDPTaskResult.getResultResponse()),
                        throwable -> AppLogger.e("err: " + throwable.getLocalizedMessage()));
        addSubscription(subscription, "DPCamDateQueryTask");
    }

    @Override
    public void stop() {
        super.stop();
    }
}
