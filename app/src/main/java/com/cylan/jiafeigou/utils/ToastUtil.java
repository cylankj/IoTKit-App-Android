package com.cylan.jiafeigou.utils;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.cylan.jiafeigou.R;

import java.lang.ref.SoftReference;

public class ToastUtil {

    private static SoftReference<TextView> toasterNormalView;
    private static SoftReference<TextView> toasterPosView;
    private static SoftReference<TextView> toasterNegView;

    public static void showToast(String content) {
        showToast(ContextUtils.getContext(), content, Gravity.CENTER, Toast.LENGTH_SHORT);
    }

    public static void showToast(String content, int gravity) {
        showToast(ContextUtils.getContext(), content, gravity, 2000);
    }

    private static void showToast(Context cxt, String content, int gravity, int duration) {
        try {
            TextView tv = toasterNormalView != null && toasterNormalView.get() != null ?
                    toasterNormalView.get() :
                    (TextView) View.inflate(cxt, R.layout.layout_toaster_normal, null);
            if (toasterNormalView == null) {
                toasterNormalView = new SoftReference<>(tv);
            }
            final Toast toast = new Toast(cxt);
            toast.setGravity(gravity, 0, 0);
            toast.setDuration(duration);
            tv.setText(content);
            toast.setView(tv);
            toast.show();
        } catch (Exception e) {
            Log.d("hunt", "err: " + e);
        }
    }

    public static void showPositiveToast(String content) {
        try {
            Context cxt = ContextUtils.getContext();
            TextView tv = toasterPosView != null && toasterPosView.get() != null ?
                    toasterPosView.get() :
                    (TextView) View.inflate(cxt, R.layout.layout_toaster_positive, null);
            if (toasterPosView == null) {
                toasterPosView = new SoftReference<>(tv);
            }
            final Toast toast = new Toast(cxt.getApplicationContext());
            toast.setView(tv);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.setDuration(Toast.LENGTH_SHORT);
            tv.setText(content);
            toast.show();
        } catch (Exception e) {
        }
    }

    public static void showNegativeToast(String content) {
        try {
            Context cxt = ContextUtils.getContext();
            TextView tv = toasterNegView != null && toasterNegView.get() != null ?
                    toasterNegView.get() :
                    (TextView) View.inflate(cxt, R.layout.layout_toaster_negative, null);
            if (toasterNegView == null) {
                toasterNegView = new SoftReference<>(tv);
            }
            final Toast toast = new Toast(cxt.getApplicationContext());
            toast.setView(tv);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.setDuration(Toast.LENGTH_SHORT);
            tv.setText(content);
            toast.show();
        } catch (Exception e) {

        }
    }

}
