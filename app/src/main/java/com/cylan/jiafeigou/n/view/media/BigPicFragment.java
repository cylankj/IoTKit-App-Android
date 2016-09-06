package com.cylan.jiafeigou.n.view.media;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cylan.jiafeigou.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BigPicFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BigPicFragment extends Fragment {

    @BindView(R.id.imgV_show_pic)
    ImageView imgVShowPic;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_wonderful_big_pic, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Glide.with(this)
                .load(getArguments().getString("test"))
                .placeholder(R.drawable.wonderful_pic_place_holder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imgVShowPic);
    }
}
