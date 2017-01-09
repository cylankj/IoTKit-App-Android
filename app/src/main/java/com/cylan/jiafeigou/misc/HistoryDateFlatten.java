package com.cylan.jiafeigou.misc;

import android.util.Log;

import com.cylan.entity.jniCall.JFGVideo;
import com.cylan.jiafeigou.BuildConfig;
import com.cylan.jiafeigou.utils.TimeUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by cylan-hunt on 16-12-22.
 */

public class HistoryDateFlatten {
    private static final String TAG = "HistoryDateFlatten";
    private static boolean DEBUG = BuildConfig.DEBUG;
    private static final Object object = new Object();
    /**
     * <凌晨时间戳,当天视频最早视频时间>
     */
    private Map<Long, Long> flattenMap = new HashMap<>();

    public void flat(ArrayList<JFGVideo> list) {
        synchronized (object) {
            final int count = list == null ? 0 : list.size();
            if (count == 0)
                return;
            //performance better than list,少量数据,性能差不多,大量数据,hash比遍历好.
            Map<String, Long> map = new HashMap<>();
            long time = System.currentTimeMillis();
            for (int i = count - 1; i >= 0; i--) {//因为list是降序
                long timeStart = list.get(i).beginTime;
                long timeEnd = list.get(i).beginTime + list.get(i).duration;
                String dayStart = TimeUtils.simpleDateFormat1.format(new Date(timeStart * 1000L));
                String dayEnd = TimeUtils.simpleDateFormat1.format(new Date(timeEnd * 1000L));
                Log.i(TAG, "delta..." + timeStart + " " + dayStart + " " + dayEnd);
                if (!map.containsKey(dayStart)) {//第一次出现
                    flattenMap.put(TimeUtils.getTimeStart(timeStart * 1000L), timeStart * 1000L);
                    map.put(dayStart, 0L);
                }
                if (!map.containsKey(dayEnd)) {//第一次出现
                    long end = TimeUtils.getTimeStart(timeEnd * 1000L);
                    flattenMap.put(end, end);
                    map.put(dayEnd, 0L);
                }
            }
            Log.d(TAG, "performance: " + count + " ---" + (System.currentTimeMillis() - time));
        }
    }

    public Map<Long, Long> getFlattenMap() {
        return flattenMap;
    }


}
