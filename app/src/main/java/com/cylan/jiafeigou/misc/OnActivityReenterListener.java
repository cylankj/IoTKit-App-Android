package com.cylan.jiafeigou.misc;

import android.content.Intent;

/**
 * Created by cylan-hunt on 16-9-7.
 */
public interface OnActivityReenterListener {
    void onActivityReenter(int requestCode, Intent data);
}
