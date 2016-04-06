package com.cylan.publicApi;

/**
 * 用于接收从camera的回调的接口
 * Created by lxh on 15-7-24.
 */
public interface IReportDataListener {
    void onReport(long now);
}
