package com.cylan.jiafeigou.n.mvp.contract.mine;

import android.graphics.Bitmap;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

import rx.Subscription;

/**
 * 作者：zsl
 * 创建时间：2016/9/6
 * 描述：
 */
public interface MineFriendScanAddContract {

    interface View extends BaseView<Presenter> {

        void onStartScan();

        void showQrCode(Bitmap bitmap);
    }

    interface Presenter extends BasePresenter {

        Bitmap encodeAsBitmap(String contents, int dimension);      //生成二维码

        int getDimension();

        /**
         * 检测扫描结果
         * @param account
         */
        void checkScannAccount(String account);

        /**
         * 扫描结果的回调
         * @return
         */
        Subscription checkAccountCallBack();

    }

}
