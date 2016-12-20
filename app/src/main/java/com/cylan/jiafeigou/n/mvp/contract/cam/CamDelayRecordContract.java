package com.cylan.jiafeigou.n.mvp.contract.cam;

import android.os.SystemClock;
import android.text.TextUtils;

import com.cylan.entity.jniCall.JFGMsgVideoResolution;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.BeanCamInfo;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.google.gson.Gson;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by yzd on 16-12-15.
 */

public interface CamDelayRecordContract {

    interface View extends BaseView<Presenter> {

        void onResolution(JFGMsgVideoResolution resolution) throws JfgException;

        void refreshRecordTime(long time);

        void onRecordFinished();
    }

    class Presenter extends AbstractPresenter<View> implements BasePresenter {
        private CompositeSubscription compositeSubscription;
        private BeanCamInfo mCamInfo;
        private Subscription mSubscribe;
        private long mRecordStartTime;
        private long mRecordDuration;

        public Presenter(View view) {
            super(view);
        }

        @Override
        public void start() {
            unSubscribe(compositeSubscription);
            compositeSubscription = new CompositeSubscription();
            compositeSubscription.add(resolutionNotifySub());
        }

        @Override
        public void stop() {

        }

        private Subscription resolutionNotifySub() {
            return RxBus.getCacheInstance().toObservable(JFGMsgVideoResolution.class)
                    .filter(jfgMsgVideoResolution -> {
                        boolean filter = getCamInfo() != null
                                && getCamInfo().deviceBase != null
                                && TextUtils.equals(getCamInfo().deviceBase.uuid, jfgMsgVideoResolution.peer)
                                && getView() != null;
                        if (!filter) {
                            AppLogger.e("getView(): " + (getView() != null));
                            AppLogger.e("this peer is out date: " + jfgMsgVideoResolution.peer);
                        }
                        return filter;
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(resolution -> {
//                            isRtcpSignal = true;
                        try {
                            getView().onResolution(resolution);
                        } catch (JfgException e) {
                            e.printStackTrace();
                        }
                        AppLogger.i("ResolutionNotifySub: " + new Gson().toJson(resolution));
                    }, throwable -> {
                        AppLogger.e("resolution err: " + throwable.getLocalizedMessage());
                    });
        }


        public BeanCamInfo getCamInfo() {
            return mCamInfo;
        }

        public void setCamInfo(BeanCamInfo camInfo) {
            mCamInfo = camInfo;
        }

        public void startRecord(int recordMode, long start, long duration) {
            mRecordStartTime = start;
            mRecordDuration = duration;
            mSubscribe = Observable.interval(1, recordMode == 0 ? 60 : 20, TimeUnit.SECONDS)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(time -> {
                        long progress = SystemClock.currentThreadTimeMillis() - mRecordStartTime;
                        if (duration < mRecordDuration) {
                            getView().refreshRecordTime(progress);
                        } else {
                            getView().onRecordFinished();
                        }
                    });
            mockRecordFinished();
        }

        public void mockRecordFinished() {
            restoreRecord();
            Observable.just("just for test").delay(10, TimeUnit.SECONDS).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(s -> getView().onRecordFinished());
        }

        public void restoreRecord() {
            if (mSubscribe != null && !mSubscribe.isUnsubscribed()) {
                mSubscribe.unsubscribe();
                mSubscribe = null;
            }
        }
    }
}
