package com.cylan.jiafeigou.n.mvp.contract.mine;

import android.graphics.Bitmap;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.mvp.model.MineAddReqBean;

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

        /**
         * 跳转到添加人详情页
         */
        void jump2FriendDetailFragment(boolean isFrom, MineAddReqBean bean);

        /**
         * 已经是好友
         */
        void isMineFriendResult();

        /**
         * 无效的二维码
         */
        void scanNoResult();

        /**
         * 显示加载进度
         */
        void showLoadingPro();

        /**
         * 隐藏加载进度
         */
        void hideLoadingPro();
    }

    interface Presenter extends BasePresenter {

        Bitmap encodeAsBitmap(String contents, int dimension);      //生成二维码

        int getDimension();

        /**
         * 检测扫描结果
         *
         * @param account
         */
        void checkScanAccount(String account);

        /**
         * 扫描结果的回调
         *
         * @return
         */
        Subscription checkAccountCallBack();

        /**
         * 获取用户的信息
         */
        Subscription getUserInfo();

        /**
         * 开始扫描
         *
         * @return
         */
        Subscription beginScan();

    }

}
