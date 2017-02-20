package com.cylan.jiafeigou.n.view.media;


import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.VideoView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.support.log.AppLogger;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PanoramicViewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PanoramicViewFragment extends Fragment {

    public static final String KEY_VIDEO_URL = "key_video_url";
    @BindView(R.id.vv_play_video)
    VideoView vvPlayVideo;

    public PanoramicViewFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create activity_cloud_live_mesg_call_out_item new instance of
     * this fragment using the provided parameters.
     *
     * @param url Parameter 1.
     * @return A new instance of fragment NormalMediaFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PanoramicViewFragment newInstance(String url) {
        PanoramicViewFragment fragment = new PanoramicViewFragment();
        Bundle bundle = new Bundle();
        bundle.putString(KEY_VIDEO_URL, url);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_video_view, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Uri uri = Uri.parse(getArguments().getString(KEY_VIDEO_URL));
        vvPlayVideo.setVideoURI(uri);
        vvPlayVideo.start();
        vvPlayVideo.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                AppLogger.d("video play err: " + what + " " + extra);
                return false;
            }
        });
        vvPlayVideo.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                AppLogger.d("video play onCompletion: ");
            }
        });
    }
}
