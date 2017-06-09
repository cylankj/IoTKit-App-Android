package com.cylan.jiafeigou.misc.bind;

/**
 * Created by hds on 17-6-9.
 */

public interface SubmitListener {
    void onSubmitStart();

    void onSubmitErr(int errCode);

    void onSubmitProgress(int progress);

    void onSubmitSuccess();
}
