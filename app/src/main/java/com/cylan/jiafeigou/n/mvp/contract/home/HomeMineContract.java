package com.cylan.jiafeigou.n.mvp.contract.home;

import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.UiThread;
import android.widget.ImageView;

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
        @UiThread
        void onPortraitUpdate(String url);

        void onBlur(Drawable drawable);

        void setUserImageHead(Drawable drawable);
    }

    interface Presenter extends BasePresenter {

        void requestLatestPortrait();

        void portraitBlur(@DrawableRes int id);

        void portraitUpdateByUrl(String url);

        boolean checkIsLogin(String userID);

        int whichLoginMethd();

    }
}
