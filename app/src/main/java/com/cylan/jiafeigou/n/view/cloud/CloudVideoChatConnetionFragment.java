package com.cylan.jiafeigou.n.view.cloud;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.cloud.CloudVideoChatConettionContract;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 作者：zsl
 * 创建时间：2016/9/26
 * 描述：
 */
public class CloudVideoChatConnetionFragment extends Fragment implements CloudVideoChatConettionContract.View {

    @BindView(R.id.iv_call_user_image_head)
    ImageView ivCallUserImageHead;
    @BindView(R.id.ll_top_layout)
    LinearLayout llTopLayout;
    @BindView(R.id.tv_ignore_call)
    TextView tvIgnoreCall;
    @BindView(R.id.tv_accept_call)
    TextView tvAcceptCall;

    private CloudVideoChatConettionOkFragment cloudVideoChatConettionOkFragment;

    public static CloudVideoChatConnetionFragment newInstance(Bundle bundle) {
        CloudVideoChatConnetionFragment fragment = new CloudVideoChatConnetionFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cloudVideoChatConettionOkFragment = cloudVideoChatConettionOkFragment.newInstance(new Bundle());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cloud_live_videochat_connetion, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void setPresenter(CloudVideoChatConettionContract.Presenter presenter) {

    }

    @OnClick({R.id.iv_call_user_image_head, R.id.tv_ignore_call, R.id.tv_accept_call})
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.iv_call_user_image_head:
                break;
            case R.id.tv_ignore_call:
                ToastUtil.showToast(getContext(),"取消连接请求。。。");
                break;
            case R.id.tv_accept_call:
                if (getView() != null)
                    ViewUtils.deBounceClick(getView().findViewById(R.id.tv_accept_call));
                AppLogger.e("tv_accept_call");
                jump2VideoChatOkFragment();
                break;
        }
    }

    private void jump2VideoChatOkFragment() {
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(android.R.id.content, cloudVideoChatConettionOkFragment, "cloudVideoChatConettionOkFragment")
                .addToBackStack("cloudVideoChatConettionFragment")
                .commit();
    }
}
