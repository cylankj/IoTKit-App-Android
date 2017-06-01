package com.cylan.jiafeigou.n.view.panorama;

import android.content.Intent;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.injector.component.FragmentComponent;
import com.cylan.jiafeigou.base.wrapper.BaseFragment;
import com.cylan.jiafeigou.databinding.FragmentPanoramaShareBinding;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ShareUtils;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;

/**
 * Created by yanzhendong on 2017/5/27.
 */

public class PanoramaShareFragment extends BaseFragment<PanoramaShareContact.Presenter> implements PanoramaShareContact.View {
    public static final String SHARE_TYPE = "share_type";
    public static final String SHARE_ITEM = "share_item";
    public static final String FILE_PATH = "file_path";
    public static final int SHARE_TYPE_TIME_LINE = 0;
    public static final int SHARE_TYPE_WECHAT = 1;
    public static final int SHARE_TYPE_QQ = 3;
    public static final int SHARE_TYPE_QZONE = 4;
    public static final int SHARE_TYPE_WEIBO = 5;
    public static final int SHARE_TYPE_FACEBOOK = 6;
    public static final int SHARE_TYPE_TWITTER = 7;
    private FragmentPanoramaShareBinding shareBinding;
    private ObservableField<String> description = new ObservableField<>();
    private ObservableBoolean uploadSuccess = new ObservableBoolean(false);
    private int shareType;
    private PanoramaAlbumContact.PanoramaItem shareItem;
    private String filePath;

    @Override
    protected void setFragmentComponent(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        shareBinding = FragmentPanoramaShareBinding.inflate(inflater);
        return shareBinding.getRoot();
    }

    @Override
    protected void initViewAndListener() {
        super.initViewAndListener();
        Bundle arguments = getArguments();
        shareType = arguments.getInt(SHARE_TYPE);
        shareItem = arguments.getParcelable(SHARE_ITEM);
        filePath = arguments.getString(FILE_PATH);
        shareBinding.setWay(getNameByType(shareType));
        shareBinding.setDescription(description);
        shareBinding.setUploadSuccess(uploadSuccess);
        shareBinding.setBackClick(this::cancelShare);
        shareBinding.setShareClick(this::share);
        shareBinding.shareContextEditor.requestFocus();
        shareBinding.shareRetry.setOnClickListener(v -> presenter.upload(shareItem.fileName, filePath));
        Glide.with(this).load(filePath).into(shareBinding.sharePreview);
        presenter.upload(shareItem.fileName, filePath);
    }

    private String getNameByType(int shareType) {
        switch (shareType) {
            case SHARE_TYPE_TIME_LINE://
                return getString(R.string.Tap2_Share_Moments);
            case SHARE_TYPE_WECHAT:
                return getString(R.string.WeChat);
            case SHARE_TYPE_QQ:
                return getString(R.string.QQ);
            case SHARE_TYPE_QZONE:
                return getString(R.string.Qzone_QQ);
            case SHARE_TYPE_WEIBO:
                return getString(R.string.Weibo);
            case SHARE_TYPE_FACEBOOK:
                return getString(R.string.facebook);
            case SHARE_TYPE_TWITTER:
                return getString(R.string.twitter);
        }
        return "";
    }

    @Override
    public void onUploadResult(int code) {
        uploadSuccess.set(code == 200);
        shareBinding.shareRetry.setVisibility(uploadSuccess.get() ? View.GONE : View.VISIBLE);
        AppLogger.d("上传到服务器返回的结果为:" + code);
    }

    @Override
    public void onShareH5Result(boolean success, String h5) {
        if (success && !TextUtils.isEmpty(h5)) {
            shareWithH5ByType(shareType, h5);
            AppLogger.d("得到上传服务器返回的 h5网址,将进行对应的分享");
        }
    }

    private void shareWithH5ByType(int shareType, String h5) {
        switch (shareType) {
            case SHARE_TYPE_TIME_LINE://
                ShareUtils.shareWebPageWechat(getActivity(), h5, SendMessageToWX.Req.WXSceneTimeline, new GlideUrl(filePath));
                break;
            case SHARE_TYPE_WECHAT:
                ShareUtils.shareWebPageWechat(getActivity(), h5, SendMessageToWX.Req.WXSceneTimeline, new GlideUrl(filePath));
                break;
            case SHARE_TYPE_QQ:
                ShareUtils.shareVideoToQQ(getActivity(), h5);
                break;
            case SHARE_TYPE_QZONE:
                break;
            case SHARE_TYPE_WEIBO:
                ShareUtils.shareH5ToWeibo(getActivity(), description.get() == null ? "" : description.get());
                break;
            case SHARE_TYPE_FACEBOOK:
                ShareUtils.shareVideoToFacebook(getActivity(), h5, new GlideUrl(filePath));
                break;
            case SHARE_TYPE_TWITTER:
                ShareUtils.shareVideoToTwitter(getActivity(), h5, new GlideUrl(filePath));
                break;
        }
    }

    public void cancelShare(View view) {
        onBackPressed();
    }

    public void share(View view) {
        presenter.share(shareItem, description.get() == null ? "" : description.get());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        AppLogger.e("GGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGG");
        ShareUtils.onActivityResult(requestCode, resultCode, data);
    }
}
