package com.cylan.jiafeigou.n.mvp.contract.mine;

import com.cylan.jiafeigou.n.mvp.BaseFragmentView;
import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.view.adapter.item.FriendContextItem;

import java.util.List;

/**
 * 作者：zsl
 * 创建时间：2016/9/6
 * 描述：
 */
public interface MineFriendsContract {

    interface View extends BaseFragmentView<Presenter> {

        void showLoading(int resId, String... args);

        void hideLoading();

        void onRequestExpired(FriendContextItem item);

        /**
         * desc：长按删除添加请求条目
         */
        void deleteItemRsp(FriendContextItem item, int code, boolean alert);

        void acceptItemRsp(FriendContextItem item, int code);

        /**
         * 网络状态变化
         */
        void onNetStateChanged(int state);

        void onInitRequestAndFriendList(List<FriendContextItem> request, List<FriendContextItem> friends);
    }

    interface Presenter extends BasePresenter {

        void initRequestAndFriendList();

        void deleteFriendRequest(FriendContextItem item, boolean alert);

        void acceptFriendRequest(FriendContextItem item);
    }

}
