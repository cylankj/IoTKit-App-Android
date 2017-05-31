package com.cylan.jiafeigou.n.view.mine;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cylan.jiafeigou.base.injector.component.FragmentComponent;
import com.cylan.jiafeigou.base.wrapper.BaseFragment;
import com.cylan.jiafeigou.databinding.FragmentShareContentH5DetailBinding;

/**
 * Created by yanzhendong on 2017/5/31.
 */

public class ShareContentH5DetailFragment extends BaseFragment {
    @Override
    protected void setFragmentComponent(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentShareContentH5DetailBinding h5DetailBinding = FragmentShareContentH5DetailBinding.inflate(inflater);
        return h5DetailBinding.getRoot();
    }
}
