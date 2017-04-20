package com.cylan.jiafeigou.base.module;

import android.support.v4.app.DialogFragment;
import android.view.View;

import com.cylan.jiafeigou.base.view.IDialogManager;

/**
 * Created by yanzhendong on 2017/4/20.
 */

public class BaseFragmentDialog extends DialogFragment implements IDialogManager.IDialogBuilder {
    @Override
    public IDialogManager.IDialogBuilder setTitle(String title) {
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
