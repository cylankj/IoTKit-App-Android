package com.cylan.jiafeigou.activity.efamily.main;

/**
 *
 * Created by yangc on 2016/3/11.
 */
public interface ClientCallEFamlStatusListener {

    void missCallByOverTime();

    void missCallByCancel();

    void haveAnswered(int timeDuration);
}
