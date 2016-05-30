package com.cylan.jiafeigou.n.view.home;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.home.HomeDiscoveryContract;

import butterknife.BindView;
import butterknife.ButterKnife;
import in.srain.cube.views.ptr.PtrClassicFrameLayout;

public class HomeDiscoveryFragment extends Fragment implements HomeDiscoveryContract.View {

    @BindView(R.id.fLayout_main_content_holder)
    PtrClassicFrameLayout fLayoutMainContentHolder;
    HomeDiscoveryContract.Presenter presenter;

    public static HomeDiscoveryFragment newInstance(Bundle bundle) {
        HomeDiscoveryFragment fragment = new HomeDiscoveryFragment();
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
        View view = inflater.inflate(R.layout.fragment_home_discovery, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void setPresenter(HomeDiscoveryContract.Presenter presenter) {
        this.presenter = presenter;
    }
}
