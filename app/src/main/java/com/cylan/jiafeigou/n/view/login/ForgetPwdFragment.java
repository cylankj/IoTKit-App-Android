package com.cylan.jiafeigou.n.view.login;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.JResultEvent;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.login.ForgetPwdContract;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.IMEUtils;
import com.cylan.jiafeigou.utils.LocaleUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.CustomToolbar;
import com.cylan.jiafeigou.widget.LoadingDialog;
import com.cylan.jiafeigou.widget.dialog.BaseDialog;
import com.cylan.jiafeigou.widget.dialog.SimpleDialogFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;

import static com.cylan.jiafeigou.misc.JFGRules.LANGUAGE_TYPE_SIMPLE_CHINESE;
import static com.cylan.jiafeigou.rx.RxEvent.ResultEvent.JFG_RESULT_VERIFY_SMS;

/**
 * 忘记密码
 * Created by lxh on 16-6-14.
 */

public class ForgetPwdFragment extends IBaseFragment implements ForgetPwdContract.View, BaseDialog.BaseDialogAction {


    @BindView(R.id.et_forget_username)
    EditText etForgetUsername;
    @BindView(R.id.iv_forget_clear_username)
    ImageView ivForgetClearUsername;
    @BindView(R.id.tv_forget_pwd_submit)
    TextView tvForgetPwdSubmit;
    @BindView(R.id.lLayout_register_input)
    LinearLayout lLayoutRegisterInput;
    @BindView(R.id.et_verification_input)
    EditText etVerificationInput;
    @BindView(R.id.tv_meter_get_code)
    TextView tvMeterGetCode;
    @BindView(R.id.fLayout_verification_code_input_box)
    FrameLayout fLayoutVerificationCodeInputBox;

    @Nullable
    @BindView(R.id.et_new_pwd_input)
    EditText etNewPwdInput;
    @Nullable
    @BindView(R.id.iv_new_clear_pwd)
    ImageView ivNewClearPwd;
    @Nullable
    @BindView(R.id.tv_new_pwd_submit)
    TextView tvNewPwdSubmit;
    @Nullable
    @BindView(R.id.rLayout_new_pwd_input_box)
    FrameLayout rLayoutRstPwdInputBox;

    @Nullable
    @BindView(R.id.cb_show_pwd)
    CheckBox cbShowPwd;
    @BindView(R.id.vs_set_account_pwd)
    ViewSwitcher vsSetAccountPwd;

    @BindView(R.id.fLayout_forget_container)
    FrameLayout fLayoutForgetContainer;
    @BindView(R.id.rLayout_forget_pwd_toolbar)
    CustomToolbar rLayoutForgetPwdToolbar;
    @BindView(R.id.ll_new_pwd_container)
    LinearLayout llNewPwdContainer;
    @BindView(R.id.ll_mail_container)
    LinearLayout llMailContainer;
    @BindView(R.id.cb_new_pwd_show)
    CheckBox cbNewPwdShow;
    @BindView(R.id.tv_email_confirm)
    TextView tvEmailConfirm;
    @BindView(R.id.tv_send_email_content)
    TextView tvSendEmailContent;

