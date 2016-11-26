package com.cylan.jiafeigou.n.mvp.impl.setting;

import android.content.res.XmlResourceParser;
import android.text.TextUtils;
import android.util.Log;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.cache.JCache;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.n.mvp.contract.setting.TimezoneContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.BeanCamInfo;
import com.cylan.jiafeigou.n.mvp.model.TimeZoneBean;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.BindUtils;
import com.google.gson.Gson;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
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
    private BeanCamInfo info;

    public TimezonePresenterImpl(TimezoneContract.View view, BeanCamInfo info) {
        super(view);
        this.info = info;
    }

    @Override
    public void start() {
        Observable.just(R.xml.timezones)
                .subscribeOn(Schedulers.computation())
                .flatMap(new Func1<Integer, Observable<List<TimeZoneBean>>>() {
                    @Override
                    public Observable<List<TimeZoneBean>> call(Integer integer) {
                        if (JCache.timeZoneBeenList != null) {
                            return Observable.just(JCache.timeZoneBeenList);
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
                                        bean.setOffset(offset);
                                        list.add(bean);
                                    }
                                }
                                xrp.next();
                            }
                            JCache.timeZoneBeenList = list;
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
    public void stop() {

    }

    @Override
    public void onSearch(String content) {
        final int count = JCache.timeZoneBeenList == null ? 0 : JCache.timeZoneBeenList.size();
        List<TimeZoneBean> newList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            TimeZoneBean bean = JCache.timeZoneBeenList.get(i);
            if (bean.getGmt().contains(content)
                    || bean.getId().contains(content)
                    || bean.getName().contains(content)) {
                newList.add(bean);
            }
        }
        getView().timezoneList(newList);
    }

    @Override
    public void updateBeanInfo(final BeanCamInfo info) {
        AppLogger.i(TAG + "save info: " + new Gson().toJson(info));
        Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        RxEvent.JfgDpMsgUpdate msgUpdate = new RxEvent.JfgDpMsgUpdate();
                        msgUpdate.uuid = info.deviceBase.uuid;
                        DpMsgDefine.DpMsg dpMsg = new DpMsgDefine.DpMsg();
                        //

                        AppLogger.i("xuyao 修改");
                        RxBus.getCacheInstance().post(msgUpdate);
                    }
                });
    }

    @Override
    public BeanCamInfo getInfo() {
        return info;
    }
}
