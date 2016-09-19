package com.cylan.jiafeigou.n.view.login;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.cylan.jiafeigou.BuildConfig;
import com.cylan.jiafeigou.NewHomeActivity;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.SmartcallActivity;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.mvp.contract.login.LoginModelContract;
import com.cylan.jiafeigou.n.mvp.impl.ForgetPwdPresenterImpl;
import com.cylan.jiafeigou.n.mvp.impl.SetupPwdPresenterImpl;
import com.cylan.jiafeigou.n.mvp.model.LoginAccountBean;
import com.cylan.jiafeigou.utils.AnimatorUtils;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.IMEUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.LoginButton;
import com.cylan.utils.RandomUtils;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;

;


/**
 * 登陆主界面
 */
public class LoginFragment extends android.support.v4.app.Fragment implements LoginModelContract.View {
    private static final String TAG = "Fragment";
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
    CheckBox cbShowPwd;


    @BindView(R.id.vsLayout_login_box)
    ViewSwitcher vsLayoutSwitcher;

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
    @BindView(R.id.iv_top_bar_left)
    ImageView ivLoginTopLeft;
    @BindView(R.id.tv_top_bar_center)
    TextView tvLoginTopCenter;
    @BindView(R.id.tv_top_bar_right)
    TextView tvLoginTopRight;
    @BindView(R.id.rLayout_pwd_input_box)
    FrameLayout rLayoutPwdInputBox;
    @BindView(R.id.view_third_party_center)
    android.view.View viewThirdPartyCenter;
    @BindView(R.id.et_register_input_box)
    EditText etRegisterInputBox;
    @BindView(R.id.iv_register_username_clear)
    ImageView ivRegisterUserNameClear;
    @BindView(R.id.et_verification_input)
    EditText etVerificationInput;
    @BindView(R.id.tv_meter_get_code)
    TextView tvMeterGetCode;
    @BindView(R.id.tv_register_submit)
    TextView tvRegisterSubmit;
    @BindView(R.id.tv_register_way_content)
    TextView tvRegisterWayContent;
    @BindView(R.id.fLayout_verification_code_input_box)
    FrameLayout fLayoutVerificationCodeInputBox;

    private VerificationCodeLogic verificationCodeLogic;
    private int registerWay = JConstant.REGISTER_BY_PHONE;
    private LoginModelContract.Presenter presenter;

    public static LoginFragment newInstance(Bundle bundle) {
        LoginFragment fragment = new LoginFragment();
        fragment.setArguments(bundle);
        return fragment;
    }


