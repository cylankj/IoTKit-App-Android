package com.cylan.jiafeigou.n.view.cloud;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.cloud.CloudCorrelationDoorBellContract;
import com.cylan.jiafeigou.n.mvp.impl.cloud.CloudCorrelationDoorBellPresenterImp;
import com.cylan.jiafeigou.n.mvp.model.BellInfoBean;
import com.cylan.jiafeigou.n.view.adapter.RelationDoorBellAdapter;
import com.cylan.jiafeigou.utils.ViewUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 作者：zsl
 * 创建时间：2016/9/29
 * 描述：
 */
public class CloudCorrelationDoorBellFragment extends Fragment implements CloudCorrelationDoorBellContract.View {

    @BindView(R.id.imgV_top_bar_center)
    TextView ivInformationBack;
    @BindView(R.id.rcy_has_correlation)
    RecyclerView rcyHasCorrelation;
    @BindView(R.id.rcy_uncorrelation)
    RecyclerView rcyUncorrelation;
    @BindView(R.id.fLayout_top_bar_container)
    FrameLayout fLayoutTopBarContainer;
    @BindView(R.id.ll_list_container)
    LinearLayout llListContainer;
    @BindView(R.id.tv_no_device)
    TextView tvNoDevice;
    @BindView(R.id.tv_relative_door_bell)
    TextView tvRelativeDoorBell;
    @BindView(R.id.tv_unrelative_door_bell)
    TextView tvUnrelativeDoorBell;

    private RelationDoorBellAdapter hasRelativeAdapter;

    private CloudCorrelationDoorBellContract.Presenter presenter;

    public static CloudCorrelationDoorBellFragment newInstance(Bundle bundle) {
        CloudCorrelationDoorBellFragment fragment = new CloudCorrelationDoorBellFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (presenter != null) {
            presenter.start();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cloud_correlation_door_bell, container, false);
        ButterKnife.bind(this, view);
        initPresenter();
        return view;
    }

    private void initPresenter() {
        presenter = new CloudCorrelationDoorBellPresenterImp(this);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        initTopBar();
    }

    private void initTopBar() {
        ViewUtils.setViewPaddingStatusBar(fLayoutTopBarContainer);
    }

    @Override
    public void setPresenter(CloudCorrelationDoorBellContract.Presenter presenter) {

    }

    @OnClick(R.id.imgV_top_bar_center)
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imgV_top_bar_center:
                getFragmentManager().popBackStack();
                break;
        }
    }

    @Override
    public void initRecycleView(List<BellInfoBean> list) {
        hasRelativeAdapter = new RelationDoorBellAdapter(getContext(), list, null);
        rcyHasCorrelation.setLayoutManager(new LinearLayoutManager(getContext()));
        rcyHasCorrelation.setAdapter(hasRelativeAdapter);
    }

    @Override
    public void showNoRelativeDevicesView() {
        tvRelativeDoorBell.setVisibility(View.INVISIBLE);
    }
}
