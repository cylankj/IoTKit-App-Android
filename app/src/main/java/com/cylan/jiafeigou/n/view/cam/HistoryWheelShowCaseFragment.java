package com.cylan.jiafeigou.n.view.cam;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cylan.jiafeigou.R;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by yanzhendong on 2017/7/11.
 */

public class HistoryWheelShowCaseFragment extends Fragment {


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.view_camera_wheel_case, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
    }

    @OnClick(R.id.btn_ok)
    public void ok() {
        getActivity().getSupportFragmentManager().popBackStack(getClass().getSimpleName(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }
}
