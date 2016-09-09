package com.cylan.jiafeigou.n.mvp.contract.mine;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.mvp.model.SuggestionChatInfoBean;

import java.util.ArrayList;

/**
 * 作者：zsl
 * 创建时间：2016/9/6
 * 描述：
 */
public interface MineRelativeAndFriendAddFromContactContract {

    interface View extends BaseView<Presenter>{
        void setRcyAdapter(ArrayList<SuggestionChatInfoBean> list);
        void InitItemClickListener();
    }

    interface Presenter extends BasePresenter{
        void initContactData();
        void addContactItem(SuggestionChatInfoBean bean);
        void filterPhoneData(String filterStr);
    }

}
