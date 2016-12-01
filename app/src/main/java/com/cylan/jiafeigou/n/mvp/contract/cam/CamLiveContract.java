package com.cylan.jiafeigou.n.mvp.contract.cam;

import com.cylan.entity.jniCall.JFGMsgVideoResolution;
import com.cylan.entity.jniCall.JFGMsgVideoRtcp;
import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.mvp.model.BeanCamInfo;
import com.cylan.jiafeigou.widget.wheel.SDataStack;

/**
 * Created by cylan-hunt on 16-6-29.
 */
public interface CamLiveContract {


    interface View extends BaseView<Presenter> {


        void onHistoryDataRsp(SDataStack timeSet);

        /**
         * 失败 0:网络失败
         *
         * @param id
         */
        void onFailed(int id);

        void onRtcp(JFGMsgVideoRtcp rtcp);

        void onResolution(JFGMsgVideoResolution resolution);

    }

    interface Presenter extends BasePresenter {


        void fetchHistoryData();

        void startPlayVideo();

        void stopPlayVideo();

        BeanCamInfo getCamInfo();
    }
}

