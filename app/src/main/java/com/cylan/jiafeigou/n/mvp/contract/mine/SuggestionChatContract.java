package com.cylan.jiafeigou.n.mvp.contract.mine;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.mvp.model.SuggestionChatInfoBean;

import java.util.ArrayList;

/**
 * 作者：zsl
 * 创建时间：2016/8/30
 * 描述：
 */
public interface SuggestionChatContract {

    interface View extends BaseView<Presenter> {
        void showChatList();

        String getEditContent();

        String getTime();

        void clearEdit();

        void keyboardListener();

        boolean editLessShowDialog();

        void notifyChatList();

        void showDialog();
    }

    interface Presenter extends BasePresenter {
        ArrayList<SuggestionChatInfoBean> initChatData();

        //模拟服务器数据
        SuggestionChatInfoBean testServerData(long times);

        void addChatItem(SuggestionChatInfoBean emMessage);

        SuggestionChatInfoBean makeEMMessageBean(String content, int type, String time);

        void showToast();

        void clearChatList(ArrayList<SuggestionChatInfoBean> list);
    }

}
