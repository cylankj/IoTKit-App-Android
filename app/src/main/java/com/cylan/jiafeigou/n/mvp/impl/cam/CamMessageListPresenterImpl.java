package com.cylan.jiafeigou.n.mvp.impl.cam;

import android.text.TextUtils;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.cache.pool.GlobalDataPool;
import com.cylan.jiafeigou.dp.BaseValue;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.Converter;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamMessageListContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.CamMessageBean;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.rx.RxHelper;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.util.ArrayList;
import java.util.Collections;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by cylan-hunt on 16-7-13.
 */
public class CamMessageListPresenterImpl extends AbstractPresenter<CamMessageListContract.View>
        implements CamMessageListContract.Presenter {

    private String uuid;
    private long querySeq;
    private CompositeSubscription compositeSubscription;

    public CamMessageListPresenterImpl(CamMessageListContract.View view, String uuid) {
        super(view);
        view.setPresenter(this);
        this.uuid = uuid;
    }

    @Override
    public void start() {
        unSubscribe(compositeSubscription);
        compositeSubscription = new CompositeSubscription();
        compositeSubscription.add(messageListSub());
        compositeSubscription.add(sdcardStatusSub());
    }

    /**
     * sd卡状态更新
     *
     * @return
     */
    private Subscription sdcardStatusSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.JFGRobotSyncData.class)
                .filter((RxEvent.JFGRobotSyncData data) -> (getView() != null && TextUtils.equals(uuid, data.identity)))
                .subscribeOn(Schedulers.io())
                .map(new Func1<RxEvent.JFGRobotSyncData, Boolean>() {
                    @Override
                    public Boolean call(RxEvent.JFGRobotSyncData jfgRobotSyncData) {
                        ArrayList<JFGDPMsg> list = jfgRobotSyncData.dataList;
                        for (int i = 0; i < list.size(); i++) {
                            JFGDPMsg dp = list.get(i);
                            if (dp.id == DpMsgMap.ID_201_NET) {

                            }
                        }
                        return null;
                    }
                })
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe((Boolean aBoolean) -> {
                    getView().updateSdStatus(aBoolean);
                });
    }

    @Override
    public void stop() {
        unSubscribe(compositeSubscription);
    }

    private Subscription messageListSub() {
        return RxBus.getCacheInstance().toObservable(Long.class)
                .subscribeOn(Schedulers.computation())
                .filter((Long aLong) -> (aLong != null && aLong == querySeq))
                .flatMap(new Func1<Long, Observable<ArrayList<CamMessageBean>>>() {
                    @Override
                    public Observable<ArrayList<CamMessageBean>> call(Long aLong) {
                        ArrayList<BaseValue> allList = new ArrayList<>();
                        ArrayList<BaseValue> list_505 = GlobalDataPool.getInstance().fetchLocalList(uuid, DpMsgMap.ID_505_CAMERA_ALARM_MSG);
                        ArrayList<BaseValue> list_204 = GlobalDataPool.getInstance().fetchLocalList(uuid, DpMsgMap.ID_204_SDCARD_STORAGE);
                        if (list_505 != null) allList.addAll(list_505);
                        if (list_204 != null) allList.addAll(list_204);
                        Collections.sort(allList);//来个排序
                        return Observable.just(Converter.convert(uuid, allList));
                    }
                })
                .map((ArrayList<CamMessageBean> camList) -> {
                    ArrayList<CamMessageBean> list = getView().getList();
                    if (list != null)
                        camList.removeAll(list);//删除重复的
                    return camList;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .filter(new RxHelper.Filter<>("messageListSub()=null?", getView() != null))
                .map((ArrayList<CamMessageBean> jfgdpMsgs) -> {
                    getView().onMessageListRsp(jfgdpMsgs);
                    AppLogger.i("messageListSub+" + jfgdpMsgs.size());
                    return null;
                })
                .retry(new RxHelper.RxException<>("messageListSub"))
                .subscribe();
    }

    @Override
    public void fetchMessageList() {
        Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .map(new Func1<Object, ArrayList<CamMessageBean>>() {
                    @Override
                    public ArrayList<CamMessageBean> call(Object o) {
                        ArrayList<JFGDPMsg> dps = new ArrayList<>();
                        dps.add(new JFGDPMsg(DpMsgMap.ID_505_CAMERA_ALARM_MSG, 0));
                        dps.add(new JFGDPMsg(DpMsgMap.ID_204_SDCARD_STORAGE, 0));
                        try {
                            querySeq = GlobalDataPool.getInstance().robotGetData(
                                    uuid,
                                    dps, 20, false, 0);
                            AppLogger.i("req: " + querySeq);
                        } catch (JfgException e) {
                            AppLogger.e("wth:+" + e.getLocalizedMessage());
                        }
                        return null;
                    }
                })
                .subscribe();
    }

    @Override
    public void removeItem(CamMessageBean bean) {
        Observable.just(bean)
                .subscribeOn(Schedulers.computation())
                .subscribe(new Action1<CamMessageBean>() {
                    @Override
                    public void call(CamMessageBean bean) {
                        long id = bean.id;
                        long version = bean.version;
                        GlobalDataPool.getInstance().delete(uuid, id, version);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e(":" + throwable.getLocalizedMessage());
                    }
                });
    }
}
