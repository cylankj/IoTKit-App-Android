package com.cylan.jiafeigou.n.view.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.cylan.jiafeigou.BuildConfig;
import com.cylan.jiafeigou.NewHomeActivity;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.SmartcallActivity;
import com.cylan.jiafeigou.cache.JCache;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.login.LoginContract;
import com.cylan.jiafeigou.n.mvp.impl.ForgetPwdPresenterImpl;
import com.cylan.jiafeigou.n.mvp.impl.LoginPresenterImpl;
import com.cylan.jiafeigou.n.mvp.impl.SetupPwdPresenterImpl;
import com.cylan.jiafeigou.n.mvp.model.LoginAccountBean;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.AnimatorUtils;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.IMEUtils;
import com.cylan.jiafeigou.utils.LocaleUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.CustomToolbar;
import com.cylan.jiafeigou.widget.LoginButton;
import com.cylan.jiafeigou.widget.dialog.BaseDialog;
import com.cylan.jiafeigou.widget.dialog.SimpleDialogFragment;
import com.facebook.CallbackManager;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.tencent.connect.common.Constants;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;


/**
 * 登陆主界面
 */
public class LoginFragment extends IBaseFragment<LoginContract.Presenter>
        implements LoginContract.View,
        BaseDialog.BaseDialogAction {
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
    LinearLayout rLayoutLogin;

    @BindView(R.id.tv_qqLogin_commit)
    TextView tvQqLoginCommit;

    @BindView(R.id.tv_xlLogin_commit)
    TextView tvXlLoginCommit;

    @BindView(R.id.tv_login_forget_pwd)
    TextView tvForgetPwd;

    @BindView(R.id.lb_login_commit)
    LoginButton lbLogin;
    @BindView(R.id.rLayout_pwd_input_box)
    FrameLayout rLayoutPwdInputBox;
    @BindView(R.id.view_third_party_center)
    View viewThirdPartyCenter;
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

    @BindView(R.id.lLayout_agreement)
    LinearLayout lLayoutAgreement;
    @BindView(R.id.tv_agreement)
    TextView tvAgreement;
    @BindView(R.id.tv_before_agreement)
    TextView before_tvAgreement;
    @BindView(R.id.tv_twitterLogin_commit)
    TextView tvTwitterLoginCommit;
    @BindView(R.id.tv_facebookLogin_commit)
    TextView tvFacebookLoginCommit;
    @BindView(R.id.rLayout_login_third_party_abroad)
    RelativeLayout rLayoutLoginThirdPartyAbroad;
    @BindView(R.id.rLayout_register_box)
    FrameLayout rLayoutRegisterBox;
    @BindView(R.id.rLayout_login_top)
    CustomToolbar rLayoutLoginToolbar;

    private VerificationCodeLogic verificationCodeLogic;
    private int registerWay = JConstant.REGISTER_BY_PHONE;
    private boolean isRegetCode;
    private String tempPhone;

    public static LoginFragment newInstance(Bundle bundle) {
        LoginFragment fragment = new LoginFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        basePresenter = new LoginPresenterImpl(this);
    }

    /**
     * 用来点击空白处隐藏键盘
     *
     * @param view
     */
    public void addOnTouchListener(View view) {
        view.setOnTouchListener((View v, MotionEvent event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                IMEUtils.hide(getActivity());
            }
            return false;
        });
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        addOnTouchListener(view);
        showLayout();
        if (BuildConfig.DEBUG) {
            ivLoginClearPwd.setVisibility(View.GONE);
            ivLoginClearUsername.setVisibility(View.GONE);
        }
        decideRegisterWay();
        initView();
        showRegisterPage();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login_layout, container, false);
        ButterKnife.bind(this, view);
        return view;
    }


    @Override
    public void onStart() {
        super.onStart();
        if (basePresenter != null) {
            basePresenter.start();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (basePresenter != null) basePresenter.stop();
        if (lbLogin != null) lbLogin.cancelAnim();
//        if (verificationCodeLogic != null) verificationCodeLogic.stop();
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        return super.onCreateAnimation(transit, enter, nextAnim);
    }

    /**
     * 不同的入口，在beforeLogin页面点击注册，进入主页页面；否则默认。
     */
    private void showRegisterPage() {
        Bundle bundle = getArguments();
        /**
         * 第三方使用亲友功能跳转到绑定手机这
         */
        if (bundle != null && bundle.containsKey("show_login_fragment")) {
            if (bundle.getBoolean(JConstant.OPEN_LOGIN_TO_BIND_PHONE))
                switchBoxBindPhone();
            else {
                switchBox();
            }
        }
    }

    private void switchBoxBindPhone() {
        //register
        int way = LocaleUtils.getLanguageType(getActivity());
        if (way == JConstant.LOCALE_SIMPLE_CN) {
            rLayoutLoginToolbar.setToolbarTitle(R.string.Tap0_BindPhoneNo);
        } else {
            rLayoutLoginToolbar.setToolbarTitle(R.string.Tap0_BindEmail);
        }
        rLayoutLoginToolbar.getTvToolbarRight().setVisibility(View.GONE);
        tvRegisterWayContent.setVisibility(View.GONE);
        tvAgreement.setVisibility(View.GONE);
        before_tvAgreement.setVisibility(View.GONE);
        vsLayoutSwitcher.setInAnimation(getContext(), R.anim.slide_in_right_overshoot);
        vsLayoutSwitcher.setOutAnimation(getContext(), R.anim.slide_out_left);
        vsLayoutSwitcher.showNext();
    }

    /**
     * 是否显示邮箱注册。
     */
    private void decideRegisterWay() {
        int way = LocaleUtils.getLanguageType(getActivity());
        if (way == JConstant.LOCALE_SIMPLE_CN) {
            tvRegisterWayContent.setVisibility(View.VISIBLE);
            registerWay = JConstant.REGISTER_BY_PHONE;
        }
        registerWay = way == JConstant.LOCALE_SIMPLE_CN ? JConstant.REGISTER_BY_PHONE : JConstant.REGISTER_BY_EMAIL;
        if (registerWay == JConstant.REGISTER_BY_PHONE) {
            //中国大陆
            tvRegisterSubmit.setText(getString(R.string.GET_CODE));
        } else {
            //只显示邮箱注册
            etRegisterInputBox.setInputType(EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            etRegisterInputBox.setHint(getString(R.string.EMAIL_1));
            ViewUtils.setTextViewMaxFilter(etRegisterInputBox, 65);
        }
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
        AnimatorUtils.onSimpleBounceUpIn(vsLayoutSwitcher, 1000, 20);
        AnimatorUtils.onSimpleBounceUpIn(rLayoutLoginThirdParty, 200, 400);
    }

    /**
     * 初始化view
     */
    private void initView() {
        ViewUtils.setChineseExclude(etLoginUsername, 65);
        ViewUtils.setChineseExclude(etLoginPwd, 12);
        ViewUtils.setChineseExclude(etRegisterInputBox, 11);
        ViewUtils.setChineseExclude(etVerificationInput, 6);
        rLayoutLoginToolbar.setBackAction(v -> getActivity().getSupportFragmentManager().popBackStack());
        tvAgreement.setText("《" + getString(R.string.TERM_OF_USE) + "》");
        if (getView() != null)
            getView().findViewById(R.id.tv_toolbar_right).setVisibility(View.VISIBLE);
        ViewUtils.setChineseExclude(etLoginPwd, JConstant.PWD_LEN_MAX);
        //大陆用户显示 第三方登陆
        rLayoutLoginThirdParty.setVisibility(LocaleUtils.getLanguageType(getActivity()) == JConstant.LOCALE_SIMPLE_CN ? View.VISIBLE : View.GONE);
        rLayoutLoginThirdPartyAbroad.setVisibility(LocaleUtils.getLanguageType(getActivity()) == JConstant.LOCALE_SIMPLE_CN ? View.GONE : View.VISIBLE);
        etLoginUsername.setHint(LocaleUtils.getLanguageType(getActivity()) == JConstant.LOCALE_SIMPLE_CN
                ? getString(R.string.SHARE_E_MAIL) : getString(R.string.EMAIL));

        //回显
        String tempAccPwd = basePresenter.getTempAccPwd();
        if (!TextUtils.isEmpty(tempAccPwd)) {
            int i = tempAccPwd.indexOf("|");
            etLoginUsername.setText(tempAccPwd.substring(0, i));
            etLoginPwd.setText(tempAccPwd.substring(i + 1));
        }

        if (!TextUtils.isEmpty(etLoginUsername.getText().toString().trim()) && !TextUtils.isEmpty(etLoginPwd.getText().toString().trim())) {
            lbLogin.setEnabled(true);
        }

        if (!TextUtils.isEmpty(etRegisterInputBox.getText().toString().trim())) {
            tvRegisterSubmit.setEnabled(JConstant.PHONE_REG.matcher(etRegisterInputBox.getText().toString().trim()).find());
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
        ivLoginClearPwd.setVisibility(flag ? View.INVISIBLE : View.VISIBLE);
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
        ivLoginClearUsername.setVisibility(flag ? View.GONE : View.VISIBLE);
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
            R.id.tv_twitterLogin_commit,
            R.id.tv_facebookLogin_commit,
            R.id.tv_toolbar_icon,
            R.id.tv_toolbar_right,
            R.id.tv_agreement
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
                boolean validAccount = JConstant.PHONE_REG.matcher(etLoginUsername.getText()).find()
                        || JConstant.EMAIL_REG.matcher(etLoginUsername.getText()).find();
//                if (!validAccount && !TextUtils.isEmpty(etLoginUsername.getText()))
//                    ToastUtil.showToast(getString(R.string.ACCOUNT_ERR));
                forgetPwd();
                break;
            case R.id.tv_qqLogin_commit:
                if (TextUtils.equals(NetUtils.getNetName(getActivity()), "offLine") || NetUtils.getJfgNetType(getActivity()) == -1) {
                    ToastUtil.showToast(getString(R.string.OFFLINE_ERR_1));
                    return;
                }
                basePresenter.getQQAuthorize(getActivity());
                break;
            case R.id.tv_xlLogin_commit:
                if (TextUtils.equals(NetUtils.getNetName(getActivity()), "offLine") || NetUtils.getJfgNetType(getActivity()) == -1) {
                    ToastUtil.showToast(getString(R.string.OFFLINE_ERR_1));
                    return;
                }
                basePresenter.startSinaAuthorize(getActivity());
                break;
            case R.id.tv_toolbar_icon:
                if (getActivity() != null && getActivity() instanceof SmartcallActivity) {
                    getActivity().finish();
                } else if (getActivity() != null && getActivity() instanceof NewHomeActivity) {
                    getActivity().onBackPressed();
                }
                break;
            case R.id.tv_toolbar_right:
                switchBox();
                break;
            case R.id.tv_agreement: {
                IMEUtils.hide(getActivity());
                AgreementFragment fragment = AgreementFragment.getInstance(null);
                ActivityUtils.addFragmentSlideInFromRight(getActivity().getSupportFragmentManager(),
                        fragment, android.R.id.content);
            }
            break;
            case R.id.tv_twitterLogin_commit:
                basePresenter.getTwitterAuthorize(getActivity());
                break;

            case R.id.tv_facebookLogin_commit:
                basePresenter.getFaceBookAuthorize(getActivity());
                break;
        }
    }

    /**
     * 页面切换
     */
    private void switchBox() {
        final String content = rLayoutLoginToolbar.getTitle().toString();
        if (TextUtils.equals(content, getString(R.string.LOGIN))) {
            //register
            rLayoutLoginToolbar.setToolbarTitle(R.string.Tap0_register);
            rLayoutLoginToolbar.setToolbarRightTitle(R.string.LOGIN);
            vsLayoutSwitcher.setInAnimation(getContext(), R.anim.slide_in_right_overshoot);
            vsLayoutSwitcher.setOutAnimation(getContext(), R.anim.slide_out_left);
            vsLayoutSwitcher.showNext();
        } else if (TextUtils.equals(content, getString(R.string.Tap0_register))) {
            rLayoutLoginToolbar.setToolbarTitle(R.string.LOGIN);
            rLayoutLoginToolbar.setToolbarRightTitle(R.string.Tap0_register);
            //延时200ms,
            vsLayoutSwitcher.setInAnimation(getContext(), R.anim.slide_in_left_overshoot);
            vsLayoutSwitcher.setOutAnimation(getContext(), R.anim.slide_out_right);
            vsLayoutSwitcher.showPrevious();
            if (!lLayoutAgreement.isShown())
                lLayoutAgreement.setVisibility(View.VISIBLE);
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

    @OnClick({R.id.lb_login_commit})
    public void login(View view) {
        if (TextUtils.equals(NetUtils.getNetName(getActivity()), "offLine") || NetUtils.getJfgNetType(getActivity()) == -1) {
            ToastUtil.showToast(getString(R.string.OFFLINE_ERR_1));
            return;
        }

        boolean b = JConstant.PHONE_REG.matcher(etLoginUsername.getText()).find()
                || JConstant.EMAIL_REG.matcher(etLoginUsername.getText()).find();
        if (!b) {
            ToastUtil.showNegativeToast(getString(R.string.ACCOUNT_ERR));
            return;
        }

        LoginAccountBean login = new LoginAccountBean();
        login.userName = ViewUtils.getTextViewContent(etLoginUsername);
        login.pwd = ViewUtils.getTextViewContent(etLoginPwd);
        if (basePresenter != null && NetUtils.getNetType(ContextUtils.getContext()) != -1) {
            basePresenter.executeLogin(login);
        }
        IMEUtils.hide(getActivity());
        AnimatorUtils.viewAlpha(tvForgetPwd, false, 300, 0);
        AnimatorUtils.viewTranslationY(LocaleUtils.getLanguageType(getActivity()) == JConstant.LOCALE_SIMPLE_CN ? rLayoutLoginThirdParty : rLayoutLoginThirdPartyAbroad, false, 100, 0, 800, 500);
        lbLogin.viewZoomSmall();
        enableEditTextCursor(false);
        enableOtherBtn(false);
    }

    /**
     * 直到登陆状态返回
     *
     * @param enable
     */
    private void enableOtherBtn(boolean enable) {
        rLayoutLoginToolbar.setEnabled(enable);
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
            bundle.putInt(JConstant.KEY_LOCALE, LocaleUtils.getLanguageType(getActivity()));
            bundle.putString(KEY_TEMP_ACCOUNT, tempAccount);
            ForgetPwdFragment forgetPwdFragment = ForgetPwdFragment.newInstance(bundle);
            new ForgetPwdPresenterImpl(forgetPwdFragment);
            ActivityUtils.addFragmentSlideInFromRight(getActivity().getSupportFragmentManager(),
                    forgetPwdFragment, containerId);
        }
    }

    @Override
    public boolean isLoginViewVisible() {
        final long time = System.currentTimeMillis();
        boolean notNull = getActivity() != null && getActivity().getWindow().getDecorView() != null;
        if (notNull) {
            View v = getActivity().getWindow().getDecorView().findViewById(android.R.id.content);
            if (v != null && v instanceof ViewGroup) {
                final int count = ((ViewGroup) v).getChildCount();
                if (count > 0) {
                    View thisLayout = ((ViewGroup) v).getChildAt(count - 1);
                    //yes this fragment is in top
                    notNull = (thisLayout != null && thisLayout.getId() == R.id.rLayout_login);
                }
            }
        }
        Log.d("perform", "perform: " + (System.currentTimeMillis() - time));
        return notNull;
    }

    @Override
    public void verifyCodeResult(int code) {
        if (!isVisible())
            return;
//        Toast.makeText(getActivity(), code == 0 ? "good" : "无效验证码", Toast.LENGTH_SHORT).show();
        if (code == JError.ErrorOK) {
            jump2NextPage();
        } else if (code == JError.ErrorSMSCodeTimeout) {
            ToastUtil.showNegativeToast(getString(R.string.RET_ESMS_CODE_TIMEOUT));
            if (verificationCodeLogic != null)
                verificationCodeLogic.initTimer();
        } else {
            ToastUtil.showNegativeToast(getString(R.string.Tap0_wrongcode));
        }
    }

    @Override
    public void loginResult(int code) {
        if (code == JError.ErrorOK) {
            if (!(getActivity() instanceof NewHomeActivity))
                getActivity().finish();
            else {
                getActivity().getSupportFragmentManager().popBackStack();
                return;
            }
            getContext().startActivity(new Intent(getContext(), NewHomeActivity.class));
        } else {
            resetView();
            if (code == JError.ErrorAccountNotExist) {
                //账号未注册
                ToastUtil.showNegativeToast(getString(R.string.RET_ELOGIN_ACCOUNT_NOT_EXIST));
//                showSimpleDialog(getString(R.string.RET_EFORGETPASS_ACCOUNT_NOT_EXIST), " ", getString(R.string.OK), false);
            } else if (code == JError.ErrorLoginInvalidPass) {
                ToastUtil.showNegativeToast(getString(R.string.RET_ELOGIN_ERROR));
            } else if (code == 162) {
                ToastUtil.showNegativeToast(getString(R.string.LOGIN_ERR));
            } else if (code == JError.ErrorConnect) {
                ToastUtil.showNegativeToast(getString(R.string.LOGIN_ERR));
            } else ToastUtil.showNegativeToast(getString(R.string.LOGIN_ERR));
        }
    }

    /**
     * 弹框，{fragment}
     */
    private void showSimpleDialog(String title, String lContent, String rContent, boolean dismiss) {
        Fragment f = getActivity().getSupportFragmentManager().findFragmentByTag("dialogFragment");
        if (f == null) {
            Bundle bundle = new Bundle();
            bundle.putString(BaseDialog.KEY_TITLE, title);
            bundle.putString(SimpleDialogFragment.KEY_LEFT_CONTENT, lContent);
            bundle.putString(SimpleDialogFragment.KEY_RIGHT_CONTENT, rContent);
            bundle.putBoolean(SimpleDialogFragment.KEY_TOUCH_OUT_SIDE_DISMISS, dismiss);
            SimpleDialogFragment dialogFragment = SimpleDialogFragment.newInstance(bundle);
            dialogFragment.setAction(this);
            dialogFragment.show(getActivity().getSupportFragmentManager(), "dialogFragment");
        }
    }

    /**
     * 登录超时，或者失败后动画复位
     */
    private void resetView() {
        lbLogin.viewZoomBig();
        AnimatorUtils.viewAlpha(tvForgetPwd, true, 300, 0);
        AnimatorUtils.viewTranslationY(LocaleUtils.getLanguageType(getActivity()) == JConstant.LOCALE_SIMPLE_CN ? rLayoutLoginThirdParty : rLayoutLoginThirdPartyAbroad, true, 100, 800, 0, 200);
        enableOtherBtn(true);
        enableEditTextCursor(true);
    }

    @Override
    public void setPresenter(LoginContract.Presenter basePresenter) {
        this.basePresenter = basePresenter;
//        AppLogger.e("setPresenter");
    }

    @Override
    public void onQQAuthorizeResult(int ret) {
        //授权成功后，直接登录，不需要回调过来
        ToastUtil.showToast("授权" + (ret == 2 ? "失败" : "取消"));
    }

    @Override
    public void onSinaAuthorizeResult(int ret) {
        //授权成功后，直接登录，不需要回调过来
        ToastUtil.showToast("授权" + (ret == 2 ? "失败" : "取消"));
    }

    @Override
    public void registerResult(int result) {
        if (result == JError.ErrorAccountAlreadyExist) {
            showSimpleDialog(getString(R.string.RET_EREGISTER_PHONE_EXIST), getString(R.string.CANCEL), getString(R.string.Tap0_register_GoToLogin), false);
        } else if (result == JError.ErrorOK) {
            if (!(getActivity() instanceof NewHomeActivity))
                getActivity().finish();
            getContext().startActivity(new Intent(getContext(), NewHomeActivity.class));
        }
    }

    @Override
    public void switchBox(String account) {
        switchBox();
        final boolean validPhoneNum = JConstant.PHONE_REG.matcher(etRegisterInputBox.getText()).find();
//        if (validPhoneNum) {
        AppLogger.i("account:" + etRegisterInputBox.getText());
        etRegisterInputBox.post(new Runnable() {
            @Override
            public void run() {
                if (registerWay == JConstant.REGISTER_BY_PHONE && !validPhoneNum) {
                    handleRegisterByMail();
                } else {
                    //email
                }
            }
        });
//        }
    }

    @Override
    public void updateAccount(final String account) {
        etLoginUsername.post(new Runnable() {
            @Override
            public void run() {
                etLoginUsername.setText(account);
            }
        });
    }

    @Override
    public void loginTimeout() {
        resetView();
        ToastUtil.showNegativeToast(getContext().getString(R.string.LOGIN_ERR));
    }

    @OnTextChanged(R.id.et_register_input_box)
    public void onRegisterEtChange(CharSequence s, int start, int before, int count) {
        boolean result;
        if (registerWay == JConstant.REGISTER_BY_PHONE) {
            if (etVerificationInput.isShown()) {
                result = JConstant.PHONE_REG.matcher(s).find() && (etVerificationInput.getText().toString().trim().length() == 6);
            } else {
                result = JConstant.PHONE_REG.matcher(s).find();
            }
        } else {
            result = Patterns.EMAIL_ADDRESS.matcher(s).find();
        }
        ivRegisterUserNameClear.setVisibility(!TextUtils.isEmpty(s) ? View.VISIBLE : View.GONE);
        tvRegisterSubmit.setEnabled(result);
    }

    @OnTextChanged(R.id.et_verification_input)
    public void onRegisterVerificationCodeEtChange(CharSequence s, int start, int before, int count) {
        boolean isValidCode = TextUtils.isDigitsOnly(s) && s.length() == 6;
        tvRegisterSubmit.setEnabled(isValidCode && JConstant.PHONE_REG.matcher(etRegisterInputBox.getText().toString().trim()).find());
    }

    /**
     * 在跳转之前，做一些清理工作
     */
    private void clearSomeThing() {
        if (verificationCodeLogic != null) {
            verificationCodeLogic.stop();
            verificationCodeLogic = null;
        }
    }

    /**
     * 校验 验证码
     */
    private void verifyCode() {
        if (basePresenter != null)
            basePresenter.verifyCode(ViewUtils.getTextViewContent(etRegisterInputBox),
                    ViewUtils.getTextViewContent(etVerificationInput),
                    PreferencesUtils.getString(JConstant.KEY_REGISTER_SMS_TOKEN));
    }

    /**
     * 手机号和验证码是否准备,或者注册类型{手机，邮箱}
     *
     * @return
     */
    @Override
    public void jump2NextPage() {
        clearSomeThing();
        //to set up pwd
        Bundle bundle = getArguments();
        if (getActivity() != null && bundle != null) {
            final int containerId = bundle.getInt(JConstant.KEY_ACTIVITY_FRAGMENT_CONTAINER_ID);
            bundle.putString(JConstant.KEY_ACCOUNT_TO_SEND, ViewUtils.getTextViewContent(etRegisterInputBox));
            bundle.putString(JConstant.KEY_PWD_TO_SEND, ViewUtils.getTextViewContent(etRegisterInputBox));
            bundle.putString(JConstant.KEY_VCODE_TO_SEND, ViewUtils.getTextViewContent(etVerificationInput));
            bundle.putInt(JConstant.KEY_SET_UP_PWD_TYPE, 1);
            RegisterPwdFragment fragment = RegisterPwdFragment.newInstance(bundle);
            new SetupPwdPresenterImpl(fragment);
            ActivityUtils.addFragmentSlideInFromRight(getActivity().getSupportFragmentManager(),
                    fragment, containerId);
        }
    }

    /**
     * 是否已注册结果
     *
     * @param callback
     */
    @Override
    public void checkAccountResult(RxEvent.CheckRegsiterBack callback) {
        if (callback.jfgResult.code != 0) {
            boolean validPhoneNum = JConstant.PHONE_REG.matcher(ViewUtils.getTextViewContent(etRegisterInputBox)).find();
            registerWay = validPhoneNum ? JConstant.REGISTER_BY_PHONE : JConstant.REGISTER_BY_EMAIL;
            if (registerWay == JConstant.REGISTER_BY_EMAIL) {
                jump2NextPage();
                AppLogger.d("jump_time:" + System.currentTimeMillis());
                return;
            }
            int codeLen = ViewUtils.getTextViewContent(etVerificationInput).length();
            boolean validCode = codeLen == JConstant.VALID_VERIFICATION_CODE_LEN;

            if (isRegetCode) {
                //重新获取验证码也要检测一下账号
                if (basePresenter.checkOverCount()) {
                    ToastUtil.showNegativeToast(getString(R.string.GetCode_FrequentlyTips));
                    isRegetCode = false;
                    return;
                }

                if (verificationCodeLogic != null)
                    verificationCodeLogic.start();
                if (basePresenter != null)
                    basePresenter.getCodeByPhone(ViewUtils.getTextViewContent(etRegisterInputBox));
                tempPhone = ViewUtils.getTextViewContent(etRegisterInputBox);
                isRegetCode = false;
            } else if (fLayoutVerificationCodeInputBox.isShown() && validCode) {
                //第二次检测账号是否注册返回执行获取校验验证码
                if (TextUtils.equals(tempPhone, ViewUtils.getTextViewContent(etRegisterInputBox))) {
                    verifyCode();
                } else {
                    ToastUtil.showToast(getString(R.string.RET_ESMS_CODE_TIMEOUT));
                }

            } else {
                //第一次检测账号是否注册返回执行获取验证码
                if (basePresenter.checkOverCount()) {
                    ToastUtil.showNegativeToast(getString(R.string.GetCode_FrequentlyTips));
                    return;
                }
                if (verificationCodeLogic == null)
                    verificationCodeLogic = new VerificationCodeLogic(tvMeterGetCode);
                verificationCodeLogic.start();
                basePresenter.getCodeByPhone(ViewUtils.getTextViewContent(etRegisterInputBox));
                tempPhone = ViewUtils.getTextViewContent(etRegisterInputBox);
                //显示验证码输入框
                handleVerificationCodeBox(true);
                tvRegisterSubmit.setText(getString(R.string.CARRY_ON));
                tvRegisterSubmit.setEnabled(false);
                lLayoutAgreement.setVisibility(View.GONE);
            }

        } else {
            ToastUtil.showToast(getString(R.string.RET_EREGISTER_PHONE_EXIST));
        }

    }

    /**
     * 验证码输入框
     *
     * @param show
     */
    private void handleVerificationCodeBox(boolean show) {
        etVerificationInput.setText("");
        fLayoutVerificationCodeInputBox.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    /**
     * 注册块，确认按钮逻辑。
     */
    private void handleRegisterConfirm() {
        final boolean validPhoneNum = JConstant.PHONE_REG.matcher(ViewUtils.getTextViewContent(etRegisterInputBox)).find();
        registerWay = validPhoneNum ? JConstant.REGISTER_BY_PHONE : JConstant.REGISTER_BY_EMAIL;
        if (registerWay == JConstant.REGISTER_BY_PHONE) {
            IMEUtils.hide(getActivity());
            //检测账号是否注册
            if (basePresenter != null)
                basePresenter.checkAccountIsReg(ViewUtils.getTextViewContent(etRegisterInputBox));
        } else {
            final boolean isValidEmail = Patterns.EMAIL_ADDRESS.matcher(ViewUtils.getTextViewContent(etRegisterInputBox)).find();
            if (!isValidEmail) {
                Toast.makeText(getActivity(), getString(R.string.EMAIL_2), Toast.LENGTH_SHORT).show();
                return;
            }
            //检测账号是否注册
            if (basePresenter != null)
                basePresenter.checkAccountIsReg(ViewUtils.getTextViewContent(etRegisterInputBox));
        }
    }

    private void handleRegisterByMail() {
        if (registerWay == JConstant.REGISTER_BY_PHONE) {
            tvRegisterWayContent.setText(getString(R.string.PHONE_SIGNUP));
            etRegisterInputBox.setText("");
            etRegisterInputBox.setHint(getString(R.string.EMAIL_1));
            etRegisterInputBox.setInputType(EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            //设置长度
            ViewUtils.setTextViewMaxFilter(etRegisterInputBox, 65);
            registerWay = JConstant.REGISTER_BY_EMAIL;
            tvRegisterSubmit.setText(getString(R.string.CARRY_ON));
            handleVerificationCodeBox(false);
        } else if (registerWay == JConstant.REGISTER_BY_EMAIL) {
            tvRegisterWayContent.setText(getString(R.string.EMAIL_SIGNUP));
            etRegisterInputBox.setText("");
            etRegisterInputBox.setHint(getString(R.string.PHONE_NUMBER_1));
            etRegisterInputBox.setInputType(EditorInfo.TYPE_CLASS_PHONE);
            ViewUtils.setTextViewMaxFilter(etRegisterInputBox, 11);
            registerWay = JConstant.REGISTER_BY_PHONE;
            tvRegisterSubmit.setText(getString(R.string.GET_CODE));
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
    public void onClickRegister(View view) {
        switch (view.getId()) {
            case R.id.tv_meter_get_code:
                isRegetCode = true;
                if (basePresenter != null)
                    basePresenter.checkAccountIsReg(ViewUtils.getTextViewContent(etRegisterInputBox));
                break;
            case R.id.tv_register_submit:
                if (NetUtils.getNetType(getContext()) == -1) {
                    ToastUtil.showToast(getString(R.string.OFFLINE_ERR_1));
                    return;
                }
                handleRegisterConfirm();
                JCache.isSmsAction = true;
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

    @Override
    public void onDialogAction(int id, Object value) {
        Fragment f = getActivity().getSupportFragmentManager().findFragmentByTag("dialogFragment");
        if (f != null && f.isVisible()) {
            ((SimpleDialogFragment) f).dismiss();
        }
        if (id == R.id.tv_dialog_btn_right) {
            etLoginPwd.setText("");
            final boolean validPhoneNum = JConstant.PHONE_REG.matcher(etRegisterInputBox.getText()).find();
            switchBox();
            if (!validPhoneNum) {
                //已经有RegisterPwdFragment，先popStack
                Fragment fragment = getActivity()
                        .getSupportFragmentManager()
                        .findFragmentByTag("RegisterPwdFragment");
                if (fragment != null && fragment.isVisible()) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            }
            etRegisterInputBox.post(new Runnable() {
                @Override
                public void run() {
                    etLoginUsername.setText(etRegisterInputBox.getText());
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        clearSomeThing();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
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

        public void reset() {
            if (timer != null)
                timer.cancel();
            if (this.viewWeakReference.get() != null) {
                this.viewWeakReference.get().setText(
                        viewWeakReference.get()
                                .getContext()
                                .getString(R.string.Button_ReObtain));
            }
        }

        private void initTimer() {
            timer = new CountDownTimer(JConstant.VERIFICATION_CODE_DEADLINE, 1000L) {
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
                    tv.setText(tv.getContext().getString(R.string.Button_ReObtain));
                    tv.setEnabled(true);
                }
            };
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        SsoHandler sinaCallBack = basePresenter.getSinaCallBack();
        if (sinaCallBack != null) {
            sinaCallBack.authorizeCallBack(requestCode, resultCode, data);
        }

        if (requestCode == Constants.REQUEST_LOGIN ||
                requestCode == Constants.REQUEST_APPBAR) {
            basePresenter.onActivityResultData(requestCode, resultCode, data);
        }

        TwitterAuthClient twitterBack = basePresenter.getTwitterBack();
        if (twitterBack != null) {
            twitterBack.onActivityResult(requestCode, resultCode, data);
        }

        CallbackManager faceBookBackObj = basePresenter.getFaceBookBackObj();
        if (faceBookBackObj != null) {
            faceBookBackObj.onActivityResult(requestCode, resultCode, data);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
