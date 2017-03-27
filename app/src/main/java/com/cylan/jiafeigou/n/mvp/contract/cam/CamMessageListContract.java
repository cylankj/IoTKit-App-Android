package com.cylan.jiafeigou.n.mvp.contract.cam;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.jiafeigou.base.module.DPConstant;
import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.mvp.model.CamMessageBean;
import com.cylan.jiafeigou.widget.wheel.WonderIndicatorWheelView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cylan-hunt on 16-6-29.
 */
public interface CamMessageListContract {


    interface View extends BaseView<Presenter> {

        void onDateMapRsp(List<WonderIndicatorWheelView.WheelItem> dateMap);

        void onListAppend(ArrayList<CamMessageBean> beanArrayList);

        void onListInsert(ArrayList<CamMessageBean> beanArrayList, int position);

        ArrayList<CamMessageBean> getList();

        /**
         * 设备信息{在线,sd卡,电量.....所有信息}
         *
         * @param id:消息id
         * @param o:      {@link DPConstant}
         */
        void deviceInfoChanged(int id, JFGDPMsg o) throws IOException;

        void onErr();

        void onMessageDeleteSuc();

        void loadingDismiss();
    }

    interface Presenter extends BasePresenter {
        /**
         * @param timeStart
         */
        void fetchMessageList(long timeStart, boolean asc);


        void removeItems(ArrayList<CamMessageBean> beanList);

        List<WonderIndicatorWheelView.WheelItem> getDateList();

        void refreshDateList();
    }
}

