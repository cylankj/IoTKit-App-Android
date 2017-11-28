package com.cylan.jiafeigou.n.mvp.contract.bind;

import com.cylan.jiafeigou.misc.bind.UdpConstant;
import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

/**
 * Created by cylan-hunt on 16-11-12.
 */

public interface SubmitBindingInfoContract {

    interface View extends BaseView {
//        /**
//         * 绑定状态: -2:超时,-1:连接中,0离线,>1 成功 ,2：已经被绑定
//         *
//         * @param state
//         * @deprecated 太笼统的方法, 不好维护,
//         */
//        void bindState(RxEvent.BindDeviceEvent state);

        void onCounting(int percent);

//        void bindState(int bindState);

        void onBindSuccess();

        void onBindFailed();

        void onBindTimeout();

        void onRebindRequired(UdpConstant.UdpDevicePortrait portrait, String reason);

        void onBindCidNotExist();

    }

    interface Presenter extends BasePresenter {
        void clean();
    }
}
