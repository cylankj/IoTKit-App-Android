package com.cylan.jiafeigou.n.view.mine;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineUserInfoLookBigHeadContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.MineUserInfoLookBigHeadPresenterIMpl;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.ToastUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 作者：zsl
 * 创建时间：2016/9/2
 * 描述：
 */
public class MineUserInfoLookBigHeadFragment extends Fragment implements MineUserInfoLookBigHeadContract.View {

    @BindView(R.id.iv_userinfo_big_image)
    ImageView ivUserinfoBigImage;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;

    private boolean loadResult = false;

    private MineUserInfoLookBigHeadContract.Presenter presenter;

    public static MineUserInfoLookBigHeadFragment newInstance(Bundle bundle) {
        MineUserInfoLookBigHeadFragment fragment = new MineUserInfoLookBigHeadFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine_userinfo_lookbigimagehead, container, false);
        ButterKnife.bind(this, view);
        initPresenter();
        loadBigImage();
        return view;
    }

    private void loadBigImage() {

        Glide.with(getContext())
                .load(PreferencesUtils.getString(JConstant.USER_IMAGE_HEAD_URL, ""))
                .asBitmap()
                .error(R.mipmap.ic_launcher)
                .centerCrop()
                .into(new BitmapImageViewTarget(ivUserinfoBigImage) {

                    @Override
                    public void onLoadStarted(Drawable placeholder) {
                        super.onLoadStarted(placeholder);
                        showLoadImageProgress();
                    }

                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        super.onResourceReady(resource, glideAnimation);
                        hideLoadImageProgress();
                        loadResult = true;
                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        super.onLoadFailed(e, errorDrawable);
                        hideLoadImageProgress();
                        loadResult = false;
                        ToastUtil.showFailToast(getContext(), "加载失败，点击重试");
                    }

                });
    }

    private void initPresenter() {
        presenter = new MineUserInfoLookBigHeadPresenterIMpl(this);
    }

    @OnClick(R.id.iv_userinfo_big_image)
    public void onClick() {
        if (loadResult) {
            getFragmentManager().popBackStack();
        } else {
            loadBigImage();
        }
    }

    @Override
    public void showLoadImageProgress() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoadImageProgress() {
        progressBar.setVisibility(View.INVISIBLE);
    }


    @Override
    public void setPresenter(MineUserInfoLookBigHeadContract.Presenter presenter) {

    }
}
