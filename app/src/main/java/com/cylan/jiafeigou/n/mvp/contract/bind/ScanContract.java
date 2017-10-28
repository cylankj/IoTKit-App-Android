package com.cylan.jiafeigou.n.mvp.contract.bind;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

/**
 * Created by cylan-hunt on 16-7-7.
 */
public interface ScanContract {


    interface View extends BaseView {

        /**
         * 成功找到匹配的AP
         */
        void onScanRsp(int state);

        /**
         * 开始扫描
         */
        void onStartScan();

        @Override
        String uuid();
    }

    interface Presenter extends BasePresenter {

        /**
         * 扫描附近设备
         */
//        void startScan();
//        void submit(Bundle bundle);
    }
}
