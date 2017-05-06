package com.cylan.jiafeigou.n.mvp.contract.cam;

import com.cylan.jiafeigou.n.mvp.BaseActivityView;
import com.cylan.jiafeigou.n.mvp.BasePresenter;

/**
 * 作者：zsl
 * 创建时间：2017/2/13
 * 描述：
 */
public interface FirmwareUpdateContract {

    interface View extends BaseActivityView<Presenter> {
    }

    interface Presenter extends BasePresenter {
    }
}
