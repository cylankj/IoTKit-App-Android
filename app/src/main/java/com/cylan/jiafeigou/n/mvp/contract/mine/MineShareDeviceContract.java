package com.cylan.jiafeigou.n.mvp.contract.mine;

import android.view.View;

import com.cylan.entity.jniCall.JFGShareListInfo;
import com.cylan.jiafeigou.misc.RxEvent;
import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.mvp.model.DeviceBean;
import com.cylan.jiafeigou.n.mvp.model.RelAndFriendBean;
import com.cylan.jiafeigou.n.view.adapter.MineShareDeviceAdapter;

import java.util.ArrayList;
import java.util.List;

import rx.Subscription;

/**
 * 作者：zsl
 * 创建时间：2016/9/5
 * 描述：
 */
public interface MineShareDeviceContract {

    interface View extends BaseView<Presenter> {

        void showShareDialog();

        /**
         * desc:初始化分享的设备列表
         */
        void initRecycleView(ArrayList<DeviceBean> list);

        /**
         * desc：跳转条设备分享管理界面
         * @param itemView
         * @param position
         */
        void jump2ShareDeviceMangerFragment(android.view.View itemView, int position,JFGShareListInfo info);

        /**
         * desc：无分享设备显示null视图
         */
        void showNoDeviceView();

    }

    interface Presenter extends BasePresenter {

        /**
         * 获取分享设备列表的数据
         * @return
         */
        Subscription initData();
        /**
         * 获取设备中已经分享的好友
         * @return
         */
        JFGShareListInfo getJFGInfo(int position);

        /**
         * 获取到已分享的亲友的数据
         * @return
         */
        ArrayList<RelAndFriendBean> getHasShareRelAndFriendList(JFGShareListInfo info);
    }

}
