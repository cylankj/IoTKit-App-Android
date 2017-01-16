package com.cylan.jiafeigou.n.mvp.impl.setting;

import com.cylan.jiafeigou.cache.SimpleCache;
import com.cylan.jiafeigou.n.mvp.contract.setting.TimezoneContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.TimeZoneBean;
import com.cylan.jiafeigou.utils.MiscUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by cylan-hunt on 16-11-26.
 */

public class TimezonePresenterImpl extends AbstractPresenter<TimezoneContract.View>
        implements TimezoneContract.Presenter {
    private static final String TAG = "TimezonePresenterImpl";
    private String uuid;

    public TimezonePresenterImpl(TimezoneContract.View view, String uuid) {
        super(view);
        this.uuid = uuid;
    }

    @Override
    public void start() {
        super.start();
        MiscUtils.loadTimeZoneList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((List<TimeZoneBean> list) -> {
                    getView().timezoneList(list);
                });
    }


    @Override
    public void onSearch(String content) {
        List<TimeZoneBean> list = SimpleCache.getInstance().timeZoneCache == null ? null :
                SimpleCache.getInstance().timeZoneCache.get();
        final int count = list == null ? 0 : list.size();
        List<TimeZoneBean> newList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            TimeZoneBean bean = list.get(i);
            if (bean.getGmt().contains(content)
                    || bean.getId().contains(content)
                    || bean.getName().contains(content)) {
                newList.add(bean);
            }
        }
        getView().timezoneList(newList);
    }

}
