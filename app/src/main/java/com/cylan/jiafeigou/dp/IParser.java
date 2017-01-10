package com.cylan.jiafeigou.dp;

import rx.Subscription;

/**
 * Created by cylan-hunt on 16-11-8.
 */
public interface IParser {

    Subscription[] register();

    void clear();
}

