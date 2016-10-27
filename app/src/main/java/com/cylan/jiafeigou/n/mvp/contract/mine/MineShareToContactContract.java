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

        void initContactReclyView(ShareToContactAdapter adapter);

        void showNoContactNullView();

        void hideTopTitle();

        void showSearchInputEdit();

        void hideSearchInputEdit();

        String getSearchInputContent();

        void showShareDeviceDialog();

    }

    interface Presenter extends BasePresenter {

        void initContactData();

        void handleSearchResult(String input);

    }

}
