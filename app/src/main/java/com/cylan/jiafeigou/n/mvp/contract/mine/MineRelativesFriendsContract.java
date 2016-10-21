package com.cylan.jiafeigou.n.mvp.contract.mine;

import android.support.v7.widget.RecyclerView;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.mvp.model.SuggestionChatInfoBean;
import com.cylan.jiafeigou.n.view.adapter.AddRelativesAndFriendsAdapter;
import com.cylan.jiafeigou.n.view.adapter.RelativesAndFriendsAdapter;

import java.util.ArrayList;

/**
 * 作者：zsl
 * 创建时间：2016/9/6
 * 描述：
 */
public interface MineRelativesFriendsContract {

    interface View extends BaseView<Presenter> {
        void showAddRequestList();

        void showRelativesAndFriendsList();
    }

    interface Presenter extends BasePresenter {
        ArrayList<SuggestionChatInfoBean> initAddRequestData();

        ArrayList<SuggestionChatInfoBean> initRelativatesAndFriendsData();

        void addItems(SuggestionChatInfoBean bean, ArrayList<SuggestionChatInfoBean> list, RecyclerView.Adapter adapter);

        boolean checkAddRequestOutTime(SuggestionChatInfoBean bean);        //检测添加请求是否超时

        void doAddRequestClick(int position, ArrayList<SuggestionChatInfoBean> requestAddList, AddRelativesAndFriendsAdapter addAdapter, ArrayList<SuggestionChatInfoBean> friendList, RelativesAndFriendsAdapter frienAdapter);
    }

}
