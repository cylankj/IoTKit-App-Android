package com.cylan.jiafeigou.n.view.activity;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.RxEvent;
import com.cylan.jiafeigou.n.mvp.contract.cloud.CloudVideoChatConettionOkContract;
import com.cylan.jiafeigou.n.mvp.impl.cloud.CloudVideoChatConettionOkPresenterImp;
import com.cylan.jiafeigou.support.rxbus.RxBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 作者：zsl
 * 创建时间：2016/10/13
 * 描述：
 */
public class CloudLiveReturnCallActivity extends AppCompatActivity implements CloudVideoChatConettionOkContract.View {

    @BindView(R.id.vv_chatvideo_from)
    VideoView vvChatvideoFrom;
    @BindView(R.id.tv_video_time)
    Chronometer tvVideoTime;
    @BindView(R.id.tv_connet_text)
    TextView tvConnetText;
    @BindView(R.id.tv_loading)
    TextView tvLoading;
    @BindView(R.id.iv_hang_up)
    ImageView ivHangUp;
    @BindView(R.id.ll_myself_video)
    LinearLayout llMyselfVideo;


    private CloudVideoChatConettionOkContract.Presenter presenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_cloud_live_videochat);
        ButterKnife.bind(this);
        initPresenter();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (presenter != null) {
            presenter.start();
        }
    }

    private void initPresenter() {
        presenter = new CloudVideoChatConettionOkPresenterImp(this);
    }

    @Override
    public void showLoadingView() {
        tvLoading.setVisibility(View.VISIBLE);
        tvConnetText.setVisibility(View.VISIBLE);
        tvVideoTime.setVisibility(View.INVISIBLE);
        tvVideoTime.stop();
        llMyselfVideo.setVisibility(View.INVISIBLE);
    }

    @Override
    public void hideLoadingView() {
        tvLoading.setVisibility(View.INVISIBLE);
        tvConnetText.setVisibility(View.INVISIBLE);
        tvVideoTime.setVisibility(View.VISIBLE);
        tvVideoTime.setBase(SystemClock.elapsedRealtime());
        tvVideoTime.start();
        llMyselfVideo.setVisibility(View.VISIBLE);
    }

    @Override
    public void setLoadingText(String text) {
        tvLoading.setText(text);
    }

    @Override
    public void showLoadResult() {
        tvConnetText.setVisibility(View.INVISIBLE);
        tvLoading.setText("连接失败");
    }

    @Override
    public void setPresenter(CloudVideoChatConettionOkContract.Presenter presenter) {

    }

    @Override
    public Context getContext() {
        return getApplicationContext();
    }

    @OnClick(R.id.iv_hang_up)
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_hang_up:
                RxBus.getCacheInstance().post(new RxEvent.HangUpVideoTalk(true,tvVideoTime.getText().toString().trim()));
                finish();
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (presenter != null) {
            presenter.stop();
        }
    }
}
