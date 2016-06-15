package com.cylan.jiafeigou.n.view.login;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.cylan.jiafeigou.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 *
 */
public class LoginModel1Fragment extends Fragment {

    @BindView(R.id.iv_login_top_left)
    ImageView ivTopLeft;
    @BindView(R.id.tv_login_top_center)
    TextView tvTopCenter;
    @BindView(R.id.tv_login_top_right)
    TextView tvTopRight;
    @BindView(R.id.fLayout_login_container)
    FrameLayout fLayoutLoginContainer;


    public LoginModel1Fragment() {
        // Required empty public constructor
    }


    public static LoginModel1Fragment newInstance(Bundle bundle) {
        LoginModel1Fragment fragment = new LoginModel1Fragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login_model1, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.slide_down_in, R.anim.slide_down_out)
                .add(R.id.fLayout_login_container,
                        LoginFragment.newInstance(null), "login").commit();
//        tvTopCenter.setText("登录");
        tvTopRight.setText("注册");
        super.onViewCreated(view, savedInstanceState);
    }

    @OnClick({R.id.iv_login_top_left, R.id.tv_login_top_right})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_login_top_left:
                getActivity().finish();
                break;
            case R.id.tv_login_top_right:
                if (flag) {
                    showRegisterFragment();
                } else {
                    showLoginFragment();
                }
                break;
        }
    }

    boolean flag = true;

    private void showLoginFragment() {
        flag = true;
        tvTopCenter.setText("登录");
        tvTopRight.setText("注册");
        LoginFragment fragment = (LoginFragment) getChildFragmentManager().findFragmentByTag("login");
        if (fragment == null) {
            fragment = LoginFragment.newInstance(null);
        }
        getChildFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right)
                .replace(R.id.fLayout_login_container, fragment, "login").commit();

    }

    private void showRegisterFragment() {
        flag = false;
        tvTopCenter.setText("注册");
        tvTopRight.setText("登录");
        RegisterByPhoneFragment fragment = (RegisterByPhoneFragment) getChildFragmentManager().findFragmentByTag("register");
        if (fragment == null) {
            fragment = RegisterByPhoneFragment.newInstance(null);
        }
        getChildFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                .replace(R.id.fLayout_login_container, fragment, "register").commit();
    }


}
