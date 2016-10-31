package com.cylan.jiafeigou.n.view.media;

/**
 * Created by cylan-hunt on 16-9-7.
 */

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.transition.Transition;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.FileDescriptorBitmapDecoder;
import com.bumptech.glide.load.resource.bitmap.VideoBitmapDecoder;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.support.log.AppLogger;

import butterknife.BindView;
import butterknife.ButterKnife;

public class VideoDetailsFragment extends PicDetailsFragment {

    @BindView(R.id.vv_play_video)
    VideoView vvPlayVideo;

    private boolean isEntering = true;

    public static VideoDetailsFragment newInstance(int position, int startingPosition, final String url) {
        Bundle args = new Bundle();
        args.putInt(ARG_ALBUM_IMAGE_POSITION, position);
        args.putInt(ARG_STARTING_ALBUM_IMAGE_POSITION, startingPosition);
        args.putString(KEY_MEDIA_URL, url);
        VideoDetailsFragment fragment = new VideoDetailsFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.layout_fragment_media_video_details, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //必须调用super
    }

    @Override
    protected void loadMedia(final ImageView imageView, final String mediaUrl) {
        BitmapPool bitmapPool = Glide.get(getContext()).getBitmapPool();
        FileDescriptorBitmapDecoder decoder = new FileDescriptorBitmapDecoder(
                new VideoBitmapDecoder(1000000),
                bitmapPool,
                DecodeFormat.PREFER_RGB_565);
        Glide.with(this)
                .load(mediaUrl)
                .asBitmap()
                .placeholder(R.drawable.wonderful_pic_place_holder)
                .videoDecoder(decoder)
                .listener(requestListener)
                .into(imageView);
        if (mStartingPosition == mAlbumPosition && isEntering) {
            //两个position相等，表示处于当前页面，其他情况属于预加载过程
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getActivity().getWindow().getSharedElementEnterTransition().addListener(new TransitionListenerAdapter() {
                    @Override
                    public void onTransitionEnd(Transition transition) {
                        AppLogger.d("video transition is end: ");
                    }
                });
            }
        }
        AppLogger.d("load url: " + mediaUrl);
    }


    @Override
    public void onPause() {
        super.onPause();
        isEntering = false;
    }

    private RequestListener<String, Bitmap> requestListener = new RequestListener<String, Bitmap>() {
        @Override
        public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
            startPostponedEnterTransition();
            return false;
        }

        @Override
        public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
            startPostponedEnterTransition();
            return false;
        }
    };

    class TransitionListenerAdapter implements Transition.TransitionListener {
        @Override
        public void onTransitionStart(Transition transition) {

        }

        @Override
        public void onTransitionEnd(Transition transition) {

        }

        @Override
        public void onTransitionCancel(Transition transition) {

        }

        @Override
        public void onTransitionPause(Transition transition) {

        }

        @Override
        public void onTransitionResume(Transition transition) {

        }
    }
}