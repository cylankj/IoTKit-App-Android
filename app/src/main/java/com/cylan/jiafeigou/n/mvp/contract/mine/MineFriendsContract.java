package com.cylan.jiafeigou.n.mvp.contract.mine;

import com.cylan.jiafeigou.n.mvp.BaseFragmentView;
import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.view.adapter.item.FriendGroupChildItem;
import com.cylan.jiafeigou.n.view.adapter.item.FriendGroupParentItem;

import java.util.List;

/**
 * 作者：zsl
 * 创建时间：2016/9/6
 * 描述：
 */
public interface MineFriendsContract {

    interface View extends BaseFragmentView<Presenter> {

        void onRequestExpired(FriendGroupChildItem item);

        /**
         * desc：长按删除添加请求条目
         */
        void deleteItemRsp(FriendGroupChildItem item, int code);

        void acceptItemRsp(FriendGroupChildItem item, int code);

        /**
         * 网络状态变化
         */
        void onNetStateChanged(int state);

        void onInitRequestAndFriendList(List<FriendGroupParentItem> result);
    }

    interface Presenter extends BasePresenter {

        /**
         * 发送添加请求
         */
        void sendAddReq(String account);

        /**
         * 同意添加成功后调用SDK
         */
        void acceptAddSDK(String account);

        /**
         * 删除好友请求
         */
        void deleteAddReq(String account);

        void removeCache(String account);

        void initRequestAndFriendList();

        void deleteFriendRequest(FriendGroupChildItem item);

        void acceptFriendRequest(FriendGroupChildItem item);
    }

}
