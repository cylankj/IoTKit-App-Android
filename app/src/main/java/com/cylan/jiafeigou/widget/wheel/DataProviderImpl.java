package com.cylan.jiafeigou.widget.wheel;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by cylan-hunt on 16-7-27.
 */
public class DataProviderImpl {

    private long[] historyTimeSet;


    private void checkHistoryDataSet() {
        if (historyTimeSet == null) {
            throw new NullPointerException("history data is null");
        }
    }

    /**
     * 一般为historyTimeSet[0]
     */
    public SDataStack initTimeLine() {
        checkHistoryDataSet();
        final long startTime = historyTimeSet[0];
        final long startTimeInTenMinute = getTenMinuteByTime(startTime);
        final long endTimeInTenMinute = getTenMinuteByTime(System.currentTimeMillis());
        final int maxCount = getTimeInterval(startTimeInTenMinute, endTimeInTenMinute);
        Log.d("SuperWheel", "maxCount: " + maxCount);

        SDataStack dataStack = new SDataStack();
        dataStack.naturalDateSet = new ArrayList<>();
        dataStack.recordTimeSet = historyTimeSet;
        dataStack.naturalDateType = new int[maxCount];
        dataStack.dateStringMap = new HashMap<>();
        for (int i = 0; i < maxCount; i++) {
            final long timeInLong = startTimeInTenMinute + i * 10 * 60 * 1000L;
            dataStack.naturalDateSet.add(timeInLong);
            final int type = getType(timeInLong);
            dataStack.naturalDateType[i] = type;
            if (type == 0)
                dataStack.dateStringMap.put(i + "", getDateString(timeInLong));
        }
        return dataStack;
    }

    /**
     * 过去最近的一个 10分钟： 2016-07-26-21:10
     *
     * @param time
     * @return :四舍五入到10分钟，小于time.
     */
    private long getTenMinuteByTime(long time) {
        final long currentTimeMinutesInMode = time / 1000 / 60 % 60;
        final long currentTimeInHour = time / 1000 / 3600;
        return 1000 * 60
                * (currentTimeInHour * 60 + currentTimeMinutesInMode
                - currentTimeMinutesInMode % 10);
    }

    /**
     * 根据开始时间和结束时间计算出
     *
     * @param startTime
     * @param endTime
     * @return
     */
    private int getTimeInterval(long startTime, long endTime) {
        return (int) ((endTime - startTime) / 1000 / 60 / 10);
    }


    public void setHistoryTimeSet(long[] historyTimeSet) {
        this.historyTimeSet = historyTimeSet;
    }

    /**
     * 保存一个type类型，不需要在onDraw的时候去计算该时间是否为整点，提升性能。
     *
     * @param time
     * @return
     */
    private int getType(final long time) {
        return time / 1000 / 60 % 60 == 0 ? 0 : 1;
    }

    /**
     * 保存一份整点对应的时间字符串，不需要在onDraw的过程计算，省时。
     *
     * @param time
     * @return
     */
    private String getDateString(final long time) {
        return simpleDateFormat.format(new Date(time));
    }


    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());


}
