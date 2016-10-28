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
public interface MineShareToRelativeAndFriendContract {

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

    }

    interface Presenter extends BasePresenter {
        /**
         * 获取亲友列表的数据
         */
        void initFriendListData();
    }

}
