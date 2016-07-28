package com.cylan.jiafeigou.n.view.media;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cylan.jiafeigou.R;
import com.cylan.photoview.PhotoView;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WonderfulBigPicFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WonderfulBigPicFragment extends Fragment {

    @BindView(R.id.imgV_show_pic)
    PhotoView imgVShowPic;

    public WonderfulBigPicFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param bundle Parameter 1.
     * @return A new instance of fragment WonderfulBigPicFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static WonderfulBigPicFragment newInstance(Bundle bundle) {
        WonderfulBigPicFragment fragment = new WonderfulBigPicFragment();
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

}
