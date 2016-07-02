package com.cylan.jiafeigou.utils;

import android.app.Activity;
import android.view.WindowManager;

/**
 * Created by cylan-hunt on 16-6-28.
 */

public class IMEUtils {
    public static void hide(Activity context) {
        if (context != null && context.getCurrentFocus() != null && context.getCurrentFocus().getWindowToken() != null) {
            context.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
//            InputMethodManager manager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
//            manager.hideSoftInputFromWindow(
//                    context.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}
