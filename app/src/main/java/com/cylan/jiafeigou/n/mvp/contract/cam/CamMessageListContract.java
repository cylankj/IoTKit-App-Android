package com.cylan.jiafeigou.n.mvp.contract.cam;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.mvp.model.CamMessageBean;
import com.cylan.jiafeigou.n.view.cam.item.FaceItem;
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
         * @param o:
         */
        void deviceInfoChanged(int id, JFGDPMsg o) throws IOException;

        void onErr();

        void onMessageDeleteSuc();

        void loadingDismiss();

        boolean isUserVisible();

        void onFaceInformationReady(List<DpMsgDefine.FaceInformation> data);
    }

    interface Presenter extends BasePresenter {
        /**
         * @param timeStart
         * @param refresh
         */
        void fetchMessageList(long timeStart, boolean asc, boolean refresh);


        void removeItems(ArrayList<CamMessageBean> beanList);

        void removeAllItems(ArrayList<CamMessageBean> beanList, boolean removeAll);

        List<WonderIndicatorWheelView.WheelItem> getDateList();

        void refreshDateList(boolean needToLoadList);
    }
}

