package com.cylan.jiafeigou.n.view.media;


import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.photoview.PhotoView;
import com.cylan.photoview.PhotoViewAttacher;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BigPicFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BigPicFragment extends Fragment {

    @BindView(R.id.imgV_show_pic)
    PhotoView imgVShowPic;
    @BindView(R.id.tv_big_pic_close)
    ImageView tvBigPicClose;
    @BindView(R.id.tv_big_pic_title)
    TextView tvBigPicTitle;
    @BindView(R.id.fLayout_big_pic_title)
    FrameLayout fLayoutBigPicTitle;
    @BindView(R.id.imgV_big_pic_download)
    ImageView imgVBigPicDownload;
    @BindView(R.id.imgV_big_pic_share)
    ImageView imgVBigPicShare;
    @BindView(R.id.imgV_big_pic_collect)
    ImageView imgVBigPicCollect;

    public BigPicFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
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
        ViewUtils.setViewMarginStatusBar(fLayoutBigPicTitle);
        Glide.with(this)
                .load(getArguments().getString(JConstant.KEY_SHARED_ELEMENT_LIST))
                .placeholder(R.drawable.wonderful_pic_place_holder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imgVShowPic);
        imgVShowPic.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
            @Override
            public void onViewTap(View view, float x, float y) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

    }

    @OnClick(R.id.tv_big_pic_close)
    public void onClick() {
    }

    @OnClick({R.id.imgV_big_pic_download, R.id.imgV_big_pic_share, R.id.imgV_big_pic_collect
            , R.id.tv_big_pic_close})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imgV_big_pic_download:
                break;
            case R.id.imgV_big_pic_share:
                break;
            case R.id.imgV_big_pic_collect:
                break;
            case R.id.tv_big_pic_close:
                break;
        }
    }
}
