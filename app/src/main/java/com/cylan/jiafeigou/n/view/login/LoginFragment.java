package com.cylan.jiafeigou.n.view.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.NewHomeActivity;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.superlog.SLog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnTextChanged;

/**
 * Created by chen on 5/26/16.
 */
public class LoginFragment extends LoginModelFragment {


    @BindView(R.id.iv_login_top_left)
    ImageView ivCloseNav;
    @BindView(R.id.tv_login_top_right)
    TextView tvRegUser;
    @BindView(R.id.rLayout_login_top)
    RelativeLayout rLayoutLoginTop;
    @BindView(R.id.et_login_username)
    EditText etLoginUsername;
    @BindView(R.id.iv_login_clear_username)
    ImageView ivLoginClearUsername;
    @BindView(R.id.et_login_pwd)
    EditText etLoginPwd;
    @BindView(R.id.iv_login_clear_pwd)
    ImageView ivLoginClearPwd;
    @BindView(R.id.cb_show_pwd)
    CheckBox rbShowPwd;
    @BindView(R.id.tv_model_commit)
    TextView tvCommit;
    @BindView(R.id.lLayout_login_input)
    LinearLayout lLayoutLoginInput;
    @BindView(R.id.view_third_party_center)
    View viewThirdPartyCenter;
    @BindView(R.id.rLayout_login_third_party)
    RelativeLayout rLayoutLoginThirdParty;
    @BindView(R.id.rLayout_login)
    RelativeLayout rLayoutLogin;
    @BindView(R.id.tv_login_top_center)
    TextView tvLoginTopCenter;

    public static LoginFragment newInstance(Bundle bundle) {
        LoginFragment fragment = new LoginFragment();
        fragment.setArguments(bundle);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login_layout, container, false);
        ButterKnife.bind(this, view);
        addOnTouchListener(view);
        initView();
        return view;
    }

    /**
     * 明文/密文 密码
     *
     * @param buttonView
     * @param isChecked
     */
    @OnCheckedChanged(R.id.cb_show_pwd)
    public void onShowPwd(CompoundButton buttonView, boolean isChecked) {
        showPwd(etLoginPwd, isChecked);
        etLoginPwd.setSelection(etLoginPwd.length());
    }


    private void initView() {
        tvRegUser.setText("注册");
        tvLoginTopCenter.setText("登录");
    }

    /**
     * 密码变化
     *
     * @param s
     * @param start
     * @param before
     * @param count
     */
    @OnTextChanged(R.id.et_login_pwd)
    public void onPwdChange(CharSequence s, int start, int before, int count) {
        boolean flag = TextUtils.isEmpty(s);
        ivLoginClearPwd.setVisibility(flag ? View.GONE : View.VISIBLE);
        if (flag) {
            setViewEnableStyle(tvCommit, false);
        } else if (!TextUtils.isEmpty(etLoginUsername.getText().toString())) {
            setViewEnableStyle(tvCommit, true);
        }

    }


    /***
     * 账号变化
     *
     * @param s
     * @param start
     * @param before
     * @param count
     */

    @OnTextChanged(R.id.et_login_username)
    public void onUserNameChange(CharSequence s, int start, int before, int count) {
        boolean flag = TextUtils.isEmpty(s);
        ivLoginClearUsername.setVisibility(flag ? View.GONE : View.VISIBLE);
        if (flag) {
            setViewEnableStyle(tvCommit, false);
        } else if (!TextUtils.isEmpty(etLoginPwd.getText().toString())) {
            setViewEnableStyle(tvCommit, true);
        }
    }


    @OnClick(R.id.iv_login_clear_pwd)
    public void clearPwd(View view) {
        etLoginPwd.getText().clear();
    }

    @OnClick(R.id.iv_login_clear_username)
    public void clearUserName(View view) {
        etLoginUsername.getText().clear();
    }


    @OnClick(R.id.tv_model_commit)
    public void loginCommit(View view) {
        if (getActivity() != null) {
            getContext().startActivity(new Intent(getContext(), NewHomeActivity.class));
            getActivity().finish();
        }
    }

    @OnClick(R.id.tv_login_forget_pwd)
    public void forgetPwd(View view) {
        //忘记密码
        ToastUtil.showToast(getContext(), "forget pwd!");
    }

    @OnClick(R.id.tv_login_top_right)
    public void regUser(View view) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment fragment = getFragmentManager().findFragmentByTag("register");
        if (fragment != null) {
            ft.hide(this).show(fragment).commit();
            return;
        }
        String timezone = getTimeZone();
        SLog.i(timezone);
        if (TextUtils.equals(timezone, "GMT+08:00")) {
            fragment = RegisterByPhoneFragment.newInstance(null);
        } else {
            fragment = RegisterByMailFragment.newInstance(null);
        }
        ft.add(R.id.fLayout_login_model_container, fragment, "register").commit();
    }

    @OnClick(R.id.iv_login_top_left)
    public void exitCurrentPage(View view) {
        //退出当前页面
        getActivity().finish();
    }


}
