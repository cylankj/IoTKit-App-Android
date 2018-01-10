package com.cylan.jiafeigou.misc.live;

import com.cylan.entity.jniCall.JFGMsgVideoRtcp;

/**
 * Created by cylan-hunt on 17-2-21.
 */

public interface IFeedRtcp {
    /**
     * feed data
     *
     * @param rtcp
     */
    void feed(JFGMsgVideoRtcp rtcp);

    void stop();

    boolean isGoodFrameNow();

    boolean isStopped();

    void setMonitorListener(MonitorListener listener);

    interface MonitorListener {
        /**
         * 真的直播不成功了.
         */
        void onFrameFailed();

        /**
         * @param slow true :slow  false others
         */
        void onFrameRate(boolean slow);
    }

}