    /**
     * {0}请输入手机号/邮箱 {1}请输入邮箱
     */
    private int acceptType = 0;
    private ForgetPwdContract.Presenter presenter;
    private CountDownTimer countDownTimer;
    private static final String DIALOG_KEY = "dialogFragment";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        acceptType = bundle.getInt(JConstant.KEY_LOCALE);
    }

    @Override
    public void onStart() {
        super.onStart();
        initCountDownTimer();
    }

    private void initCountDownTimer() {
        countDownTimer = new CountDownTimer(90 * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                final String content = millisUntilFinished / 1000 + "s";
                tvMeterGetCode.setText(content);
                tvMeterGetCode.setEnabled(false);
            }

            @Override
            public void onFinish() {
                if (isAdded()) {
                    tvMeterGetCode.setText(getString(R.string.ANEW_SEND));
                    if (checkInputType() == JConstant.TYPE_PHONE) {
                        tvMeterGetCode.setEnabled(true);
                    }
                }
            }
        };

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        initTitleBar();
        initView(view);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_forget_pwd, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (presenter != null)
            presenter.start();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (countDownTimer != null) {
            countDownTimer.onFinish();
            countDownTimer = null;
        }
        if (presenter != null)
            presenter.stop();
    }

    private void initTitleBar() {
        rLayoutForgetPwdToolbar.setBackAction(v -> {
            if (rLayoutForgetPwdToolbar.getTitle().equals(getString(R.string.FORGOT_PWD))) {
                getFragmentManager().popBackStack();
            } else {
                showSimpleDialog(getString(R.string.Tap3_logout_tips), getString(R.string.Button_No), getString(R.string.Button_Yes), false);
            }
        });
    }

    public static ForgetPwdFragment newInstance(Bundle bundle) {
        ForgetPwdFragment fragment = new ForgetPwdFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    private void initView(View view) {
        ViewUtils.setChineseExclude(etForgetUsername, 65);
        ViewUtils.setChineseExclude(etVerificationInput, 6);
        etForgetUsername.setEnabled(true);
        ViewUtils.setChineseExclude(etNewPwdInput, JConstant.PWD_LEN_MAX);
        if (acceptType == 1) {
            etForgetUsername.setHint(getString(R.string.EMAIL));
        }
        Bundle bundle = getArguments();
        if (bundle != null && !TextUtils.isEmpty(bundle.getString(LoginFragment.KEY_TEMP_ACCOUNT))) {
            etForgetUsername.setText(bundle.getString(LoginFragment.KEY_TEMP_ACCOUNT));
            ivForgetClearUsername.setVisibility(View.GONE);
        }
        if (TextUtils.isEmpty(etForgetUsername.getText())) {
            final int type = LocaleUtils.getLanguageType(getActivity());
            etForgetUsername.setHint(type == JConstant.LOCALE_SIMPLE_CN ? getString(R.string.SHARE_E_MAIL) : getString(R.string.EMAIL));
        }
    }


    private int checkInputType() {
        final String account = ViewUtils.getTextViewContent(etForgetUsername);
        final boolean isEmail = Patterns.EMAIL_ADDRESS.matcher(account).find();
        if (isEmail) {
            return JConstant.TYPE_EMAIL;
        } else {
            final boolean isPhone = JConstant.PHONE_REG.matcher(account).find();
            if (isPhone) {
                return JConstant.TYPE_PHONE;
            }
            return JConstant.TYPE_INVALID;
        }
    }

    @OnClick(R.id.vs_set_account_pwd)
    public void dismissIME() {
        if (getActivity() != null) IMEUtils.hide(getActivity());
    }

    @OnClick(R.id.tv_meter_get_code)
    public void reGetVerificationCode(View v) {
//        ViewUtils.deBounceClick(v);//这里会在一秒后自动设置成 enable ,所以不用
        tvMeterGetCode.setEnabled(false);
        if (presenter.checkOverCount(ViewUtils.getTextViewContent(etForgetUsername))) {
            ToastUtil.showNegativeToast(getString(R.string.GetCode_FrequentlyTips));
            return;
        }
        countDownTimer.start();
        tvMeterGetCode.setEnabled(false);
        if (presenter != null)
            presenter.getVerifyCode(ViewUtils.getTextViewContent(etForgetUsername));
    }

    @Override
    public void showLoading() {
        LoadingDialog.showLoading(getActivity(), getString(R.string.LOADING), true);
    }

    @Override
    public void hideLoading() {
        LoadingDialog.dismissLoading();
    }

    @OnClick(R.id.iv_forget_clear_username)
    public void clearUsername(View view) {
        etForgetUsername.setText("");
    }

    @OnFocusChange(R.id.et_forget_username)
    public void onEtFocusChange(View view, boolean focused) {
        ivForgetClearUsername.setVisibility(focused ? View.VISIBLE : View.INVISIBLE);
    }

    /**
     * 这里有个动画，就是setVisibility(View.VISIBLE),加上
     * android:animateLayoutChanges="true",就有transition的效果。
     */
    private void start2HandleVerificationCode() {

        fLayoutVerificationCodeInputBox.setVisibility(View.VISIBLE);
        countDownTimer.start();
        tvMeterGetCode.setEnabled(false);
        tvForgetPwdSubmit.setEnabled(true);
    }

    //判读是手机号还是邮箱
    private void next() {
        final int type = checkInputType();
        tvForgetPwdSubmit.setEnabled(true);
        switch (type) {
            case JConstant.TYPE_INVALID:
                Toast.makeText(getActivity(), getString(R.string.ACCOUNT_ERR_1), Toast.LENGTH_SHORT).show();
                enableEditTextCursor(true);
                return;
            case JConstant.TYPE_PHONE:
                if (fLayoutVerificationCodeInputBox.getVisibility() == View.GONE) {
                    //获取验证码
                    if (presenter != null) {
//                        tvForgetPwdSubmit.setEnabled(true);
                        presenter.getVerifyCode(ViewUtils.getTextViewContent(etForgetUsername));

                    }
                } else {
                    //提交手机号和验证码
                    final String code = ViewUtils.getTextViewContent(etVerificationInput);
                    if (!TextUtils.isEmpty(code) && code.length() != JConstant.VALID_VERIFICATION_CODE_LEN) {
                        Toast.makeText(getActivity(), getString(R.string.Tap0_wrongcode), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!JConstant.PHONE_REG.matcher(etForgetUsername.getText()).find()) {
                        Toast.makeText(getActivity(), getString(R.string.PHONE_NUMBER_2), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (presenter != null) {
                        presenter.submitPhoneAndCode(ViewUtils.getTextViewContent(etForgetUsername), ViewUtils.getTextViewContent(etVerificationInput));
                        AppLogger.d("提交账号与验证码:");
                    }
                }
                break;
            case JConstant.TYPE_EMAIL:
                enableEditTextCursor(true);
                if (presenter != null)
                    presenter.checkMailByAccount(ViewUtils.getTextViewContent(etForgetUsername));
                break;
        }
        IMEUtils.hide(getActivity());
    }

    /**
     * 清除焦点。
     */
    private void enableEditTextCursor(boolean enable) {
        if (isResumed() && getActivity() != null) {
            ViewUtils.enableEditTextCursor(etForgetUsername, enable);
        }
    }

    @OnTextChanged(R.id.et_forget_username)
    public void userNameChange(CharSequence s, int start, int before, int count) {
        final boolean flag = TextUtils.isEmpty(s);
        ivForgetClearUsername.setVisibility(flag ? View.GONE : View.VISIBLE);
        int visibility = fLayoutVerificationCodeInputBox.getVisibility();
        int inputType = checkInputType();
//        boolean user = !TextUtils.isEmpty(etForgetUsername.getText()) && TextUtils.isDigitsOnly(etForgetUsername.getText());
//        tvForgetPwdSubmit.setEnabled(self && user);
        boolean codeValid = inputType != JConstant.TYPE_INVALID && (visibility != View.VISIBLE ||
                (inputType == JConstant.TYPE_PHONE && etForgetUsername.getText().length() > 0));
        tvForgetPwdSubmit.setEnabled(!flag && codeValid);
        if (TextUtils.equals(tvMeterGetCode.getText(), getString(R.string.ANEW_SEND)) && inputType == JConstant.TYPE_PHONE) {
            tvMeterGetCode.setEnabled(true);
        }
        tvMeterGetCode.setEnabled(inputType == JConstant.TYPE_PHONE && TextUtils.equals(tvMeterGetCode.getText(), getString(R.string.ANEW_SEND)));

    }

    @OnTextChanged(R.id.et_verification_input)
    public void verificationInputChange(CharSequence s, int start, int before, int count) {
        boolean self = !TextUtils.isEmpty(s) && TextUtils.isDigitsOnly(s);
        boolean user = !TextUtils.isEmpty(etForgetUsername.getText()) && TextUtils.isDigitsOnly(etForgetUsername.getText());
        tvForgetPwdSubmit.setEnabled(self && user);
    }

    @OnClick(R.id.tv_forget_pwd_submit)
    public void forgetPwdCommit(View v) {
        /*
        //这里不用 deBounceClick 因为会在一秒后自动设置 button 为 enable ,
        bug单号为:#116624
        android（1.0.0.469） 忘记密码页面，
        只有手机号没有输入验证码，继续按钮应该置灰，现在未置灰点击继续，提示“请求超时”
        * */
//        ViewUtils.deBounceClick(v);
        if (NetUtils.getJfgNetType(getContext()) == 0) {
            Toast.makeText(getContext(), getString(R.string.NO_NETWORK_4), Toast.LENGTH_SHORT).show();
            return;
        }
        int language = JFGRules.getLanguageType();
        if (language != LANGUAGE_TYPE_SIMPLE_CHINESE) {
            //非中文,只能填邮箱
            if (!Patterns.EMAIL_ADDRESS.matcher(ViewUtils.getTextViewContent(etForgetUsername)).find()) {
                ToastUtil.showToast(getString(R.string.EMAIL_2));
                return;
            }
        }
        next();
    }

    private void initTitle(final int ret) {
        if (ret == -1)
            return;
        if (ret == JConstant.TYPE_EMAIL)
            rLayoutForgetPwdToolbar.setToolbarTitle(R.string.EMAIL);
        else if (ret == JConstant.TYPE_PHONE) {
            rLayoutForgetPwdToolbar.setToolbarTitle(R.string.NEW_PWD);
            if (vsSetAccountPwd.getCurrentView().getId() == R.id.layout_get_code)
                vsSetAccountPwd.showNext();
        } else if (ret == JConstant.TYPE_EMAIL_VERIFY) {
            rLayoutForgetPwdToolbar.setToolbarTitle(R.string.Tap0_register_EmailVerification);
        }
    }

    /**
     * 设置新邮箱账号的密码。
     */
    private void prepareMailView() {
        View view = vsSetAccountPwd.findViewById(R.id.layout_to_be_update);
        if (view != null) {
            vsSetAccountPwd.removeView(view);
        }
        View mailView = LayoutInflater.from(getActivity())
                .inflate(R.layout.fragment_forget_pwd_by_email, null);
        if (mailView == null) {
            return;
        }
//        final String content = String.format(getString(R.string.EMAIL_RESET_PWD),ViewUtils.getTextViewContent(etForgetUsername));
        initTitle(JConstant.TYPE_EMAIL_VERIFY);
        final String content = ViewUtils.getTextViewContent(etForgetUsername);
        ((TextView) mailView.findViewById(R.id.tv_send_email_content)).setText(content);
        View btn = mailView.findViewById(R.id.tv_email_confirm);
        btn.setEnabled(true);
        btn.setOnClickListener(v -> {
            getActivity().getSupportFragmentManager().popBackStack();
            RxBus.getCacheInstance().post(new RxEvent.LoginPopBack(etForgetUsername.getText().toString()));
        });
        if (vsSetAccountPwd.getChildCount() < 2) {
            vsSetAccountPwd.addView(mailView);
        }
        vsSetAccountPwd.setInAnimation(getContext(), R.anim.slide_in_right_overshoot);
        vsSetAccountPwd.setOutAnimation(getContext(), R.anim.slide_out_left);
        vsSetAccountPwd.showNext();
    }

    /**
     * 设置新手机账号的密码。
     */
    private void preparePhoneView() {
        View view = vsSetAccountPwd.findViewById(R.id.layout_to_be_update);
        if (view != null) {
            vsSetAccountPwd.removeView(view);
        }
        View phoneNewPwdView = LayoutInflater.from(getContext())
                .inflate(R.layout.fragment_set_new_pwd, null);
        if (phoneNewPwdView == null) {
            return;
        }
        initTitle(JConstant.TYPE_PHONE);
        if (vsSetAccountPwd.getChildCount() == 1) {//##103929
            vsSetAccountPwd.addView(phoneNewPwdView, 1);
            vsSetAccountPwd.setInAnimation(getContext(), R.anim.slide_in_right_overshoot);
            vsSetAccountPwd.setOutAnimation(getContext(), R.anim.slide_out_left);
            vsSetAccountPwd.showNext();
        }
        initNewPwdView(phoneNewPwdView);
    }

    /**
     * 新密码设置界面
     *
     * @param phoneNewPwdView
     */
    private void initNewPwdView(View phoneNewPwdView) {
        final TextView sureBtn = (TextView) phoneNewPwdView.findViewById(R.id.tv_new_pwd_submit);
        ImageView iv_Clear = (ImageView) phoneNewPwdView.findViewById(R.id.iv_new_clear_pwd);
        EditText et_newpass = (EditText) phoneNewPwdView.findViewById(R.id.et_new_pwd_input);
        CheckBox cb_pwd_visiable = (CheckBox) phoneNewPwdView.findViewById(R.id.cb_new_pwd_show);
        ViewUtils.setChineseExclude(et_newpass, getResources().getInteger(R.integer.max_password_length));
        et_newpass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (sureBtn != null)
                    sureBtn.setEnabled(!TextUtils.isEmpty(s));
                if (ivNewClearPwd != null) {
                    ivNewClearPwd.setVisibility(TextUtils.isEmpty(s) ? View.GONE : View.VISIBLE);
                    ivNewClearPwd.setClickable(true);
                }
            }
        });

        sureBtn.setOnClickListener(v -> {
            if (et_newpass.getText().toString().trim().length() < 6) {
                ToastUtil.showToast(getString(R.string.PASSWORD_LESSTHAN_SIX));
                return;
            }
            if (NetUtils.getNetType(ContextUtils.getContext()) == -1) {
                ToastUtil.showToast(getString(R.string.OFFLINE_ERR_1));
                return;
            }
            presenter.submitNewPass(et_newpass.getText().toString().trim());
        });

        iv_Clear.setOnClickListener(v -> et_newpass.setText(""));

        cb_pwd_visiable.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ViewUtils.showPwd(et_newpass, isChecked);
            et_newpass.setSelection(et_newpass.length());
        });
    }

    @Override
    public void onResult(int event, final int errId) {
        if (!isAdded()) return;
        if (getView() != null) {
            switch (event) {
                case JConstant.AUTHORIZE_MAIL:
                    tvForgetPwdSubmit.setEnabled(true);
                    prepareMailView();
                    break;
                case JConstant.AUTHORIZE_PHONE_SMS:
                    if (errId == JError.ErrorOK) {
                        tvForgetPwdSubmit.setEnabled(true);
                        preparePhoneView();
                    }
                    break;
                case JConstant.GET_SMS_BACK:
                    if (errId == JError.ErrorOK)
                        start2HandleVerificationCode();
                    else
                        tvForgetPwdSubmit.setEnabled(true);
                    break;
                case JFG_RESULT_VERIFY_SMS:
                    if (errId == 0) {
                        tvForgetPwdSubmit.setEnabled(true);
                        preparePhoneView();
                    }
                    break;
                case JConstant.CHECK_TIMEOUT:
                    tvForgetPwdSubmit.setEnabled(true);
                    ToastUtil.showToast(getString(R.string.Request_TimeOut));
                    break;
                case JResultEvent.JFG_RESULT_CHANGE_PASS:
                    if (errId == JError.ErrorOK) {
                        ToastUtil.showToast(getString(R.string.PWD_OK));
                        ActivityUtils.justPop(getActivity());
                    }
                    break;
            }
            switch (errId) {
                case JError.ErrorAccountNotExist:
                    tvForgetPwdSubmit.setEnabled(true);
                    ToastUtil.showToast(getString(R.string.INVALID_ACCOUNT));
                    break;
                case JError.ErrorGetCodeTooFrequent:
                    tvForgetPwdSubmit.setEnabled(true);
                    ToastUtil.showNegativeToast(getString(R.string.GetCode_FrequentlyTips));
                    break;
                case JError.ErrorSamePass:
                    tvForgetPwdSubmit.setEnabled(true);
                    ToastUtil.showToast(getString(R.string.RET_ECHANGEPASS_SAME));
                    break;
                case JError.ErrorSMSCodeTimeout: {
                    tvForgetPwdSubmit.setEnabled(true);
                    ToastUtil.showToast(getString(R.string.INVALID_CODE));
                    break;
                }
                case JError.ErrorSMSCodeNotMatch:
                    tvForgetPwdSubmit.setEnabled(true);
                    ToastUtil.showToast(getString(R.string.Tap0_wrongcode));
                    break;
            }
        }
    }

    @Override
    public void setPresenter(ForgetPwdContract.Presenter presenter) {
        this.presenter = presenter;
    }

