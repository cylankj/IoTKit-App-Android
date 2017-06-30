package com.cylan.jiafeigou.n.mvp.contract.mine;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.view.adapter.item.ShareFriendItem;
import com.cylan.jiafeigou.rx.RxEvent;

import java.util.ArrayList;

/**
 * 作者：zsl
 * 创建时间：2016/9/13
 * 描述：
 */
public interface MineShareToFriendContract {

    interface View extends BaseView<Presenter> {
        /**
         * 初始化列表显示
         *
         * @param list
         */
        void onInitCanShareFriendList(ArrayList<ShareFriendItem> list);

        void showShareToFriendsResult(RxEvent.MultiShareDeviceEvent result);

        void showLoading(int resId, Object... args);

        void hideLoading();
    }

    interface Presenter extends BasePresenter {

        /**
         * 点击确定发送分享请求给服务器
         */
        void shareDeviceToFriend(String cid, ArrayList<ShareFriendItem> friendItems);

        void getCanShareFriendsList(String uuid);
    }

}
