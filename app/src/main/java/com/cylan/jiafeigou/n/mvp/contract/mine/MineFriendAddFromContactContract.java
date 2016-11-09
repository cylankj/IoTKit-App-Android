package com.cylan.jiafeigou.n.mvp.contract.mine;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.mvp.model.RelAndFriendBean;
import com.cylan.jiafeigou.n.mvp.model.SuggestionChatInfoBean;
import com.cylan.jiafeigou.n.view.adapter.RelativeAndFriendAddFromContactAdapter;

import java.util.ArrayList;

/**
 * 作者：zsl
 * 创建时间：2016/9/6
 * 描述：
 */
public interface MineFriendAddFromContactContract {

    interface View extends BaseView<Presenter> {

        void initContactRecycleView(ArrayList<RelAndFriendBean> list);

        void jump2SendAddMesgFragment(RelAndFriendBean bean);

        void showNoContactView();
    }

    interface Presenter extends BasePresenter {

        void initContactData();

        void addContactItem(RelAndFriendBean bean);

        void filterPhoneData(String filterStr);
    }

}
