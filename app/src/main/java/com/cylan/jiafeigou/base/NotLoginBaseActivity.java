package com.cylan.jiafeigou.base;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;

public abstract class NotLoginBaseActivity extends RootActivity {

    RelativeLayout rootLayout;

    private ImageView mBackView;
    private TextView mTitleView;


    @Override
    public void setContentView(int layoutResID) {
        LayoutInflater inflater = this.getLayoutInflater();
        rootLayout = (RelativeLayout) inflater.inflate(R.layout.auto_baseview, null);
        LinearLayout mContainerView = (LinearLayout) rootLayout.findViewById(R.id.StandardView_Abstract);
        View v = inflater.inflate(layoutResID, null);
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        params.setMargins(0, 0, 0, 0);
        mContainerView.addView(v, params);
        super.setContentView(rootLayout);
        initItems();
        initView();
        initData();
    }

    private void initItems() {
        mBackView = (ImageView) rootLayout.findViewById(R.id.ico_back);
        mBackView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mTitleView = (TextView) rootLayout.findViewById(R.id.title);
    }


    protected void setTitle(String str) {
        mTitleView.setText(str);
    }

    protected void setBackBtnOnClickListener(OnClickListener ocl) {
        mBackView.setOnClickListener(ocl);
    }

    protected void setBackBtnResourse(int res) {
        mBackView.setImageResource(res);
    }

    protected abstract void initView();

    protected abstract void initData();


}
