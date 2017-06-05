package com.cylan.jiafeigou.support.network;

import android.content.Context;
import android.content.Intent;

/**
 * Created by hds on 17-6-5.
 */

public interface NetworkCallback {
    void onNetworkChanged(Context context, Intent intent);
}