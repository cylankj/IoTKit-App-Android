package com.cylan.jiafeigou.n.mvp.impl.cam;

import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.db.impl.BaseDPTaskDispatcher;
import com.cylan.jiafeigou.cache.db.impl.BaseDPTaskResult;
import com.cylan.jiafeigou.cache.db.module.DPEntity;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.cache.db.view.DBAction;
import com.cylan.jiafeigou.cache.db.view.DBOption;
import com.cylan.jiafeigou.cache.db.view.IDPEntity;
import com.cylan.jiafeigou.cache.db.view.IDPTaskResult;
import com.cylan.jiafeigou.dp.BaseDataPoint;
import com.cylan.jiafeigou.dp.DataPoint;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.module.Command;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamMessageListContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.CamMessageBean;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ListUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.TimeUtils;
import com.cylan.jiafeigou.widget.wheel.WonderIndicatorWheelView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by cylan-hunt on 16-7-13.
 */
public class CamMessageListPresenterImpl extends AbstractPresenter<CamMessageListContract.View>
        implements CamMessageListContract.Presenter {

    private List<WonderIndicatorWheelView.WheelItem> dateItemList = new ArrayList<>();

    public CamMessageListPresenterImpl(CamMessageListContract.View view, String uuid) {
        super(view);
    }

    @Override
    public void start() {
        super.start();
        monitorSdcardStatusSub();
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
    private void monitorSdcardStatusSub() {
        Subscription subscribe = RxBus.getCacheInstance().toObservable(RxEvent.DeviceSyncRsp.class)
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
                }, e -> AppLogger.d(e.getMessage()));
        addStopSubscription(subscribe);
    }


    private List<IDPEntity> buildEntity(long timeStart, boolean asc, boolean isMaxTime, boolean useMaxLimit) {
        List<IDPEntity> list = new ArrayList<>();
        list.add(new DPEntity()
                .setMsgId(222)
                .setUuid(uuid)
                .setAction(DBAction.CAM_MULTI_QUERY)
                .setOption(new DBOption.MultiQueryOption(timeStart, asc, isMaxTime, useMaxLimit))
                .setAccount(DataSourceManager.getInstance().getJFGAccount().getAccount()));
        list.add(new DPEntity()
                .setMsgId(505)
                .setUuid(uuid)
                .setAction(DBAction.CAM_MULTI_QUERY)
                .setOption(new DBOption.MultiQueryOption(timeStart, asc, isMaxTime, useMaxLimit))
                .setAccount(DataSourceManager.getInstance().getJFGAccount().getAccount()));
        list.add(new DPEntity()
                .setMsgId(512)
                .setUuid(uuid)
                .setAction(DBAction.CAM_MULTI_QUERY)
                .setOption(new DBOption.MultiQueryOption(timeStart, asc, isMaxTime, useMaxLimit))
                .setAccount(DataSourceManager.getInstance().getJFGAccount().getAccount()));
        if (JFGRules.isBell(getDevice().pid)) {
            //adapter for doorbell
            list.add(new DPEntity()
                    .setMsgId(401)
                    .setUuid(uuid)
                    .setAction(DBAction.CAM_MULTI_QUERY)
                    .setOption(new DBOption.MultiQueryOption(timeStart, asc, isMaxTime, useMaxLimit))
                    .setAccount(DataSourceManager.getInstance().getJFGAccount().getAccount()));
        }
        if (JFGRules.isFaceFragment(getDevice().pid)) {
            list.add(new DPEntity()
                    .setMsgId(DpMsgMap.ID_518_CAM_SETFACEIDSTATUS)
                    .setUuid(uuid)
                    .setAction(DBAction.CAM_MULTI_QUERY)
                    .setOption(new DBOption.MultiQueryOption(timeStart, asc, isMaxTime, useMaxLimit))
                    .setAccount(DataSourceManager.getInstance().getJFGAccount().getAccount()));
        }
        if (JFGRules.hasDoorLock(getDevice().pid)) {
            // TODO: 2017/12/12 加入门锁消息 DP id
//            list.add(new DPEntity()
//                    .setMsgId(DpMsgMap.ID_518_CAM_SETFACEIDSTATUS)
//                    .setUuid(uuid)
//                    .setAction(DBAction.CAM_MULTI_QUERY)
//                    .setOption(new DBOption.MultiQueryOption(timeStart, asc, isMaxTime, useMaxLimit))
//                    .setAccount(DataSourceManager.getInstance().getJFGAccount().getAccount()));
        }
        return list;
    }

    /**
     * 消息列表请求
     *
     * @param timeStart
     * @param asc:true:从timeStart开始，往前查，直到timeEnd.,false:从timeStart开始，到现在。 false:向后查。true:向前查。
     * @return
     */
    private Observable<IDPTaskResult> getMessageListQuery(long timeStart, boolean asc) {
        Log.d("getMessageListQuery", "getMessageListQuery:" + timeStart + ",asc: " + asc);
        try {
            boolean isMax = TimeUtils.getSpecificDayEndTime(timeStart) == timeStart;
            boolean useMaxLimit = !JFGRules.isFaceFragment(getDevice().pid);
            return BaseDPTaskDispatcher.getInstance().perform(buildEntity(timeStart, asc, isMax, useMaxLimit));
        } catch (Exception e) {
            return Observable.just(BaseDPTaskResult.ERROR);
        }
    }

    /**
     * 初次进入页面，先要确定第一天的时间。
     */
    private void loadDataListFirst() {
        refreshDateList(true);
    }

    @Override
    public void fetchMessageListByFaceId(long timeStart, boolean asc, boolean refresh) {
        //1.timeStart==0->服务器，本地
        //服务器：1.日历。2.偏移到最靠近有数据的一天。开始查。以后，点击开始查。
        //本地，查出日历。
        if (timeStart == 0 && !JFGRules.isFaceFragment(getDevice().pid)) {
            loadDataListFirst();
        }
        Subscription subscription = getMessageListQuery(timeStart, false)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap(baseDPTaskResult -> {
                    List<DataPoint> result = baseDPTaskResult.getResultResponse();
                    AppLogger.d("fetchLocalList: " + ListUtils.getSize(result));
                    if (ListUtils.getSize(result) == 0) {
                        return Observable.just(new ArrayList<CamMessageBean>());
                    }
                    ArrayList<CamMessageBean> list = new ArrayList<>();
                    for (DataPoint dataPoint : result) {
                        CamMessageBean bean = new CamMessageBean();
                        bean.message = (BaseDataPoint) dataPoint;
                        if (!list.contains(bean))//防止重复
                        {
                            list.add(bean);
                        }
                    }
                    return Observable.just(list);
                })
                .filter(result -> mView != null && result != null)
                .flatMap(camList -> {
                    //需要和列表里面的items 融合
                    ArrayList<CamMessageBean> list = new ArrayList<>(mView.getList());
                    if (list.size() > 0) {
                        boolean isFooter = list.get(list.size() - 1).viewType == CamMessageBean.ViewType.FOOT;
                        if (isFooter) {
                            list.remove(list.size() - 1);
                        }
                    }
                    camList.removeAll(list);//camList是降序
                    AppLogger.d("uiList: " + ListUtils.getSize(list) + ",newList: " + ListUtils.getSize(camList));
                    if (camList.size() > 0) {
                        //检查是否 append 或者insert
                        if (ListUtils.getSize(list) == 0) {//已有的列表为空,直接append
                            return Observable.just(new Pair<>(camList, true));
                        }
                        if (camList.get(camList.size() - 1).message.version >= list.get(0).message.version) {
                            return Observable.just(new Pair<>(camList, false));//insert(0)
                        }
                        return Observable.just(new Pair<>(camList, true));//append
                    }
                    return Observable.just(new Pair<>(camList, true));
                })
                .observeOn(AndroidSchedulers.mainThread())
                .filter(ret -> mView != null && ret != null)
                .subscribeOn(Schedulers.io())
                .delay(500, TimeUnit.MILLISECONDS)
                .filter(ret -> mView != null)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnTerminate(() -> mView.loadingDismiss())
                .subscribe(result -> {
                            if (result.second) {
                                mView.onListAppend(result.first);
                            } else {
                                mView.onListInsert(result.first, 0);
                            }
                        },
                        throwable -> {
                            AppLogger.e("err: " + throwable.getLocalizedMessage());
                            mView.onErr();
                        });
        addSubscription(subscription, "DPCamMultiQueryTask");
    }

    private List<IDPEntity> buildMultiEntities(ArrayList<CamMessageBean> beanList) {
        List<IDPEntity> entities = new ArrayList<>();
        for (CamMessageBean bean : beanList) {
            DPEntity dpEntity = new DPEntity();
            dpEntity.setUuid(uuid);
            dpEntity.setAccount(DataSourceManager.getInstance().getJFGAccount().getAccount());
            dpEntity.setMsgId((int) bean.message.msgId);
            dpEntity.setVersion(bean.message.version);
            dpEntity.setAction(DBAction.DELETED);
            entities.add(dpEntity);
            Log.d(TAG, "删除:" + bean.message);
        }
        return entities;
    }

    public Observable<IDPTaskResult> perform(List<? extends IDPEntity> entity) {
        return BaseDPTaskDispatcher.getInstance().perform(entity);
    }

    @Override
    public void removeItems(ArrayList<CamMessageBean> beanList) {
        if (ListUtils.isEmpty(beanList)) {
            return;
        }
        List<IDPEntity> list = buildMultiEntities(beanList);
        Subscription subscription = BaseDPTaskDispatcher.getInstance().perform(list)
                .subscribeOn(Schedulers.io())
                .filter(result -> mView != null)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(idpTaskResult -> {
                    if (idpTaskResult.getResultCode() == 0) {
                        //good
                        mView.onMessageDeleteSuc();
                        AppLogger.d("需要加载下一页");
                        if (ListUtils.getSize(mView.getList()) == 0) {
                            //需要刷新日期.
                            refreshDateList(false);
                        }
                    }
                }, throwable -> {
                    AppLogger.e("err:" + throwable.getLocalizedMessage());
                    mView.onErr();
                });
        addSubscription(subscription, "removeItems");
    }

    @Override
    public void removeAllItems(ArrayList<CamMessageBean> beanList, boolean removeAll) {

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
    private Observable<IDPTaskResult> getDateListQuery() {
        DPEntity entity = new DPEntity();
        JFGAccount account = DataSourceManager.getInstance().getJFGAccount();
        Device device = DataSourceManager.getInstance().getDevice(uuid);
        entity.setAccount(account == null ? "" : account.getAccount());
        entity.setUuid(uuid);
//        if (device.available()) {
//        if (JFGRules.isFaceFragment(device.pid)) {
//            entity.setOption(DBOption.BaseDBOption.CamMultiDateOption.CAMERA_FACE_ANY_DAYS);
//        } else
        if (JFGRules.isBell(device.pid)) {
            entity.setOption(DBOption.BaseDBOption.CamMultiDateOption.BELL_7_DAYS);
        } else if (JFGRules.isCamera(device.pid)) {
            entity.setOption(DBOption.BaseDBOption.CamMultiDateOption.BELL_7_DAYS);
        }
//        } else {
//            entity.setOption(DBOption.BaseDBOption.CamMultiDateOption.BELL_7_DAYS);
//        }
        entity.setAction(DBAction.CAM_DATE_QUERY);
        try {
            return BaseDPTaskDispatcher.getInstance().perform(entity);
        } catch (Exception e) {
            return Observable.just(BaseDPTaskResult.ERROR);
        }
    }

    @Override
    public void refreshDateList(boolean needToLoadList) {
        DataSourceManager.getInstance().syncDeviceProperty(uuid, 204);
        Subscription subscription = getDateListQuery()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(throwable -> AppLogger.e("err: " + throwable.getLocalizedMessage()))
                .filter(ret -> mView != null && ret != null && ret.getResultResponse() != null)
                .map(result -> {
                    if (result.getResultCode() == 0) {
//                        mView.onDateMapRsp(dateItemList = result.getResultResponse());
                        dateItemList = result.getResultResponse();
                    }
                    return dateItemList;
                })
                .filter(ret -> needToLoadList)
                .subscribeOn(Schedulers.computation())
                .map(wheelItems -> {
                    long timeHit = 0;
                    WonderIndicatorWheelView.WheelItem theItem = null;
                    for (WonderIndicatorWheelView.WheelItem item : wheelItems) {
                        if (item.wonderful) {//找出最近的一天.就可以了.
                            if (item.time > timeHit) {
                                timeHit = item.time;
                                theItem = item;
                            }
                        }
                    }
                    if (timeHit != 0) {
                        //需要保证这个timeHit是当天的最大的一个,干脆用一天的最后一秒
                        AppLogger.d("Max dateList timeHit before:" + timeHit);
                        timeHit = TimeUtils.getSpecificDayEndTime(timeHit);
                        AppLogger.d("Max dateList timeHit:" + timeHit);
                        fetchMessageListByFaceId(timeHit, false, false);
                    }
                    if (theItem != null) {
                        int index = dateItemList.indexOf(theItem);
                        theItem.selected = true;
                        if (index >= 0) {
                            dateItemList.get(index).selected = true;
                        }
                        Log.d("selected", "selected?" + dateItemList);
                    }
                    return new Pair<>(timeHit, theItem);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .filter(ret -> mView != null)
                .subscribe(pair -> {
                    if (pair.first == 0) {
                        mView.onErr();//no date list
                    } else {
                        mView.onDateMapRsp(dateItemList);
                    }
                }, AppLogger::e);
        addSubscription(subscription, "DPCamDateQueryTask");
    }

    @Override
    public void fetchVisitorMessageList(int type, final String id, long sec, boolean refresh) {
        Subscription subscribe = Observable.just("fetchVisitorMessageList")
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .map(cmd -> {
                    try {
                        DpMsgDefine.FetchMsgListReq reqContent = new DpMsgDefine.FetchMsgListReq();
                        reqContent.cid = uuid;
                        reqContent.faceId = id == null ? "" : id;
                        reqContent.msgType = type;
                        reqContent.seq = refresh ? 0 : sec;
                        AppLogger.i(reqContent.toString());
                        return Command.getInstance().sendUniservalDataSeq(8, DpUtils.pack(reqContent));
                    } catch (Exception e) {
                        AppLogger.e(MiscUtils.getErr(e));
                        throw new RuntimeException(e);
                    }
                })
                .flatMap(seq -> RxBus.getCacheInstance().toObservable(RxEvent.UniversalDataRsp.class).first(rsp -> rsp.seq == seq))
                .map(rsp -> {
                    AppLogger.e("Fetch Information seq:" + rsp.seq);
                    DpMsgDefine.FetchMsgListRsp rrsp = DpUtils.unpackDataWithoutThrow(rsp.data, DpMsgDefine.FetchMsgListRsp.class, null);
                    AppLogger.i("Raw Fetch Result:" + rrsp);
                    //转化出。
                    ArrayList<CamMessageBean> list = new ArrayList<>();
                    if (rrsp != null && TextUtils.equals(rrsp.cid, uuid) && rrsp.dataList != null) {
                        CamMessageBean bean;
                        for (DpMsgDefine.DPHeader header : rrsp.dataList) {
                            if (header.msgId == DpMsgMap.ID_505_CAMERA_ALARM_MSG) {
                                bean = new CamMessageBean();
                                DpMsgDefine.DPAlarm dpAlarm = DpUtils.unpackDataWithoutThrow(header.bytes, DpMsgDefine.DPAlarm.class, null);
                                if (dpAlarm != null) {
                                    dpAlarm.version = header.version;
                                    dpAlarm.msgId = header.msgId;
                                    bean.message = dpAlarm;
                                    list.add(bean);
                                }
                            } else if (header.msgId == DpMsgMap.ID_222_SDCARD_SUMMARY) {
                                bean = new CamMessageBean();
                                DpMsgDefine.DPSdcardSummary sdcardSummary = DpUtils.unpackDataWithoutThrow(header.bytes, DpMsgDefine.DPSdcardSummary.class, null);
                                if (sdcardSummary != null) {
                                    sdcardSummary.version = header.version;
                                    sdcardSummary.msgId = header.msgId;
                                    bean.message = sdcardSummary;
                                    list.add(bean);
                                }
                            } else if (header.msgId == DpMsgMap.ID_401_BELL_CALL_STATE) {
                                bean = new CamMessageBean();
                                DpMsgDefine.DPBellCallRecord bellCallRecord = DpUtils.unpackDataWithoutThrow(header.bytes, DpMsgDefine.DPBellCallRecord.class, null);
                                if (bellCallRecord != null) {
                                    bellCallRecord.msgId = header.msgId;
                                    bellCallRecord.version = header.version;
                                    list.add(bean);
                                }
                            } else if (header.msgId == DpMsgMap.ID_512_CAMERA_ALARM_MSG_V3) {
                                bean = new CamMessageBean();
                                DpMsgDefine.DPAlarm dpAlarm = DpUtils.unpackDataWithoutThrow(header.bytes, DpMsgDefine.DPAlarm.class, null);
                                if (dpAlarm != null) {
                                    dpAlarm.version = header.version;
                                    dpAlarm.msgId = header.msgId;
                                    bean.message = dpAlarm;
                                    list.add(bean);
                                }
                            } else if (header.msgId == DpMsgMap.ID_518_CAM_SETFACEIDSTATUS) {
                                bean = new CamMessageBean();
                                DpMsgDefine.DPSetFaceIdStatus faceIdStatus = DpUtils.unpackDataWithoutThrow(header.bytes, DpMsgDefine.DPSetFaceIdStatus.class, null);
                                if (faceIdStatus != null) {
                                    faceIdStatus.version = header.version;
                                    faceIdStatus.msgId = header.msgId;
                                    bean.message = faceIdStatus;
                                    list.add(bean);
                                }
                            } else if (header.msgId == DpMsgMap.ID_526_CAM_AI_WARM_MSG) {
                                bean = new CamMessageBean();
                                DpMsgDefine.DPCameraAIWarmMsg cameraAIWarmMsg = DpUtils.unpackDataWithoutThrow(header.bytes, DpMsgDefine.DPCameraAIWarmMsg.class, null);
                                if (cameraAIWarmMsg != null) {
                                    cameraAIWarmMsg.version = header.version;
                                    cameraAIWarmMsg.msgId = header.msgId;
                                    bean.message = cameraAIWarmMsg;
                                }
                                list.add(bean);
                            }
                        }
                    }
                    return list;
                })
                .timeout(10, TimeUnit.SECONDS, Observable.just(null))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(items -> {
//                    AppLogger.e("Fetch Result:" + items);
                    AppLogger.e("Fetch Result: items.size: " + items.size());
                    if (refresh) {
                        mView.onVisitorListInsert(items);
                    } else {
                        mView.onVisitorListAppend(items);
                    }
                }, e -> {
                    e.printStackTrace();
                    AppLogger.e(e);
                });
        addSubscription(subscribe, "fetchMessageList_faceId");
    }
}
