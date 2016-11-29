package com.cylan.jiafeigou.n.mvp.contract.mine;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

import rx.Subscription;

/**
 * 作者：zsl
 * 创建时间：2016/9/7
 * 描述：
 */
public interface MineAddFromContactContract {

    interface View extends BaseView<Presenter> {

        void initEditText(String alids);

        String getSendMesg();

        void showResultDialog();

        /**
         * 发送请求的进度
         */
        void showSendReqHint();

        /**
         * 隐藏发送的请求的标志
         */
        void hideSendReqHint();
    }

    interface Presenter extends BasePresenter {

        void sendRequest(String account,String mesg);

        /**
         * 获取到昵称
         * @return
         */
        Subscription getAccountAlids();

        /**
         * 获取到用户的昵称
         * @return
         */
        String getUserAlias();
    }

}
