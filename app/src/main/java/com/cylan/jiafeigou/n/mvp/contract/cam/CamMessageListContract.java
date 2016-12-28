package com.cylan.jiafeigou.n.mvp.contract.cam;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.mvp.model.CamMessageBean;

import java.util.ArrayList;

/**
 * Created by cylan-hunt on 16-6-29.
 */
public interface CamMessageListContract {


    interface View extends BaseView<Presenter> {

        void onMessageListRsp(ArrayList<CamMessageBean> beanArrayList);

        ArrayList<CamMessageBean> getList();

        /**
         * 设备信息{在线,sd卡,电量.....所有信息}
         *
         * @param id:消息id
         * @param o: {@link com.cylan.jiafeigou.dp.DpMsgConstant}
         */
        void deviceInfoChanged(int id, Object o);
    }

    interface Presenter extends BasePresenter {
        void fetchMessageList();

        void removeItem(CamMessageBean bean);
    }
}

