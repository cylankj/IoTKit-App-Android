package com.cylan.jiafeigou.n.mvp.contract.mine;

import com.cylan.entity.jniCall.JFGFriendAccount;
import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.rx.RxEvent;

import java.util.ArrayList;

/**
 * 作者：zsl
 * 创建时间：2016/9/8
 * 描述：
 */
public interface MineDevicesShareManagerContract {

    interface View extends BaseView<Presenter> {


        /**
         * desc：弹出取消分享的对话框
         */
        void showCancelShareDialog(JFGFriendAccount bean);

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

        void onInitShareDeviceList(ArrayList<JFGFriendAccount> friends);
    }

    interface Presenter extends BasePresenter {

        void initShareDeviceList(String uuid);

        /**
         * desc：取消分享
         *
         * @param bean
         */
        void cancelShare(String cid, JFGFriendAccount bean);
    }

}
