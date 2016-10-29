package com.cylan.jiafeigou.n.mvp.contract.mine;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.mvp.model.RelAndFriendBean;

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
         * @param list
         */
        void initRecycleView(ArrayList<RelAndFriendBean> list);

        /**
         * 显示没有亲友的null视图
         */
        void showNoFriendNullView();

        /**
         * 设置以分享的亲友的人数
         * @param number
         */
        void setHasShareFriendNum(boolean isChange,int number);

        /**
         * 全部分享成功结果显示
         */
        void showShareAllSuccess();

        /**
         * 显示部分分享失败
         */
        void showShareSomeFail(int some);

        /**
         * 显示全部分享失败
         */
        void showShareAllFail();

    }

    interface Presenter extends BasePresenter {
        /**
         * 获取亲友列表的数据
         */
        void initFriendListData();

        /**
         * 处理已经分享的亲友人数
         */
        void handlerHasShareFriendNumber();

        /**
         * 获取到已分享的亲友人数
         * @return
         */
        int getHasShareFriendNumber();

        /**
         * 点击确定发送分享请求给服务器
         */
        void sendShareToFriendReq(ArrayList<RelAndFriendBean> list);
    }

}
