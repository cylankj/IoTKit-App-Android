package com.cylan.jiafeigou.n.mvp.impl.cam;

import android.text.TextUtils;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.RobotoGetDataRsp;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DataPoint;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.Converter;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamMessageListContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.CamMessageBean;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.rx.RxHelper;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ListUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.TimeUtils;
import com.cylan.jiafeigou.widget.wheel.WonderIndicatorWheelView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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

    private Subscription querySub;
    private Device device;

    public CamMessageListPresenterImpl(CamMessageListContract.View view, String uuid) {
        super(view, uuid);
        view.setPresenter(this);
        device = DataSourceManager.getInstance().getJFGDevice(uuid);
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

    @Override
    public void fetchMessageList(final int count, boolean asc) {
        unSubscribe(querySub);
        querySub = queryTimeLine(count, 0, asc)
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


    private Observable<ArrayList<CamMessageBean>> queryTimeLine(int count, long version, boolean asc) {
        return Observable.just(null)
                .subscribeOn(Schedulers.io())
                .map(o -> DataSourceManager.getInstance().syncJFGCameraWarn(uuid, version, asc, count))
                .filter(aLong -> aLong > 0)
                .flatMap(aLong -> RxBus.getCacheInstance().toObservable(RobotoGetDataRsp.class)
                        .filter(robotoGetDataRsp -> aLong == robotoGetDataRsp.seq)
                        .timeout(2000, TimeUnit.MILLISECONDS, Observable.just("makeReq timeout")
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
                        return Observable.just(Converter.convert(allList, device.regionType));
                    }
                });


    }

//    private Observable<Object> makeLocalQuery(int msgId, long version, boolean asc, int count) {
//        IDPEntity entity = new DPEntity();
//        entity.setUuid(uuid)
//                .setAccount(DataSourceManager.getInstance().getJFGAccount().getAccount())
//                .setMsgId(msgId)
//                .setAction(DBAction.QUERY)
//                .setVersion(version)
//                .setOption(new DBOption.SingleQueryOption(asc, count));
//        BaseDPTaskDispatcher.getInstance().perform(entity);
//    }

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

    private List<WonderIndicatorWheelView.WheelItem> dateItemList = new ArrayList<>();

    @Override
    public List<WonderIndicatorWheelView.WheelItem> getDateList() {
        return dateItemList;
    }

    @Override
    public void refreshDateList() {
        Observable.just("go to get and assemble Date List")
                .subscribeOn(Schedulers.io())
                .flatMap(s -> {
                    //今天凌晨时间戳。
                    long todayTimeStamp = TimeUtils.getTodayStartTime();
                    ArrayList<JFGDPMsg> list = (ArrayList<JFGDPMsg>) MiscUtils.getCamDateVersionList(todayTimeStamp);
                    try {
                        long ret = JfgCmdInsurance.getCmd().robotGetData(uuid, list, 1, true, 0);
                        return Observable.just(ret);
                    } catch (JfgException e) {
                        AppLogger.e("err: " + e.getLocalizedMessage());
                        return Observable.just(-1L);
                    }
                })
                .filter(aLong -> aLong != -1)
                .flatMap(aLong ->
                        RxBus.getCacheInstance().toObservable(RobotoGetDataRsp.class)
                                .subscribeOn(Schedulers.computation())
                                .filter(result -> result != null && TextUtils.equals(result.identity, uuid) && result.seq == aLong))
                .subscribeOn(Schedulers.computation())
                .map(robotoGetDataRsp -> {
                    ArrayList<JFGDPMsg> list = new ArrayList<>();
                    Iterator<Integer> iterator = robotoGetDataRsp.map.keySet().iterator();
                    while (iterator.hasNext()) {
                        list.addAll(robotoGetDataRsp.map.get(iterator.next()));
                    }
                    int size = ListUtils.getSize(list);
                    HashMap<String, Long> dateMap = new HashMap<>();
                    for (int i = 0; i < size; i++) {
                        String date = TimeUtils.getMediaPicTimeInString(list.get(i).version);
                        if (!dateMap.containsKey(date)) {
                            dateMap.put(date, list.get(i).version);
                        }
                    }
                    dateItemList.clear();
                    long timeStart = TimeUtils.getTodayStartTime();
                    for (int i = 0; i < 15; i++) {
                        long time = timeStart - i * 24 * 3600 * 1000L;
                        String date = TimeUtils.getMediaPicTimeInString(time);
                        Long r = dateMap.get(date);
                        WonderIndicatorWheelView.WheelItem item = new WonderIndicatorWheelView.WheelItem();
                        item.wonderful = (r != null && r > 0);
                        item.time = time;
                        dateItemList.add(item);
                    }
                    AppLogger.d("dateList size :" + size + " " + dateMap);

                    return dateItemList;
                })
                .doOnError(throwable -> AppLogger.e("err: " + throwable.getLocalizedMessage()))
                .observeOn(AndroidSchedulers.mainThread())
                .filter(a -> mView != null)
                .subscribe(mapResult -> mView.onDateMapRsp(dateItemList),
                        throwable -> AppLogger.e("err: " + throwable.getLocalizedMessage()));
    }

    @Override
    public void stop() {
        super.stop();
        unSubscribe(querySub);
    }
}
