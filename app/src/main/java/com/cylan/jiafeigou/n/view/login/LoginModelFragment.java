package com.cylan.jiafeigou.n.view.login;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.impl.LoginPresenterImpl;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.superlog.SLog;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 *
 */
public class LoginModelFragment extends LoginBaseFragment {

    @BindView(R.id.iv_login_top_left)
    public ImageView ivTopLeft;
    @BindView(R.id.tv_login_top_center)
    public TextView tvTopCenter;
    @BindView(R.id.tv_login_top_right)
    public TextView tvTopRight;
    @BindView(R.id.fLayout_login_container)
    FrameLayout fLayoutLoginContainer;

    @BindView(R.id.rLayout_login_top)
    View topView;


    public LoginModelFragment() {
        // Required empty public constructor
    }


    public static LoginModelFragment newInstance(Bundle bundle) {
        LoginModelFragment fragment = new LoginModelFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            ///
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        List<Fragment> list = getFragmentManager().getFragments();

        for (Fragment f : list) {
            if (f != null) {
                SLog.w(f.toString());
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login_model, container, false);
        ButterKnife.bind(this, view);
        addOnTouchListener(view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewUtils.setViewMarginStatusBar(topView);
        showLoginFragment();
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    private void showLoginFragment() {
        Bundle bundle = new Bundle();
        bundle.putBoolean("first", true);
        LoginFragment fragment = LoginFragment.newInstance(null);
        fragment.setArguments(bundle);
        new LoginPresenterImpl(fragment);
        getChildFragmentManager().beginTransaction().
                setCustomAnimations(R.anim.slide_down_in, R.anim.slide_down_out)
                .add(R.id.fLayout_login_container, fragment, "login").commit();
    }


    @OnClick({R.id.iv_login_top_left})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_login_top_left:
                getActivity().finish();
                break;
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }


}
