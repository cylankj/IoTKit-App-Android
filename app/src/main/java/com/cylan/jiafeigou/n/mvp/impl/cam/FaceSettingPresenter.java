package com.cylan.jiafeigou.n.mvp.impl.cam;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.JFGDPMsgRet;
import com.cylan.entity.jniCall.RobotoGetDataRsp;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.base.wrapper.BasePresenter;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.module.Command;
import com.cylan.jiafeigou.n.mvp.contract.cam.FaceSettingContract;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by yanzhendong on 2018/1/26.
 */

public class FaceSettingPresenter extends BasePresenter<FaceSettingContract.View> implements FaceSettingContract.Presenter {
    @Inject
    public FaceSettingPresenter(FaceSettingContract.View view) {
        super(view);
    }

    @Override
    public void performCheckFaceDetectionSetting() {
        Subscription subscribe = Observable.create((Observable.OnSubscribe<Long>) subscriber -> {
            try {
                ArrayList<JFGDPMsg> params = new ArrayList<>();
                JFGDPMsg msg = new JFGDPMsg(DpMsgMap.ID_525_CAM_UPLOAD_IMAGE, 0);
                params.add(msg);
                long seq = Command.getInstance().robotGetData(uuid, params, 1, false, 0);
                subscriber.onNext(seq);
                subscriber.onCompleted();
            } catch (JfgException e) {
                e.printStackTrace();
                subscriber.onError(e);
            }
        })
                .subscribeOn(Schedulers.io())
                .flatMap(seq -> RxBus.getCacheInstance().toObservable(RobotoGetDataRsp.class).filter(rsp -> rsp.seq == seq))
                .map(robotoGetDataRsp -> {
                    Boolean uploadImage = true;
                    if (robotoGetDataRsp.map != null && robotoGetDataRsp.map.get(DpMsgMap.ID_525_CAM_UPLOAD_IMAGE) != null) {
                        ArrayList<JFGDPMsg> msgs = robotoGetDataRsp.map.get(DpMsgMap.ID_525_CAM_UPLOAD_IMAGE);
                        if (msgs.size() > 0) {
                            JFGDPMsg msg = msgs.get(0);
                            uploadImage = DpUtils.unpackDataWithoutThrow(msg.packValue, boolean.class, true);
                        }
                    }
                    return uploadImage;
                })
                .first()
                .timeout(10, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        mView.onQueryFaceDetectionFinished(aBoolean);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                        AppLogger.e(throwable);
                        mView.onQueryFaceDetectionFinished(true);//默认开启
                    }
                });
        addStopSubscription(subscribe);
    }

    @Override
    public void performChangeFaceDetectionAction(boolean isChecked) {
        Subscription subscribe = Observable.create((Observable.OnSubscribe<Long>) subscriber -> {
            try {
                ArrayList<JFGDPMsg> params = new ArrayList<>();
                params.add(new JFGDPMsg(DpMsgMap.ID_525_CAM_UPLOAD_IMAGE, System.currentTimeMillis(), DpUtils.pack(isChecked)));
                long seq = Command.getInstance().robotSetData(uuid, params);
                subscriber.onNext(seq);
                subscriber.onCompleted();
            } catch (JfgException e) {
                e.printStackTrace();
                AppLogger.e(e);
                subscriber.onError(e);
            }
        })
                .subscribeOn(Schedulers.io())
                .flatMap(seq -> RxBus.getCacheInstance().toObservable(RxEvent.SetDataRsp.class).filter(rsp -> rsp.seq == seq))
                .map(setDataRsp -> {
                    boolean success = false;
                    if (setDataRsp != null && setDataRsp.rets != null && setDataRsp.rets.size() > 0) {
                        JFGDPMsgRet msgRet = setDataRsp.rets.get(0);
                        success = msgRet.ret == 0;
                    }
                    return success;
                })
                .first()
                .timeout(10, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        mView.onChangeFaceDetectionFinished(aBoolean);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                        AppLogger.e(throwable);
                        mView.onChangeFaceDetectionFinished(false);
                    }
                });
        addStopSubscription(subscribe);
    }
}
