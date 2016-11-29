package com.cylan.jiafeigou.n.mvp.contract.bind;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

/**
 * Created by cylan-hunt on 16-7-7.
 */
public interface SetDeviceAliasContract {


    interface View extends BaseView<Presenter> {
        /**
         * 设置设备昵称,成功
         */
        void setupAliasDone();
    }

    interface Presenter extends BasePresenter {
        /**
         * 设置设备昵称
         *
         * @param alias
         */
        void setupAlias(String alias);
    }
}
