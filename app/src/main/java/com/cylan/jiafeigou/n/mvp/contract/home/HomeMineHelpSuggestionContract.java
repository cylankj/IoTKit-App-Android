package com.cylan.jiafeigou.n.mvp.contract.home;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.mvp.model.MineHelpSuggestionBean;

import java.util.ArrayList;

import rx.Subscription;

/**
 * 创建者     谢坤
 * 创建时间   2016/8/22 10:56
 */
public interface HomeMineHelpSuggestionContract {

    interface View extends BaseView<Presenter> {
        /**
         * 初始化显示列表
         * @param list
         */
        void initRecycleView(ArrayList<MineHelpSuggestionBean> list);

        /**
         * 添加服务器回复
         */
        void addServerItem();

        /**
         * 添加自动回复条目
         */
        void addAutoReply();
    }

    interface Presenter extends BasePresenter {

        /**
         * 获取列表的数据
         */
        void initData();
        /**
         * 清空记录
         */
        void onClearAllTalk();
        /**
         * 获取到用户的信息拿到数据库对象
         */
        Subscription getAccountInfo();
    }
}
