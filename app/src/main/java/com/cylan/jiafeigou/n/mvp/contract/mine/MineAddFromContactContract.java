package com.cylan.jiafeigou.n.mvp.contract.mine;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

/**
 * 作者：zsl
 * 创建时间：2016/9/7
 * 描述：
 */
public interface MineAddFromContactContract {

    interface View extends BaseView<Presenter> {
        void initEditText();

        String getSendMesg();

        void showResultDialog();
    }

    interface Presenter extends BasePresenter {
        void sendRequest(String mesg);
    }

}
