package com.cylan.jiafeigou.n.mvp.contract.mine;

import com.cylan.jiafeigou.n.mvp.BaseFragmentView;
import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.mvp.model.RelAndFriendBean;

import java.util.ArrayList;

import rx.Subscription;

/**
 * 作者：zsl
 * 创建时间：2016/9/6
 * 描述：
 */
public interface AddFriendContract {

    interface View extends BaseFragmentView<Presenter> {
        /**
         * 初始化联系人列表
         *
         * @param list
         */
        void initContactRecycleView(ArrayList<RelAndFriendBean> list);

        /**
         * 跳转到发送添加请求界面
         */
        void jump2SendAddMesgFragment();

        /**
         * 显示联系人为空视图
         */
        void showNoContactView();

        /**
         * 隐藏联系人为空视图
         */
        void hideNoContactView();

        /**
         * 显示进度浮层
         */
        void showLoadingPro();

        /**
         * 隐藏进度浮层
         */
        void hideLoadingPro();

        /**
         * 发送短信邀请
         */
        void openSendSms();

        /**
         * 网络状态变化
         */
        void onNetStateChanged(int state);


    }

    interface Presenter extends BasePresenter {
        /**
         * 过滤数据
         *
         * @param filterStr
         */
        void filterPhoneData(String filterStr);

        /**
         * 获取好友列表的数据
         *
         * @return
         */
        void getFriendListData();

        /**
         * 获取好友的列表的回调
         *
         * @return
         */
        Subscription getFriendListDataCallBack();

        /**
         * 检测账号是否已经注册
         */
        void checkFriendAccount(String account);

        /**
         * 检测账号的回调
         *
         * @return
         */
        Subscription checkFriendAccountCallBack();

        /**
         * 检测短信权限
         *
         * @return
         */
        boolean checkSmsPermission();


    }

}
