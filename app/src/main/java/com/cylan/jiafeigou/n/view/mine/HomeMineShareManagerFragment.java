package com.cylan.jiafeigou.n.view.mine;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.injector.component.FragmentComponent;
import com.cylan.jiafeigou.base.wrapper.BaseFragment;
import com.cylan.jiafeigou.databinding.FragmentMineShareManagerBinding;
import com.cylan.jiafeigou.utils.ActivityUtils;

/**
 * Created by yanzhendong on 2017/5/26.
 */

public class HomeMineShareManagerFragment extends BaseFragment implements View.OnClickListener {
    private FragmentMineShareManagerBinding managerBinding;

    @Override
    protected void setFragmentComponent(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @Override
    protected int getContentViewID() {
        return R.layout.fragment_mine_share_manager;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        managerBinding = FragmentMineShareManagerBinding.inflate(inflater);
        return managerBinding.getRoot();
    }

    @Override
    protected void initViewAndListener() {
        super.initViewAndListener();
        managerBinding.setListener(this);
    }

    public static HomeMineShareManagerFragment newInstance(Bundle bundle) {
        HomeMineShareManagerFragment fragment = new HomeMineShareManagerFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_toolbar_icon:
                getActivity().onBackPressed();
                break;
            case R.id.sharedContent:
                sharedContent();
                break;
            case R.id.sharedDevice:
                sharedDevice();
                break;
        }
    }

    private void sharedContent() {
        HomeMineShareContentFragment mineShareDeviceFragment = HomeMineShareContentFragment.newInstance(null);
        ActivityUtils.addFragmentSlideInFromRight(getActivity().getSupportFragmentManager(), mineShareDeviceFragment, android.R.id.content);
    }

    private void sharedDevice() {
        MineShareDeviceFragment mineShareDeviceFragment = MineShareDeviceFragment.newInstance(null);
        ActivityUtils.addFragmentSlideInFromRight(getActivity().getSupportFragmentManager(), mineShareDeviceFragment, android.R.id.content);
    }
}
