package com.cylan.jiafeigou.n.mvp.contract.setting;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.mvp.model.BeanCamInfo;
import com.cylan.jiafeigou.n.mvp.model.TimeZoneBean;

import java.util.List;

/**
 * Created by hunt on 16-5-26.
 */

public interface TimezoneContract {

    interface View extends BaseView<Presenter> {
        void timezoneList(List<TimeZoneBean> list);
    }

    interface Presenter extends BasePresenter {
        void onSearch(String content);

        void saveCamInfoBean(BeanCamInfo info, int id);

        BeanCamInfo getInfo();
    }
}