    @Nullable
    @Override
    public android.view.View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        android.view.View view = inflater.inflate(R.layout.fragment_login_layout, container, false);
        ButterKnife.bind(this, view);
        addOnTouchListener(view);
        showLayout();
        return view;
    }

    /**
     * 用来点击空白处隐藏键盘
     *
     * @param view
     */
    public void addOnTouchListener(android.view.View view) {
        view.setOnTouchListener(new android.view.View.OnTouchListener() {
            @Override
            public boolean onTouch(android.view.View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    IMEUtils.hide(getActivity());
                }
                return false;
            }
        });
    }

    @Override
    public void onViewCreated(android.view.View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (BuildConfig.DEBUG) {
            etLoginUsername.setText("18576670453");
            etLoginPwd.setText("1234567");
            ivLoginClearPwd.setVisibility(android.view.View.GONE);
            ivLoginClearUsername.setVisibility(android.view.View.GONE);
        }
        decideRegisterWay();
        initView();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (presenter != null) {
            presenter.start();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (presenter != null) presenter.stop();
        if (lbLogin != null) lbLogin.cancelAnim();
//        if (verificationCodeLogic != null) verificationCodeLogic.stop();
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        return super.onCreateAnimation(transit, enter, nextAnim);
    }

    /**
     * 是否显示邮箱注册。
     */
    private void decideRegisterWay() {
        Bundle bundle = getArguments();
        registerWay = bundle.getInt(JConstant.KEY_LOCALE);
        if (registerWay == JConstant.LOCALE_CN) {
            //中国大陆
            tvRegisterWayContent.setVisibility(android.view.View.VISIBLE);
            tvRegisterSubmit.setText(getString(R.string.item_get_verification_code));
        } else {
            //只显示邮箱注册
            etRegisterInputBox.setInputType(EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            etRegisterInputBox.setHint(getString(R.string.item_input_email));
        }

    }

    @OnFocusChange(R.id.et_login_username)
    public void onUserNameLoseFocus(android.view.View view, boolean focus) {
        Log.d(TAG, "onUserNameLoseFocus: " + focus);
        final boolean visibility = !TextUtils.isEmpty(etLoginUsername.getText()) && focus;
        ivLoginClearUsername.setVisibility(visibility ? android.view.View.VISIBLE : android.view.View.INVISIBLE);
    }

    @OnFocusChange(R.id.et_login_pwd)
    public void onPwdLoseFocus(android.view.View view, boolean focus) {
        Log.d(TAG, "onPwdLoseFocus: " + focus);
        final boolean visibility = !TextUtils.isEmpty(etLoginPwd.getText()) && focus;
        ivLoginClearPwd.setVisibility(visibility ? android.view.View.VISIBLE : android.view.View.INVISIBLE);
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
        AnimatorUtils.onSimpleBounceUpIn(vsLayoutSwitcher, 1000, 20);
        AnimatorUtils.onSimpleBounceUpIn(rLayoutLoginThirdParty, 200, 400);
    }


    /**
     * 初始化view
     */
    private void initView() {
        if (getView() != null)
            getView().findViewById(R.id.tv_top_bar_right).setVisibility(View.VISIBLE);
    }

    /**
     * 明文/密文 密码
     *
     * @param buttonView
     * @param isChecked
     */
    @OnCheckedChanged(R.id.cb_show_pwd)
    public void onShowPwd(CompoundButton buttonView, boolean isChecked) {
        ViewUtils.showPwd(etLoginPwd, isChecked);
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
        ivLoginClearPwd.setVisibility(flag ? android.view.View.INVISIBLE : android.view.View.VISIBLE);
        if (flag || s.length() < 6) {
            lbLogin.setEnabled(false);
        } else if (!TextUtils.isEmpty(ViewUtils.getTextViewContent(etLoginUsername))) {
            lbLogin.setEnabled(true);
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
        ivLoginClearUsername.setVisibility(flag ? android.view.View.GONE : android.view.View.VISIBLE);
        final String pwd = ViewUtils.getTextViewContent(etLoginPwd);
        if (flag) {
            lbLogin.setEnabled(false);
        } else if (!TextUtils.isEmpty(pwd) && pwd.length() >= 6) {
            lbLogin.setEnabled(true);
        }
    }

    @OnClick({
            R.id.tv_qqLogin_commit,
            R.id.tv_xlLogin_commit,
            R.id.iv_login_clear_pwd,
            R.id.iv_login_clear_username,
            R.id.tv_login_forget_pwd,
            R.id.iv_top_bar_left,
            R.id.tv_top_bar_right
    })
    public void onClick(android.view.View view) {
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
                presenter.getQQAuthorize(getActivity());
                break;
            case R.id.tv_xlLogin_commit:
                presenter.getSinaAuthorize(getActivity());
                break;
            case R.id.iv_top_bar_left:
                if (getActivity() != null && getActivity() instanceof SmartcallActivity) {
                    getActivity().finish();
                } else if (getActivity() != null && getActivity() instanceof NewHomeActivity) {
                    getActivity().onBackPressed();
                }
                break;
            case R.id.tv_top_bar_right:
                switchBox();
                break;
        }
    }

    /**
     * 页面切换
     */
    private void switchBox() {
        final String content = tvLoginTopRight.getText().toString();
        if (TextUtils.equals(content, getString(R.string.item_register))) {
            //register
            tvLoginTopCenter.setText(getString(R.string.item_register));
            tvLoginTopRight.setText(getString(R.string.SignIn));
            vsLayoutSwitcher.setInAnimation(getContext(), R.anim.slide_in_right_overshoot);
            vsLayoutSwitcher.setOutAnimation(getContext(), R.anim.slide_out_left);
            vsLayoutSwitcher.showNext();
        } else if (TextUtils.equals(content, getString(R.string.SignIn))) {
            tvLoginTopCenter.setText(getString(R.string.SignIn));
            tvLoginTopRight.setText(getString(R.string.item_register));
            //延时200ms,
            vsLayoutSwitcher.setInAnimation(getContext(), R.anim.slide_in_left_overshoot);
            vsLayoutSwitcher.setOutAnimation(getContext(), R.anim.slide_out_right);
            vsLayoutSwitcher.showPrevious();
        }
    }

    /**
     * 清除焦点。
     */
    private void enableEditTextCursor(boolean enable) {
        if (isResumed() && getActivity() != null) {
            ViewUtils.enableEditTextCursor(etLoginPwd, enable);
            ViewUtils.enableEditTextCursor(etLoginUsername, enable);
        }
    }

    @OnClick(R.id.lb_login_commit)
    public void login(android.view.View view) {
        IMEUtils.hide(getActivity());
        lbLogin.viewZoomSmall();
        AnimatorUtils.viewAlpha(tvForgetPwd, false, 300, 0);
        AnimatorUtils.viewTranslationY(rLayoutLoginThirdParty, false, 100, 0, 800, 500);
        LoginAccountBean login = new LoginAccountBean();
        login.userName = ViewUtils.getTextViewContent(etLoginUsername);
        login.pwd = ViewUtils.getTextViewContent(etLoginPwd);
        if (presenter != null) {
            presenter.executeLogin(login);
        }
        enableEditTextCursor(false);
        enableOtherBtn(false);
    }

    /**
     * 直到登陆状态返回
     *
     * @param enable
     */
    private void enableOtherBtn(boolean enable) {
        tvLoginTopRight.setEnabled(enable);
        cbShowPwd.setEnabled(enable);
    }

    /**
     * 忘记密码
     */
    private void forgetPwd() {
        //忘记密码
        if (getActivity() != null) {
            Bundle bundle = getArguments();
            final int containerId = bundle.getInt(JConstant.KEY_ACTIVITY_FRAGMENT_CONTAINER_ID);
            final String tempAccount = ViewUtils.getTextViewContent(etLoginUsername);
            bundle.putInt(JConstant.KEY_LOCALE, RandomUtils.getRandom(2));
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
        enableOtherBtn(true);
        lbLogin.viewZoomBig();
        AnimatorUtils.viewAlpha(tvForgetPwd, true, 300, 0);
        AnimatorUtils.viewTranslationY(rLayoutLoginThirdParty, true, 100, 800, 0, 200);
    }


    @Override
    public void setPresenter(LoginModelContract.Presenter presenter) {
        this.presenter = presenter;
        AppLogger.e("setPresenter");
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

    @OnTextChanged(R.id.et_register_input_box)
    public void onRegisterEtChange(CharSequence s, int start, int before, int count) {
        boolean result;
        if (registerWay == JConstant.REGISTER_BY_PHONE) {
            result = JConstant.PHONE_REG.matcher(s).find();
        } else {
            result = Patterns.EMAIL_ADDRESS.matcher(s).find();
        }
        ivRegisterUserNameClear.setVisibility(!TextUtils.isEmpty(s) ? android.view.View.VISIBLE : android.view.View.GONE);
        tvRegisterSubmit.setEnabled(result);
    }

    @OnTextChanged(R.id.et_verification_input)
    public void onRegisterVerificationCodeEtChange(CharSequence s, int start, int before, int count) {
        boolean isValidCode = TextUtils.isDigitsOnly(s) && s.length() == 6;
    }

    /**
     * 在跳转之前，做一些清理工作
     */
    private void clearSomeThing() {
//        if (verificationCodeLogic != null)
//            verificationCodeLogic.stop();

    }

    /**
     * 手机号和验证码是否准备,或者注册类型{手机，邮箱}
     *
     * @return
     */
    private void jump2NextPage() {
        clearSomeThing();
        //to set up pwd
        Bundle bundle = getArguments();
        if (getActivity() != null && bundle != null) {
            final int containerId = bundle.getInt(JConstant.KEY_ACTIVITY_FRAGMENT_CONTAINER_ID);
            bundle.putString(JConstant.KEY_ACCOUNT_TO_SEND, ViewUtils.getTextViewContent(etRegisterInputBox));
            bundle.putString(JConstant.KEY_PWD_TO_SEND, ViewUtils.getTextViewContent(etRegisterInputBox));
            bundle.putString(JConstant.KEY_VCODE_TO_SEND, ViewUtils.getTextViewContent(etVerificationInput));
            SetupPwdFragment fragment = SetupPwdFragment.newInstance(bundle);
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.slide_right_in, R.anim.slide_out_left
                            , R.anim.slide_out_right, R.anim.slide_out_right)
                    .add(containerId, fragment)
                    .addToBackStack("SetupPwdFragment")
                    .commit();
            new SetupPwdPresenterImpl(fragment);
        }
    }

    /**
     * 验证码输入框
     *
     * @param show
     */
    private void handleVerificationCodeBox(boolean show) {
        fLayoutVerificationCodeInputBox.setVisibility(show ? android.view.View.VISIBLE : android.view.View.GONE);
    }

    /**
     * 注册块，确认按钮逻辑。
     */
    private void handleRegisterConfirm() {
        if (registerWay == JConstant.REGISTER_BY_PHONE) {
            final int codeLen = ViewUtils.getTextViewContent(etVerificationInput).length();
            if (fLayoutVerificationCodeInputBox.isShown()) {
                boolean validPhoneNum = JConstant.PHONE_REG.matcher(ViewUtils.getTextViewContent(etRegisterInputBox)).find();
                if (!validPhoneNum) {
                    Toast.makeText(getActivity(), "invalid phoneNum", Toast.LENGTH_SHORT).show();
                    return;
                }
                boolean validCode = codeLen == JConstant.VALID_VERIFICATION_CODE_LEN;
                //显示重新发送，表示无效验证码
                boolean aliveCode = TextUtils.equals(ViewUtils.getTextViewContent(tvMeterGetCode),
                        getString(R.string.item_reget_verification_code));
                if (validCode && aliveCode) {
                    Toast.makeText(getActivity(), getString(R.string.item_verification_code_is_outdate), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (validCode) {
                    Toast.makeText(getActivity(), "to next", Toast.LENGTH_SHORT).show();
                    jump2NextPage();
                    return;
                } else {
                    Toast.makeText(getActivity(), "invalid code", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            if (verificationCodeLogic == null)
                verificationCodeLogic = new VerificationCodeLogic(tvMeterGetCode);
            verificationCodeLogic.start();
            Toast.makeText(getActivity(), "获取验证码", Toast.LENGTH_SHORT).show();
            //获取验证码
            if (presenter != null)
                presenter.
                        registerByPhone(ViewUtils.getTextViewContent(etRegisterInputBox),
                                ViewUtils.getTextViewContent(etVerificationInput));
            //显示验证码输入框
            handleVerificationCodeBox(true);

            tvRegisterSubmit.setText(getString(R.string.item_carry_on));
        } else {
            final boolean isValidEmail = Patterns.EMAIL_ADDRESS.matcher(ViewUtils.getTextViewContent(etRegisterInputBox)).find();
            if (!isValidEmail) {
                Toast.makeText(getActivity(), "请输入有效的邮箱", Toast.LENGTH_SHORT).show();
                return;
            }
            jump2NextPage();
        }
    }

    private void handleRegisterByMail() {
        if (registerWay == JConstant.REGISTER_BY_PHONE) {
            tvRegisterWayContent.setText(getString(R.string.PHONE_SIGNUP));
            etRegisterInputBox.setText("");
            etRegisterInputBox.setHint(getString(R.string.item_input_email));
            etRegisterInputBox.setInputType(EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            //设置长度
            ViewUtils.setTextViewMaxFilter(etRegisterInputBox, 60);
            registerWay = JConstant.REGISTER_BY_EMAIL;
            tvRegisterSubmit.setText(getString(R.string.item_carry_on));
            handleVerificationCodeBox(false);
        } else if (registerWay == JConstant.REGISTER_BY_EMAIL) {
            tvRegisterWayContent.setText(getString(R.string.EMAIL_SIGNUP));
            etRegisterInputBox.setText("");
            etRegisterInputBox.setHint(getString(R.string.item_input_phone_num));
            etRegisterInputBox.setInputType(EditorInfo.TYPE_CLASS_PHONE);
            ViewUtils.setTextViewMaxFilter(etRegisterInputBox, 11);
            registerWay = JConstant.REGISTER_BY_PHONE;
            tvRegisterSubmit.setText(getString(R.string.item_get_verification_code));
        }
    }

    /**
     * 控件抖动
     */
    private void toBeStar() {
        AnimatorUtils.onSimpleTangle(400, 10, tvRegisterSubmit);
        AnimatorUtils.onSimpleTangle(400, 10, etRegisterInputBox);
    }

    @OnClick({R.id.tv_meter_get_code,
            R.id.tv_register_submit,
            R.id.tv_register_way_content,
            R.id.iv_register_username_clear})
    public void onClickRegister(android.view.View view) {
        switch (view.getId()) {
            case R.id.tv_meter_get_code:
                break;
            case R.id.tv_register_submit:
                handleRegisterConfirm();
                break;
            case R.id.tv_register_way_content:
                handleRegisterByMail();
                toBeStar();
                break;
            case R.id.iv_register_username_clear:
                etRegisterInputBox.setText("");
                break;
        }
    }

    /**
     * 管理着注册方式的切换与输入
     */
    private static class RegisterWay {

    }

    /**
     * 验证码
     */
    private static class VerificationCodeLogic {
        WeakReference<TextView> viewWeakReference;
        CountDownTimer timer;

        public VerificationCodeLogic(TextView textView) {
            this.viewWeakReference = new WeakReference<>(textView);
            initTimer();
            textView.setOnClickListener(new android.view.View.OnClickListener() {
                @Override
                public void onClick(android.view.View v) {
                    if (timer != null)
                        timer.start();
                }
            });
        }

        private void start() {
            if (timer == null) {
                initTimer();
            }
            if (this.viewWeakReference.get() != null) {
                viewWeakReference.get().setEnabled(false);
                timer.start();
            }
        }

        public void stop() {
            timer.cancel();
            if (this.viewWeakReference.get() != null) {
                this.viewWeakReference.get().setText("");
            }
        }

        private void initTimer() {
            timer = new CountDownTimer(JConstant.VERIFICATION_CODE_DEADLINE / 10, 1000L) {
                @Override
                public void onTick(long millisUntilFinished) {
                    if (viewWeakReference.get() == null)
                        return;
                    final String content = millisUntilFinished / 1000 + "s";
                    viewWeakReference.get().setText(content);
                    viewWeakReference.get().setEnabled(false);
                }

                @Override
                public void onFinish() {
                    if (viewWeakReference.get() == null)
                        return;
                    TextView tv = viewWeakReference.get();
                    tv.setText(tv.getContext().getString(R.string.item_reget_verification_code));
                    tv.setEnabled(true);
                }
            };
        }
    }


}
