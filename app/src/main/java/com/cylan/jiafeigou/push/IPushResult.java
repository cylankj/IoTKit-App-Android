package com.cylan.jiafeigou.push;

import android.os.Bundle;

/**
 * 可能接入很多推送.
 * Created by hds on 17-4-21.
 */

public interface IPushResult {

    void onMessageReceived(Object object, Bundle bundle);
}
