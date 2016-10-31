package com.cylan.jiafeigou.n.mvp.contract.mine;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

/**
 * 作者：zsl
 * 创建时间：2016/9/23
 * 描述：
 */
public interface MineSetRemarkNameContract {

    interface View extends BaseView<Presenter> {
        String getEditName();
    }

    interface Presenter extends BasePresenter {
        boolean isEditEmpty(String string);
    }
}
