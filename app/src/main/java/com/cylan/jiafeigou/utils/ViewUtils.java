package com.cylan.jiafeigou.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.text.InputFilter;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by cylan-hunt on 16-6-12.
 */

public class ViewUtils {

    private static int height;

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && height == 0) {
            height = getStatusBarHeight(context);
            return height;
//            return getStatusBarHeight(context);
        } else return height;
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

    public static void setViewPaddingStatusBar(View v) {
        final int height = getCompatStatusBarHeight(v.getContext());
        v.setPadding(v.getPaddingLeft(), v.getPaddingTop() + height, v.getPaddingRight(), v.getPaddingBottom());
    }

    public static void showPwd(EditText text, boolean show) {
        text.setTransformationMethod(show ?
                HideReturnsTransformationMethod.getInstance()
                : PasswordTransformationMethod.getInstance());
    }

    /**
     * 重新获取焦点，显示游标。
     *
     * @param editText
     * @param enable
     */
    public static void enableEditTextCursor(EditText editText, boolean enable) {
        editText.setFocusable(enable);
        editText.setFocusableInTouchMode(enable);
    }

    public static void setTextViewMaxFilter(final TextView textView, final int maxLen) {
        InputFilter[] filterArray = new InputFilter[1];
        filterArray[0] = new InputFilter.LengthFilter(maxLen);
        textView.setFilters(filterArray);
    }

    public static String getTextViewContent(TextView textView) {
        if (textView != null) {
            final CharSequence text = textView.getText();
            return text != null ? text.toString().trim() : "";
        }
        return "";
    }

    public static void deBounceClick(final View view) {
        if (view == null)
            return;
        view.setEnabled(false);
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (view != null)
                    view.setEnabled(true);
            }
        }, 1000);
    }

    public static void updateViewHeight(View view, float ratio) {
        final int width = Resources.getSystem().getDisplayMetrics().widthPixels;
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        lp.height = (int) (width * ratio);
        view.setLayoutParams(lp);
    }

    public static void updateViewMatchScreenHeight(View view) {
        final int height = Resources.getSystem().getDisplayMetrics().heightPixels;
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        lp.height = height;
        view.setLayoutParams(lp);
    }


    public static void setRequestedOrientation(Activity activity, int orientation) {
        activity.setRequestedOrientation(orientation);
    }
}

