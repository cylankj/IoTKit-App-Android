package com.cylan.jiafeigou.n.mvp.contract.mine;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.rx.RxEvent;

/**
 * 作者：zsl
 * 创建时间：2016/9/2
 * 描述：
 */
public interface MineInfoSetAliasContract {

    interface View extends BaseView<Presenter> {
        /**
         * 获取到输入的昵称
         *
         * @return
         */
        String getEditName();

        /**
         * 显示进度圈
         */
        void showSendHint();

        /**
         * 隐藏进度圈
         */
        void hideSendHint();

        /**
         * 处理回调的结果
         *
         * @param getUserInfo
         */
        void handlerResult(RxEvent.AccountArrived getUserInfo);

        /**
         * 网络状态变化
         */
        void onNetStateChanged(int state);
    }

    interface Presenter extends BasePresenter {
        /**
         * 发送修改昵称请求
         */
        void saveName(String newAlias);

        boolean isEditEmpty(String string);

    }

}
