package com.cylan.jiafeigou.n.view.mine;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineLookBigImageContract;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 作者：zsl
 * 创建时间：2016/9/21
 * 描述：
 */
public class MineLookBigImageFragment extends Fragment implements MineLookBigImageContract.View {

    @BindView(R.id.iv_look_big_image)
    ImageView ivLookBigImage;
    @BindView(R.id.iv_load_small_iamge)
    ImageView ivLoadSmallIamge;
    @BindView(R.id.progress_loading)
    ProgressBar progressLoading;

    public static MineLookBigImageFragment newInstance(Bundle bundle) {
        return new MineLookBigImageFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine_long_big_image, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void setPresenter(MineLookBigImageContract.Presenter presenter) {

    }


    @OnClick({R.id.iv_look_big_image, R.id.iv_load_small_iamge})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_look_big_image:                //点击大图退出全屏

                break;
            case R.id.iv_load_small_iamge:              //点击小图重心加载

                break;
        }
    }
}
