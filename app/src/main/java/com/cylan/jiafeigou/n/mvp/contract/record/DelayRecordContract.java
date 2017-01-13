package com.cylan.jiafeigou.n.mvp.contract.record;

import com.cylan.jiafeigou.base.view.JFGPresenter;
import com.cylan.jiafeigou.base.view.JFGView;

import java.util.List;

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

        String VIEW_HANDLER_TO_MAIN_VIEW = "VIEW_HANDLER_TO_MAIN_VIEW";
        String VIEW_HANDLER_ENABLE_DEVICE = "VIEW_HANDLER_ENABLE_DEVICE";

        void onShowRecordMainView(String uuid);//显示延时摄影主页面

        void onShowRecordGuideView(String uuid);//显示第一次的用户引导页

        void onShowRecordDeviceView(List<String> devices);//显示可用设备选择列表界面

        void onShowDeviceSettingView(String uuid);//进入设备设置页,用于关闭待机模式

    }

    interface Presenter extends JFGPresenter {

    }

}
