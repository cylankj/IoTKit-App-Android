package com.cylan.jiafeigou.n.mvp.impl.mine;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineUserInfoLookBigHeadContract;
import com.cylan.jiafeigou.utils.PreferencesUtils;

/**
 * 作者：zsl
 * 创建时间：2016/9/2
 * 描述：
 */
public class MineUserInfoLookBigHeadPresenterIMpl implements MineUserInfoLookBigHeadContract.Presenter {

    private MineUserInfoLookBigHeadContract.View view;
    private Context context;

    public MineUserInfoLookBigHeadPresenterIMpl(MineUserInfoLookBigHeadContract.View view) {
        this.view = view;
        view.setPresenter(this);
    }

    @Override
    public void loadImage(ImageView imageView) {

        Glide.with(view.getContext())
                .load(PreferencesUtils.getString(view.getContext(),JConstant.USER_IMAGE_HEAD_URL,""))
                .asBitmap()
                .centerCrop()
                .into(new BitmapImageViewTarget(imageView) {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                         super.onResourceReady(resource, glideAnimation);

                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        super.onLoadFailed(e, errorDrawable);

                    }

                });
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}
