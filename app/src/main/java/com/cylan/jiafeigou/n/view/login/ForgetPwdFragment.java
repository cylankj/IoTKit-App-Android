package com.cylan.jiafeigou.n.view.login;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.login.LoginModelContract;
import com.cylan.jiafeigou.n.mvp.presenter.LoginPresenterImpl;
import com.superlog.SLog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

/**
 * 忘记密码
 * Created by lxh on 16-6-14.
 */

public class ForgetPwdFragment extends LoginBaseFragment {


    @BindView(R.id.et_forget_username)
    EditText etForgetUsername;
    @BindView(R.id.iv_forget_clear_username)
    ImageView ivForgetClearUsername;
    @BindView(R.id.tv_model_commit)
    TextView tvModelCommit;
    @BindView(R.id.lLayout_register_input)
    LinearLayout lLayoutRegisterInput;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forget_pwd, container, false);
        ButterKnife.bind(this, view);
        initView(view);
        return view;
    }

    public static ForgetPwdFragment newInstance(Bundle bundle) {
        ForgetPwdFragment fragment = new ForgetPwdFragment();
        fragment.setArguments(bundle);
        return fragment;
    }


    private void initView(View view) {

    }


    @OnClick(R.id.iv_forget_clear_username)
    public void clearUsername(View view) {
        etForgetUsername.getText().clear();
    }


    //判读是手机号还是邮箱
    private void next() {
        String account = etForgetUsername.getText().toString();
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        LoginBaseFragment fragment;
        //做判断
        if (account.length() > 1) {
            fragment = FindPwdByPhoneFragment.newInstance(null);
            Bundle bundle = new Bundle();
            bundle.putString("phone", account);
            fragment.setArguments(bundle);
        } else {
            fragment = LoginFragment.newInstance(null);
        }
        new LoginPresenterImpl((LoginModelContract.LoginView) fragment);

        ft.add(R.id.fLayout_login_container, fragment).commit();
    }


    @OnTextChanged(R.id.et_forget_username)
    public void userNameChange(CharSequence s, int start, int before, int count) {
        boolean flag = TextUtils.isEmpty(s);
        setViewEnableStyle(tvModelCommit, !flag);
        ivForgetClearUsername.setVisibility(flag ? View.GONE : View.VISIBLE);
    }


    @OnClick(R.id.tv_model_commit)
    public void forgetPwdCommit(View v) {
        next();
//        float curTranslationY = v.getTranslationY();
//        ObjectAnimator animator = ObjectAnimator.ofFloat(v, "translationY", curTranslationY + 20f);
//        animator.setDuration(1000);
//        animator.start();
//        next();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        initParentFragmentView();
        SLog.e("onAttach Context");
    }

    private void initParentFragmentView() {
        LoginModelFragment fragment = (LoginModelFragment) getActivity().getSupportFragmentManager().getFragments().get(0);
        fragment.tvTopCenter.setText("忘记密码");
        fragment.tvTopRight.setVisibility(View.GONE);
    }


}
