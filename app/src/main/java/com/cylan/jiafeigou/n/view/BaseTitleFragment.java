package com.cylan.jiafeigou.n.view;


import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.utils.ViewUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public abstract class BaseTitleFragment extends Fragment {


    @BindView(R.id.tv_top_bar_center)
    TextView tvLoginTopCenter;
    @BindView(R.id.iv_top_bar_left)
    ImageView ivTopBarLeft;
    @BindView(R.id.tv_top_bar_right)
    TextView tvTopBarRight;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(getSubContentViewId(), container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        initTitleBarAction();
    }

    protected abstract int getSubContentViewId();

    private void initTitleBarAction() {
        View view = getView();
        if (view == null)
            return;
        ViewUtils.setViewMarginStatusBar(view.findViewById(R.id.fLayout_top_bar));
        tvLoginTopCenter.setVisibility(View.GONE);
    }

    @OnClick({R.id.iv_top_bar_left, R.id.tv_top_bar_center})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_top_bar_left:
                getActivity().getSupportFragmentManager().popBackStack();
                break;
        }
    }

    protected void updateNavBackIcon(@DrawableRes final int resId) {
        ivTopBarLeft.setImageResource(resId);
    }

    /**
     * @param stringId
     */
    protected void updateNavCenterContent(@StringRes final int stringId) {
        tvLoginTopCenter.setText(stringId);
    }
}
