package com.cylan.jiafeigou.n.mvp.contract.mine;

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
public interface MineFriendAddFromContactContract {

    interface View extends BaseView<Presenter> {

        void initContactRecycleView(ArrayList<RelAndFriendBean> list);

        void jump2SendAddMesgFragment();

        void showNoContactView();

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
        void sendSms();

    }

    interface Presenter extends BasePresenter {

        void filterPhoneData(String filterStr);

        /**
         * 获取好友列表的数据
         * @return
         */
        void getFriendListData();

        /**
         * 获取好友的列表的回调
         * @return
         */
        Subscription getFriendListDataCallBack();

        /**
         * 检测账号是否已经注册
         */
        void checkFriendAccount(String account);

        /**
         * 检测账号的回调
         * @return
         */
        Subscription checkFriendAccountCallBack();
    }

}
