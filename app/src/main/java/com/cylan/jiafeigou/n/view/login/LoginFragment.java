package com.cylan.jiafeigou.n.view.login;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.NewHomeActivity;
import com.cylan.jiafeigou.n.model.BeanInfoLogin;
import com.cylan.jiafeigou.n.mvp.contract.login.LoginContract;
import com.cylan.jiafeigou.n.mvp.impl.login.LoginPresenterImpl;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by chen on 5/26/16.
 */
public class LoginFragment extends Fragment implements LoginContract.ViewRequiredOps {


    @BindView(R.id.btnLogin)
    Button btnLogin;

    private LoginContract.PresenterOps mPresent;
    private BeanInfoLogin infoLogin;

    public static LoginFragment newInstance(Bundle bundle) {
        LoginFragment fragment = new LoginFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_login_layout, container, false);
        ButterKnife.bind(this, mView);

        initView();
        return mView;
    }

    private void initView() {
        mPresent = new LoginPresenterImpl(this);
    }

    private void initData() {
        infoLogin = new BeanInfoLogin();
    }


    @Override
    public void onResume() {
        initData();
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @OnClick(R.id.btnLogin)
    public void onLoginClick() {
        infoLogin.userName = "";
        infoLogin.pwd = "";
        mPresent.executeLogin(infoLogin);
        startActivity(new Intent(getActivity(), NewHomeActivity.class));
        getActivity().finish();
    }

    @Override
    public void LoginExecuted(String succeed) {
        if (succeed.equals("succeed")) {
            //login succeed
        } else {
            //show the reason of failed,and "succeed" carried the message
        }
    }

}
