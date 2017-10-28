package com.cylan.jiafeigou.n.mvp.impl.setting;

import com.cylan.jiafeigou.base.wrapper.BasePresenter;
import com.cylan.jiafeigou.n.mvp.contract.setting.AIRecognitionContact;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by yanzhendong on 2017/8/2.
 */

public class AIRecognitionPresenter extends BasePresenter<AIRecognitionContact.View> implements AIRecognitionContact.Presenter {

    @Inject
    public AIRecognitionPresenter(AIRecognitionContact.View view) {
        super(view);
    }

    @Override
    public void getObjectDetect() {
//        Subscription subscribe = Observable.just(new DPEntity(uuid, DpMsgMap.ID_515_CAM_ObjectDetect, 0, DBAction.QUERY, singleq))
//                .observeOn(Schedulers.io())
//                .flatMap(this::perform)
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(result -> {
//                    DpMsgDefine.DPCameraObjectDetect response = result.getResultResponse();
//                    mView.onDeviceUpdate(response);
//                }, e -> {
//                    AppLogger.e(e.getMessage());
//                });
//        addSubscription(subscribe);
    }

    @Override
    public void start() {
        super.start();
        addSubscription(LIFE_CYCLE.LIFE_CYCLE_STOP, "AIRecognitionPresenter#getSyncSub", getSyncSub());
    }

    private Subscription getSyncSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.DeviceSyncRsp.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    // TODO: 2017/8/3 监听其他端的操作,及时更新
                }, e -> {
                    AppLogger.e(e.getMessage());
                    e.printStackTrace();
                });
    }
}
