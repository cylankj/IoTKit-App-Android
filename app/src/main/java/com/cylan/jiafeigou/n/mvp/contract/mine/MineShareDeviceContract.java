package com.cylan.jiafeigou.n.mvp.contract.mine;

import com.cylan.entity.jniCall.JFGShareListInfo;
import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.mvp.model.DeviceBean;
import com.cylan.jiafeigou.n.mvp.model.RelAndFriendBean;

import java.util.ArrayList;

import rx.Subscription;

/**
 * 作者：zsl
 * 创建时间：2016/9/5
 * 描述：
 */
public interface MineShareDeviceContract {

    interface View extends BaseView<Presenter> {

        void showShareDialog(int layoutPosition, DeviceBean item);

        /**
         * desc:初始化分享的设备列表
         */
        void initRecycleView(ArrayList<DeviceBean> list);

        /**
         * desc：跳转条设备分享管理界面
         */
        void jump2ShareDeviceMangerFragment(DeviceBean bean, int position);

        /**
         * desc：无分享设备显示null视图
         */
        void showNoDeviceView();

        /**
         * 显示加载进度
         */
        void showLoadingDialog();

        /**
         * 隐藏加载进度
         */
        void hideLoadingDialog();

    }

    interface Presenter extends BasePresenter {

        /**
         * 获取分享设备列表的数据
         *
         * @return
         */
        Subscription initData();

        /**
         * 获取设备中已经分享的好友
         *
         * @return
         */
        ArrayList<RelAndFriendBean> getJFGInfo(int position);

        /**
         * 获取到已分享的亲友的数据
         *
         * @return
         */
        ArrayList<RelAndFriendBean> getHasShareRelAndFriendList(JFGShareListInfo info);

        /**
         * 获取到设备已分享的亲友数
         *
         * @param cid
         */
        void getDeviceInfo(ArrayList<String> cid);

        /**
         * 获取设备已分享的亲友数的回调
         *
         * @return
         */
        Subscription getDeviceInfoCallBack();

        /**
         * 检测是否拥有联系人的权限
         */
        boolean checkPermission();

        /**
         * 取消分享成功后列表中删除该亲友
         *
         * @param arrayList
         */
        void unShareSucceedDel(int position, ArrayList<String> arrayList);

        /**
         * 分享成功后列表中添加一条数据
         */
        void shareSucceedAdd(int key,ArrayList<RelAndFriendBean> list);

        /**
         * fragment销毁时清除临时数据
         */
        void clearData();
    }

}
