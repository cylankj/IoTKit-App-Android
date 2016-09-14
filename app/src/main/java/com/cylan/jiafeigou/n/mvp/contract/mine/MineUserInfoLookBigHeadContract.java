package com.cylan.jiafeigou.n.mvp.contract.mine;

import android.widget.ImageView;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

/**
 * 作者：zsl
 * 创建时间：2016/9/2
 * 描述：查看大头像
 */
public interface MineUserInfoLookBigHeadContract {

    interface View extends BaseView<Presenter> {
        void showLoadImageProgress();

        void hideLoadImageProgress();

        void loadImageSuccess();

        void loadImageFail();
    }

    interface Presenter extends BasePresenter {
        void loadImage(ImageView view);
    }

}
