package com.cylan.jiafeigou.support.share;

import android.os.Bundle;

import com.cylan.jiafeigou.base.wrapper.BasePresenter;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;

/**
 * Created by yanzhendong on 2017/6/1.
 */

public class SharePresenter extends BasePresenter<ShareContact.View> implements ShareContact.Presenter {

    private UMShareListener listener = new UMShareListener() {
        @Override
        public void onStart(SHARE_MEDIA share_media) {
            AppLogger.e("onStart,分享开始啦!,当前分享到的平台为:" + share_media);
        }

        @Override
        public void onResult(SHARE_MEDIA share_media) {
            AppLogger.e("onResult,分享成功啦!,当前分享到的平台为:" + share_media);
        }

        @Override
        public void onError(SHARE_MEDIA share_media, Throwable throwable) {
            AppLogger.e("onError,分享失败啦!,当前分享到的平台为:" + share_media + ",错误原因为:" + throwable.getMessage());
        }

        @Override
        public void onCancel(SHARE_MEDIA share_media) {
            AppLogger.e("onCancel,分享取消啦!,当前分享到的平台为:" + share_media);
        }
    };

    public void share(int shareStyle, Bundle bundle) {
//        Observable.create((Observable.OnSubscribe<ShareAction>) subscriber -> {
//            if (shareStyle == SHARE_CONTENT_PICTURE) {
//                AppLogger.e("图片分享,直接分享");
//                String imageUrl = bundle.getString(ShareContanst.SHARE_IMAGE_URL);
//                Glide.with(mView.getAppContext()).load(imageUrl)
//                        .downloadOnly(new SimpleTarget<File>() {
//                            @Override
//                            public void onResourceReady(File resource, GlideAnimation<? super File> glideAnimation) {
//
//                            }
//                        });
//                UMImage image = new UMImage(mView.getAppContext(), imageUrl);
//                shareAction = new ShareAction(this);
//                shareAction.setPlatform(getPlatform(Integer.valueOf((String) view.getTag())));
//                shareAction.withMedia(image);
//                shareAction.withText("SSSSSSS");
//                shareAction.withExtra(image);
//                shareAction.setCallback(listener);
//                shareAction.share();
//            } else if (shareStyle == SHARE_CONTENT_WEB_URL) {
//                AppLogger.e("网址分享,不带上传");
//                String shareLinkUrl = ShareParser.getInstance().parserWebLinkUrl(getIntent().getExtras());
//                UMWeb web = new UMWeb(shareLinkUrl);
//                shareAction = new ShareAction(this);
//                shareAction.setPlatform(getPlatform(Integer.valueOf((String) view.getTag())));
//                shareAction.withMedia(web);
//                shareAction.setCallback(listener);
//                shareAction.share();
//            }
//
//        })
//                .subscribeOn(Schedulers.io())
    }


}
