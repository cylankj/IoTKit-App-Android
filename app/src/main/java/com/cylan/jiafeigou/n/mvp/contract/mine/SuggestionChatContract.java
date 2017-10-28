package com.cylan.jiafeigou.n.mvp.contract.mine;

import com.cylan.jiafeigou.cache.db.module.SysMsgBean;
import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

import java.util.ArrayList;

/**
 * 作者：zsl
 * 创建时间：2016/8/30
 * 描述：
 */
public interface SuggestionChatContract {

    interface View extends BaseView {
//        void showChatList();
//
//        String getEditContent();
//
//        String getTime();
//
//        void clearEdit();
//
//        void keyboardListener();
//
//        boolean editLessShowDialog();
//
//        void notifyChatList();
//
//        void showDialog();
    }

    interface Presenter extends BasePresenter {
        ArrayList<SysMsgBean> initChatData();

        //模拟服务器数据
        SysMsgBean testServerData(long times);

        void addChatItem(SysMsgBean emMessage);

        SysMsgBean makeEMMessageBean(String content, int type, String time);

        void showToast();

        void clearChatList(ArrayList<SysMsgBean> list);
    }

}
