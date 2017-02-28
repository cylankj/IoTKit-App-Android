package com.cylan.jiafeigou.n.mvp.impl.cam;

import android.text.TextUtils;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.RobotoGetDataRsp;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.cache.pool.GlobalDataProxy;
import com.cylan.jiafeigou.dp.BaseValue;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.Converter;
import com.cylan.jiafeigou.misc.JFGRules;
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

    private String uuid;
    private long querySeq;

    public CamMessageListPresenterImpl(CamMessageListContract.View view, String uuid) {
        super(view);
        view.setPresenter(this);
        this.uuid = uuid;
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
        return RxBus.getCacheInstance().toObservable(RxEvent.DataPoolUpdate.class)
                .filter((RxEvent.DataPoolUpdate data) -> (getView() != null && TextUtils.equals(uuid, data.uuid)))
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<RxEvent.DataPoolUpdate, Boolean>() {
                    @Override
                    public Boolean call(RxEvent.DataPoolUpdate update) {
                        if (update.id == DpMsgMap.ID_204_SDCARD_STORAGE) {
                            DpMsgDefine.DPSdStatus sdStatus = GlobalDataProxy.getInstance().getValue(uuid, DpMsgMap.ID_204_SDCARD_STORAGE, null);
                            getView().deviceInfoChanged(update.id, sdStatus);
                        } else if (update.id == DpMsgMap.ID_222_SDCARD_SUMMARY) {
                            DpMsgDefine.DPSdcardSummary sdcardSummary = GlobalDataProxy.getInstance().getValue(uuid, DpMsgMap.ID_222_SDCARD_SUMMARY, null);
                            getView().deviceInfoChanged(update.id, sdcardSummary);
                        } else if (update.id == DpMsgMap.ID_201_NET) {
                            DpMsgDefine.DPNet net = GlobalDataProxy.getInstance().getValue(uuid, DpMsgMap.ID_201_NET, null);
                            getView().deviceInfoChanged(update.id, net);
                        }
                        return null;
                    }
                })
                .retry(new RxHelper.RxException<>("sdcardStatusSub"))
                .subscribe();
    }

    @Override
    public void fetchMessageList(final boolean manually, boolean asc) {
        queryTimeLine(20, asc)
                .map((ArrayList<CamMessageBean> camList) -> {
                    ArrayList<CamMessageBean> list = getView().getList();
                    if (list != null)
                        camList.removeAll(list);//删除重复的
                    return camList;
                })
                .delay(1,TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .filter(new RxHelper.Filter<>("messageListSub()=null?", getView() != null))
                .map((ArrayList<CamMessageBean> jfgdpMsgs) -> {
                    getView().onMessageListRsp(jfgdpMsgs);
                    AppLogger.i("messageListSub+" + jfgdpMsgs.size());
                    return null;
                })
                .retry(new RxHelper.RxException<>("messageListSub"))
                .timeout(5000, TimeUnit.MILLISECONDS, Observable
                        .just("makeReq timeout")
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .filter(s -> (getView() != null))
                        .map(s -> {
                            getView().onMessageListRsp(null);
                            AppLogger.e(s);
                            return null;
                        }))
                .subscribe(o -> {
                }, throwable -> AppLogger.e("messageList err:" + throwable.getLocalizedMessage()));
    }

    private ArrayList<JFGDPMsg> getReqList(long[] versions, int[] ids) {
        if (versions == null || versions.length == 0 || ids == null || ids
                .length == 0 || ids.length != versions.length) {
            return null;
        }
        ArrayList<JFGDPMsg> dps = new ArrayList<>();
        for (int i = 0; i < versions.length; i++) {
            dps.add(new JFGDPMsg(ids[i], versions[i]));
        }
        return dps;
    }

    private Observable<ArrayList<CamMessageBean>> queryTimeLine(int count, boolean asc) {
        return Observable.just(null)
                .subscribeOn(Schedulers.io())
                .filter(o -> !JFGRules.isShareDevice(uuid))
                .map(o -> {
                    ArrayList<JFGDPMsg> dps = getReqList(new long[]{getVersion(DpMsgMap.ID_505_CAMERA_ALARM_MSG, asc),
                                    getVersion(DpMsgMap.ID_222_SDCARD_SUMMARY, asc)},
                            new int[]{DpMsgMap.ID_505_CAMERA_ALARM_MSG, DpMsgMap.ID_222_SDCARD_SUMMARY});
                    try {
                        long req = GlobalDataProxy.getInstance().robotGetDataReq(uuid, dps, count, asc, 0);
                        AppLogger.i("req: " + req);
                        return req;
                    } catch (JfgException e) {
                        AppLogger.e("wth:+" + e.getLocalizedMessage());
                        return 0L;
                    }
                })
                .filter(aLong -> aLong > 0)
                .flatMap(aLong -> RxBus.getCacheInstance().toObservable(RobotoGetDataRsp.class)
                        .filter(robotoGetDataRsp -> aLong == robotoGetDataRsp.seq)
                        .timeout(100, TimeUnit.MILLISECONDS, Observable.just("makeReq timeout")
                                .subscribeOn(AndroidSchedulers.mainThread())
                                .filter(s -> getView() != null)
                                .map(s -> {
//                                    getView().setRefresh(false);
                                    AppLogger.e(s);
                                    return null;
                                }))
                        .first())
                .flatMap(new Func1<RobotoGetDataRsp, Observable<ArrayList<CamMessageBean>>>() {
                    @Override
                    public Observable<ArrayList<CamMessageBean>> call(RobotoGetDataRsp robotoGetDataRsp) {
                        ArrayList<BaseValue> allList = new ArrayList<>();
                        ArrayList<BaseValue> list_505 = GlobalDataProxy.getInstance().fetchLocalList(uuid, DpMsgMap.ID_505_CAMERA_ALARM_MSG);
                        ArrayList<BaseValue> list_222 = GlobalDataProxy.getInstance().fetchLocalList(uuid, DpMsgMap.ID_222_SDCARD_SUMMARY);
                        if (list_505 != null) allList.addAll(list_505);
                        if (list_222 != null) allList.addAll(list_222);
                        allList = new ArrayList<>(new HashSet<>(allList));
                        Collections.sort(allList);//来个排序
                        AppLogger.i("get msgList: " + allList.size());
                        return Observable.just(Converter.convert(uuid, allList));
                    }
                });


    }

    private long getVersion(int id, boolean asc) {
        ArrayList<BaseValue> list = GlobalDataProxy.getInstance().fetchLocalList(uuid, id);
        if (list == null) return 0;
        if (asc) {
            BaseValue value = Collections.min(list);
            return value != null ? value.getVersion() : 0;
        } else {
            BaseValue value = Collections.max(list);
            return value != null ? value.getVersion() : 0;
        }
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
                        boolean result = GlobalDataProxy.getInstance().deleteAll(uuid, id, map.get(id));
                        AppLogger.i("delete: " + result + " id:" + id);
                    }
                }, (Throwable throwable) -> {
                    AppLogger.e(":" + throwable.getLocalizedMessage());
                });
    }
}
