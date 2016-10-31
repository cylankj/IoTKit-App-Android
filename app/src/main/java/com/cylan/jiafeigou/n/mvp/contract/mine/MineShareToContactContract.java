package com.cylan.jiafeigou.n.mvp.contract.mine;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.mvp.model.SuggestionChatInfoBean;
import com.cylan.jiafeigou.n.view.adapter.ShareToContactAdapter;

import java.util.ArrayList;

/**
 * 作者：zsl
 * 创建时间：2016/9/13
 * 描述：
 */
public interface MineShareToContactContract {

    interface View extends BaseView<Presenter> {

        void initContactReclyView(ArrayList<SuggestionChatInfoBean> list);

        void showNoContactNullView();

        void hideTopTitle();

        void showSearchInputEdit();

        void hideSearchInputEdit();

        void showShareDeviceDialog(SuggestionChatInfoBean bean);

        /**
         * 显示正在分享的进度提示
         */
        void showShareingProHint();

        /**
         * 隐藏正在分享进度的提示
         */
        void hideShareingProHint();

        /**
         * 修改正在分享的进度提示
         */
        void changeShareingProHint(String finish);

        /**
         * 分享不同状态提示
         */
        void showPersonOverDialog(String content);

        /**
         * 调用系统发送短信的界面
         * @param info
         */
        void startSendMesgActivity(SuggestionChatInfoBean info);

    }

    interface Presenter extends BasePresenter {

        void initContactData();

        void handleSearchResult(String input);

        void shareToContact(SuggestionChatInfoBean bean);
        /**
         * 处理点击按钮
         */
        void handlerShareClick(SuggestionChatInfoBean bean);

    }

}
