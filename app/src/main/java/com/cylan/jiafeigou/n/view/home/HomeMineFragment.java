package com.cylan.jiafeigou.n.view.home;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.home.HomeMineContract;
import com.cylan.jiafeigou.n.view.fragment.AccountInfoFragment;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.ImageViewTip;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class HomeMineFragment extends Fragment
        implements HomeMineContract.View {

    private static final String TAG = "HomeMineFragment";

    @BindView(R.id.tv_home_mine_portrait)
    TextView tvMinePortrait;
    @BindView(R.id.img_home_mine_msg)
    ImageViewTip imgHomeMineMsg;
    @BindView(R.id.img_home_mine_dear_friend)
    ImageView imgHomeMineDearFriend;
    @BindView(R.id.img_home_mine_share_device)
    ImageView imgHomeMineShareDevice;
    @BindView(R.id.img_home_mine_help_feedback)
    ImageView imgHomeMineHelpFeedback;
    @BindView(R.id.img_home_mine_setting)
    ImageView imgHomeMineSetting;


    private HomeMineContract.Presenter presenter;

    public static HomeMineFragment newInstance(Bundle bundle) {
        HomeMineFragment fragment = new HomeMineFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle;
        if (getArguments() != null) {
            bundle = getArguments();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home_mine, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.start();
    }

    @Override
    public void onStop() {
        super.onStop();
        presenter.stop();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @OnClick(R.id.img_home_mine_msg)
    public void onClickMsg() {
        ToastUtil.showToast(getContext(), "xiao xi");
    }

    @OnClick(R.id.tv_home_mine_portrait)
    public void onClickPortrait() {
        ToastUtil.showToast(getContext(), "推荐fragment");
        ActivityUtils.addFragmentToActivity(getActivity().getSupportFragmentManager(),
                AccountInfoFragment.getInstance(), R.id.rLayout_new_home_container, 0);
    }

    @OnClick(R.id.img_home_mine_dear_friend)
    public void onClickFriends() {
        ToastUtil.showToast(getContext(), "tou onClickFriends");
    }

    @OnClick(R.id.img_home_mine_share_device)
    public void onClickShare() {
        ToastUtil.showToast(getContext(), "tou onClickShare");
    }

    @OnClick(R.id.img_home_mine_help_feedback)
    public void onClickFeedback() {
        ToastUtil.showToast(getContext(), "tou onClickFeeback");
    }

    @OnClick(R.id.img_home_mine_setting)
    public void onClickSettings() {
        ToastUtil.showToast(getContext(), "tou onClickSettings");
    }

    @Override
    public void setPresenter(HomeMineContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void onPortraitUpdate(String url) {
        tvMinePortrait.setText(url);
    }
}
