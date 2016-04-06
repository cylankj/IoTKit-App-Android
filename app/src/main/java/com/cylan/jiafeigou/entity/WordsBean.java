package com.cylan.jiafeigou.entity;

import com.cylan.jiafeigou.entity.msg.EfamilyVoicemsgData;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by HeBin on 2015/2/27.
 */
public class WordsBean implements Comparable<WordsBean>, Serializable {

    public static final int HAS_SEND = 0x00;
    public static final int SENDING = 0x01;
    public static final int HAS_READ = 0x02;
    public static final int SEND_FAIL = 0x03;
    public static final int SOUND_DOWNLOAD = 0x04;
    /**
     * 0：已发送  1：发送中   2：已读  3：发送失败 *
     */
    private boolean isPlay;
    private int sendState;
    private int isRead;
    private long timeBegin;
    private int timeDuration;
    private String url;
    private String path;
    private int progress;

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isPlay() {
        return isPlay;
    }

    public void setPlay(boolean isPlay) {
        this.isPlay = isPlay;
    }

    public int getSendState() {
        return isRead != 0 ? HAS_READ : sendState;
    }

    public void setSendState(int sendState) {
        this.sendState = sendState;
    }

    public int getIsRead() {
        return isRead;
    }

    public void setIsRead(int isRead) {
        this.isRead = isRead;
    }

    public long getTimeBegin() {
        return timeBegin;
    }

    public void setTimeBegin(long timeBegin) {
        this.timeBegin = timeBegin;
    }

    public int getTimeDuration() {
        return timeDuration;
    }

    public void setTimeDuration(int timeDuration) {
        this.timeDuration = timeDuration;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


    public static List<WordsBean> parseMsgpack(List<EfamilyVoicemsgData> array) {
        List<WordsBean> list = null;
        list = new ArrayList<WordsBean>();
        for (int i = 0; i < array.size(); i++) {
            EfamilyVoicemsgData object = array.get(i);
            WordsBean bean = new WordsBean();
            bean.setIsRead(object.isRead);
            bean.setTimeBegin(object.timeBegin);
            bean.setTimeDuration(object.timeDuration);
            bean.setUrl(object.url);
            list.add(bean);
        }
        return list;
    }

    @Override
    public int compareTo(WordsBean another) {
        return this.getTimeBegin() < another.getTimeBegin() ? -1 : 1;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        return this.getTimeBegin() == ((WordsBean) o).getTimeBegin();
    }
}
