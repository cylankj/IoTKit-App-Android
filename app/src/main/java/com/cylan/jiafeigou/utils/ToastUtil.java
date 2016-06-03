package com.cylan.jiafeigou.utils;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.cylan.jiafeigou.R;

public class ToastUtil {


    public static void showToast(Context cxt, String content) {
        showToast(cxt, content, Gravity.CENTER, Toast.LENGTH_SHORT);
    }

    public static void showToast(Context cxt, String content, int gravity) {
        showToast(cxt, content, gravity, 2000);
    }

    public static void showToast(Context cxt, String content, int gravity, int duration) {
        try {
            TextView tv = (TextView) View.inflate(cxt, R.layout.layout_toast_text, null);
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

    public static void showSuccessToast(Context cxt, String content) {
        try {
            TextView tv = (TextView) View.inflate(cxt, R.layout.layout_toast_text, null);
            final Toast toast = new Toast(cxt.getApplicationContext());
            toast.setView(tv);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.setDuration(Toast.LENGTH_SHORT);
            tv.setText(content);
//            tv.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_notify_result, 0, 0);
            toast.show();
        } catch (Exception e) {
        }
    }

    public static void showFailToast(Context cxt, String content) {
        try {
            TextView tv = (TextView) View.inflate(cxt, R.layout.layout_toast_text, null);
            final Toast toast = new Toast(cxt.getApplicationContext());
            toast.setView(tv);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.setDuration(Toast.LENGTH_SHORT);
            tv.setText(content);
//            tv.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_fail_notify_result, 0, 0);
            toast.show();
        } catch (Exception e) {
        }
    }

}
