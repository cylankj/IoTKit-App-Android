package com.cylan.jiafeigou.n.view.cloud;

import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.cloud.CloudVideoChatConnectOkContract;
import com.cylan.jiafeigou.n.mvp.impl.cloud.CloudVideoChatCallOutPresenterImp;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 作者：zsl
 * 创建时间：2016/9/26
 * 描述：
 */
public class CloudVideoChatCallOutFragment extends Fragment implements CloudVideoChatConnectOkContract.View {

    @BindView(R.id.vv_chatvideo_from)
    VideoView vvChatvideoFrom;
    @BindView(R.id.tv_loading)
    TextView tvLoading;
    @BindView(R.id.tv_connet_text)
    TextView tvConnetText;
    @BindView(R.id.tv_video_time)
    Chronometer tvVideoTime;
    @BindView(R.id.ll_myself_video)
    LinearLayout llMyselfVideo;
    @BindView(R.id.iv_hang_up)
    ImageView ivHangUp;

    private CloudVideoChatConnectOkContract.Presenter presenter;

    public OnHangUpListener listener;

    public interface OnHangUpListener {
        void onHangup(String time);
    }

    public void setOnHangupListener(OnHangUpListener listener) {
        this.listener = listener;
    }

    public static CloudVideoChatCallOutFragment newInstance(Bundle bundle) {
        CloudVideoChatCallOutFragment fragment = new CloudVideoChatCallOutFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cloud_live_videochat, container, false);
        ButterKnife.bind(this, view);
        initPresenter();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (presenter != null) presenter.start();
    }

    private void initPresenter() {
        presenter = new CloudVideoChatCallOutPresenterImp(this);
    }

    @Override
    public void setPresenter(CloudVideoChatConnectOkContract.Presenter presenter) {

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
        tvLoading.setText(getString(R.string.WIFI_ERR_INFO));
    }

    @Override
    public void onStop() {
        super.onStop();
        if (presenter != null) {
            presenter.stop();
        }
    }

    @OnClick(R.id.iv_hang_up)
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_hang_up:               //挂断
                RxBus.getCacheInstance().post(new RxEvent.HangUpVideoTalk(true, tvVideoTime.getText().toString().trim()));
                getFragmentManager().popBackStack();
                break;
        }
    }
}
