package com.cylan.jiafeigou.n.mvp.contract.mine;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.mvp.model.SuggestionChatInfoBean;

import java.util.ArrayList;

/**
 * 作者：zsl
 * 创建时间：2016/9/6
 * 描述：
 */
public interface MineRelativesFriendsContract {

    interface View extends BaseView<Presenter>{
        void showAddRequestList();
        void showRelativesAndFriendsList();
    }

    interface Presenter extends BasePresenter{
        ArrayList<SuggestionChatInfoBean> initAddRequestData();
        ArrayList<SuggestionChatInfoBean> initRelativatesAndFriendsData();
        void addItems(SuggestionChatInfoBean bean);
    }

}
