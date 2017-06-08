package com.cylan.jiafeigou.support.share;

import android.content.DialogInterface;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.injector.component.FragmentComponent;
import com.cylan.jiafeigou.base.wrapper.BaseFragment;
import com.cylan.jiafeigou.databinding.FragmentPanoramaShareBinding;
import com.cylan.jiafeigou.n.view.panorama.PanoramaAlbumContact;
import com.cylan.jiafeigou.n.view.panorama.PanoramaShareContact;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.widget.LoadingDialog;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMWeb;

import java.io.File;

import static com.cylan.jiafeigou.support.share.ShareConstant.SHARE_CONTENT_H5_WITH_UPLOAD_EXTRA_FILE_PATH;
import static com.cylan.jiafeigou.support.share.ShareConstant.SHARE_CONTENT_H5_WITH_UPLOAD_EXTRA_SHARE_ITEM;
import static com.cylan.jiafeigou.support.share.ShareConstant.SHARE_CONTENT_H5_WITH_UPLOAD_EXTRA_THUMB_PATH;
import static com.cylan.jiafeigou.support.share.ShareConstant.SHARE_PLATFORM_TYPE;
import static com.cylan.jiafeigou.support.share.ShareConstant.SHARE_PLATFORM_TYPE_FACEBOOK;
import static com.cylan.jiafeigou.support.share.ShareConstant.SHARE_PLATFORM_TYPE_QQ;
import static com.cylan.jiafeigou.support.share.ShareConstant.SHARE_PLATFORM_TYPE_QZONE;
import static com.cylan.jiafeigou.support.share.ShareConstant.SHARE_PLATFORM_TYPE_TIME_LINE;
import static com.cylan.jiafeigou.support.share.ShareConstant.SHARE_PLATFORM_TYPE_TWITTER;
import static com.cylan.jiafeigou.support.share.ShareConstant.SHARE_PLATFORM_TYPE_WECHAT;
import static com.cylan.jiafeigou.support.share.ShareConstant.SHARE_PLATFORM_TYPE_WEIBO;

/**
 * Created by yanzhendong on 2017/5/27.
 */

public class H5ShareEditorFragment extends BaseFragment<PanoramaShareContact.Presenter> implements PanoramaShareContact.View {

    private FragmentPanoramaShareBinding shareBinding;
    private ObservableField<String> description = new ObservableField<>();
    private ObservableBoolean uploadSuccess = new ObservableBoolean(false);
    private int shareType;
    private PanoramaAlbumContact.PanoramaItem shareItem;
    private String filePath;
    private String thumbPath;


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
    protected boolean onBackPressed() {
        getActivity().finish();
        return true;

    }

    @Override
    protected void initViewAndListener() {
        super.initViewAndListener();
        Bundle arguments = getArguments();
        shareType = arguments.getInt(SHARE_PLATFORM_TYPE);
        shareItem = arguments.getParcelable(SHARE_CONTENT_H5_WITH_UPLOAD_EXTRA_SHARE_ITEM);
        filePath = arguments.getString(SHARE_CONTENT_H5_WITH_UPLOAD_EXTRA_FILE_PATH);
        thumbPath = arguments.getString(SHARE_CONTENT_H5_WITH_UPLOAD_EXTRA_THUMB_PATH);
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
            case SHARE_PLATFORM_TYPE_TIME_LINE://
                return getString(R.string.Tap2_Share_Moments);
            case SHARE_PLATFORM_TYPE_WECHAT:
                return getString(R.string.WeChat);
            case SHARE_PLATFORM_TYPE_QQ:
                return "QQ";
            case SHARE_PLATFORM_TYPE_QZONE:
                return getString(R.string.Qzone_QQ);
            case SHARE_PLATFORM_TYPE_WEIBO:
                return getString(R.string.Weibo);
            case SHARE_PLATFORM_TYPE_FACEBOOK:
                return "Facebook";
            case SHARE_PLATFORM_TYPE_TWITTER:
                return "Twitter";
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

    private String getDescription() {
        if (TextUtils.isEmpty(description.get())) {
            return getString(R.string.share_default_description);
        } else {
            return description.get();
        }
    }

    private void shareWithH5ByType(int shareType, String h5) {
        if (!LoadingDialog.isShowing(getActivity().getSupportFragmentManager())) {
            LoadingDialog.showLoading(getActivity().getSupportFragmentManager(), getString(R.string.LOADING), false, dialog -> getActivity().finish());
        }
        Bundle arguments = getArguments();
        String thumbPath = arguments.getString(ShareConstant.SHARE_CONTENT_H5_WITH_UPLOAD_EXTRA_THUMB_PATH);
        UMWeb umWeb = new UMWeb(h5);
        umWeb.setTitle(getString(R.string.share_default_title));
        umWeb.setDescription(getDescription());
        if (!TextUtils.isEmpty(thumbPath)) {
            umWeb.setThumb(new UMImage(getContext(), new File(thumbPath)));
        }
        ShareAction shareAction = new ShareAction(getActivity());
        shareAction.withMedia(umWeb);
        if (getActivity() != null && getActivity() instanceof UMShareListener) {
            shareAction.setCallback((UMShareListener) getActivity());
        }
        switch (shareType) {
            case SHARE_PLATFORM_TYPE_TIME_LINE://
                shareAction.setPlatform(SHARE_MEDIA.WEIXIN_CIRCLE);
                break;
            case SHARE_PLATFORM_TYPE_WECHAT:
                shareAction.setPlatform(SHARE_MEDIA.WEIXIN);
                break;
            case SHARE_PLATFORM_TYPE_QQ:
                shareAction.setPlatform(SHARE_MEDIA.QQ);
                break;
            case SHARE_PLATFORM_TYPE_QZONE:
                shareAction.setPlatform(SHARE_MEDIA.QZONE);
                break;
            case SHARE_PLATFORM_TYPE_WEIBO:
                shareAction.setPlatform(SHARE_MEDIA.SINA);
                break;
            case SHARE_PLATFORM_TYPE_FACEBOOK:
                shareAction.setPlatform(SHARE_MEDIA.FACEBOOK);
                break;
            case SHARE_PLATFORM_TYPE_TWITTER:
                shareAction.setPlatform(SHARE_MEDIA.TWITTER);
                break;
        }
        shareAction.share();
    }

    private void cancelLoading(DialogInterface dialogInterface) {

    }

    public void cancelShare(View view) {
        onBackPressed();
    }

    public void share(View view) {
        presenter.share(shareItem, description.get() == null ? "" : description.get(), thumbPath);
    }
}
