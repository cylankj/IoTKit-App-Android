package com.cylan.jiafeigou.n.view.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
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
import com.cylan.jiafeigou.n.model.LoginAccountBean;
import com.cylan.jiafeigou.n.mvp.contract.login.LoginModelContract;
import com.cylan.jiafeigou.utils.AnimatorUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.LoginButton;
import com.superlog.SLog;
import com.tencent.connect.common.Constants;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnTextChanged;

/**
 * 登陆主界面
 */
public class LoginFragment extends LoginBaseFragment implements LoginModelContract.LoginView {

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


    @BindView(R.id.lLayout_login_input)
    LinearLayout lLayoutLoginInput;

    @BindView(R.id.rLayout_login_third_party)
    RelativeLayout rLayoutLoginThirdParty;

    @BindView(R.id.rLayout_login)
    RelativeLayout rLayoutLogin;

    @BindView(R.id.tv_qqLogin_commit)
    TextView tvQqLoginCommit;

    @BindView(R.id.tv_xlLogin_commit)
    TextView tvXlLoginCommit;

    @BindView(R.id.tv_login_forget_pwd)
    TextView tvForgetPwd;

    @BindView(R.id.lb_login_commint)
    LoginButton lbLogin;


    /**
     * 倒计时，用作超时统计
     */
    CountDownTimer timer;

    // 开始登录的时间
    long begin;


