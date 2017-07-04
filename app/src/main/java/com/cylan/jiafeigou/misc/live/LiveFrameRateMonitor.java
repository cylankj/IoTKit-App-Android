package com.cylan.jiafeigou.misc.live;

import android.util.Log;

import com.cylan.entity.jniCall.JFGMsgVideoRtcp;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by cylan-hunt on 17-2-21.
 */

public class LiveFrameRateMonitor implements IFeedRtcp {


    private static final int MAX_SIZE = 30;//现在统一30秒 loading
    private static final int TARGET_SIZE = 8;
    private static final int FINAL_LEVEL = 2;
    private static final int FAILED_WINDOW = 10;//判断失败的窗口期
    private static final int LOADING_WINDOW = 3;
    private static final int FAILED_TARGET = 10;
    private static final int LOADING_TARGET = 3;
    private static final int BAD_LEVEL = 0;
    private boolean preStatus;
    private final ReentrantLock lock = new ReentrantLock();
    private long lastNotifyTime;
    private static final int NOTIFY_WINDOW = 4000;
    private volatile int badFrameCount = 0;
    /**
     * 10内的规则.
     */
    private List<JFGMsgVideoRtcp> frameRateList = new LinkedList<>();

    @Override
    public void feed(JFGMsgVideoRtcp rtcp) {
        AppLogger.d("rtcp:" + rtcp.frameRate);
        badFrameCount = rtcp.frameRate == 0 ? ++badFrameCount : 0;
        if (monitorListener == null) return;
        boolean isFrameFailed = badFrameCount >= FAILED_TARGET;
        boolean isFrameLoading = badFrameCount >= LOADING_TARGET;
        Log.d("LiveFrameRateMonitor", "视频帧率分析结果, 是否加载失败:" + isFrameFailed + ",是否 Loading:" + isFrameLoading + ",badCount:" + badFrameCount);
        long dutation = System.currentTimeMillis() - lastNotifyTime;
        if (isFrameLoading && isFrameFailed && dutation > NOTIFY_WINDOW) {//加载失败了,4秒通知
            lastNotifyTime = System.currentTimeMillis();
            monitorListener.onFrameFailed();
            AppLogger.d("onFrameFailed");
        } else if (dutation > NOTIFY_WINDOW && preStatus != isFrameLoading) {
            preStatus = isFrameLoading;
            lastNotifyTime = System.currentTimeMillis();
            monitorListener.onFrameRate(isFrameLoading);
            AppLogger.d("onFrameRate" + isFrameLoading);
        }
    }

    @Override
    public void stop() {
//        frameRateList.clear();
        preStatus = false;
        badFrameCount = 0;
    }

    /**
     * 非常粗糙的设计
     */
    private void startAnalyze() {
        if (monitorListener == null) return;
        boolean isFrameLoading = false;
        boolean isFrameFailed;
        int badCount = 0;
        synchronized (lock) {
            for (int i = 0; i < frameRateList.size() && i < FAILED_WINDOW; i++) {
                JFGMsgVideoRtcp rtcp = frameRateList.get(i);
                if (rtcp.frameRate <= BAD_LEVEL) {
                    badCount++;
                }
                if (i < LOADING_WINDOW && badCount == LOADING_TARGET) {
                    isFrameLoading = true;
                }
            }
        }
        isFrameFailed = badCount >= FAILED_TARGET;
        AppLogger.d("视频帧率分析结果, 是否加载失败:" + isFrameFailed + ",是否 Loading:" + isFrameLoading + ",badCount:" + badCount);
//        System.out.println("视频帧率分析结果, 是否加载失败:" + isFrameFailed + ",是否 Loading:" + isFrameLoading + ",badCount:" + badCount);

        if (isFrameLoading && isFrameFailed && System.currentTimeMillis() - lastNotifyTime > NOTIFY_WINDOW) {//加载失败了,4秒通知
            lastNotifyTime = System.currentTimeMillis();
            monitorListener.onFrameFailed();
            AppLogger.d("onFrameFailed");
        } else if (preStatus != isFrameLoading) {
            preStatus = isFrameLoading;
            monitorListener.onFrameRate(isFrameLoading);
            AppLogger.d("onFrameRate" + isFrameLoading);
        }
    }

    /**
     * @return
     */
    private boolean last3Bit(int weight) {
        int size = frameRateList.size();
        if (size > 2) {
            return frameRateList.get(size - 1).frameRate
                    + frameRateList.get(size - 2).frameRate
                    + frameRateList.get(size - 3).frameRate
                    <= weight;
        }
        return false;
    }

    /**
     * 这个list中,最后total个值<level的个数count.
     *
     * @param list
     * @param level :参考值
     * @param count :list中
     * @return
     */
    private boolean isBad(List<JFGMsgVideoRtcp> list, int level, int total, int count) {
        if (list == null || list.size() < count || list.size() < total)
            return false;
        final int size = list.size();
        int result = 0;
        for (int i = size - 1; i >= size - total; i--) {
            if (list.get(i).frameRate < level)//这组数,小于level的总个数>=count.
                result++;
        }
        return result >= count;
    }

    private MonitorListener monitorListener;

    public void setMonitorListener(MonitorListener monitorListener) {
        this.monitorListener = monitorListener;
    }


}
