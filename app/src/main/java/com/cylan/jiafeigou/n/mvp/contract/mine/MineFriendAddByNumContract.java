package com.cylan.jiafeigou.n.mvp.contract.mine;

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

        void showFindResult(UserInfoBean bean);

        void showFindLoad();
    }

    interface Presenter extends BasePresenter {
        void findUserFromServer(String number);
    }
}
