package com.cylan.jiafeigou.base.module;

import android.view.View;

import com.cylan.jiafeigou.base.view.IDialogManager;

/**
 * Created by yanzhendong on 2017/4/19.
 */

public class BaseDialogBuilder implements IDialogManager.IDialogBuilder {
    @Override
    public IDialogManager.IDialogBuilder setTitle(String title) {
        return null;
    }

    @Override
    public IDialogManager.IDialogBuilder setContent(String content) {
        return null;
    }

    @Override
    public IDialogManager.IDialogBuilder setCustomContentView(View contentView) {
        return null;
    }

    @Override
    public IDialogManager.IDialogBuilder setClickListener(int resId, View.OnClickListener listener) {
        return null;
    }

    @Override
    public IDialogManager.IDialogBuilder addParam(String key, Object value) {
        return null;
    }
}
