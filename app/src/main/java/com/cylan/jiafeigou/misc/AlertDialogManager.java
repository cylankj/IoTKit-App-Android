package com.cylan.jiafeigou.misc;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

import com.cylan.jiafeigou.R;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by hds on 17-5-5.
 */

public class AlertDialogManager {

    private static AlertDialogManager instance;
    private Map<String, WeakReference<AlertDialog>> weakReferenceMap;

    public static AlertDialogManager getInstance() {
        if (instance == null)
            synchronized (AlertDialogManager.class) {
                if (instance == null) instance = new AlertDialogManager();
            }
        return instance;
    }

    /**
     * 全局使用同一个Theme
     * 其实没有必要这么做,只需要在AndroidManifest.xml#Application的appTheme#android:theme="@style/AppTheme"
     * <style name="AppTheme" parent="Theme.AppCompat.Light.NoActionBar">
     * <item name="android:alertDialogStyle">@style/CustomDialogTheme</item>
     * </style>
     *
     * @param activity
     * @return
     */
    public AlertDialog.Builder getCustomDialog(Activity activity) {
        return new AlertDialog.Builder(activity);
    }


    public void showDialog(String tag, Activity activity, AlertDialog.Builder builder) {
        if (TextUtils.isEmpty(tag)) return;
        AlertDialog dialog = getAlertDialog(tag);
        if (dialog != null && dialog.isShowing()) return;
        dismissOtherDialog();
        if (dialog == null) {
            dialog = builder.create();
            if (weakReferenceMap == null)
                weakReferenceMap = new HashMap<>();
            weakReferenceMap.put(tag, new WeakReference<>(dialog));
        }
        finalShow(activity, dialog);
    }

    public void showDialog(Activity activity, String tag, String message) {
        if (TextUtils.isEmpty(tag)) return;
        AlertDialog dialog = getAlertDialog(tag);
        if (activity == null || activity.isFinishing()) return;
        if (dialog != null && dialog.isShowing()) return;
        dismissOtherDialog();
//        if (dialog == null) {
        dialog = getCustomDialog(activity)
                .setMessage(message)
                .create();
        if (weakReferenceMap == null)
            weakReferenceMap = new HashMap<>();
        weakReferenceMap.put(tag, new WeakReference<>(dialog));
//        }
        finalShow(activity, dialog);
    }

    public void showDialog(Activity activity, String tag, String message, boolean canceltouchOutSide) {
        if (TextUtils.isEmpty(tag)) return;
        AlertDialog dialog = getAlertDialog(tag);
        if (activity == null || activity.isFinishing()) return;
        if (dialog != null && dialog.isShowing()) return;
        dismissOtherDialog();
//        if (dialog == null) {
        dialog = getCustomDialog(activity)
                .setMessage(message)
                .create();
        if (weakReferenceMap == null)
            weakReferenceMap = new HashMap<>();
        weakReferenceMap.put(tag, new WeakReference<>(dialog));
//        }
        dialog.setCanceledOnTouchOutside(canceltouchOutSide);
        dialog.setCancelable(canceltouchOutSide);
        finalShow(activity, dialog);
    }

    public void showDialog(Activity activity, String tag, String message, String ok, DialogInterface.OnClickListener okClickListener) {
        if (TextUtils.isEmpty(tag)) return;
        AlertDialog dialog = getAlertDialog(tag);
        if (activity == null || activity.isFinishing()) return;
        if (dialog != null && dialog.isShowing()) return;
        dismissOtherDialog();
//        if (dialog == null) {
        dialog = getCustomDialog(activity)
                .setMessage(message)
                .setPositiveButton(ok, okClickListener)
                .create();
        if (weakReferenceMap == null)
            weakReferenceMap = new HashMap<>();
        weakReferenceMap.put(tag, new WeakReference<>(dialog));
//        }
        finalShow(activity, dialog);
    }

    public void showDialog(Activity activity, String tag, String message, String ok, DialogInterface.OnClickListener okClickListener, boolean mCancelable) {
        if (TextUtils.isEmpty(tag)) return;
        AlertDialog dialog = getAlertDialog(tag);
        if (activity == null || activity.isFinishing()) return;
        if (dialog != null && dialog.isShowing()) return;
        dismissOtherDialog();
//        if (dialog == null) {
        dialog = getCustomDialog(activity)
                .setMessage(message)
                .setPositiveButton(ok, okClickListener)
                .create();
        if (weakReferenceMap == null)
            weakReferenceMap = new HashMap<>();
        weakReferenceMap.put(tag, new WeakReference<>(dialog));
//        }
        dialog.setCanceledOnTouchOutside(mCancelable);
        dialog.setCancelable(mCancelable);
        finalShow(activity, dialog);
    }

