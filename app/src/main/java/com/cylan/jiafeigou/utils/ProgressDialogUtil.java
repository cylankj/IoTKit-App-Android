package com.cylan.jiafeigou.utils;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import cylan.log.DswLog;

public class ProgressDialogUtil {

    private Context mContext;
    private Dialog mProgressDlg = null;
    private TextView text;


    public ProgressDialogUtil(Context ctx) {
        View mAlertDialogLayout = LayoutInflater.from(ctx).inflate(R.layout.loading_dialog, null);
        text = (TextView) mAlertDialogLayout.findViewById(R.id.text1);
        mProgressDlg = dialogBuilder(ctx, mAlertDialogLayout);
        this.mContext = ctx;
    }


    public boolean isShow() {
        return mProgressDlg.isShowing();
    }

    public void dismissDialog() {
        if (mProgressDlg.isShowing())
            mProgressDlg.dismiss();
    }

    public void showDialog(String msg) {
        try {
            text.setText(msg);
            mProgressDlg.show();
        } catch (Exception e) {
            DswLog.ex(e.toString());
        }
    }

    public void showDialog(int id) {
        showDialog(mContext.getString(id));
    }

    private Dialog dialogBuilder(Context context, View view) {
        Dialog mProgressDlg = new Dialog(context, R.style.Theme_Dialog);
        mProgressDlg.setContentView(view);
        mProgressDlg.setCanceledOnTouchOutside(false);
        mProgressDlg.setCancelable(true);
        return mProgressDlg;
    }


    public void setIsCanceledOnTouchOutside(boolean is) {
        mProgressDlg.setCanceledOnTouchOutside(is);
    }

    public void setIsCancelable(boolean is) {
        mProgressDlg.setCancelable(is);
    }
}
