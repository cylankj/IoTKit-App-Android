package com.cylan.jiafeigou.misc.bind;

/**
 * Created by hds on 17-6-9.
 */

public interface ISubmit<T extends SubmitResult> {

    T getSubmitResult();

    void startTask(String uuid);

    void cancelTask();

    void setTaskListener(SubmitListener listener);
}
