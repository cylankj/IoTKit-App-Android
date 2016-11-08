package com.cylan.jiafeigou.n.mvp.contract.mine;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.mvp.model.RelAndFriendBean;
import com.cylan.jiafeigou.n.view.adapter.MineHasShareAdapter;

import java.util.ArrayList;

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
        void inintHasShareFriendRecyView(ArrayList<RelAndFriendBean> list);

        /**
         * desc：显示空视图
         */
        void showNoHasShareFriendNullView();

        /**
         * desc：弹出取消分享的对话框
         */
        void showCancleShareDialog(RelAndFriendBean bean);

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
         * @param bean
         */
        void deleteItems(RelAndFriendBean bean);
    }

    interface Presenter extends BasePresenter {
        /**
         * desc:初始化显示已分享给的好友的数据
         */
        void initHasShareListData(ArrayList<RelAndFriendBean> shareDeviceFriendlist);

        /**
         * desc：取消分享
         * @param bean
         */
        void cancleShare(RelAndFriendBean bean);

    }

}
