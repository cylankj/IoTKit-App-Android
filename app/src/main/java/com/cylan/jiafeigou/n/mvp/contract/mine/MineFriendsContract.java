package com.cylan.jiafeigou.n.mvp.contract.mine;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.cylan.entity.jniCall.JFGFriendAccount;
import com.cylan.entity.jniCall.JFGFriendRequest;
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
public interface MineFriendsContract {

    interface View extends BaseView<Presenter> {
        /**
         * desc:初始化好友列表
         */
        void initFriendRecyList(ArrayList<JFGFriendAccount> list);


        void initAddReqRecyList(ArrayList<JFGFriendRequest> list);

        /**
         * desc：显示好友列表标题
         */
        void showFriendListTitle();

        /**
         * desc：没有好友时不显示列表标题
         */
        void hideFriendListTitle();

        /**
         * desc：有添加请求时显示添加请求标题
         */
        void showAddReqListTitle();

        /**
         * desc：无添加请求时不显示添加请求标题
         */
        void hideAddReqListTitle();

        void jump2FriendDetailFragment(int position,JFGFriendAccount account);

        void showLongClickDialog(int position,JFGFriendRequest bean);

        void jump2AddReqDetailFragment(int position, JFGFriendRequest bean);

        void showReqOutTimeDialog();

        /**
         * desc：显示空界面
         */
        void showNullView();

        /**
         * desc：删除添加请求条目
         * @param position
         * @param bean
         */
        void addReqDeleteItem(int position,JFGFriendRequest bean);

        /**
         * desc：好友列表添加条目
         * @param position
         * @param bean
         */
        void friendlistAddItem(int position,JFGFriendAccount bean);

    }

    interface Presenter extends BasePresenter {

        ArrayList<JFGFriendRequest> initAddRequestData();

        ArrayList<JFGFriendAccount> initRelativatesAndFriendsData();

        boolean checkAddRequestOutTime(JFGFriendRequest bean);        //检测添加请求是否超时

        /**
         * desc：初始化处理好友列表
         */
        void initFriendRecyListData();

        /**
         * desc：初始化处理添加请求列表
         */
        void initAddReqRecyListData();

        /**
         * desc：检查是否为空界面
         */
        void checkAllNull();
    }

}
