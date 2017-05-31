package com.cylan.jiafeigou.widget.dialog;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cylan.jiafeigou.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 */
public class PickImageFragment extends BaseDialog {


    @BindView(R.id.tv_pick_photo)
    TextView tvPickPhoto;
    @BindView(R.id.tv_take_photo)
    TextView tvTakePhoto;

    public static PickImageFragment newInstance(Bundle bundle) {
        PickImageFragment fragment = new PickImageFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    private View.OnClickListener takeListener;
    private View.OnClickListener pickListener;

    public void setClickListener(View.OnClickListener takeListener, View.OnClickListener pickListener) {
        this.takeListener = takeListener;
        this.pickListener = pickListener;
    }

    @Override
    protected int getCustomHeight() {
        return ViewGroup.LayoutParams.WRAP_CONTENT;
    }

    public PickImageFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.pick_image_fragment_dialog, container, true);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @OnClick({R.id.tv_pick_photo, R.id.tv_take_photo})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_pick_photo:
                if (pickListener != null) pickListener.onClick(view);
                break;
            case R.id.tv_take_photo:
                if (takeListener != null) takeListener.onClick(view);
                break;
        }
        dismiss();
    }
}
