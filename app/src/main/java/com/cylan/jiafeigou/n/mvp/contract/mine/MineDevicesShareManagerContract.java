package com.cylan.jiafeigou.n.mvp.contract.mine;

import com.cylan.jiafeigou.cache.db.module.FriendBean;
import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.rx.RxEvent;

import java.util.ArrayList;

import rx.Subscription;

/**
 * 作者：zsl
 * 创建时间：2016/9/8
 * 描述：
 */
public interface MineDevicesShareManagerContract {

    interface View extends BaseView<Presenter> {
        /**
         * desc:有分享给好友时显示已分享标题头
         */
        void showHasShareListTitle();

        /**
         * desc：无已分享给好友时不显示已分享标题头
         */
        void hideHasShareListTitle();

        /**
         * desc：初始化显示已分享的列表
         */
        void initHasShareFriendRecyView(ArrayList<FriendBean> list);

        /**
         * desc：显示空视图
         */
        void showNoHasShareFriendNullView();

        /**
         * desc：弹出取消分享的对话框
         */
        void showCancleShareDialog(FriendBean bean);

        /**
         * desc：显示取消分享的进度
         */
        void showCancleShareProgress();

        /**
         * desc：隐藏取消分享的进度
         */
        void hideCancleShareProgress();

        /**
         * desc：删除列表的一个条目
         */
        void deleteItems();

        /**
         * 取消分享的结果
         */
        void showUnShareResult(RxEvent.UnShareDeviceCallBack unshareDeviceCallBack);

        /**
         * 显示顶部标题头
         *
         * @param name
         */
        void setTopTitle(String name);

        /**
         * 网络状态变化
         */
        void onNetStateChanged(int state);
    }

    interface Presenter extends BasePresenter {

        /**
         * 获取已分享的好友
         *
         * @param cid
         */
        void getHasShareList(String cid);

        /**
         * 获取到到已分享好友的回调
         *
         * @return
         */
        Subscription getHasShareListCallback();

        /**
         * desc:初始化显示已分享给的好友的数据
         */
        void initHasShareListData(ArrayList<FriendBean> shareDeviceFriendlist);

        /**
         * desc：取消分享
         *
         * @param bean
         */
        void cancelShare(String cid, FriendBean bean);

        /**
         * 取消分享的回调
         */
        Subscription cancelShareCallBack();

    }

}
