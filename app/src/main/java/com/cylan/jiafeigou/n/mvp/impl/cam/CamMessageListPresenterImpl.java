package com.cylan.jiafeigou.n.mvp.impl.cam;

import android.text.TextUtils;

import com.cylan.entity.jniCall.RobotoGetDataRsp;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.dp.DataPoint;
import com.cylan.jiafeigou.dp.DpMsgDefine;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

    private Subscription qeurySub;

    public CamMessageListPresenterImpl(CamMessageListContract.View view, String uuid) {
        super(view, uuid);
        view.setPresenter(this);
    }

    @Override
    protected Subscription[] register() {
        return new Subscription[]{sdcardStatusSub()};
    }

    /**
     * sd卡状态更新
     *
     * @return
     */
    private Subscription sdcardStatusSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.DeviceSyncRsp.class)
                .filter((RxEvent.DeviceSyncRsp data) -> (getView() != null && TextUtils.equals(uuid, data.uuid)))
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<RxEvent.DeviceSyncRsp, Boolean>() {
                    @Override
                    public Boolean call(RxEvent.DeviceSyncRsp update) {
                        DpMsgDefine.DPSdStatus status = DataSourceManager.getInstance().getValueSafe(uuid, DpMsgMap.ID_204_SDCARD_STORAGE, DpMsgDefine.DPSdStatus.empty);
                        getView().deviceInfoChanged(DpMsgMap.ID_204_SDCARD_STORAGE, status);
                        DpMsgDefine.DPNet net = DataSourceManager.getInstance().getValueSafe(uuid, DpMsgMap.ID_201_NET, DpMsgDefine.DPNet.empty);
                        getView().deviceInfoChanged(DpMsgMap.ID_201_NET, net);
                        AppLogger.e("收到刷新");
                        return null;
                    }
                })
                .retry(new RxHelper.RxException<>("sdcardStatusSub"))
                .subscribe();
    }

    @Override
    public void fetchMessageList(final boolean manually, boolean asc) {
        unSubscribe(qeurySub);
        qeurySub = queryTimeLine(20, asc)
                .map((ArrayList<CamMessageBean> camList) -> {
                    ArrayList<CamMessageBean> list = getView().getList();
                    if (list != null)
                        camList.removeAll(list);//删除重复的
                    return camList;
                })
                .delay(1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .filter(new RxHelper.Filter<>("getView()=null?", getView() != null))
                .map((ArrayList<CamMessageBean> jfgdpMsgs) -> {
                    getView().onMessageListRsp(jfgdpMsgs);
                    AppLogger.i("messageListSub+" + jfgdpMsgs.size());
                    return null;
                })
                .retry(new RxHelper.RxException<>("messageListSub"))
                .subscribe(o -> {
                }, throwable -> AppLogger.e("messageList err:" + throwable.getLocalizedMessage()));
    }


    private Observable<ArrayList<CamMessageBean>> queryTimeLine(int count, boolean asc) {
        return Observable.just(null)
                .subscribeOn(Schedulers.io())
                .map(o -> DataSourceManager.getInstance().syncJFGCameraWarn(uuid, asc, count))
                .filter(aLong -> aLong > 0)
                .flatMap(aLong -> RxBus.getCacheInstance().toObservable(RobotoGetDataRsp.class)
                        .filter(robotoGetDataRsp -> aLong == robotoGetDataRsp.seq)
                        .timeout(1000, TimeUnit.MILLISECONDS, Observable.just("makeReq timeout")
                                .subscribeOn(AndroidSchedulers.mainThread())
                                .filter(s -> getView() != null)
                                .map(s -> {
                                    getView().onMessageBulkInsert(null, 0);
                                    AppLogger.e(s);
                                    return null;
                                }))
                        .first())
                .flatMap(new Func1<RobotoGetDataRsp, Observable<ArrayList<CamMessageBean>>>() {
                    @Override
                    public Observable<ArrayList<CamMessageBean>> call(RobotoGetDataRsp robotoGetDataRsp) {
                        ArrayList<DataPoint> allList = new ArrayList<>();
                        List<DpMsgDefine.DPAlarm> list_505 = DataSourceManager.getInstance().getValueBetween(uuid, (long) DpMsgMap.ID_505_CAMERA_ALARM_MSG, (long) 0, System.currentTimeMillis());
                        List<DpMsgDefine.DPSdcardSummary> list_222 = DataSourceManager.getInstance().getValueBetween(uuid, (long) DpMsgMap.ID_222_SDCARD_SUMMARY, (long) 0, System.currentTimeMillis());
                        if (list_505 != null) allList.addAll(list_505);
                        if (list_222 != null) allList.addAll(list_222);
                        allList = new ArrayList<>(new HashSet<>(allList));
                        Collections.sort(allList);//来个排序
                        AppLogger.i("get msgList: " + allList.size());
                        return Observable.just(Converter.convert(allList));
                    }
                });


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
                        AppLogger.i("delete: " + result + " id:" + id);
                    }
                }, (Throwable throwable) -> {
                    AppLogger.e(":" + throwable.getLocalizedMessage());
                });
    }

    @Override
    public void stop() {
        super.stop();
        unSubscribe(qeurySub);
    }
}
