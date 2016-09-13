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

    /**
     * 1：选中  0：默认
     */
    public boolean selected;

    @Override
    public int compareTo(BellCallRecordBean another) {
        if (another.timeInLong != this.timeInLong) {
            return (int) (this.timeInLong - another.timeInLong);
        } else return (int) (another.timeInLong - this.timeInLong);
    }
}
