package com.cylan.jiafeigou.misc.live;

import com.cylan.entity.jniCall.JFGMsgVideoRtcp;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by cylan-hunt on 17-2-21.
 */

public class LiveFrameRateMonitor implements IFeedRtcp {


    private static final int MAX_SIZE = 10;
    private static final int TARGET_SIZE = 8;
    private static final int FINAL_LEVEL = 2;
    private static final int SHOW_FREQUENCY = 3000;
    private boolean preStatus;
    private final ReentrantLock lock = new ReentrantLock();
    private long showFailedTime;
    /**
     * 10内的规则.
     */
    private List<JFGMsgVideoRtcp> frameRateList = new LinkedList<>();

    @Override
    public void feed(JFGMsgVideoRtcp rtcp) {
        synchronized (lock) {
            if (frameRateList.size() > MAX_SIZE - 1) {
                frameRateList.remove(0);
            }
            //默认加到末端
            frameRateList.add(rtcp);
            startAnalyze();
        }
    }

    @Override
    public void stop() {
        frameRateList.clear();
        preStatus = false;
    }

    /**
     * 非常粗糙的设计
     */
    private void startAnalyze() {

        synchronized (lock) {
            boolean isBad = false;
            //显示loading规则.
            if (frameRateList.size() >= 5) {
                isBad = isBad(frameRateList, 1, 5, 3) && last3Bit(1);
                if (preStatus != isBad) {//相反状态才需要通知
                    preStatus = isBad;
                    if (frameRateList != null)
                        monitorListener.onFrameRate(isBad);
                }
            }
            if (frameRateList.size() == MAX_SIZE) {
                //1.10s内的规则.
                boolean _10_s_rules = isBad(frameRateList, FINAL_LEVEL, MAX_SIZE, TARGET_SIZE);
                if (System.currentTimeMillis() - showFailedTime > SHOW_FREQUENCY) {
                    //3s内提醒一次
                    showFailedTime = System.currentTimeMillis();
                    if (monitorListener != null && isBad && _10_s_rules) {
                        monitorListener.onFrameFailed();
                    }
                }
            }
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
