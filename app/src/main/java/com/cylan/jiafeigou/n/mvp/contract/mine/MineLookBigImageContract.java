package com.cylan.jiafeigou.n.mvp.contract.mine;

import android.graphics.Bitmap;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

/**
 * 作者：zsl
 * 创建时间：2016/9/21
 * 描述：
 */
public interface MineLookBigImageContract {

    interface View extends BaseView<Presenter> {
    }

    interface Presenter extends BasePresenter {
        /**
         * 长按保存图片
         */
        void saveImage(Bitmap bmp);
    }
}
