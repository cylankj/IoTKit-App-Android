package com.cylan.jiafeigou.n.view.media;

/**
 * Created by cylan-hunt on 16-9-7.
 */

import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.photoview.PhotoView;
import com.cylan.jiafeigou.widget.LazyFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PicDetailsFragment extends LazyFragment {

    public static final String KEY_MEDIA_URL = "key_media_url";
    public static final String ARG_MEDIA_POSITION = "arg_media_position";
    public static final String ARG_MEDIA_START_POSITION = "arg_media_start_position";
    public static final String ARG_MEDIA_TYPE = "arg_media_type";
    @BindView(R.id.details_album_image)
    PhotoView detailsAlbumImage;

    protected int mStartPosition;
    protected int mPosition;
    protected int mMediaType;//0:pic;1:video
    protected View mRootView;
    protected String mAlbumImageUrl;

    //是否可见
    protected boolean isVisble;
    // 标志位，标志Fragment已经初始化完成。
    public boolean isPrepared = false;

    public static PicDetailsFragment newInstance(int position, int startingPosition, final String url) {
        Bundle args = new Bundle();
        args.putInt(ARG_MEDIA_TYPE, 0);
        args.putInt(ARG_MEDIA_POSITION, position);
        args.putInt(ARG_MEDIA_START_POSITION, startingPosition);
        args.putString(KEY_MEDIA_URL, url);
        PicDetailsFragment fragment = new PicDetailsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mStartPosition = getArguments().getInt(ARG_MEDIA_START_POSITION);
        mPosition = getArguments().getInt(ARG_MEDIA_POSITION);
        AppLogger.e("ASSSSSSSSSSSSSSSSSSSSSSSSSs");
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = inflater.inflate(getLayoutID(), container, false);
            ButterKnife.bind(this, mRootView);
            initView();
        }
        return mRootView;
    }

    protected void initView() {

    }

    public void initData() {
        loadMedia(mAlbumImageUrl);
    }

    protected int getLayoutID() {
        return R.layout.layout_fragment_media_pic_details;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        //设置transition名字，这个名字需要对应前一个scene的ImageView的transitionName,
        ViewCompat.setTransitionName(getTransitionView(),
                mPosition + JConstant.KEY_SHARED_ELEMENT_TRANSITION_NAME_SUFFIX);
        mAlbumImageUrl = getArguments().getString(KEY_MEDIA_URL);
        isPrepared = true;
        loadMedia(mAlbumImageUrl);
    }

    protected View getTransitionView() {
        return detailsAlbumImage;
    }

    /**
     * 加载资源
     */
    protected void loadMedia(final String mediaUrl) {
        Glide.with(this)
                .load(mediaUrl)
                .listener(requestListener)
                .placeholder(R.drawable.wonderful_pic_place_holder)
//                .fitCenter()
                .into(detailsAlbumImage);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        AppLogger.d("onDestroyView: " + mPosition);
    }

    @Override
    public void startPostponedEnterTransition() {
        if (mPosition == mStartPosition) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                AppLogger.d("transition: startPostponedEnterTransition: " + mPosition + "\n" +
                        detailsAlbumImage.getTransitionName());
            }
            detailsAlbumImage.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    detailsAlbumImage.getViewTreeObserver().removeOnPreDrawListener(this);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        if (mStartPosition == mPosition) {
                            getActivity().startPostponedEnterTransition();
                        }
                    }
                    return true;
                }
            });
        }
    }


    private RequestListener<String, GlideDrawable> requestListener = new RequestListener<String, GlideDrawable>() {
        @Override
        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
            startPostponedEnterTransition();
            return false;
        }

        @Override
        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
            startPostponedEnterTransition();
            return false;
        }
    };

    /**
     * Returns the shared element that should be transitioned back to the previous Activity,
     * or null if the view is not visible on the screen.
     */
    @Nullable
    public ImageView getAlbumImage() {
        if (isViewInBounds(getActivity().getWindow().getDecorView(), detailsAlbumImage)) {
            return detailsAlbumImage;
        }
        return null;
    }

    /**
     * Returns true if {@param view} is contained within {@param container}'account bounds.
     */
    private static boolean isViewInBounds(@NonNull View container, @NonNull View view) {
        Rect containerBounds = new Rect();
        container.getHitRect(containerBounds);
        return view.getLocalVisibleRect(containerBounds);
    }

}
