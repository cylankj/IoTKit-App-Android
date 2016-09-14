package com.cylan.jiafeigou.n.mvp.contract.home;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.mvp.model.MineHelpSuggestionBean;

import java.util.ArrayList;

/**
 * 创建者     谢坤
 * 创建时间   2016/8/22 10:56
 */
public interface HomeMineHelpSuggestionContract {


    interface View extends BaseView<Presenter> {
        void onTalkList(ArrayList<MineHelpSuggestionBean> beanOfArrayList);

        void onClearAllTalk();             //清空所有会话
    }

    interface Presenter extends BasePresenter {
        void addItemOfList();

        void onClearAllTalk();
    }
}
