package com.cylan.jiafeigou.misc;

import android.os.Bundle;

/**
 * Created by cylan-hunt on 16-7-6.
 */
public class RxEvent {

    public static class NeedLoginEvent {
        public Bundle bundle;

        public NeedLoginEvent(Bundle bundle) {
            this.bundle = bundle;
        }
    }
}
