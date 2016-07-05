package com.cylan.jiafeigou.n.view;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.utils.ViewUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public abstract class BaseTitleFragment extends Fragment {


    @BindView(R.id.tv_login_top_center)
    TextView tvLoginTopCenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_base_title, container, false);
        ButterKnife.bind(this, view);
        ((ViewGroup) view.findViewById(R.id.fLayout_base_fragment_container)).addView(getSubContentView(inflater));
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        initTitleBarAction();
    }

    protected abstract View getSubContentView(LayoutInflater inflater);

    private void initTitleBarAction() {
        View view = getView();
        if (view == null)
            return;
        ViewUtils.setViewMarginStatusBar(view.findViewById(R.id.fLayout_top_bar));
        tvLoginTopCenter.setVisibility(View.GONE);
    }

    @OnClick({R.id.iv_login_top_left, R.id.tv_login_top_center})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_login_top_left:
                getActivity().getSupportFragmentManager().popBackStack();
                break;
        }
    }
}
