package com.cylan.jiafeigou.n.mvp.impl.cam;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.JFGDPMsgRet;
import com.cylan.entity.jniCall.RobotoGetDataRsp;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.base.wrapper.BasePresenter;
import com.cylan.jiafeigou.dp.DpMsgDefine;
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
    public void performCheckAndInitFaceSetting() {
        Subscription subscribe = Observable.create((Observable.OnSubscribe<Long>) subscriber -> {
            try {
                ArrayList<JFGDPMsg> params = new ArrayList<>();
                params.add(new JFGDPMsg(DpMsgMap.ID_525_CAM_UPLOAD_IMAGE, 0));
                params.add(new JFGDPMsg(DpMsgMap.ID_529_CAM_AI_FACE_SIZE_MSG, 0));
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
                .first()
                .timeout(10, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(robotoGetDataRsp -> {
                    if (robotoGetDataRsp.map != null && robotoGetDataRsp.map.size() > 0) {
                        ArrayList<JFGDPMsg> uploadSetting = robotoGetDataRsp.map.get(DpMsgMap.ID_525_CAM_UPLOAD_IMAGE);
                        ArrayList<JFGDPMsg> faceSizeSetting = robotoGetDataRsp.map.get(DpMsgMap.ID_529_CAM_AI_FACE_SIZE_MSG);
                        Boolean uploadOpened = true;
                        DpMsgDefine.DPDetectionSize detectionSize = null;
                        if (uploadSetting != null && uploadSetting.size() > 0) {
                            uploadOpened = DpUtils.unpackDataWithoutThrow(uploadSetting.get(0).packValue, boolean.class, true);
                        }
                        if (faceSizeSetting != null && faceSizeSetting.size() > 0) {
                            detectionSize = DpUtils.unpackDataWithoutThrow(faceSizeSetting.get(0).packValue, DpMsgDefine.DPDetectionSize.class, null);
                        }
                        mView.onQueryFaceDetectionFinished(uploadOpened);
                        mView.onQueryFaceDetectionSizeFinished(detectionSize);
                    }
                }, error -> {
                    error.printStackTrace();
                    AppLogger.e(error);
                    mView.onQueryFaceDetectionFinished(true);//默认开启
                    mView.onQueryFaceDetectionSizeFinished(new DpMsgDefine.DPDetectionSize());
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

    @Override
    public void performChangeFaceDetectionSizeAction(DpMsgDefine.DPDetectionSize detectionSize) {
        Subscription subscribe = Observable.create((Observable.OnSubscribe<Long>) subscriber -> {
            try {
                ArrayList<JFGDPMsg> params = new ArrayList<>();
                params.add(new JFGDPMsg(DpMsgMap.ID_529_CAM_AI_FACE_SIZE_MSG, System.currentTimeMillis(), DpUtils.pack(detectionSize)));
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
                .subscribe(success -> mView.onChangeFaceDetectionSizeFinished(success, detectionSize), new Action1<Throwable>() {
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
