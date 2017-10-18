package com.cylan.jiafeigou.n.view.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.ContextView;

public abstract class NewBaseActivity extends FragmentActivity implements ContextView {


    TextView tvBaseTitleBack;
    TextView tvBaseTitleTitle;
    TextView tvBaseTitleDone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_new_base_activity);
        tvBaseTitleBack = (TextView) findViewById(R.id.tv_base_title_back);
        tvBaseTitleTitle = (TextView) findViewById(R.id.tv_base_title_title);
        tvBaseTitleDone = (TextView) findViewById(R.id.tv_base_title_done);
        //是一个FrameLayout
        addContentView((ViewGroup) findViewById(R.id.fLayout_base_content));
    }

    protected abstract void addContentView(ViewGroup viewGroup);

    @Override
    public Context getContext() {
        return getApplicationContext();
    }

    protected void setBackVisibility(int visibility, View.OnClickListener listener) {
        tvBaseTitleBack.setVisibility(visibility);
        if (listener == null || visibility == View.GONE) {
            return;
        }
        tvBaseTitleBack.setOnClickListener(listener);
    }

    protected void setTitleVisibility(int visibility, View.OnClickListener listener) {
        tvBaseTitleTitle.setVisibility(visibility);
        if (listener == null || visibility == View.GONE) {
            return;
        }
        tvBaseTitleTitle.setOnClickListener(listener);
    }

    protected void setDoneVisibility(int visibility, View.OnClickListener listener) {
        tvBaseTitleDone.setVisibility(visibility);
        if (listener == null || visibility == View.GONE) {
            return;
        }
        tvBaseTitleDone.setOnClickListener(listener);
    }
}
