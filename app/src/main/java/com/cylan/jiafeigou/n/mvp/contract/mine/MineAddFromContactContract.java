package com.cylan.jiafeigou.n.mvp.contract.mine;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.rx.RxEvent;

import rx.Subscription;

/**
 * 作者：zsl
 * 创建时间：2016/9/7
 * 描述：
 */
public interface MineAddFromContactContract {

    interface View extends BaseView<Presenter> {

        void initEditText(String alias);

        String getSendMesg();

        void showResultDialog(RxEvent.CheckAccountCallback callback);

        /**
         * 发送请求的进度
         */
        void showSendReqHint();

        /**
         * 隐藏发送的请求的标志
         */
        void hideSendReqHint();

        /**
         * 网络状态变化
         */
        void onNetStateChanged(int state);
    }

    interface Presenter extends BasePresenter {

        void sendRequest(String account, String mesg);

        /**
         * 获取到昵称
         *
         * @return
         */
        Subscription getAccountAlids();

        /**
         * 获取到用户的昵称
         *
         * @return
         */
        String getUserAlias();

        /**
         * 检测账号
         * @param account
         */
        void checkAccount(String account);

        /**
         * 检测账号的回调
         * @return
         */
        Subscription checkAccountCallBack();

        /**
         * 注册网络监听
         */
        void registerNetworkMonitor();

        /**
         * 移除网络监听
         */
        void unregisterNetworkMonitor();
    }

}
