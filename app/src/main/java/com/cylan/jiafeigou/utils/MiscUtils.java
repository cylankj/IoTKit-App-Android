package com.cylan.jiafeigou.utils;

import android.content.res.XmlResourceParser;
import android.text.TextUtils;
import android.util.Log;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.cache.SimpleCache;
import com.cylan.jiafeigou.n.mvp.model.TimeZoneBean;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by cylan-hunt on 16-11-16.
 */

public class MiscUtils {


    public static boolean isInRange(int start, int end, int dst) {
        return dst >= start && dst <= end;
    }

    public static boolean isBad(List<Integer> list, int level, int count) {
        if (list == null || list.size() < count)
            return false;
        final int size = list.size();
        int result = 0;
        for (int i = 0; i < size; i++) {
            if (list.get(i) < level)
                result++;
        }
        return result >= count;
    }

    public static int parseTime(String time) {
        String[] times = time.split(":");
        final String h = Integer.toBinaryString(times[0].length() > 1 ? Integer.parseInt(times[0].substring(times[0].length() - 2, times[0].length())) : Integer.parseInt(times[0].substring(times[0].length() - 1, times[0].length())));
        final String m = String.format(Locale.getDefault(), "%08d",
                Integer.parseInt(Integer.toBinaryString(Integer.parseInt(times[1]))));
        return Integer.parseInt(h + m, 2);
    }

    public static String parse2Time(int value) {
        return String.format(Locale.getDefault(), "%02d", value >> 8)
                + String.format(Locale.getDefault(), ":%02d", (((byte) value << 8) >> 8));
    }

    public static String getBit(int flow) {
        int factor = flow / 1024;
        if (factor >= 1024) {
            return "GB";
        }
        if (factor >= 1) {
            return "Mb";
        }
        return "Kb";
    }

//    public static void main(String[] args) {
//        System.out.println(getBit(1024 * 1024 + 1));
//        System.out.println(getBit(1024 * 1024));
//        System.out.println(getBit(1024 * 1024 - 1));
//        System.out.println(getBit(1025));
//        System.out.println(getBit(1024));
//        System.out.println(getBit(1023));
//        System.out.println(getCount(1));
//        System.out.println(getCount(1));
//        System.out.println(getCount(1));
//        System.out.println(getCount(1));
//    }

    public static int getCount(int sum) {
        int count = 0;
        for (int i = 0; i < 3; i++) {
            if ((sum >> i & 0x01) == 1) count++;
        }
        return count;
    }

    @SuppressWarnings("unchecked")
    public static <T> T cast(Object object, T defaultValue) {
        try {
            return object == null ? defaultValue : (T) object;
        } catch (ClassCastException e) {
            return defaultValue;
        }
    }

    public static String bytesToHex(byte[] in) {
        final StringBuilder builder = new StringBuilder();
        for (byte b : in) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

    public static Observable<List<TimeZoneBean>> loadTimeZoneList() {
        return Observable.just(R.xml.timezones)
                .subscribeOn(Schedulers.computation())
                .flatMap(new Func1<Integer, Observable<List<TimeZoneBean>>>() {
                    @Override
                    public Observable<List<TimeZoneBean>> call(Integer integer) {
                        WeakReference<List<TimeZoneBean>> weakReference = SimpleCache.getInstance().timeZoneCache;
                        if (weakReference != null && weakReference.get() != null && weakReference.get().size() > 0) {
                            return Observable.just(weakReference.get());
                        }
                        XmlResourceParser xrp = ContextUtils.getContext().getResources().getXml(integer);
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
                                        String region = xrp.nextText().replace("\n", "");
                                        bean.setName(region);
                                        int factor = timeGmtName.contains("+") ? 1 : -1;
                                        String digitGmt = BindUtils.getDigitsString(timeGmtName);
                                        int offset = factor * Integer.parseInt(digitGmt.substring(0, 2)) * 3600 +
                                                factor * (timeGmtName.contains(":30") ? 3600 / 2 : 0);
                                        bean.setOffset(offset);
                                        list.add(bean);
                                    }
                                }
                                xrp.next();
                            }
                            SimpleCache.getInstance().timeZoneCache = new WeakReference<>(list);
                            Log.d(tag, "timezone: " + list);
                        } catch (XmlPullParserException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                        }
                        return Observable.just(list);
                    }
                });
    }
}