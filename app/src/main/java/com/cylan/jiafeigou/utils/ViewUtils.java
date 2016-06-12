package com.cylan.jiafeigou.utils;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by cylan-hunt on 16-6-12.
 */

public class ViewUtils {
    // A method to find height of the status bar
    public static int getStatusBarHeight(Context context) {
        int result = 0;
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static int getCompatStatusBarHeight(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            final int height = getStatusBarHeight(context);
            Log.d("hunt", "hunt height: " + height);
            return height;
//            return getStatusBarHeight(context);
        } else return 0;
    }

    /**
     * @param v
     * @param l
     * @param t
     * @param r
     * @param b
     */
    public static void setMargins(View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(p.leftMargin + l, p.topMargin + t, p.rightMargin + r, p.bottomMargin + b);
            v.requestLayout();
        }
    }

    public static void setViewMarginStatusBar(View v) {
        final int height = getCompatStatusBarHeight(v.getContext());
        setMargins(v, 0, height, 0, 0);
    }
}
