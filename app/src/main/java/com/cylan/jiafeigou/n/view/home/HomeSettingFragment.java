package com.cylan.jiafeigou.n.view.home;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.home.HomeSettingContract;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 作者：zsl
 * 创建时间：2016/9/5
 * 描述：
 */
public class HomeSettingFragment extends Fragment implements HomeSettingContract.View {


    @BindView(R.id.iv_home_setting_back)
    ImageView ivHomeSettingBack;
    @BindView(R.id.rl_home_setting_about)
    RelativeLayout rlHomeSettingAbout;

    private HomeSettingAboutFragment homeSettingAboutFragment;

    public static HomeSettingFragment newInstance() {
        return new HomeSettingFragment();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        homeSettingAboutFragment = HomeSettingAboutFragment.newInstance();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_mine_setting, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void setPresenter(HomeSettingContract.Presenter presenter) {

    }

    @OnClick({R.id.iv_home_setting_back,R.id.rl_home_setting_about})
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.iv_home_setting_back:
                getFragmentManager().popBackStack();
                break;
            case R.id.rl_home_setting_about:
                getFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                                , R.anim.slide_in_left, R.anim.slide_out_right)
                        .add(android.R.id.content,homeSettingAboutFragment,"homeSettingAboutFragment")
                        .addToBackStack("mineHelpFragment")
                        .commit();
                break;
        }

    }

}
