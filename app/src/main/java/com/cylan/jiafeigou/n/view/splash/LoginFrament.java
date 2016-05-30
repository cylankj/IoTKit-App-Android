package com.cylan.jiafeigou.n.view.splash;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by chen on 5/26/16.
 */
public class LoginFrament extends Fragment {


    @BindView(R.id.loginTelNum)
    EditText loginTelNum;
    @BindView(R.id.layout_username)
    LinearLayout layoutUsername;
    @BindView(R.id.loginPass)
    EditText loginPass;
    @BindView(R.id.layout_password)
    LinearLayout layoutPassword;
    @BindView(R.id.btnLogin)
    Button btnLogin;
    @BindView(R.id.loginForgetpwd)
    TextView loginForgetpwd;
    @BindView(R.id.login_frame)
    RelativeLayout loginFrame;
    @BindView(R.id.the_third_login_app1)
    TextView theThirdLoginApp1;
    @BindView(R.id.line)
    View line;
    @BindView(R.id.the_third_login_app2)
    TextView theThirdLoginApp2;
    @BindView(R.id.third_login_app)
    LinearLayout thirdLoginApp;

    public static LoginFrament newInstance(Bundle bundle) {
        LoginFrament fragment = new LoginFrament();
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
        return mView;
    }

    @Override
    public void onResume() {
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
}
