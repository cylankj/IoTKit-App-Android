package com.cylan.jiafeigou.n.mvp.model;

/**
 * Created by cylan-hunt on 16-8-3.
 */
public class BellCallRecordBean implements Comparable<BellCallRecordBean> {
    public String url;
    //应该隐藏
    public String date, timeStr;
    public long timeInLong;
    public int answerState;

    @Override
    public int compareTo(BellCallRecordBean another) {
        return (int) (timeInLong - another.timeInLong);
    }
}
