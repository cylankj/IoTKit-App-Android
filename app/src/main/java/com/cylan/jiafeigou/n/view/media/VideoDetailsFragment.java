package com.cylan.jiafeigou.n.view.media;

/**
 * Created by cylan-hunt on 16-9-7.
 */

import android.graphics.Bitmap;
import android.os.Bundle;
import android.transition.Transition;
import android.widget.VideoView;

import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.cylan.jiafeigou.R;

import butterknife.BindView;

public class VideoDetailsFragment extends PicDetailsFragment {

    @BindView(R.id.vv_play_video)
    VideoView vvPlayVideo;

    private boolean isEntering = true;

    public static VideoDetailsFragment newInstance(int position, int startingPosition, final String url) {
        Bundle args = new Bundle();
        args.putInt(ARG_MEDIA_POSITION, position);
        args.putInt(ARG_MEDIA_START_POSITION, startingPosition);
        args.putString(KEY_MEDIA_URL, url);
        args.putInt(ARG_MEDIA_TYPE, 1);
        VideoDetailsFragment fragment = new VideoDetailsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutID() {
        return R.layout.layout_fragment_media_video_details;
    }

//    @Override
//    protected View getTransitionView() {
//        return vvPlayVideo;
//    }

//    @Override
//    protected void loadMedia(final String mediaUrl) {
////
//        startPostponedEnterTransition();
////        if (mStartPosition == mPosition && isEntering) {
////            //两个position相等，表示处于当前页面，其他情况属于预加载过程
////            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
////                getActivity().getWindow().getSharedElementEnterTransition().addListener(new TransitionListenerAdapter() {
////                    @Override
////                    public void onTransitionEnd(Transition transition) {
////                        AppLogger.d("video transition is end: ");
////                        Log.e("HGGGGGGGGGGGGGGGGGGG", "onTransitionEnd: ");
////                    }
////                });
////            }
////        }
////        AppLogger.d("load url: " + mediaUrl);
//    }


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