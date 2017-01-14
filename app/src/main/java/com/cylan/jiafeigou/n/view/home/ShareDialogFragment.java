package com.cylan.jiafeigou.n.view.home;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.load.model.GlideUrl;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.utils.ShareUtils;
import com.cylan.jiafeigou.widget.dialog.BaseDialog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.tencent.mm.sdk.modelmsg.SendMessageToWX.Req.WXSceneSession;
import static com.tencent.mm.sdk.modelmsg.SendMessageToWX.Req.WXSceneTimeline;

/**
 * Created by cylan-hunt on 16-7-26.
 */
public class ShareDialogFragment extends BaseDialog {

    public static final String KEY_MEDIA_CONTENT = "key_media_content";

    @BindView(R.id.lLayout_dialog_share_wonderful)
    CardView lLayoutDialogShareWonderful;
    @BindView(R.id.tv_share_to_timeline)
    TextView tvShareToFriends;
    @BindView(R.id.tv_share_to_wechat_friends)
    TextView tvShareToWechat;

//    private DPWonderItem mMediaBean;

    private GlideUrl glideUrl;

    public static ShareDialogFragment newInstance(Bundle bundle) {
        ShareDialogFragment fragment = new ShareDialogFragment();
        if (bundle != null) fragment.setArguments(bundle);
        return fragment;
    }

    public static ShareDialogFragment newInstance() {
        return new ShareDialogFragment();
    }

    public void setGlideUrl(GlideUrl glideUrl) {
        this.glideUrl = glideUrl;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_dialog_share_wonderful, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @OnClick({R.id.tv_share_to_timeline, R.id.tv_share_to_wechat_friends})
    public void onClick(View view) {
        dismiss();
        switch (view.getId()) {
            case R.id.tv_share_to_timeline:
                ShareUtils.shareToWechat(getActivity(), glideUrl, WXSceneTimeline);
                break;
            case R.id.tv_share_to_wechat_friends:
                ShareUtils.shareToWechat(getActivity(), glideUrl, WXSceneSession);
                break;
        }
    }
}
