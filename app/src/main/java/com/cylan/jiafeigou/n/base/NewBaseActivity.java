package com.cylan.jiafeigou.n.base;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.cylan.jiafeigou.n.mvp.ContextView;

public class NewBaseActivity extends FragmentActivity implements ContextView {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Context getContext() {
        return getApplicationContext();
    }
}
