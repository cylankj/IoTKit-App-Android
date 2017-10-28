package com.cylan.jiafeigou.n.mvp.contract.mine;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.view.adapter.item.FriendContextItem;
import com.cylan.jiafeigou.n.view.adapter.item.ShareContactItem;
import com.cylan.jiafeigou.rx.RxEvent;

import java.util.List;

/**
 * 作者：zsl
 * 创建时间：2016/9/13
 * 描述：
 */
public interface MineShareToContactContract {

    interface View extends BaseView {

        void onInitContactFriends(List<ShareContactItem> friendItems);

        void showLoading(int resId, String... args);

        void hideLoading();

        void onCheckFriendAccountResult(FriendContextItem friendContextItem, ShareContactItem item, boolean accountExist);

        void onShareDeviceResult(ShareContactItem shareContactItem, RxEvent.ShareDeviceCallBack result);
    }

    interface Presenter extends BasePresenter {

        void checkAndInitContactList(int contactType);

        void shareDeviceToContact(ShareContactItem shareContactItem);

        void checkFriendAccount(ShareContactItem item);
    }

}
