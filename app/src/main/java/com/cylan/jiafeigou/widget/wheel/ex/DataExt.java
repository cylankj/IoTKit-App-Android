package com.cylan.jiafeigou.widget.wheel.ex;

import android.util.Log;

import com.cylan.jiafeigou.BuildConfig;
import com.cylan.jiafeigou.cache.db.module.HistoryFile;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ListUtils;
import com.cylan.jiafeigou.utils.TimeUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by cylan-hunt on 16-12-19.
 */

public class DataExt implements IData {
    private static DataExt instance;
    private SimpleDateFormat dateFormat;

    public static DataExt getInstance() {
        if (instance == null)
            synchronized (DataExt.class) {
                if (instance == null)
                    instance = new DataExt();
            }
        return instance;
    }

    private DataExt() {
    }

    private static boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "DataExt";
    private int index = 0;
    /**
     * s为单位
     */
    private ArrayList<Long> flattenDataList = new ArrayList<>();
    private ArrayList<HistoryFile> rawList = new ArrayList<>();
    private Map<Long, Integer> timeWithType = new HashMap<>();

    private Map<Long, String> dateFormatMap = new HashMap<>();

    private void initDateFormat(TimeZone zone) {
        dateFormat = new SimpleDateFormat("HH:mm", Locale.UK);
        dateFormat.setTimeZone(zone);
    }

    @Override
    public void flattenData(ArrayList<HistoryFile> list, TimeZone zone) {
        this.rawList = list;
        flattenDataList.clear();
        int size = list.size();
        if (size > 0) {
            initDateFormat(zone);
            //需要判断顺序.....
            AppLogger.e("需要判断顺序");
            int maxIndex = list.get(0).time >= list.get(list.size() - 1).time ? 0 : list.size() - 1;
            int minIndex = maxIndex == 0 ? list.size() - 1 : 0;
            long timeMax = getTenMinuteByTimeRight((list.get(maxIndex).time + list.get(maxIndex).duration) * 1000L);
            long timeMin = getTenMinuteByTimeLeft((list.get(minIndex).time) * 1000L);
            size = 0;
            Log.d(TAG, String.format(Locale.getDefault(), "timeMax:%s,timeMin:%s", timeMax, timeMin));
            for (long time = timeMax; time >= timeMin; ) {
                flattenDataList.add(time);
                fillMap(time);
                if (DEBUG)
                    Log.d(TAG, "i:" + size + " " + TimeUtils.getHistoryTime(time));
                size++;
                time -= 10 * 60 * 1000L;
            }
        }
    }

    /**
     * 标记整点的时间戳
     *
     * @param time ;milliseconds
     */
    private void fillMap(long time) {
        if (time / 1000L % 3600 == 0) {
            timeWithType.put(time, 1);
            dateFormatMap.put(time, TimeUtils.getHistoryTime(time));
        }
    }

    /**
     * 过去最近的一个 10分钟： 2016-07-26-21:10
     *
     * @param time
     * @return :四舍五入到10分钟，小于time.
     */
    private long getTenMinuteByTimeRight(long time) {
        final long currentTimeMinutesInMode = time / 1000 / 60 % 60;
        final long currentTimeInHour = time / 1000L / 3600;
        long result = 1000L * 60 * (currentTimeInHour * 60 + currentTimeMinutesInMode
                - currentTimeMinutesInMode % 10);
        return result > time ? result : result + 10 * 60 * 1000L;
    }

    /**
     * 过去最近的一个 10分钟： 2016-07-26-21:10
     *
     * @param time
     * @return :四舍五入到10分钟，小于time.
     */
    private long getTenMinuteByTimeLeft(long time) {
        final long currentTimeMinutesInMode = time / 1000 / 60 % 60;
        final long currentTimeInHour = time / 1000L / 3600;
        long result = 1000L * 60 * (currentTimeInHour * 60 + currentTimeMinutesInMode
                - currentTimeMinutesInMode % 10);
        return result > time ? result - 10 * 60 * 1000L : result;
    }


    @Override
    public long[] getTimeArray(int end, int start) {
        if (DEBUG)
            Log.d(TAG, String.format("getData:%s,%s", end, start));
        if (start < 0 || start > end || end > flattenDataList.size()) {
//            System.out.println("出界: " + initSubscription);
            end = flattenDataList.size() - start;
            if (end <= 0)
                return null;
        }
        long[] data = new long[end - start];
        for (int i = start; i < end; i++) {
            data[i - start] = flattenDataList.get(i);
        }
        return data;
    }

