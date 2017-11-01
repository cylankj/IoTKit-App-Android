package com.cylan.jiafeigou.n.view.panorama;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cylan.jiafeigou.base.wrapper.BaseFragment;
import com.cylan.jiafeigou.databinding.FragmentConnectionDescriptionBinding;

/**
 * Created by yanzhendong on 2017/6/6.
 */

public class ConnectionDescriptionFragment extends BaseFragment {

    private FragmentConnectionDescriptionBinding descriptionBinding;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        descriptionBinding = FragmentConnectionDescriptionBinding.inflate(inflater);
        descriptionBinding.setUp.setOnClickListener(this::setUp);
        descriptionBinding.customToolbar.setBackAction(v -> getActivity().onBackPressed());
        return descriptionBinding.getRoot();
    }

    private void setUp(View view) {
        startActivity(new Intent(Settings.ACTION_SETTINGS));
    }

    public static ConnectionDescriptionFragment newInstance() {
        ConnectionDescriptionFragment fragment = new ConnectionDescriptionFragment();
        Bundle bundle = new Bundle();
        fragment.setArguments(bundle);
        return fragment;
    }

//    @Override
//    protected boolean onBackPressed() {
//        if (getActivity() != null && getActivity() instanceof BaseActivity) {
//            BaseActivity activity = (BaseActivity) getActivity();
//            // TODO: 2017/10/26
//           // activity.onViewAction(JFGView.VIEW_ACTION_OK, null, null);
//        }
//        return super.onBackPressed();
//    }

    @Override
    public boolean performBackIntercept(boolean willExit) {

        return super.performBackIntercept(willExit);

    }
}
