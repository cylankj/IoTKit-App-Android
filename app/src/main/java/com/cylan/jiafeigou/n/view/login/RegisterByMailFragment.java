package com.cylan.jiafeigou.n.view.login;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.AnimatorUtils;
import com.superlog.SLog;

import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

/**
 * Created by lxh on 16-6-8.
 */

public class RegisterByMailFragment extends LoginModelFragment {

    @BindView(R.id.et_register_username)
    EditText etRegisterUsername;
    @BindView(R.id.iv_register_clear_username)
    ImageView ivRegisterClearUsername;
    @BindView(R.id.tv_model_commit)
    TextView tvCommit;
    @BindView(R.id.tv_register_user_agreement)
    TextView tvRegisterUserAgreement;
    @BindView(R.id.lLayout_register_input)
    LinearLayout lLayoutRegisterInput;
    @BindView(R.id.tv_register_switch)
    TextView tvRegisterSwitch;
    @BindView(R.id.rLayout_register)
    RelativeLayout rLayoutRegister;
    private boolean isChina = true;

    public static RegisterByMailFragment newInstance(Bundle bundle) {
        RegisterByMailFragment fragment = new RegisterByMailFragment();
        fragment.setArguments(bundle);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register_by_mail_layout, container, false);
        ButterKnife.bind(this, view);
        initView(view);
        editTextLimitMaxInput(etRegisterUsername, 60);
        return view;
    }


    private void initView(View view) {
        if (!inChina()) {
            isChina = false;
            tvRegisterSwitch.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isChina) {
            AnimatorUtils.viewTranslationX(tvRegisterSwitch, true, 0, 800, 0, -30, 500);
        }
        AnimatorUtils.viewTranslationX(lLayoutRegisterInput, true, 0, 800, 0, -30, 500);
    }

    @Override
    public void onAttach(Context context) {
        initParentFragmentView();
        super.onAttach(context);
    }


    private void initParentFragmentView() {
        LoginModel1Fragment fragment = (LoginModel1Fragment) getActivity().getSupportFragmentManager().getFragments().get(0);
        fragment.tvTopCenter.setText("注册");
        fragment.tvTopRight.setText("登录");
        fragment.tvTopRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoginFragment();
            }
        });
    }


    private void showLoginFragment() {
        LoginFragment fragment = (LoginFragment) getFragmentManager()
                .findFragmentByTag("login");
        if (fragment == null) {
            fragment = LoginFragment.newInstance(null);
        }
        getActivity().getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right)
                .replace(R.id.fLayout_login_container, fragment, "login").commit();

    }


    @OnClick(R.id.tv_model_commit)
    public void regCommit(View view) {
        SetPwdFragment fragment = SetPwdFragment.newInstance(null);
        ActivityUtils.addFragmentToActivity(getChildFragmentManager(), fragment, R.id.rLayout_register);
    }


    /***
     * 账号变化
     *
     * @param s
     * @param start
     * @param before
     * @param count
     */

    @OnTextChanged(R.id.et_register_username)
    public void onUserNameChange(CharSequence s, int start, int before, int count) {
        boolean flag = TextUtils.isEmpty(s);
        setViewEnableStyle(tvCommit, !flag);
        ivRegisterClearUsername.setVisibility(flag ? View.GONE : View.VISIBLE);
    }

    @OnClick(R.id.iv_register_clear_username)
    public void clearUserName(View view) {
        etRegisterUsername.getText().clear();
    }


    /**
     * 切换注册方式
     *
     * @param view
     */
    @OnClick(R.id.tv_register_switch)
    public void switchRegisterType(View view) {
        RegisterByPhoneFragment fragment = RegisterByPhoneFragment.newInstance(null);
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fLayout_login_container, fragment, "register").commit();
    }

}
