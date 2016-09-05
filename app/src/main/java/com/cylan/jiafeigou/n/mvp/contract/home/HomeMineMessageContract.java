package com.cylan.jiafeigou.n.mvp.contract.home;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.mvp.model.SuggestionChatInfoBean;

import java.util.ArrayList;

/**
 * 作者：zsl
 * 创建时间：2016/9/5
 * 描述：
 */
public interface HomeMineMessageContract {

    interface View extends BaseView<Presenter>{
        void showMessageList();
        void notifyMessageList();
        void showClearDialog();
    }

    interface Presenter extends BasePresenter{
        ArrayList<SuggestionChatInfoBean> initMessageData();
        void addMessageItem();
    }

}
