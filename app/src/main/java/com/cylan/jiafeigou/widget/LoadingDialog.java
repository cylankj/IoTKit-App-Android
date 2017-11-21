package com.cylan.jiafeigou.widget;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatDialog;
import android.widget.TextView;

import com.cylan.jiafeigou.R;

/**
 * Created by cylan-hunt on 16-10-27.
 */

public class LoadingDialog extends AppCompatDialog {
    private static final String TAG = LoadingDialog.class.getSimpleName();
    private DialogInterface.OnCancelListener listener;
    private static LoadingDialog loadingDialog;
    TextView message;

    public LoadingDialog(Context context) {
        super(context, R.style.dialog);
        setContentView(R.layout.layout_loading_normal);
        message = (TextView) findViewById(R.id.tv_loading_content);
    }

    @Override
    public void cancel() {
        super.cancel();
        if (listener != null) {
            listener.onCancel(this);
        }
        if (loadingDialog == this) {
            loadingDialog = null;
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (loadingDialog == this) {
            loadingDialog = null;
        }
    }


    public void setMessageText(String content) {
        if (message != null) {
            message.setText(content);
        }
    }


    public static LoadingDialog getLoadingDialog(Context context) {
        final LoadingDialog dialog = loadingDialog;
        loadingDialog = null;
        if (dialog != null) {
            dialog.dismiss();
        }
        loadingDialog = new LoadingDialog(context);
        return loadingDialog;
    }


    public static void showLoading(Context context, String content, boolean dismissTouchOutside, DialogInterface.OnCancelListener listener) {
        LoadingDialog dialog = getLoadingDialog(context);
        dialog.setMessageText(content);
        dialog.setCanceledOnTouchOutside(dismissTouchOutside);
        dialog.setOnCancelListener(listener);
        dialog.show();
    }

    public static void showLoading(Context context, String content, boolean dismissTouchOutside, DialogInterface.OnCancelListener listener, OnShowListener showListener) {
        LoadingDialog dialog = getLoadingDialog(context);
        dialog.setMessageText(content);
        dialog.setCanceledOnTouchOutside(dismissTouchOutside);
        dialog.setOnCancelListener(listener);
        dialog.setOnShowListener(showListener);
        dialog.show();
    }

    public static boolean isShowLoading() {
        return loadingDialog != null && loadingDialog.isShowing();
    }


    public static void showLoading(Context context, String content, boolean cancelable) {
        showLoading(context, content, cancelable, null);
    }


    public static void showLoading(Context context) {
        showLoading(context, "", true, null);
    }

    public static void dismissLoading() {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
            loadingDialog = null;
        }
    }

    public static void clearListener() {
        if (loadingDialog != null) {
            loadingDialog.setOnCancelListener(null);
        }
    }

}
