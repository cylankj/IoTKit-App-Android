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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.cloud.CloudCorrelationDoorBellContract;
import com.cylan.jiafeigou.n.mvp.impl.cloud.CloudCorrelationDoorBellPresenterImp;
import com.cylan.jiafeigou.n.mvp.model.BellBean;
import com.cylan.jiafeigou.n.view.adapter.RelationDoorBellAdapter;
import com.cylan.jiafeigou.n.view.adapter.UnRelationDoorBellAdapter;
import com.cylan.jiafeigou.support.superadapter.internal.SuperViewHolder;
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
    @BindView(R.id.progress_show)
    ProgressBar progressShow;

    private RelationDoorBellAdapter hasRelativeAdapter;

    private CloudCorrelationDoorBellContract.Presenter presenter;
    private UnRelationDoorBellAdapter unRelationDoorBellAdapter;

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

    @Override
    public String getUuid() {
        return null;
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
    public void initRelativeRecycleView(List<BellBean> list) {
        hasRelativeAdapter = new RelationDoorBellAdapter(getContext(), list, null);
        rcyHasCorrelation.setLayoutManager(new LinearLayoutManager(getContext()));
        rcyHasCorrelation.setAdapter(hasRelativeAdapter);
    }

    @Override
    public void initUnRelativeRecycleView(List<BellBean> list) {
        unRelationDoorBellAdapter = new UnRelationDoorBellAdapter(getContext(), list, null);
        rcyUncorrelation.setLayoutManager(new LinearLayoutManager(getContext()));
        rcyUncorrelation.setAdapter(unRelationDoorBellAdapter);
    }

    @Override
    public void showNoRelativeDevicesView(int flag) {
        if (flag == 1) {
            tvRelativeDoorBell.setVisibility(View.VISIBLE);
        } else {
            tvRelativeDoorBell.setVisibility(View.GONE);
        }
    }

    @Override
    public void showNoUnRelativeDevicesView(int flag) {
        if (flag == 1) {
            tvUnrelativeDoorBell.setVisibility(View.GONE);
        } else {
            tvUnrelativeDoorBell.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void setOnUnRelItemClickListener(UnRelationDoorBellAdapter.OnRelativeClickListener listener) {
        unRelationDoorBellAdapter.setOnRelativeClickListener(listener);
    }

    @Override
    public void setOnRelaItemClickListener(RelationDoorBellAdapter.OnUnRelaItemClickListener listener) {
        hasRelativeAdapter.setOnUnRelaItemClickListener(listener);
    }

    @Override
    public void notifyUnRelativeRecycle(SuperViewHolder holder, int viewType, int layoutPosition, BellBean item, int flag) {
        if (flag == 1) {
            unRelationDoorBellAdapter.remove(layoutPosition);
            unRelationDoorBellAdapter.notifyDataSetChanged();
            if (unRelationDoorBellAdapter.getCount() == 0) {
                showNoUnRelativeDevicesView(flag);
            }
        } else if (flag == 2) {
            unRelationDoorBellAdapter.add(item);
            unRelationDoorBellAdapter.notifyDataSetChanged();
            if (unRelationDoorBellAdapter.getCount() != 0) {
                showNoUnRelativeDevicesView(flag);
            }
        }
    }

    @Override
    public void notifyRelativeRecycle(SuperViewHolder holder, int viewType, int layoutPosition, BellBean item, int flag) {
        if (flag == 1) {
            hasRelativeAdapter.add(item);
            hasRelativeAdapter.notifyDataSetChanged();
            if (hasRelativeAdapter.getCount() != 0) {
                showNoRelativeDevicesView(flag);
            }
        } else if (flag == 2) {
            hasRelativeAdapter.remove(layoutPosition);
            hasRelativeAdapter.notifyDataSetChanged();
            if (hasRelativeAdapter.getCount() == 0) {
                showNoRelativeDevicesView(flag);
            }
        }
    }

    @Override
    public void showProgress() {
        progressShow.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgress() {
        progressShow.setVisibility(View.INVISIBLE);
    }

}
