package com.cylan.jiafeigou.n.mvp.contract.home;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.mvp.model.MediaBean;

import java.util.List;

/**
 * Created by hunt on 16-5-23.
 */
public interface HomeWonderfulContract {

    interface View extends BaseView<Presenter> {

        void onMediaListRsp(List<MediaBean> list);

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
    }

    interface Presenter extends BasePresenter {
        void startRefresh();

        void startLoadMore(long startTime);

        /**
         * 删除每日精彩条目
         *
         * @param time
         */
        void deleteTimeline(long time);

        /**
         * 检查微信是否已经安装
         */
        boolean checkWechat();

        void unregisterWechat();

        void shareToWechat(MediaBean mediaBean, int type);
    }

}
