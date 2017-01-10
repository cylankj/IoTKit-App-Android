package com.cylan.jiafeigou.n.mvp.contract.bind;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

/**
 * Created by cylan-hunt on 16-11-12.
 */

public interface SubmitBindingInfoContract {

    interface View extends BaseView<Presenter> {
        /**
         * 绑定状态: -1:连接中,0离线,>0 成功
         */
        void bindState(int state);

        void onCounting(int percent);
    }

    interface Presenter extends BasePresenter {

        /**
         * 开始模拟动画
         */
        void startCounting();

        /**
         * 动画结束:
         */
        void endCounting();
    }
}