    @Override
    public long getFlattenMaxTime() {
        int size = flattenDataList.size();
        return size > 0 ? flattenDataList.get(0) : 0;
    }

    @Override
    public long getFlattenMinTime() {
        int size = flattenDataList.size();
        return size > 0 ? flattenDataList.get(size - 1) : 0;
    }

    @Override
    public int getDataCount() {
        return ListUtils.getSize(flattenDataList);
    }

    @Override
    public int getBottomType(long time) {
        Object o = timeWithType.get(time);
        return o == null ? 0 : (int) o;
    }

    @Override
    public String getDateInFormat(long time) {
        return dateFormatMap.get(time);
    }

    @Override
    public ArrayList<HistoryFile> getMaskList(long start, long end) {
        if (rawList == null || rawList.size() == 0)
            return null;
        HistoryFile vStart = getVideo(start);
        HistoryFile vEnd = getVideo(end);
        int startIndex = Collections.binarySearch(rawList, vStart);
        if (startIndex < 0) {
            startIndex = -(startIndex + 1);
            if (startIndex > 0) {
                startIndex -= 1;
            }
        }
        int endIndex = Collections.binarySearch(rawList, vEnd);
        if (endIndex < 0) {
            endIndex = -(endIndex + 1);
            if (rawList.size() - 1 > endIndex) {
                endIndex += 2;
            }
        }
        if (endIndex < startIndex) return null;
        ArrayList<HistoryFile> finalList = new ArrayList<>(endIndex - startIndex);
        for (int i = startIndex; i < endIndex; i++) {
            finalList.add(rawList.get(i));
        }
        return finalList;
    }


    private long getNextFocusTime(long time) {
        if (rawList.size() == 0)
            return 0;
        HistoryFile v = getVideo(time);
        index = Collections.binarySearch(rawList, v);
        index = -(index + 1);
//        Log.d("getNextFocusTime", "getNextFocusTime: " + index);
        if (index < 0 && rawList.size() > 0) {
            index = 0;
            return rawList.get(0).time * 1000L;
        }
        if (index > rawList.size() - 1 && rawList.size() > 0) {
            index = rawList.size() - 1;
            return rawList.get(index).time * 1000L;
        }
        return rawList.get(index).time * 1000L;
    }

    @Override
    public long getNextFocusTime(long time, int considerDirection) {
        long tmpTime = getNextFocusTime(time);
        if (considerDirection == -1)//不考虑方向
            return tmpTime;
        //0:向左滑动
        index += 1;
        if (index > rawList.size() - 1) {
            index = rawList.size() - 1;
        }
        //1:向右滑动
        index -= 1;
        if (index < 0 && rawList.size() > 0) {
            index = 0;
        }
        return rawList.get(index).time * 1000L;
    }

    @Override
    public boolean isHotRect(long time) {
        if (rawList == null || rawList.size() == 0)
            return false;
        HistoryFile v = getVideo(time);
        int i = Collections.binarySearch(rawList, v);
        i = -(i + 1);
        if (i < 0 || i > rawList.size() - 1) {
            return false;//超出范围
        }
        if (DEBUG)
            Log.d(TAG, "index: " + i + " " + TimeUtils.simpleDateFormat2.format(new Date(time)));
        v = rawList.get(i);
        return v.time * 1000L <= time && (v.time + v.duration) * 1000L >= time;
    }

    @Override
    public HistoryFile getMaxHistoryFile() {
        //rawList 是一个降序
        return ListUtils.getSize(rawList) > 0 ? rawList.get(0) : null;
    }

    @Override
    public HistoryFile getMinHistoryFile() {
        return ListUtils.getSize(rawList) > 0 ? rawList.get(rawList.size() - 1) : null;
    }

    @Override
    public HistoryFile getMinHistoryFileByStartTime(long startTime) {
        //rawList是降序的
        startTime = startTime / 1000;
        final int count = ListUtils.getSize(rawList);
        for (int i = count - 1; i >= 0; i--) {
            if (rawList.get(i).getTime() >= startTime)
                return rawList.get(i);
        }
        return null;
    }

    @Override
    public void clean() {
        dateFormatMap.clear();
        timeWithType.clear();
        rawList.clear();
        flattenDataList.clear();
    }

    private HistoryFile getVideo(long time) {
        HistoryFile video = new HistoryFile(0L, time, 0, "", "");
        video.time = time / 1000L;
        return video;
    }

}
