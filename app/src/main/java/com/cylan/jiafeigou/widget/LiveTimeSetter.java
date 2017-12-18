package com.cylan.jiafeigou.widget;

import java.util.TimeZone;

/**
 * Created by cylan-hunt on 16-12-23.
 */

public interface LiveTimeSetter {

    void setContent(int liveType, long liveTime);

    void setTimeZone(TimeZone timeZone);
}