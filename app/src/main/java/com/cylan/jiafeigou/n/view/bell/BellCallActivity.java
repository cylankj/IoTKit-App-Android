package com.cylan.jiafeigou.n.view.bell;

import android.os.Bundle;
import android.widget.Button;

import com.cylan.jiafeigou.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BellCallActivity extends ProcessActivity {

    @BindView(R.id.btn_back)
    Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bell_call);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        ButterKnife.bind(this);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @OnClick(R.id.btn_back)
    public void onClick() {
        finishExt();
    }

    @Override
    public void onBackPressed() {
        onClick();
    }
}
