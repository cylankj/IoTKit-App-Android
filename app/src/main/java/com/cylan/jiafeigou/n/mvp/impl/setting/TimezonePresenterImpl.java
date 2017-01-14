package com.cylan.jiafeigou.n.mvp.impl.setting;

import android.content.res.XmlResourceParser;
import android.text.TextUtils;
import android.util.Log;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.cache.SimpleCache;
import com.cylan.jiafeigou.n.mvp.contract.setting.TimezoneContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.TimeZoneBean;
import com.cylan.jiafeigou.utils.BindUtils;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

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
        Observable.just(R.xml.timezones)
                .subscribeOn(Schedulers.computation())
                .flatMap(new Func1<Integer, Observable<List<TimeZoneBean>>>() {
                    @Override
                    public Observable<List<TimeZoneBean>> call(Integer integer) {
                        if (SimpleCache.getInstance().timeZoneBeenList != null) {
                            return Observable.just(SimpleCache.getInstance().timeZoneBeenList.get());
                        }
                        XmlResourceParser xrp = getView().getContext().getResources().getXml(integer);
                        List<TimeZoneBean> list = new ArrayList<>();
                        try {
                            final String tag = "timezone";
                            while (xrp.getEventType() != XmlResourceParser.END_DOCUMENT) {
                                if (xrp.getEventType() == XmlResourceParser.START_TAG) {
                                    TimeZoneBean bean = new TimeZoneBean();
                                    final String name = xrp.getName();
                                    if (TextUtils.equals(name, tag)) {
                                        final String timeGmtName = xrp.getAttributeValue(0);
                                        bean.setGmt(timeGmtName);
                                        final String timeIdName = xrp.getAttributeValue(1);
                                        bean.setId(timeIdName);
                                        String rigon = xrp.nextText();
                                        bean.setName(rigon);
                                        int factor = timeGmtName.contains("+") ? 1 : -1;
                                        String digitGmt = BindUtils.getDigitsString(timeGmtName);
                                        int offset = factor * Integer.parseInt(digitGmt.substring(0, 2));
                                        bean.setOffset(offset * 3600);
                                        list.add(bean);
                                    }
                                }
                                xrp.next();
                            }
                            SimpleCache.getInstance().timeZoneBeenList = new WeakReference<>(list);
                            Log.d(tag, "timezone: " + list);
                        } catch (XmlPullParserException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                        }
                        return Observable.just(list);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<TimeZoneBean>>() {
                    @Override
                    public void call(List<TimeZoneBean> list) {
                        getView().timezoneList(list);
                    }
                });
    }


    @Override
    public void onSearch(String content) {
        List<TimeZoneBean> list = SimpleCache.getInstance().timeZoneBeenList == null ? null :
                SimpleCache.getInstance().timeZoneBeenList.get();
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
