package com.cylan.jiafeigou.n.mvp.impl.setting;

import com.cylan.jiafeigou.n.mvp.contract.setting.TimezoneContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.TimeZoneBean;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.MiscUtils;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;

/**
 * Created by cylan-hunt on 16-11-26.
 */

public class TimezonePresenterImpl extends AbstractPresenter<TimezoneContract.View>
        implements TimezoneContract.Presenter {
    private static final String TAG = "TimezonePresenterImpl";
    private List<TimeZoneBean> timeZoneBeenList;

    public TimezonePresenterImpl(TimezoneContract.View view, String uuid) {
        super(view);
        this.uuid = uuid;
    }

    @Override
    public void start() {
        super.start();
        MiscUtils.loadTimeZoneList()
                .flatMap(timeZoneBeen -> Observable.just(timeZoneBeenList = timeZoneBeen))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((List<TimeZoneBean> list) -> {
                    getView().timezoneList(list);
                }, AppLogger::e);
    }


    @Override
    public void onSearch(String content) {
        final int count = timeZoneBeenList == null ? 0 : timeZoneBeenList.size();
        List<TimeZoneBean> newList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            TimeZoneBean bean = timeZoneBeenList.get(i);
            if (bean.getGmt().contains(content)
                    || bean.getId().contains(content)
                    || bean.getName().contains(content)) {
                newList.add(bean);
            }
        }
        getView().timezoneList(newList);
    }

}
