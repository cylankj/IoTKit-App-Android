package com.cylan.jiafeigou.misc;

import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.widget.wheel.WheelViewDataSet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

;

/**
 * Created by cylan-hunt on 16-7-4.
 */
public class TimeLineAssembler {


    public TimeLineAssembler() {

    }

    public void setMediaBeanLinkedList(LinkedList<DpMsgDefine.DPWonderItem> mediaBeanLinkedList) {
        this.mediaBeanLinkedList = mediaBeanLinkedList;
    }


    public WheelViewDataSet generateDataSet() {
        WheelViewDataSet dataSet = new WheelViewDataSet();
        Middle middle = expandRawList();
        if (middle == null) {
            AppLogger.d("god ,data is empty");
            return null;
        }
        if (middle.timeInLongList == null || middle.timeInStrList == null || middle.typeList == null) {
            AppLogger.d("god ,data is empty again");
            return null;
        }
        final int count_0 = middle.timeInLongList.size();
        final int count_1 = middle.timeInStrList.size();
        final int count_2 = middle.typeList.size();
        if (count_0 != count_1 || count_0 != count_2) {
            AppLogger.d("god ,data is empty again again");
        }

        dataSet.dataSet = new long[count_0];
        dataSet.dataTypeSet = new int[count_0];
        dataSet.dateInStr = new HashMap<>();

        for (int i = 0; i < count_0; i++) {
            dataSet.dateInStr.put(i, middle.timeInStrList.get(i));
            dataSet.dataSet[i] = middle.timeInLongList.get(i);
            dataSet.dataTypeSet[i] = middle.typeList.get(i);
            AppLogger.d("set: i:" + i + " type:" + dataSet.dataTypeSet[i]);
        }


        return dataSet;
    }

    public LinkedList<DpMsgDefine.DPWonderItem> mediaBeanLinkedList;

    final static SimpleDateFormat simpleDate = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
    final static SimpleDateFormat simpleDateCNShort = new SimpleDateFormat("M月", Locale.getDefault());
    final static SimpleDateFormat simpleDateCNNormal = new SimpleDateFormat("M月d日", Locale.getDefault());

    final static long oneDay = 24 * 3600 * 1000L;

    private String getDate(final long time) {
        return simpleDate.format(new Date(time));
    }

    private int getDataType(final int type, final String time) {
        if (type == 0) {
            //has data
            return time.endsWith("15") || time.endsWith("01") ? WheelViewDataSet.TYPE_LONG_VALID : WheelViewDataSet.TYPE_SHORT_VALID;
        } else {
            //no data
            return time.endsWith("15") || time.endsWith("01") ? WheelViewDataSet.TYPE_LONG_INVALID : WheelViewDataSet.TYPE_SHORT_INVALID;
        }
    }

    private String getCustomDateInStr(final String timeInStr, final long timeInLong, final int type) {
        switch (type) {
            case WheelViewDataSet.TYPE_LONG_VALID:
            case WheelViewDataSet.TYPE_LONG_INVALID:
                if (timeInStr.endsWith("01"))
                    return simpleDateCNShort.format(new Date(timeInLong));
                else return simpleDateCNNormal.format(new Date(timeInLong));
            case WheelViewDataSet.TYPE_SHORT_VALID:
            case WheelViewDataSet.TYPE_SHORT_INVALID:
                if (timeInStr.endsWith("01"))
                    return simpleDateCNShort.format(new Date(timeInLong));
                else return simpleDateCNNormal.format(new Date(timeInLong));
        }
        return "";
    }

    /**
     * 把原始数据填充，成为日历。
     */
    public Middle expandRawList() {
        Collections.reverse(mediaBeanLinkedList);
        Middle middle = new Middle();
        final int count = mediaBeanLinkedList.size();

        for (int i = 0; i < count - 1; i++) {
            final String leftStr = getDate(mediaBeanLinkedList.get(i).time);
            final String rightStr = getDate(mediaBeanLinkedList.get(i + 1).time);
            final long leftLong = mediaBeanLinkedList.get(i).time;
            final long rightLong = mediaBeanLinkedList.get(i + 1).time;
            middle.timeInLongList.add(leftLong);
            int type = getDataType(0, leftStr);
            middle.typeList.add(type);
            middle.timeInStrList.add(getCustomDateInStr(leftStr, leftLong, type));
            if (i == count - 2) {
                middle.timeInLongList.add(rightLong);
                type = getDataType(0, rightStr);
                middle.typeList.add(type);
                middle.timeInStrList.add(getCustomDateInStr(rightStr, rightLong, type));
            }

            final int factor = leftLong - rightLong > 0 ? -1 : 1;
            if (leftStr.equals(rightStr))
                continue;

            for (int jj = 0; jj < Integer.MAX_VALUE; jj++) {
                final long today = factor * jj * oneDay + leftLong;
                final String todayStr = getDate(today);
                if (todayStr.equals(leftStr))
                    continue;
                if (todayStr.equals(rightStr))
                    break;
                middle.timeInLongList.add(today);
                type = getDataType(1, todayStr);
                middle.typeList.add(type);
                middle.timeInStrList.add(getCustomDateInStr(todayStr, today, type));
            }
        }
        return middle;
    }

    private static class Middle {
        List<Long> timeInLongList = new ArrayList<>();
        List<String> timeInStrList = new ArrayList<>();
        List<Integer> typeList = new ArrayList<>();
    }
}