//    @Nullable
//    @OnTextChanged(R.id.et_new_pwd_input)
//    public void newPwdInputBoxChanged(CharSequence s, final int before, final int count, final int len) {
//        if (ivNewClearPwd != null) {
//            ivNewClearPwd.setVisibility(TextUtils.isEmpty(s) ? View.GONE : View.VISIBLE);
//            ivNewClearPwd.setClickable(true);
//        }
//        if (tvNewPwdSubmit != null)
//            tvNewPwdSubmit.setEnabled(!TextUtils.isEmpty(s));
//    }

    @OnCheckedChanged(R.id.cb_new_pwd_show)
    public void onNewPwdCheckBoxState(CompoundButton view, boolean isChecked) {
        ViewUtils.showPwd(etNewPwdInput, isChecked);
        if (etNewPwdInput != null) etNewPwdInput.setSelection(etNewPwdInput.length());
    }

    @OnClick({R.id.iv_new_clear_pwd, R.id.tv_new_pwd_submit, R.id.tv_email_confirm})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_new_clear_pwd:
                if (etNewPwdInput != null) etNewPwdInput.setText("");
                break;
            case R.id.tv_new_pwd_submit:
                //*********
                if (etNewPwdInput.getText().toString().trim().length() < 6) {
                    ToastUtil.showToast(getString(R.string.PASSWORD_LESSTHAN_SIX));
                    return;
                }
                if (NetUtils.getNetType(ContextUtils.getContext()) == -1) {
                    ToastUtil.showToast(getString(R.string.OFFLINE_ERR_1));
                    return;
                }
                presenter.submitNewPass(etNewPwdInput.getText().toString().trim());
                break;
            case R.id.tv_email_confirm:
                //邮箱点击激活
                getActivity().getSupportFragmentManager().popBackStack();
                RxBus.getCacheInstance().post(new RxEvent.LoginPopBack(etForgetUsername.getText().toString()));
                break;
        }
    }

    private void showSimpleDialog(String title,
                                  String lContent,
                                  String rContent,
                                  boolean dismiss) {
        Fragment f = getActivity().getSupportFragmentManager().findFragmentByTag(DIALOG_KEY);
        if (f == null) {
            Bundle bundle = new Bundle();
            bundle.putString(BaseDialog.KEY_TITLE, title);
            bundle.putString(SimpleDialogFragment.KEY_LEFT_CONTENT, lContent);
            bundle.putString(SimpleDialogFragment.KEY_RIGHT_CONTENT, rContent);
            bundle.putBoolean(SimpleDialogFragment.KEY_TOUCH_OUT_SIDE_DISMISS, dismiss);
            SimpleDialogFragment dialogFragment = SimpleDialogFragment.newInstance(bundle);
            dialogFragment.setAction(this);
            dialogFragment.show(getActivity().getSupportFragmentManager(), DIALOG_KEY);
        }
    }

    @Override
    public void onDialogAction(int id, Object value) {
        if (id == R.id.tv_dialog_btn_right) {
            rLayoutForgetPwdToolbar.setToolbarTitle(R.string.FORGOT_PWD);
            vsSetAccountPwd.setInAnimation(getContext(), R.anim.slide_in_left_overshoot);
            vsSetAccountPwd.setOutAnimation(getContext(), R.anim.slide_out_right);
            vsSetAccountPwd.showPrevious();
        }
    }
}
