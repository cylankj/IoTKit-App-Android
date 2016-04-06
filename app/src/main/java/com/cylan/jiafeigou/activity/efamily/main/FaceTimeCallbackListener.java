package com.cylan.jiafeigou.activity.efamily.main;

/**
 * Created by cylan on 2016/3/21.
 */
public interface FaceTimeCallbackListener {

    void missCallByOverTime(boolean isCalled, String cid);

    void missCallByCancel(boolean isCalled, String cid);

    void haveAnswered(boolean isCalled, int timeDuration, String cid);
}
