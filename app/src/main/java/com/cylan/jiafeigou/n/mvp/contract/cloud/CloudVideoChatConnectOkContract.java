package com.cylan.jiafeigou.n.mvp.contract.cloud;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

/**
 * 作者：zsl
 * 创建时间：2016/9/26
 * 描述：
 */
public interface CloudVideoChatConnectOkContract {

    interface View extends BaseView<Presenter>{
        void showLoadingView();
        void hideLoadingView();
        void setLoadingText(String text);
        void showLoadResult();
    }

    interface Presenter extends BasePresenter{
        void loadVideo();
        void handlerHangUp(String time);
    }
}
