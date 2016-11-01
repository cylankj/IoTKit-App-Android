package com.cylan.jiafeigou.n.mvp.contract.mine;

import com.cylan.entity.jniCall.JFGFriendRequest;
import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.mvp.model.UserInfoBean;

/**
 * 作者：zsl
 * 创建时间：2016/9/7
 * 描述：
 */
public interface MineFriendAddByNumContract {

    interface View extends BaseView<Presenter> {

        String getInputNum();

        void showFindResult(JFGFriendRequest bean);

        /**
         * 显示查询进度
         */
        void showFindLoading();

        /**
         * 隐藏查询进度
         */
        void hideFindLoading();

        /**
         * 查询无结果
         */
        void showFindNoResult();

        /**
         * 隐藏显示无结果
         */
        void hideFindNoResult();

        /**
         * 设置搜索结果
         * isFrom:是否从添加请求界面点击进入
         * hasSendToMe：是否已向我发送请求过
         */
        void setFindResult(boolean isFrom,boolean hasSendToMe,JFGFriendRequest bean);

    }

    interface Presenter extends BasePresenter {

        void findUserFromServer(String number);

        /**
         * 判断是否已向我发送添加请求
         */
        void checkIsSendAddReqToMe(JFGFriendRequest bean);
    }
}
