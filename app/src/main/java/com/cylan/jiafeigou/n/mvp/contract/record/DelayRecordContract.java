package com.cylan.jiafeigou.n.mvp.contract.record;

import com.cylan.jiafeigou.base.view.JFGPresenter;
import com.cylan.jiafeigou.base.view.JFGView;

/*
 *  @项目名：  JFGAndroid 
 *  @包名：    com.cylan.jiafeigou.n.mvp.contract.record
 *  @文件名:   DelayRecordContract
 *  @创建者:   yanzhendong
 *  @创建时间:  2017/1/4 22:00
 *  @描述：    TODO
 */
public interface DelayRecordContract {

    interface View extends JFGView {
        String VIEW_LAUNCH_WAY_SETTING = "VIEW_LAUNCH_WAY_SETTING";
        String VIEW_LAUNCH_WAY_WONDERFUL = "VIEW_LAUNCH_WAY_WONDERFUL";

        String VIEW_HANDLER_GUIDE_START_NOW = "VIEW_HANDLER_GUIDE_START_NOW";
        String VIEW_HANDLER_GUIDE_ENABLE_DEVICE = "VIEW_HANDLER_GUIDE_ENABLE_DEVICE";

        void onShowRecordMainView();//显示延时摄影主页面

        void onShowRecordGuideView();//显示第一次的用户引导页

        void onShowRecordDeviceView();//显示可用设备选择列表界面

        void onShowDeviceSettingView();//进入设备设置页,用于关闭待机模式


    }

    interface Presenter extends JFGPresenter {

    }

}
