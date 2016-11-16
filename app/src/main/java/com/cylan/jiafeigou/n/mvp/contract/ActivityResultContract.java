package com.cylan.jiafeigou.n.mvp.contract;

import com.cylan.jiafeigou.misc.RxEvent;
import com.cylan.jiafeigou.n.mvp.BasePresenter;

import org.msgpack.annotation.NotNullable;

/**
 * Created by cylan-hunt on 16-8-4.
 */
public interface ActivityResultContract {

    interface View {

        /**
         * @param result ： 不使用
         */
        void onActivityResult(@NotNullable RxEvent.ActivityResult result);
    }

    interface Presenter<T> extends BasePresenter {

        void setActivityResult(RxEvent.ActivityResult result);
    }
}
