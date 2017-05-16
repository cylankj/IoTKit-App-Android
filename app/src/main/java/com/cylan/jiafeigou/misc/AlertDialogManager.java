package com.cylan.jiafeigou.misc;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by hds on 17-5-5.
 */

public class AlertDialogManager {

    private static AlertDialogManager instance;

    public static AlertDialogManager getInstance() {
        if (instance == null)
            synchronized (AlertDialogManager.class) {
                if (instance == null) instance = new AlertDialogManager();
            }
        return instance;
    }

    private Map<String, WeakReference<AlertDialog>> weakReferenceMap;

    public void showDialog(Activity activity, String tag, String message) {
        if (TextUtils.isEmpty(tag)) return;
        AlertDialog dialog = getAlertDialog(tag);
        if (activity == null || activity.isFinishing()) return;
        if (dialog != null && dialog.isShowing()) return;
        dismissOtherDialog();
        if (dialog == null) {
            dialog = new AlertDialog.Builder(activity)
                    .setMessage(message)
                    .create();
            if (weakReferenceMap == null)
                weakReferenceMap = new HashMap<>();
            weakReferenceMap.put(tag, new WeakReference<>(dialog));
        }
        dialog.show();
    }

    public void showDialog(Activity activity, String tag, String message, boolean canceltouchOutSide) {
        if (TextUtils.isEmpty(tag)) return;
        AlertDialog dialog = getAlertDialog(tag);
        if (activity == null || activity.isFinishing()) return;
        if (dialog != null && dialog.isShowing()) return;
        dismissOtherDialog();
        if (dialog == null) {
            dialog = new AlertDialog.Builder(activity)
                    .setMessage(message)
                    .create();
            if (weakReferenceMap == null)
                weakReferenceMap = new HashMap<>();
            weakReferenceMap.put(tag, new WeakReference<>(dialog));
        }
        dialog.setCanceledOnTouchOutside(canceltouchOutSide);
        dialog.show();
    }

    public void showDialog(Activity activity, String tag, String message, String ok, DialogInterface.OnClickListener okClickListener) {
        if (TextUtils.isEmpty(tag)) return;
        AlertDialog dialog = getAlertDialog(tag);
        if (activity == null || activity.isFinishing()) return;
        if (dialog != null && dialog.isShowing()) return;
        dismissOtherDialog();
        if (dialog == null) {
            dialog = new AlertDialog.Builder(activity)
                    .setMessage(message)
                    .setPositiveButton(ok, okClickListener)
                    .create();
            if (weakReferenceMap == null)
                weakReferenceMap = new HashMap<>();
            weakReferenceMap.put(tag, new WeakReference<>(dialog));
        }
        dialog.show();
    }

    public void showDialog(Activity activity, String tag, String message, String ok, DialogInterface.OnClickListener okClickListener, boolean mCancelable) {
        if (TextUtils.isEmpty(tag)) return;
        AlertDialog dialog = getAlertDialog(tag);
        if (activity == null || activity.isFinishing()) return;
        if (dialog != null && dialog.isShowing()) return;
        dismissOtherDialog();
        if (dialog == null) {
            dialog = new AlertDialog.Builder(activity)
                    .setMessage(message)
                    .setPositiveButton(ok, okClickListener)
                    .create();
            if (weakReferenceMap == null)
                weakReferenceMap = new HashMap<>();
            weakReferenceMap.put(tag, new WeakReference<>(dialog));
        }
        dialog.setCanceledOnTouchOutside(mCancelable);
        dialog.show();
    }

    public void showDialog(Activity activity, String tag, String message, String ok, DialogInterface.OnClickListener okClickListener, String cancel, DialogInterface.OnClickListener cancelClickListener) {
        if (TextUtils.isEmpty(tag)) return;
        AlertDialog dialog = getAlertDialog(tag);
        if (activity == null || activity.isFinishing()) return;
        if (dialog != null && dialog.isShowing()) return;
        dismissOtherDialog();
        if (dialog == null) {
            dialog = new AlertDialog.Builder(activity)
                    .setMessage(message)
                    .setPositiveButton(ok, okClickListener)
                    .setNegativeButton(cancel, cancelClickListener)
                    .create();
            if (weakReferenceMap == null)
                weakReferenceMap = new HashMap<>();
            weakReferenceMap.put(tag, new WeakReference<>(dialog));
        }
        dialog.show();
    }

    public void showDialog(Activity activity, String tag, String message, String ok, DialogInterface.OnClickListener okClickListener, String cancel, DialogInterface.OnClickListener cancelClickListener, boolean mCancelable) {
        if (TextUtils.isEmpty(tag)) return;
        AlertDialog dialog = getAlertDialog(tag);
        if (activity == null || activity.isFinishing()) return;
        if (dialog != null && dialog.isShowing()) return;
        dismissOtherDialog();
        if (dialog == null) {
            dialog = new AlertDialog.Builder(activity)
                    .setMessage(message)
                    .setPositiveButton(ok, okClickListener)
                    .setNegativeButton(cancel, cancelClickListener)
                    .create();
            if (weakReferenceMap == null)
                weakReferenceMap = new HashMap<>();
            weakReferenceMap.put(tag, new WeakReference<>(dialog));
        }
        dialog.setCanceledOnTouchOutside(mCancelable);
        dialog.show();
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
}
