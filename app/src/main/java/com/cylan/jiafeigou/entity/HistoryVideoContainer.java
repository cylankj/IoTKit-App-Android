package com.cylan.jiafeigou.entity;

import com.cylan.jiafeigou.entity.msg.MsgTimeData;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryVideoContainer {

    static SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);

    private String day;
    private List<MsgTimeData> mList;

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public List<MsgTimeData> getmList() {
        return mList;
    }

    public void setmList(List<MsgTimeData> mList) {
        this.mList = mList;
    }

    public static List<HistoryVideoContainer> parseJson(List<MsgTimeData> list) {

        Collections.sort(list);
        List<HistoryVideoContainer> mList = new ArrayList<>();
        int a = 0;
        for (int i = 0, count = list.size(); i < count; i++) {
            HistoryVideoContainer mVideo = new HistoryVideoContainer();
            mVideo.setDay(mSimpleDateFormat.format(new Date(list.get(i).begin * 1000)));
            if (!mList.contains(mVideo)) {
                List<MsgTimeData> mVideoList = new ArrayList<>();
                for (int j = a; j < count; j++) {
                    if (mVideo.getDay().equals(mSimpleDateFormat.format(new Date(list.get(j).begin * 1000)))) {
                        mVideoList.add(list.get(j));
                        a = j + 1;
                    } else {
                        break;
                    }
                }
                mVideo.setmList(mVideoList);
                mList.add(mVideo);
            }
        }
        return mList;

    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        return this.getDay().equals(((HistoryVideoContainer) o).getDay());
    }

    public HistoryVideoContainer(String day) {
        this.day = day;
    }

    public HistoryVideoContainer() {
    }
}
