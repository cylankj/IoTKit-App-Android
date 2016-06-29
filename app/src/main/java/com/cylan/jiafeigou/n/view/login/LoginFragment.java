package com.cylan.jiafeigou.n.view.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.NewHomeActivity;
import com.cylan.jiafeigou.n.mvp.contract.login.LoginModelContract;
import com.cylan.jiafeigou.n.mvp.impl.ForgetPwdPresenterImpl;
import com.cylan.jiafeigou.n.mvp.model.LoginAccountBean;
import com.cylan.jiafeigou.utils.AnimatorUtils;
import com.cylan.jiafeigou.utils.IMEUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.LoginButton;
import com.cylan.utils.RandomUtils;
import com.superlog.SLog;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;

import static com.cylan.jiafeigou.n.view.login_ex.LoginContainerFragment.KEY_ACTIVITY_FRAGMENT_CONTAINER_ID;

/**
 * 登陆主界面
 */
public class LoginFragment extends LoginBaseFragment implements LoginModelContract.LoginView {
    private static final String TAG = "LoginFragment";
    public static final String KEY_TEMP_ACCOUNT = "temp_account";
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


    @BindView(R.id.Layout_login_box)
    RelativeLayout rLayoutLoginInput;

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

    @BindView(R.id.lb_login_commit)
    LoginButton lbLogin;


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

    @Override
    public void onStart() {
        super.onStart();
        if (loginPresenter != null) {
            loginPresenter.start();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (loginPresenter != null) loginPresenter.stop();
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        return super.onCreateAnimation(transit, enter, nextAnim);
    }

    @OnFocusChange(R.id.et_login_username)
    public void onUserNameLoseFocus(View view, boolean focus) {
        Log.d(TAG, "onUserNameLoseFocus: " + focus);
        final boolean visibility = !TextUtils.isEmpty(etLoginUsername.getText()) && focus;
        ivLoginClearUsername.setVisibility(visibility ? View.VISIBLE : View.INVISIBLE);
    }

    @OnFocusChange(R.id.et_login_pwd)
    public void onPwdLoseFocus(View view, boolean focus) {
        Log.d(TAG, "onPwdLoseFocus: " + focus);
        final boolean visibility = !TextUtils.isEmpty(etLoginPwd.getText()) && focus;
        ivLoginClearPwd.setVisibility(visibility ? View.VISIBLE : View.INVISIBLE);
    }

    /**
     * 显示当前的布局
     */
    private void showLayout() {
        showAllLayout(false);
    }

    /**
     * 动画的表现方式
     *
     * @param orientation true 为垂直方向展现动画，false为水平方向展现动画
     */
    private void showAllLayout(boolean orientation) {
        AnimatorUtils.onSimpleBounceUpIn(rLayoutLoginInput, 1000, 20);
        AnimatorUtils.onSimpleBounceUpIn(rLayoutLoginThirdParty, 200, 400);
    }


    private void printFragment() {
        List<Fragment> list = getFragmentManager().getFragments();
        for (Fragment f : list) {
            if (f != null) {
                SLog.e(f.toString());
            }
        }
    }


    /**
     * 初始化view
     */
    private void initView() {
        setViewEnableStyle(lbLogin, false);
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
        ivLoginClearPwd.setVisibility(flag ? View.INVISIBLE : View.VISIBLE);
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
                loginPresenter.getQQAuthorize(getActivity());
                break;
            case R.id.tv_xlLogin_commit:
                loginPresenter.getSinaAuthorize(getActivity());
                break;
        }
    }

    /**
     * 清除焦点。
     */
    private void enableEditTextCursor(boolean enable) {
        if (isResumed() && getActivity() != null) {
            etLoginPwd.setFocusable(enable);
            etLoginPwd.setFocusableInTouchMode(enable);
            etLoginUsername.setFocusable(enable);
            etLoginUsername.setFocusableInTouchMode(enable);
        }
    }

    @OnClick(R.id.lb_login_commit)
    public void login(View view) {
        IMEUtils.hide(getActivity());
        enableEditTextCursor(false);
        lbLogin.viewZoomSmall();
        AnimatorUtils.viewAlpha(tvForgetPwd, false, 300, 0);
        AnimatorUtils.viewTranslationY(rLayoutLoginThirdParty, false, 100, 0, 800, 500);
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
        if (getActivity() != null) {
            Bundle bundle = getArguments();
            final int containerId = bundle.getInt(KEY_ACTIVITY_FRAGMENT_CONTAINER_ID);
            final String tempAccount = etLoginUsername.getText().toString().trim();
            bundle.putInt(ForgetPwdFragment.ACCEPT_TYPE, RandomUtils.getRandom(2));
            bundle.putString(KEY_TEMP_ACCOUNT, tempAccount);
            ForgetPwdFragment forgetPwdFragment = ForgetPwdFragment.newInstance(bundle);
            new ForgetPwdPresenterImpl(forgetPwdFragment);
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.slide_right_in, R.anim.slide_out_left
                            , R.anim.slide_out_right, R.anim.slide_out_right)
                    .add(containerId, forgetPwdFragment, "forgetPwd")
                    .addToBackStack("forgetPwd")
                    .commit();
        }
    }

    @Override
    public void onAttach(Context context) {
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


    @Override
    public void loginResult(final LoginAccountBean login) {
        if (login != null && login.ret == 0) {
            getContext().startActivity(new Intent(getContext(), NewHomeActivity.class));
            getActivity().finish();
        } else {
            resetView();
        }
    }

    /**
     * 登录超时，或者失败后动画复位
     */
    private void resetView() {
        enableEditTextCursor(true);
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
