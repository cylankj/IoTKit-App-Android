package com.cylan.jiafeigou.n.view.cam;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cylan.jiafeigou.R;

import butterknife.ButterKnife;

/**
 * Created by yanzhendong on 2018/1/16.
 */

public class VisitorStatisticsDialogFragment extends DialogFragment {


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_visitor_statistics_pop, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
    }

    public static VisitorStatisticsDialogFragment newInstance() {
        VisitorStatisticsDialogFragment fragment = new VisitorStatisticsDialogFragment();
        return fragment;
    }
}
