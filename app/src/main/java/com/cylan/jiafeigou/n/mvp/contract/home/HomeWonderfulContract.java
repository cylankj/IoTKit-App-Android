package com.cylan.jiafeigou.n.mvp.contract.home;

import com.cylan.jiafeigou.base.view.JFGPresenter;
import com.cylan.jiafeigou.base.view.JFGView;
import com.cylan.jiafeigou.dp.DpMsgDefine;

import java.util.List;

/**
 * Created by hunt on 16-5-23.
 */
public interface HomeWonderfulContract {

    interface View extends JFGView {

        int VIEW_TYPE_HIDE = -1;
        int VIEW_TYPE_EMPTY = 0;
        int VIEW_TYPE_GUIDE = 1;

        void onMediaListRsp(List<DpMsgDefine.DPWonderItem> list);

        void onHeadBackgroundChang(int daytime);

        void onTimeLineDataUpdate(List<Long> wheelViewDataSet);

        /**
         * @param dayTime：0白天 1黑夜
         */
        void onTimeTick(int dayTime);

        /**
         * 收回
         */
        void onPageScrolled();

        void onWechatCheckRsp(boolean installed);

        void chooseEmptyView(int type);//type:0:empty:1:guide:-1:hide
    }

    interface Presenter extends JFGPresenter {
        void startRefresh();

        void startLoadMore();

        void deleteTimeline(long time);

        boolean checkWechat();

        void removeGuideAnymore();

        void loadSpecificDay(long timeStamp);

    }

}
