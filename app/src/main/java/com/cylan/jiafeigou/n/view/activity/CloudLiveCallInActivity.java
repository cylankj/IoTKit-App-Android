package com.cylan.jiafeigou.n.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.cloud.CloudLiveCallInContract;
import com.cylan.jiafeigou.n.mvp.impl.cloud.CloudLiveCallInPresenterImp;
import com.cylan.jiafeigou.n.view.cloud.CloudVideoChatCallOutFragment;
import com.cylan.jiafeigou.utils.ViewUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 作者：zsl
 * 创建时间：2016/10/18
 * 描述：
 */
public class CloudLiveCallInActivity extends AppCompatActivity implements CloudLiveCallInContract.View {

    @BindView(R.id.iv_call_user_image_head)
    ImageView ivCallUserImageHead;
    @BindView(R.id.ll_top_layout)
    LinearLayout llTopLayout;
    @BindView(R.id.tv_ignore_call)
    TextView tvIgnoreCall;
    @BindView(R.id.tv_accept_call)
    TextView tvAcceptCall;

    private CloudLiveCallInContract.Presenter presenter;

    private CloudVideoChatCallOutFragment cloudVideoChatConettionOkFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_cloud_live_videochat_connect);
        ButterKnife.bind(this);
        initPrestener();
    }

    private void initPrestener() {
        presenter = new CloudLiveCallInPresenterImp(this);
    }

    @OnClick({R.id.iv_call_user_image_head, R.id.tv_ignore_call, R.id.tv_accept_call})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_call_user_image_head:
                break;
            case R.id.tv_ignore_call:
                Intent backIntent = new Intent();
                backIntent.putExtra("ignore", getString(R.string.EFAMILY_MISSED_CALL));
                setResult(1, backIntent);
                finish();
                break;
            case R.id.tv_accept_call:
                ViewUtils.deBounceClick(findViewById(R.id.tv_accept_call));
                jump2VideoChatOkFragment();
                break;
        }
    }

    private void jump2VideoChatOkFragment() {
        cloudVideoChatConettionOkFragment = new CloudVideoChatCallOutFragment();
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(android.R.id.content, cloudVideoChatConettionOkFragment, "cloudVideoChatConettionOkFragment")
                .addToBackStack("cloudVideoChatConettionFragment")
                .commit();
    }

    @Override
    public void setPresenter(CloudLiveCallInContract.Presenter presenter) {

    }

    @Override
    public Context getContext() {
        return getApplicationContext();
    }
}
