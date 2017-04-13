package com.cylan.jiafeigou.n.mvp.contract.cam;


import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.base.view.PropertyView;
import com.cylan.jiafeigou.base.view.ViewableView;
import com.cylan.jiafeigou.base.wrapper.BaseViewablePresenter;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;

import java.util.ArrayList;

import rx.Subscription;

/**
 * Created by yzd on 16-12-15.
 */

public interface CamDelayRecordContract {

    interface View extends ViewableView, PropertyView {
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

        public void startRecord(int cycle, int start, int duration) {
            DpMsgDefine.DPTimeLapse lapse = new DpMsgDefine.DPTimeLapse();
            lapse.timePeriod = cycle;
            lapse.timeStart = start;
            lapse.timeDuration = duration;
            lapse.status = 1;
            JFGDPMsg msg = new JFGDPMsg(DpMsgMap.ID_506_CAMERA_TIME_LAPSE_PHOTOGRAPHY, 0);
            msg.packValue = DpUtils.pack(lapse);
            ArrayList<JFGDPMsg> params = new ArrayList<>();
            params.add(msg);
            try {
                JfgCmdInsurance.getCmd().robotSetData(mUUID, params);
            } catch (JfgException e) {
                e.printStackTrace();
            }
        }

        public void restoreRecord() {
            if (mSubscribe != null && !mSubscribe.isUnsubscribed()) {
                mSubscribe.unsubscribe();
                mSubscribe = null;
            }
        }
    }
}
