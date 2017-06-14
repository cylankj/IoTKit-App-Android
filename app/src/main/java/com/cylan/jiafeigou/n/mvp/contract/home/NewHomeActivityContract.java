package com.cylan.jiafeigou.n.mvp.contract.home;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.rx.RxEvent;

import java.io.File;

/**
 * Created by hunt on 16-5-23.
 */

public interface NewHomeActivityContract {

    interface View extends BaseView<Presenter> {
        void initView();

        void updateProcess(long currentByte, long totalByte);

        void failed(Throwable throwable);

        void finished(File file);

        void start();

        void needUpdate(@RxEvent.UpdateType int type, String desc, String filePath, int force);

        void refreshHint(boolean show);
    }


    interface Presenter extends BasePresenter {
        //void loadTable();

        //void doSample();

//        void startUpdate();
    }
}
