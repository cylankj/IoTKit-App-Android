package com.cylan.jiafeigou.dp;

import rx.Subscription;

/**
 * Created by cylan-hunt on 16-11-16.
 */

public interface IParser {

    Subscription[] register();

    void unregister();
}
