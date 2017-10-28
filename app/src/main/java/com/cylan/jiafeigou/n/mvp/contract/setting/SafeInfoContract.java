package com.cylan.jiafeigou.n.mvp.contract.setting;

import android.content.Context;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DataPoint;
import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

import java.io.IOException;

/**
 * Created by hunt on 16-5-26.
 */

public interface SafeInfoContract {

    interface View extends BaseView {

        void onAIStrategyRsp();

        void deviceUpdate(Device device);

        void deviceUpdate(JFGDPMsg msg) throws IOException;
    }

    interface Presenter extends BasePresenter {
        /**
         * @param value {@link com.cylan.jiafeigou.dp.BaseValue#setValue(Object)}  }
         * @param id
         */
        <T extends DataPoint> void updateInfoReq(T value, long id);

        String getRepeatMode(Context context);

        void getAIStrategy();
    }
}
