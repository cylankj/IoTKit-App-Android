package com.cylan.jiafeigou.entity;

import android.content.res.XmlResourceParser;

import com.cylan.support.DswLog;

import org.xmlpull.v1.XmlPullParser;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class TimeZoneBean implements Comparable<TimeZoneBean>, Serializable {
    private String timezoneid;
    private String timezonename;
    private String timezone;
    private Boolean isChecked;

    public Boolean getIsChecked() {
        return isChecked;
    }

    public void setIsChecked(Boolean isChecked) {
        this.isChecked = isChecked;
    }

    public String getTimezoneid() {
        return timezoneid;
    }

    public void setTimezoneid(String timezoneid) {
        this.timezoneid = timezoneid;
    }

    public String getTimezonename() {
        return timezonename;
    }

    public void setTimezonename(String timezonename) {
        this.timezonename = timezonename;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }


    public static List<TimeZoneBean> parseXml(XmlResourceParser xmlParser) {
        SimpleDateFormat mHourFormat = new SimpleDateFormat("hh:mm");
        TimeZone tz = TimeZone.getTimeZone("GMT+0");
        mHourFormat.setTimeZone(tz);
        List<TimeZoneBean> mList = new ArrayList<>();
        try {
            int evtType = xmlParser.getEventType();

            while (evtType != XmlPullParser.END_DOCUMENT) {

                switch (evtType) {
                    case XmlResourceParser.START_TAG:
                        String tag = xmlParser.getName();
                        // 通知信息
                        if (tag.equals("timezone")) {
                            TimeZoneBean tzb = new TimeZoneBean();
                            tzb.setTimezone(xmlParser.getAttributeValue(0));
                            tzb.setTimezoneid(xmlParser.getAttributeValue(1));
                            tzb.setTimezonename(xmlParser.nextText());
                            tzb.setIsChecked(false);
                            mList.add(tzb);
                        }
                        break;

                }
                // 如果xml没有结束，则导航到下一个节点
                evtType = xmlParser.next();
            }
        } catch (Exception e) {
            DswLog.ex(e.toString());
        } finally {
            xmlParser.close();
        }

        return mList;

    }


    /**
     * 获取系统当前默认时区与指定时区的时间差.(单位:毫秒)
     *
     * @param timeZoneId 时区Id
     * @return 系统当前默认时区与指定时区的时间差.(单位:毫秒)
     */
    private static int getDiffTimeZoneRawOffset(String timeZoneId) {
        return TimeZone.getTimeZone(timeZoneId).getRawOffset();
    }

    @Override
    public int compareTo(TimeZoneBean another) {
        return getDiffTimeZoneRawOffset(another.getTimezoneid()) < getDiffTimeZoneRawOffset(this.getTimezoneid()) ? 1 : -1;
    }
}
