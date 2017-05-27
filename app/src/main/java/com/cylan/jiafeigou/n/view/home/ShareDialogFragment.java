package com.cylan.jiafeigou.n.view.home;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.load.model.GlideUrl;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.utils.ShareUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
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
    @BindView(R.id.tv_share_to_tencent_qq)
    TextView tvShareToQQ;
    @BindView(R.id.tv_share_to_tencent_qzone)
    TextView tvShareToQZnoe;
    @BindView(R.id.tv_share_to_sina_weibo)
    TextView tvShareToWeibo;

    private GlideUrl glideUrl;
    private String mVideoURL;

    public static ShareDialogFragment newInstance(Bundle bundle) {
        ShareDialogFragment fragment = new ShareDialogFragment();
        if (bundle != null) fragment.setArguments(bundle);
        return fragment;
    }

    public static ShareDialogFragment newInstance() {
        return new ShareDialogFragment();
    }

    public void setPictureURL(GlideUrl glideUrl) {
        this.glideUrl = glideUrl;
    }

    public void setVideoURL(String url) {
        this.mVideoURL = url;
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

    @OnClick({R.id.tv_share_to_timeline,
            R.id.tv_share_to_wechat_friends,
            R.id.tv_share_to_facebook_friends,
            R.id.tv_share_to_tencent_qq,
            R.id.tv_share_to_tencent_qzone,
            R.id.tv_share_to_sina_weibo,
            R.id.tv_share_to_twitter_friends})
    public void onClick(View view) {
        dismiss();
        switch (view.getId()) {
            case R.id.tv_share_to_timeline:
                if (!ShareUtils.isWechatInstalled()) {
                    ToastUtil.showToast(getString(R.string.Tap2_share_unabletoshare));
                    return;
                }
                if (glideUrl != null && !TextUtils.isEmpty(mVideoURL)) {
                    ShareUtils.shareVideoToWechat(getActivity(), mVideoURL, WXSceneTimeline, glideUrl);
                } else if (glideUrl != null) {
                    ShareUtils.sharePictureToWechat(getActivity(), glideUrl, WXSceneTimeline);
                } else {
                    ToastUtil.showNegativeToast(getString(R.string.Tap3_ShareDevice_FailTips));
                }
                break;
            case R.id.tv_share_to_wechat_friends:
                if (!ShareUtils.isWechatInstalled()) {
                    ToastUtil.showToast(getString(R.string.Tap2_share_unabletoshare));
                    return;
                }
                if (glideUrl != null && !TextUtils.isEmpty(mVideoURL)) {
                    ShareUtils.shareVideoToWechat(getActivity(), mVideoURL, WXSceneSession, glideUrl);
                } else if (glideUrl != null) {
                    ShareUtils.sharePictureToWechat(getActivity(), glideUrl, WXSceneSession);
                } else {
                    ToastUtil.showNegativeToast(getString(R.string.Tap3_ShareDevice_FailTips));
                }
                break;
            case R.id.tv_share_to_twitter_friends:
                if (!ShareUtils.isTwitterInstalled()) {
                    ToastUtil.showNegativeToast(getString(R.string.Tap0_Login_NoInstalled, "Twitter"));
                    return;
                }
                if (glideUrl != null && !TextUtils.isEmpty(mVideoURL)) {
                    ShareUtils.shareVideoToTwitter(getActivity(), mVideoURL, glideUrl);
                } else if (glideUrl != null) {
                    ShareUtils.sharePictureToTwitter(getActivity(), glideUrl);
                } else {
                    ToastUtil.showNegativeToast(getString(R.string.Tap3_ShareDevice_FailTips));
                }
                break;
            case R.id.tv_share_to_facebook_friends:
                if (!ShareUtils.isFacebookInstalled()) {
                    ToastUtil.showNegativeToast(getString(R.string.Tap0_Login_NoInstalled, "facebook"));
                    return;
                }
                if (glideUrl != null && !TextUtils.isEmpty(mVideoURL)) {
                    ShareUtils.shareVideoToFacebook(getActivity(), mVideoURL, glideUrl);
                } else if (glideUrl != null) {
                    ShareUtils.shareToFacebook(getActivity(), glideUrl);
                } else {
                    ToastUtil.showNegativeToast(getString(R.string.Tap3_ShareDevice_FailTips));
                }
                break;
            case R.id.tv_share_to_tencent_qq:
                if (!TextUtils.isEmpty(mVideoURL)) {
                    ShareUtils.shareLinkToQQ(getActivity(), mVideoURL);
                } else if (glideUrl != null) {
                    ShareUtils.sharePictureToQQ(getActivity(), glideUrl);
                } else {
                    ToastUtil.showNegativeToast(getString(R.string.Tap3_ShareDevice_FailTips));
                }
                break;
            case R.id.tv_share_to_tencent_qzone:
                if (!TextUtils.isEmpty(mVideoURL)) {
                    ShareUtils.shareVideoToQZone(getActivity());
                } else if (glideUrl != null) {
                    ShareUtils.shareToFacebook(getActivity(), glideUrl);
                } else {
                    ToastUtil.showNegativeToast(getString(R.string.Tap3_ShareDevice_FailTips));
                }
                break;
            case R.id.tv_share_to_sina_weibo:
                if (glideUrl != null && !TextUtils.isEmpty(mVideoURL)) {
                    ShareUtils.shareVideoToFacebook(getActivity(), mVideoURL, glideUrl);
                } else if (glideUrl != null) {
                    ShareUtils.shareToFacebook(getActivity(), glideUrl);
                } else {
                    ToastUtil.showNegativeToast(getString(R.string.Tap3_ShareDevice_FailTips));
                }
                break;
        }
    }
}
