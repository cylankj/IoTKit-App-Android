package com.cylan.jiafeigou.n.mvp.contract.home;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

/**
 * Created by hunt on 16-5-23.
 */
public interface HomeMineContract {

    interface View extends BaseView<Presenter> {
        /**
         * @param url: 返回url,可以使用`glide`或者`uil`直接加载
         */
        void onPortraitUpdate(String url);
    }

    interface Presenter extends BasePresenter {
        void requestLatestPortrait();
    }
}
