package com.cylan.jiafeigou.n.mvp.contract.cam;


import com.cylan.jiafeigou.base.view.ViewableView;
import com.cylan.jiafeigou.base.wrapper.BaseViewablePresenter;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by yzd on 16-12-15.
 */

public interface CamDelayRecordContract {

    interface View extends ViewableView {
        String HANDLE_TIME_INTERVAL = "HANDLE_TIME_INTERVAL";
        String HANDLE_TIME_DURATION = "HANDLE_TIME_DURATION";

        void refreshRecordTime(long time);

        void onMarkRecordInformation(int interval, int recordDuration, int remainTime);

        void onRecordFinished();
    }

    class Presenter extends BaseViewablePresenter<View> {
        private Subscription mSubscribe;

        private int mRecordMode = 0;
        private int mRecordTime = 24;
        private int mRecordRemainTime;
        private long mRecordStartTime = -1;
        private long mRecordDuration = -1;


        @Override
        public void onViewAction(int action, String handle, Object extra) {
            switch (handle) {
                case View.HANDLE_TIME_INTERVAL:

                    break;
                case View.HANDLE_TIME_DURATION:
                    break;
            }
            mView.onMarkRecordInformation(mRecordMode, mRecordTime, mRecordRemainTime);
        }

        public void startRecord() {
            mSubscribe = Observable.interval(1, mRecordMode == 0 ? 60 : 20, TimeUnit.SECONDS)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(time -> {
                        long progress = System.currentTimeMillis() - mRecordStartTime;
                        if (progress < mRecordDuration) {
                            mView.refreshRecordTime(progress);
                        } else {
                            mView.onRecordFinished();
                        }
                    });
            mSubscriptions.add(mSubscribe);
        }

        public void restoreRecord() {
            if (mSubscribe != null && !mSubscribe.isUnsubscribed()) {
                mSubscribe.unsubscribe();
                mSubscribe = null;
            }
        }
    }
}
