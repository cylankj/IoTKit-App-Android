package com.cylan.jiafeigou.widget.wheel.ex;


import com.cylan.jiafeigou.cache.db.module.HistoryFile;

import java.util.ArrayList;
import java.util.TimeZone;

/**
 * Created by cylan-hunt on 16-12-19.
 */

public interface IData {

    void flattenData(ArrayList<HistoryFile> list, TimeZone zone);

    long[] getTimeArray(int leftIndex, int maxCount);

    /**
     * 延展过后的最小时间
     *
     * @return
     */
    long getFlattenMinTime();

    /**
     * 延展过后的最大时间
     *
     * @return
     */
    long getFlattenMaxTime();

    /**
     * 数据总量
     *
     * @return
     */
    int getDataCount();

    /**
     * 根据时间找出{整点,半小时}
     *
     * @param time
     * @return
     */
    int getBottomType(long time);

    String getDateInFormat(long time);

    ArrayList<HistoryFile> getMaskList(long start, long end);

    /**
     * 快速滑动,或者滑动停止后,有一个自动定位的需求,---a--A----isFriend---B---c----C--d--
     *
     * @param time
     * @return
     */
    long getNextFocusTime(long time, int considerDirection);

    boolean isHotRect(long time);

    HistoryFile getMaxHistoryFile();

    HistoryFile getMinHistoryFile();

    /**
     * 凌晨时间戳
     *
     * @param startTime
     * @return
     */
    HistoryFile getMinHistoryFileByStartTime(long startTime);

    public void clean();
}
