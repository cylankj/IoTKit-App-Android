package com.cylan.jiafeigou.n.mvp.impl.bind;

import android.text.TextUtils;

import com.cylan.entity.jniCall.JFGResult;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.misc.JResultEvent;
import com.cylan.jiafeigou.misc.SimulatePercent;
import com.cylan.jiafeigou.n.mvp.contract.bind.SubmitBindingInfoContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.rx.RxHelper;
import com.cylan.jiafeigou.rx.RxUiEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.utils.ListUtils;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by cylan-hunt on 16-11-12.
 */

public class SubmitBindingInfoContractImpl extends
        AbstractPresenter<SubmitBindingInfoContract.View>
        implements SubmitBindingInfoContract.Presenter,
        SimulatePercent.OnAction {

    private boolean success = false;

    private SimulatePercent simulatePercent;
    //    private CompositeSubscription compositeSubscription;
    private String uuid;

    public SubmitBindingInfoContractImpl(SubmitBindingInfoContract.View view, String uuid) {
        super(view);
        view.setPresenter(this);
        this.uuid = uuid;
        simulatePercent = new SimulatePercent();
        simulatePercent.setOnAction(this);
    }

    @Override
    public void startCounting() {
        if (simulatePercent != null)
            simulatePercent.start();
    }

    @Override
    public void endCounting() {
        if (simulatePercent != null)
            simulatePercent.stop();
    }

    @Override
    protected Subscription[] register() {
        return new Subscription[]{
                robotSyncDataSub(),
                bindResultSub(),
                monitorBulkDeviceList()
        };
    }

    @Override
    public void start() {
        super.start();
        //查询
        RxBus.getCacheInstance().post(new RxUiEvent.BulkUUidListReq());
    }

    /**
     * 绑定结果:通过{@link com.cylan.jiafeigou.n.engine.DataSourceService#OnResult(JFGResult)}
     * {@link com.cylan.jiafeigou.misc.JResultEvent#JFG_RESULT_BINDDEV}
     *
     * @return
     */
    private Subscription bindResultSub() {
        return RxBus.getCacheInstance().toObservableSticky(RxEvent.BindDeviceEvent.class)
                .filter((RxEvent.BindDeviceEvent bindDeviceEvent) -> {
                    return getView() != null
                            && bindDeviceEvent.jfgResult.event == JResultEvent.JFG_RESULT_BINDDEV;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .map((RxEvent.BindDeviceEvent bindDeviceEvent) -> {
                    if (simulatePercent != null)
                        simulatePercent.boost();
                    success = true;
                    AppLogger.i("bind success");
                    RxBus.getCacheInstance().removeStickyEvent(RxEvent.BindDeviceEvent.class);
                    return null;
                })
                .retry(new RxHelper.RxException<>("bindResultSub"))
                .subscribe();
    }

    /**
     * 客户端登陆成功后,会批量查询设备.
     *
     * @return
     */
    private Subscription monitorBulkDeviceList() {
        return RxBus.getCacheInstance().toObservableSticky(RxUiEvent.BulkDeviceListRsp.class)
                .filter((RxUiEvent.BulkDeviceListRsp deviceList) -> {
                    return getView() != null
                            && deviceList != null
                            && !ListUtils.isEmpty(deviceList.allDevices);
                })
                .flatMap(new Func1<RxUiEvent.BulkDeviceListRsp, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(RxUiEvent.BulkDeviceListRsp deviceList) {
                        AppLogger.i("monitorBulkDeviceList: " + deviceList.allDevices);
                        final int count = deviceList.allDevices.size();
                        for (int i = 0; i < count; i++) {
                            DpMsgDefine.DpWrap wrap = deviceList.allDevices.get(i);
                            if (wrap == null || wrap.baseDpDevice == null) continue;
                            if (TextUtils.equals(uuid,
                                    wrap.baseDpDevice.uuid)) {
                                //hit the binding cid
                                return Observable.just(true);
                            }
                        }
                        return Observable.just(false);
                    }
                })
                .retry(new RxHelper.RxException<>("SubmitBindingInfoContractImpl"))
                .subscribe();
    }


    /**
     * 某些设备上线下面,各种状态变化,都是通过{@link com.cylan.jiafeigou.rx.RxEvent.JFGRobotSyncData}
     *
     * @return
     */
    private Subscription robotSyncDataSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.JFGRobotSyncData.class)
                .subscribeOn(Schedulers.newThread())
                .filter((RxEvent.JFGRobotSyncData jfgRobotSyncData) -> {
                    boolean filter = TextUtils.equals(uuid, jfgRobotSyncData.identity);
                    AppLogger.i("filter: " + filter);
                    return filter;
                })
                .flatMap(new Func1<RxEvent.JFGRobotSyncData, Observable<Integer>>() {
                    @Override
                    public Observable<Integer> call(RxEvent.JFGRobotSyncData jfgRobotSyncData) {
                        if (jfgRobotSyncData.dataList != null) {
                            DpMsgDefine.MsgNet net = DpUtils.getMsg(jfgRobotSyncData.dataList,
                                    DpMsgMap.ID_201_NET,
                                    DpMsgDefine.MsgNet.class);
                            AppLogger.i("yes hit net: " + net);
                            if (net != null)
                                return Observable.just(net.net);
                        }
                        return null;
                    }
                })
                .retry(new RxHelper.RxException<>("robotSyncDataSub"))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((Integer integer) -> {
                    getView().bindState(integer != null ? integer : -1);
                });
    }

    @Override
    public void stop() {
        super.stop();
        if (simulatePercent != null)
            simulatePercent.stop();
    }

    @Override
    public void actionDone() {
        Observable.just(null)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe((Object integer) -> {
                    AppLogger.i("actionDone: " + integer);
                    getView().bindState(1);
                });
    }

    @Override
    public void actionPercent(int percent) {
        Observable.just(percent)
                .filter((Integer integer) -> (getView() != null))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((Integer integer) -> {
                    getView().onCounting(integer);
                });
    }

}
