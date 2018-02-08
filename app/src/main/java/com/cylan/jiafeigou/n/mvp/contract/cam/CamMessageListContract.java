package com.cylan.jiafeigou.n.mvp.contract.cam;

import com.cylan.entity.jniCall.JFGDPMsg;
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


    interface View extends BaseView {


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

        void onVisitorListAppend(ArrayList<CamMessageBean> beanArrayList);

        void onVisitorListInsert(ArrayList<CamMessageBean> beans);
    }

    interface Presenter extends BasePresenter {
        /**
         * @param timeStart
         * @param refresh
         */
        void fetchMessageListByFaceId(long timeStart, boolean asc, boolean refresh);


        void removeItems(ArrayList<CamMessageBean> beanList);

        void removeAIItems(ArrayList<CamMessageBean> beanList);

        void removeAllItems(ArrayList<CamMessageBean> beanList, boolean removeAll);

        List<WonderIndicatorWheelView.WheelItem> getDateList();

        void refreshDateList(boolean needToLoadList);

        /**
         * int,       type     // 检索条件：1-陌生人 2-已注册人物 3-全部
         * string,    id       // type为1：填充face id 表示查询指定陌生人消息， 为空则查询所有陌生人消息；type为2：填充person id；type为3：空
         * int64,     timeMsec // 时间戳，单位：毫秒
         */
        void fetchVisitorMessageList(int type, String id, long sec, boolean refresh);

        void removeHint();
    }
}

