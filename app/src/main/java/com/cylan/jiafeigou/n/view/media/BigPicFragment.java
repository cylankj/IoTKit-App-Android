package com.cylan.jiafeigou.n.view.media;


import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.photoview.PhotoView;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BigPicFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BigPicFragment extends Fragment {

    public static final String KEY_TITLE = "KEY_TITLE";
    //    public static final String KEY_URL = "key_url";
    @BindView(R.id.imgV_show_pic)
    PhotoView imgVShowPic;


    public BigPicFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create activity_cloud_live_mesg_video_talk_item new instance of
     * this fragment using the provided parameters.
     *
     * @param bundle Parameter 1.
     * @return A new instance of fragment BigPicFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BigPicFragment newInstance(Bundle bundle) {
        BigPicFragment fragment = new BigPicFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    //http://www.androiddesignpatterns.com/2015/03/activity-postponed-shared-element-transitions-part3b.html
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_wonderful_big_pic, container, false);
        ButterKnife.bind(this, view);
        // Postpone the shared element enter transition in onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            getActivity().postponeEnterTransition();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Glide.with(this)
                .load(getArguments().getString(JConstant.KEY_SHARED_ELEMENT_LIST))
                .placeholder(R.drawable.wonderful_pic_place_holder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imgVShowPic);
        imgVShowPic.setOnViewTapListener((View v, float x, float y) -> {
            if (callBack != null) callBack.click();
        });
    }

    private CallBack callBack;

    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }

    public interface CallBack {
        void click();
    }
}
