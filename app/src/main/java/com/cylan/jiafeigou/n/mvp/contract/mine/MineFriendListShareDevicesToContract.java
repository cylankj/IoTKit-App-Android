package com.cylan.jiafeigou.n.mvp.contract.mine;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.mvp.model.DeviceBean;
import com.cylan.jiafeigou.n.mvp.model.RelAndFriendBean;
import com.cylan.jiafeigou.rx.RxEvent;

import java.util.ArrayList;

import rx.Subscription;

/**
 * 作者：zsl
 * 创建时间：2016/9/6
 * 描述：
 */
public interface MineFriendListShareDevicesToContract {

    interface View extends BaseView<Presenter> {
        /**
         * 初始化头部标题显示
         */
        void initTitleView(RelAndFriendBean bean);

        /**
         * 初始化列表的显示
         *
         * @param list
         */
        void initRecycleView(ArrayList<DeviceBean> list);

        /**
         * 没有可分享的设备时
         */
        void showNoDeviceView();

        /**
         * 有可分享的设备时
         */
        void hideNoDeviceView();

        /**
         * 显示完成按钮
         */
        void showFinishBtn();

        /**
         * 隐藏完成按钮
         */
        void hideFinishBtn();

        /**
         * 显示发送分享请求的进度提示
         */
        void showSendReqProgress();

        /**
         * 隐藏发送分享请求的进度提示
         */
        void hideSendReqProgress();

        /**
         * 设置分享请求发送结果
         */
        void showSendReqFinishReuslt(ArrayList<RxEvent.ShareDeviceCallBack> list);

        /**
         * 显示加载进度
         */
        void showLoadingDialog();

        /**
         * 隐藏加载进度
         */
        void hideLoadingDialog();

        /**
         * 网络状态变化
         */
        void onNetStateChanged(int state);

    }

    interface Presenter extends BasePresenter {

        /**
         * 获取设备列表是数据
         */
        Subscription initDeviceListData();

        /**
         * 发送分享设备给的亲友的请求
         */
        void sendShareToReq(ArrayList<DeviceBean> chooseList, RelAndFriendBean bean);

        /**
         * 检测是否有选中的
         */
        void checkIsChoose(ArrayList<DeviceBean> list);

        /**
         * 分享设备的回调
         *
         * @return
         */
        Subscription shareDeviceCallBack();

        /**
         * 获取到设备已经分享的亲友数
         *
         * @param cid
         */
        void getDeviceInfo(ArrayList<String> cid);

        /**
         * 获取到设备信息的回调
         *
         * @return
         */
        Subscription getDeviceInfoCallBack();

        /**
         * 注册网络监听
         */
        void registerNetworkMonitor();

        /**
         * 移除网络监听
         */
        void unregisterNetworkMonitor();

    }

}
