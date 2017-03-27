package com.cylan.jiafeigou.n.mvp.contract.cam;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.utils.CamWarnGlideURL;

/**
 * Created by cylan-hunt on 16-11-25.
 */

public interface CamMediaContract {

    interface View extends BaseView<Presenter> {
        void savePicResult(boolean state);

        /**
         * 1:微信未安装.
         *
         * @param err
         */
        void onCollectingRsp(int err);

        int getCurrentIndex();

        void onItemCollectionCheckRsp(boolean state);
    }

    interface Presenter extends BasePresenter {
        void saveImage(CamWarnGlideURL glideURL);

        void collect(int index, long version);

        void unCollect(int index, long version);

        void checkCollection(long time, int index);
    }
}
