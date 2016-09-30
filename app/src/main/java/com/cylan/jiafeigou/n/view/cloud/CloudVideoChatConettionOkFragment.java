package com.cylan.jiafeigou.n.view.cloud;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.cloud.CloudVideoChatConettionOkContract;
import com.cylan.jiafeigou.n.mvp.impl.cloud.CloudVideoChatConettionOkPresenterImp;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 作者：zsl
 * 创建时间：2016/9/26
 * 描述：
 */
public class CloudVideoChatConettionOkFragment extends Fragment implements CloudVideoChatConettionOkContract.View {

    @BindView(R.id.vv_chatvideo_from)
    VideoView vvChatvideoFrom;
    @BindView(R.id.tv_loading)
    TextView tvLoading;
    @BindView(R.id.tv_connet_text)
    TextView tvConnetText;
    @BindView(R.id.tv_video_time)
    TextView tvVideoTime;
    @BindView(R.id.ll_myself_video)
    LinearLayout llMyselfVideo;

    private CloudVideoChatConettionOkContract.Presenter presenter;

    public static CloudVideoChatConettionOkFragment newInstance(Bundle bundle) {
        CloudVideoChatConettionOkFragment fragment = new CloudVideoChatConettionOkFragment();
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
        presenter = new CloudVideoChatConettionOkPresenterImp(this);
    }

    @Override
    public void setPresenter(CloudVideoChatConettionOkContract.Presenter presenter) {

    }

    @Override
    public void showLoadingView() {
        tvLoading.setVisibility(View.VISIBLE);
        tvConnetText.setVisibility(View.VISIBLE);
        tvVideoTime.setVisibility(View.INVISIBLE);
        llMyselfVideo.setVisibility(View.INVISIBLE);
    }

    @Override
    public void hideLoadingView() {
        tvLoading.setVisibility(View.INVISIBLE);
        tvConnetText.setVisibility(View.INVISIBLE);
        tvVideoTime.setVisibility(View.VISIBLE);
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
    public void onDestroy() {
        super.onDestroy();
        if (presenter != null) {
            presenter.stop();
        }
    }
}
