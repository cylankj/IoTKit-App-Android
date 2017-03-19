package com.cylan.jiafeigou.n.view.panorama;


import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.wrapper.BaseFragment;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.panorama.Panoramic720View;


/**
 * Use the {@link Pan720FullFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Pan720FullFragment extends BaseFragment<Pan720FullContract.Presenter> {

    public Pan720FullFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param bundle Parameter 1.
     * @return A new instance of fragment Pan720FullFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static Pan720FullFragment newInstance(Bundle bundle) {
        Pan720FullFragment fragment = new Pan720FullFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    protected Pan720FullContract.Presenter onCreatePresenter() {
        return new Pan720FullPresenter();
    }

    @Override
    protected int getContentViewID() {
        return R.layout.fragment_pan720_full;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        final Panoramic720View panoramic720View = new Panoramic720View(getContext());
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        ((ViewGroup) view).addView(panoramic720View, 0, lp);
        Glide.with(this)
                .load(getArguments().getString("item_url"))
                .asBitmap()
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        panoramic720View.loadImage(resource);
                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        AppLogger.e("err: " + e);
                    }
                });
    }
}
