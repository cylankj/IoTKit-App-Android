package com.cylan.jiafeigou.n.mvp.contract.mine;

import com.cylan.entity.jniCall.JFGShareListInfo;
import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

import java.util.ArrayList;

/**
 * 作者：zsl
 * 创建时间：2016/9/5
 * 描述：
 */
public interface MineShareDeviceContract {

    interface View extends BaseView<Presenter> {


        /**
         * desc:初始化分享的设备列表
         */
        void onInitShareList(ArrayList<JFGShareListInfo> list);

        /**
         * 显示加载进度
         */
        void showLoadingDialog();

        /**
         * 隐藏加载进度
         */
        void hideLoadingDialog();

    }

    interface Presenter extends BasePresenter {

        void initShareList();

    }

}