    public void showDialog(Activity activity, String tag, String message, String ok, DialogInterface.OnClickListener okClickListener, String cancel, DialogInterface.OnClickListener cancelClickListener) {
        if (TextUtils.isEmpty(tag)) return;
        AlertDialog dialog = getAlertDialog(tag);
        if (activity == null || activity.isFinishing()) return;
        if (dialog != null && dialog.isShowing()) return;
        dismissOtherDialog();
//        if (dialog == null) {
        dialog = getCustomDialog(activity)
                .setMessage(message)
                .setPositiveButton(ok, okClickListener)
                .setNegativeButton(cancel, cancelClickListener)
                .create();
        if (weakReferenceMap == null)
            weakReferenceMap = new HashMap<>();
        weakReferenceMap.put(tag, new WeakReference<>(dialog));
//        }
        finalShow(activity, dialog);
    }

    public void showDialog(Activity activity, String tag, String message, String ok, DialogInterface.OnClickListener okClickListener, String cancel, DialogInterface.OnClickListener cancelClickListener, boolean mCancelable) {
        if (TextUtils.isEmpty(tag)) return;
        AlertDialog dialog = getAlertDialog(tag);
        if (activity == null || activity.isFinishing()) return;
        if (dialog != null && dialog.isShowing()) return;
        dismissOtherDialog();
//        if (dialog == null) {
        dialog = getCustomDialog(activity)
                .setMessage(message)
                .setPositiveButton(ok, okClickListener)
                .setNegativeButton(cancel, cancelClickListener)
                .create();
        if (weakReferenceMap == null)
            weakReferenceMap = new HashMap<>();
        weakReferenceMap.put(tag, new WeakReference<>(dialog));
//        }
        dialog.setCanceledOnTouchOutside(mCancelable);
        dialog.setCancelable(mCancelable);
        finalShow(activity, dialog);

    }

    private AlertDialog getAlertDialog(String tag) {
        if (weakReferenceMap == null || weakReferenceMap.size() == 0) return null;
        Iterator<String> tagSet = weakReferenceMap.keySet().iterator();
        while (tagSet.hasNext()) {
            String t = tagSet.next();
            if (TextUtils.equals(t, tag)) {
                WeakReference<AlertDialog> ref = weakReferenceMap.get(tag);
                if (ref == null || ref.get() == null) return null;
                return ref.get();
            }
        }
        return null;
    }

    private void dismissOtherDialog() {
        if (weakReferenceMap == null || weakReferenceMap.size() == 0) return;
        Iterator<String> tagSet = weakReferenceMap.keySet().iterator();
        while (tagSet.hasNext()) {
            String t = tagSet.next();
            WeakReference<AlertDialog> ref = weakReferenceMap.get(t);
            if (ref == null || ref.get() == null) continue;
            if (ref.get().isShowing())
                ref.get().dismiss();
        }
    }

    public void dismissOtherDialog(String activityTag) {
        if (weakReferenceMap == null || weakReferenceMap.size() == 0) return;
        Iterator<String> tagSet = weakReferenceMap.keySet().iterator();
        while (tagSet.hasNext()) {
            String t = tagSet.next();
            WeakReference<AlertDialog> ref = weakReferenceMap.get(t);
            if (ref == null || ref.get() == null) continue;
            if (ref.get().isShowing()) {
                if (ref.get().getOwnerActivity() != null && TextUtils.equals(activityTag, ref.get().getOwnerActivity().getClass().getSimpleName()))
                    ref.get().dismiss();
            }
        }
    }

    private void finalShow(Activity activity, AlertDialog dialog) {
        try {
            if (activity != null && !activity.isFinishing()) {
                dialog.show();
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(dialog.getContext().getResources().getColor(R.color.color_4b9fd5));
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(dialog.getContext().getResources().getColor(R.color.color_4b9fd5));
            }
        } catch (Exception e) {

        }
    }

    /**
     * 简单配置DialogTheme
     */
    private static class CustomDialogBuilder extends AlertDialog.Builder {

        protected CustomDialogBuilder(@NonNull Context context, @StyleRes int themeResId) {
            super(context, themeResId);
        }
    }
}
