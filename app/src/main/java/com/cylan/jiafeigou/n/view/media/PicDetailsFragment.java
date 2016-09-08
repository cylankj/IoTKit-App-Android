package com.cylan.jiafeigou.n.view.media;

/**
 * Created by cylan-hunt on 16-9-7.
 */

import android.annotation.TargetApi;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.cylan.photoview.PhotoView;

import butterknife.BindView;
import butterknife.ButterKnife;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class PicDetailsFragment extends Fragment {

    public static final String KEY_MEDIA_URL = "key_media_url";
    public static final String ARG_ALBUM_IMAGE_POSITION = "arg_album_image_position";
    public static final String ARG_STARTING_ALBUM_IMAGE_POSITION = "arg_starting_album_image_position";
    @BindView(R.id.details_album_image)
    PhotoView detailsAlbumImage;

    protected int mStartingPosition;
    protected int mAlbumPosition;

    public static PicDetailsFragment newInstance(int position, int startingPosition, final String url) {
        Bundle args = new Bundle();
        args.putInt(ARG_ALBUM_IMAGE_POSITION, position);
        args.putInt(ARG_STARTING_ALBUM_IMAGE_POSITION, startingPosition);
        args.putString(KEY_MEDIA_URL, url);
        PicDetailsFragment fragment = new PicDetailsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mStartingPosition = getArguments().getInt(ARG_STARTING_ALBUM_IMAGE_POSITION);
        mAlbumPosition = getArguments().getInt(ARG_ALBUM_IMAGE_POSITION);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.layout_fragment_media_pic_details, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        //设置transition名字，这个名字需要对应前一个scene的ImageView的transitionName,
        ViewCompat.setTransitionName(detailsAlbumImage,
                mAlbumPosition + JConstant.KEY_SHARED_ELEMENT_TRANSITION_NAME_POSTFIX);
        final String albumImageUrl = getArguments().getString(KEY_MEDIA_URL);
        loadMedia(detailsAlbumImage, albumImageUrl);
    }

    /**
     * 加载资源
     */
    protected void loadMedia(final ImageView imageView, final String mediaUrl) {
        Glide.with(this)
                .load(mediaUrl)
                .listener(requestListener)
                .placeholder(R.drawable.wonderful_pic_place_holder)
                .fitCenter()
                .into(imageView);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        AppLogger.d("onDestroyView: " + mAlbumPosition);
    }

    protected void startPostponedEnterTransition() {
        if (mAlbumPosition == mStartingPosition) {
            AppLogger.d("transition: startPostponedEnterTransition: " + mAlbumPosition + "\n" +
                    detailsAlbumImage.getTransitionName());
            detailsAlbumImage.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public boolean onPreDraw() {
                    detailsAlbumImage.getViewTreeObserver().removeOnPreDrawListener(this);
                    getActivity().startPostponedEnterTransition();
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
     * Returns true if {@param view} is contained within {@param container}'s bounds.
     */
    private static boolean isViewInBounds(@NonNull View container, @NonNull View view) {
        Rect containerBounds = new Rect();
        container.getHitRect(containerBounds);
        return view.getLocalVisibleRect(containerBounds);
    }
}