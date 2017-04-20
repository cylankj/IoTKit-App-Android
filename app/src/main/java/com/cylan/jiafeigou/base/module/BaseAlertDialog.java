package com.cylan.jiafeigou.base.module;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.cylan.jiafeigou.base.view.IDialogManager;

/**
 * Created by yanzhendong on 2017/4/20.
 */

public class BaseAlertDialog extends AlertDialog implements IDialogManager.IDialogBuilder {


    protected BaseAlertDialog(@NonNull Context context) {
        super(context);

    }

    @Override
    public IDialogManager.IDialogBuilder setTitle(String title) {
        setTitle(title);
        return this;
    }

    @Override
    public IDialogManager.IDialogBuilder setContent(String content) {
        return this;
    }


    @Override
    public IDialogManager.IDialogBuilder setCustomContentView(View contentView) {
        return this;
    }

    @Override
    public IDialogManager.IDialogBuilder setCustomContentView(int resId) {

        return this;
    }

    @Override
    public IDialogManager.IDialogBuilder setClickListener(int resId, View.OnClickListener listener) {

        return this;
    }

    @Override
    public IDialogManager.IDialogBuilder addParam(String key, Object value) {

        return this;
    }
}