    private LoginModelContract.LoginPresenter loginPresenter;

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
        printFragment();
        showLayout();
        return view;
    }

    /**
     * 显示当前的布局
     */
    private void showLayout() {
        boolean first = false;
        if (getArguments() != null) {
            first = this.getArguments().getBoolean("first", false);
        }
        final boolean flag = first;  //判断动画的表现方式
        lLayoutLoginInput.postDelayed(new Runnable() {
            @Override
            public void run() {
                showAllLayout(flag);
            }
        }, flag ? 500 : 10);
    }

    /**
     * 动画的表现方式
     *
     * @param orientation ture 为垂直方向展现动画，false为水平方向展现动画
     */
    private void showAllLayout(boolean orientation) {
        ViewGroup parent = (ViewGroup) lLayoutLoginInput.getParent();
        int distance = parent.getHeight() - lLayoutLoginInput.getTop();
        if (orientation) {
            AnimatorUtils.viewTranslationY(lLayoutLoginInput, true, 0, 800, 0, 600);
            AnimatorUtils.viewTranslationY(rLayoutLoginThirdParty, true, 200, 800, 0, 600);
        } else {
            AnimatorUtils.viewTranslationX(lLayoutLoginInput, true, 0, -800, 0, 500);
            AnimatorUtils.viewTranslationX(rLayoutLoginThirdParty, true, 100, -800, 0, 500);
        }
    }


    private void printFragment() {
        List<Fragment> list = getFragmentManager().getFragments();
        for (Fragment f : list) {
            if (f != null) {
                SLog.e(f.toString());
            }
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        if (loginPresenter != null) {
            loginPresenter.start();
        }
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

    /**
     * 初始化view
     */
    private void initView() {
        lLayoutLoginInput.setVisibility(View.INVISIBLE);
        rLayoutLoginThirdParty.setVisibility(View.INVISIBLE);
        setViewEnableStyle(lbLogin, false);
        timer = new CountDownTimer(30 * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                resetView();
            }
        };
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
        if (flag || s.length() < 6) {
            setViewEnableStyle(lbLogin, false);
        } else if (!TextUtils.isEmpty(etLoginUsername.getText().toString())) {
            setViewEnableStyle(lbLogin, true);
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
        String pwd = etLoginPwd.getText().toString().trim();
        if (flag) {
            setViewEnableStyle(lbLogin, false);
        } else if (!TextUtils.isEmpty(pwd) && pwd.length() >= 6) {
            setViewEnableStyle(lbLogin, true);
        }
    }

    @OnClick({
            R.id.tv_qqLogin_commit,
            R.id.tv_xlLogin_commit,
            R.id.iv_login_clear_pwd,
            R.id.iv_login_clear_username,
            R.id.tv_login_forget_pwd
    })
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_login_clear_pwd:
                etLoginPwd.getText().clear();
                break;
            case R.id.iv_login_clear_username:
                etLoginUsername.getText().clear();
                break;
            case R.id.tv_login_forget_pwd:
                forgetPwd();
                break;
            case R.id.tv_qqLogin_commit:
                loginPresenter.getQQAuthorize();
                break;
            case R.id.tv_xlLogin_commit:
                loginPresenter.getSinaAuthorize();
                break;
        }
    }


    @OnClick(R.id.lb_login_commint)
    public void login(View view) {
        lbLogin.viewZoomSmall();
        AnimatorUtils.viewAlpha(tvForgetPwd, false, 300, 0);
        AnimatorUtils.viewTranslationY(rLayoutLoginThirdParty, false, 100, 0, 800, 500);
        timer.start();  //开始倒计时
        begin = System.currentTimeMillis(); // 记时，用于计算登陆成功后的耗时，如果太快的话，要做相应的延时，一遍动画执行一圈。
        LoginAccountBean login = new LoginAccountBean();
        login.userName = etLoginUsername.getText().toString().trim();
        login.pwd = etLoginPwd.getText().toString().trim();
        if (loginPresenter != null) {
            loginPresenter.executeLogin(login);
        }
    }


    /**
     * 忘记密码
     */
    private void forgetPwd() {
        //忘记密码
        ForgetPwdFragment fragment = (ForgetPwdFragment) getFragmentManager().findFragmentByTag("forget");
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        if (fragment == null) {
            fragment = ForgetPwdFragment.newInstance(null);
            ft.replace(R.id.fLayout_login_container, fragment, "forget");
        }
        ft.hide(this).show(fragment).commit();
    }

    @Override
    public void onAttach(Context context) {
        initParentFragmentView();
        super.onAttach(context);
    }


    private void initParentFragmentView() {
        LoginModelFragment fragment = (LoginModelFragment) getActivity()
                .getSupportFragmentManager().getFragments().get(0);
        fragment.tvTopRight.setText("注册");
        fragment.tvTopCenter.setText("登录");
        fragment.tvTopRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRegisterFragment();
            }
        });
    }


    /**
     * 判断时区，如果是中国的就首选手机注册
     */
    private void showRegisterFragment() {
        Fragment fragment;
        fragment = getFragmentManager().findFragmentByTag("register");
        if (inChina()) {
            if (fragment == null) {
                fragment = RegisterByPhoneFragment.newInstance(null);
            }
        } else {
            if (fragment == null) {
                fragment = RegisterByMailFragment.newInstance(null);
            }
        }
        getActivity().getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                .replace(R.id.fLayout_login_container, fragment, "register").commit();

    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //在某些低端机上调用登录后，由于内存紧张导致APP被系统回收，登录成功后无法成功回传数据
        if (requestCode == Constants.REQUEST_API) {
            if (resultCode == Constants.REQUEST_LOGIN) {
//                TencentLoginUtils curTencent = mPresenter.getTencentObj();
//                if (curTencent != null)
//                    curTencent.getMyTencent().handleLoginData(data, new BaseUiListener());
            }
        } else {
//            SinaWeiboUtil curSina = mPresenter.getSinaObj();
//            if (curSina != null && curSina.getMySsoHandler() != null)
//                curSina.getMySsoHandler().authorizeCallBack(requestCode, resultCode, data);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onStop() {
        super.onStop();
        loginPresenter.stop();
    }

    @Override
    public void loginResult(final LoginAccountBean login) {
        int delay = 0;
        timer.cancel(); // 有结果返回了，不需要超时设置
        if (System.currentTimeMillis() - begin < 1200) {
            delay = 800;  //留有足够的时间展示动画
        }
        lbLogin.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (login != null && login.ret == 0) {
                    getContext().startActivity(new Intent(getContext(), NewHomeActivity.class));
                    getActivity().finish();
                } else {
                    resetView();
                }
            }
        }, delay);

    }

    /**
     * 登录超时，或者失败后动画复位
     */
    private void resetView() {
        lbLogin.viewZoomBig();
        AnimatorUtils.viewAlpha(tvForgetPwd, true, 300, 0);
        AnimatorUtils.viewTranslationY(rLayoutLoginThirdParty, true, 100, 800, 0, 200);
    }


    @Override
    public void setPresenter(LoginModelContract.LoginPresenter presenter) {
        loginPresenter = presenter;
        SLog.e("setPresenter");
    }

    @Override
    public void onQQAuthorizeResult(int ret) {
        //授权成功后，直接登录，不需要回调过来
        ToastUtil.showToast(getContext(), "授权" + (ret == 2 ? "失败" : "取消"));
    }

    @Override
    public void onSinaAuthorizeResult(int ret) {
        //授权成功后，直接登录，不需要回调过来
        ToastUtil.showToast(getContext(), "授权" + (ret == 2 ? "失败" : "取消"));
    }
}
