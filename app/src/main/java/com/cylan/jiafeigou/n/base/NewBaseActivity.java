package com.cylan.jiafeigou.n.base;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.cylan.jiafeigou.n.view.BaseView;

public class NewBaseActivity extends FragmentActivity implements BaseView {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void initView() {

    }

    @Override
    public Context getContext() {
        return getApplicationContext();
    }
}
