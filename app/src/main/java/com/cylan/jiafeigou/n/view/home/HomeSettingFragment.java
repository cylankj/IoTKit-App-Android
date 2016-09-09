package com.cylan.jiafeigou.n.view.home;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.mvp.contract.home.HomeSettingContract;
import com.cylan.jiafeigou.n.mvp.impl.home.HomeSettingPresenterImp;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.kyleduo.switchbutton.SwitchButton;

import java.text.BreakIterator;

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
    @BindView(R.id.rl_home_setting_clear)
    RelativeLayout rlHomeSettingClear;
    @BindView(R.id.progressbar_load_cache_size)
    ProgressBar progressbarLoadCacheSize;
    @BindView(R.id.tv_cache_size)
    TextView tvCacheSize;
    @BindView(R.id.progressbar_clearing_cache)
    ProgressBar progressbarClearingCache;
    @BindView(R.id.btn_item_switch_accessMes)
    SwitchButton btnItemSwitchAccessMes;
    @BindView(R.id.btn_item_switch_voide)
    SwitchButton btnItemSwitchVoide;
    @BindView(R.id.btn_item_switch_shake)
    SwitchButton btnItemSwitchShake;

    private HomeSettingContract.Presenter presenter;

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
        initPresenter();
        presenter.calculateCacheSize();
        return view;
    }

    private void initPresenter() {
        presenter = new HomeSettingPresenterImp(this);
    }

    @Override
    public void setPresenter(HomeSettingContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @OnClick({R.id.iv_home_setting_back, R.id.rl_home_setting_about, R.id.rl_home_setting_clear,
            R.id.btn_item_switch_accessMes, R.id.btn_item_switch_voide, R.id.btn_item_switch_shake})
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.iv_home_setting_back:
                getFragmentManager().popBackStack();
                break;
            case R.id.rl_home_setting_about:
                getFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                                , R.anim.slide_in_left, R.anim.slide_out_right)
                        .add(android.R.id.content, homeSettingAboutFragment, "homeSettingAboutFragment")
                        .addToBackStack("mineHelpFragment")
                        .commit();
                break;

            case R.id.rl_home_setting_clear:
                presenter.clearCache();
                break;

            case R.id.btn_item_switch_accessMes:
                presenter.savaSwitchState(switchAcceptMesg(), JConstant.RECEIVE_MESSAGE_NOTIFICATION);
                break;

            case R.id.btn_item_switch_voide:
                presenter.savaSwitchState(switchVoice(), JConstant.OPEN_VOICE);
                break;

            case R.id.btn_item_switch_shake:
                presenter.savaSwitchState(switchShake(), JConstant.OPEN_SHAKE);
                break;
        }

    }

    @Override
    public void showLoadCacheSizeProgress() {
        progressbarLoadCacheSize.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoadCacheSizeProgress() {
        progressbarLoadCacheSize.setVisibility(View.GONE);
    }

    @Override
    public void setCacheSize(String size) {
        tvCacheSize.setText(size);
    }

    @Override
    public void showClearingCacheProgress() {
        progressbarClearingCache.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideClearingCacheProgress() {
        progressbarClearingCache.setVisibility(View.INVISIBLE);
    }

    @Override
    public void clearFinish() {
        tvCacheSize.setText("0.0MB");
        ToastUtil.showToast(getContext(), "清理成功");
    }

    @Override
    public void clearNoCache() {
        ToastUtil.showToast(getContext(), "暂无缓存");
    }

    @Override
    public boolean switchAcceptMesg() {
        return presenter.getNegation();
    }

    @Override
    public boolean switchVoice() {
        return presenter.getNegation();
    }

    @Override
    public boolean switchShake() {
        return presenter.getNegation();
    }

    @Override
    public void initSwitchState() {
        btnItemSwitchAccessMes.setChecked(presenter.getSwitchState(JConstant.RECEIVE_MESSAGE_NOTIFICATION));
        btnItemSwitchVoide.setChecked(presenter.getSwitchState(JConstant.OPEN_VOICE));
        btnItemSwitchShake.setChecked(presenter.getSwitchState(JConstant.OPEN_SHAKE));
    }

    @Override
    public void onStop() {
        super.onStop();
        if (presenter != null) {
            presenter.stop();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        initSwitchState();
    }
}
