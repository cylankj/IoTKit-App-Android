package com.cylan.jiafeigou.n.view.home;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.engine.task.TestPresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

import butterknife.BindView;
import butterknife.ButterKnife;
import in.srain.cube.views.ptr.PtrClassicFrameLayout;

public class HomeMineFragment extends Fragment implements BaseView {

    private static final String TAG = "HomeMineFragment";
    @BindView(R.id.fLayout_main_content_holder)
    PtrClassicFrameLayout fLayoutMainContentHolder;
    TestPresenter testPresenter;

    public static HomeMineFragment newInstance(Bundle bundle) {
        HomeMineFragment fragment = new HomeMineFragment();
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
        testPresenter = new TestPresenter(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home_mine, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        testPresenter.start();

    }

    @Override
    public void onStop() {
        super.onStop();
        testPresenter.stop();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void initView() {

    }
}
