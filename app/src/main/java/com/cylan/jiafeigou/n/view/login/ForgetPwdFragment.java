package com.cylan.jiafeigou.n.view.login;

import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
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
import com.cylan.jiafeigou.cache.JCache;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.login.ForgetPwdContract;
import com.cylan.jiafeigou.n.mvp.model.RequestResetPwdBean;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.IMEUtils;
import com.cylan.jiafeigou.utils.LocaleUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.CustomToolbar;
import com.cylan.jiafeigou.widget.dialog.BaseDialog;
import com.cylan.jiafeigou.widget.dialog.SimpleDialogFragment;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;

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

    /**
     * {0}请输入手机号/邮箱 {1}请输入邮箱
     */
    private int acceptType = 0;
    private ForgetPwdContract.Presenter presenter;
    private CountDownTimer countDownTimer;
    private String newPwd;
    private boolean isCheckAgain;
    private boolean isCheckAccAgain;
    private String tempAcc;
    private static final String DIALOG_KEY = "dialogFragment";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        acceptType = bundle.getInt(JConstant.KEY_LOCALE);
        List<Fragment> fragmentList = getActivity().getSupportFragmentManager().getFragments();
        Log.d("", "" + fragmentList);
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
            }

            @Override
            public void onFinish() {
                if (isAdded()){
                    tvMeterGetCode.setText(getString(R.string.ANEW_SEND));
                    tvMeterGetCode.setEnabled(true);
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
        if (countDownTimer != null)
            countDownTimer.onFinish();
        if (presenter != null)
            presenter.stop();
    }

    private void initTitleBar() {
        rLayoutForgetPwdToolbar.setBackAction(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSimpleDialog(getString(R.string.Tap3_logout_tips), getString(R.string.Button_Yes), getString(R.string.Button_No), false);
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
            etForgetUsername.setHint(getString(R.string.EMAIL_2));
        }
        Bundle bundle = getArguments();
        if (bundle != null && !TextUtils.isEmpty(bundle.getString(LoginFragment.KEY_TEMP_ACCOUNT))) {
            etForgetUsername.setText(bundle.getString(LoginFragment.KEY_TEMP_ACCOUNT));
            ivForgetClearUsername.setVisibility(View.GONE);
        }
        if (TextUtils.isEmpty(etForgetUsername.getText())) {
            final int type = LocaleUtils.getLanguageType(getActivity());
            etForgetUsername.setHint(type == JConstant.LOCALE_SIMPLE_CN ? getString(R.string.SHARE_E_MAIL) : getString(R.string.EMAIL_2));
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

    @OnClick(R.id.tv_meter_get_code)
    public void reGetVerificationCode() {
        if (presenter.checkOverCount()){
            ToastUtil.showNegativeToast(getString(R.string.GetCode_FrequentlyTips));
            return;
        }
        countDownTimer.start();
        tvMeterGetCode.setEnabled(false);
        if (presenter != null)
            Toast.makeText(getActivity(), getString(R.string.Tap3_FriendsAdd_Contacts_Sent), Toast.LENGTH_SHORT).show();
        presenter.submitAccount(ViewUtils.getTextViewContent(etForgetUsername));
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
        tvForgetPwdSubmit.setEnabled(false);
    }

    //判读是手机号还是邮箱
    private void next() {
        final int type = checkInputType();
        switch (type) {
            case JConstant.TYPE_INVALID:
                Toast.makeText(getActivity(), getString(R.string.ACCOUNT_ERR_1), Toast.LENGTH_SHORT).show();
                enableEditTextCursor(true);
                return;
            case JConstant.TYPE_PHONE:
                if (fLayoutVerificationCodeInputBox.getVisibility() == View.GONE) {
                    if (presenter != null) {
                        presenter.checkIsReg(ViewUtils.getTextViewContent(etForgetUsername));
                    }
                } else {
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
                        isCheckAccAgain = true;
                        presenter.checkIsReg(ViewUtils.getTextViewContent(etForgetUsername));
                    }

                }
                break;
            case JConstant.TYPE_EMAIL:
                enableEditTextCursor(true);
                if (presenter != null)
                    presenter.checkIsReg(ViewUtils.getTextViewContent(etForgetUsername));
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
        boolean codeValid = checkInputType() != JConstant.TYPE_INVALID;
        tvForgetPwdSubmit.setEnabled(!flag && codeValid);
    }

    @OnTextChanged(R.id.et_verification_input)
    public void verificationInputChange(CharSequence s, int start, int before, int count) {
        boolean self = !TextUtils.isEmpty(s) && TextUtils.isDigitsOnly(s);
        boolean user = !TextUtils.isEmpty(etForgetUsername.getText()) && TextUtils.isDigitsOnly(etForgetUsername.getText());
        tvForgetPwdSubmit.setEnabled(self && user);
    }

    @OnClick(R.id.tv_forget_pwd_submit)
    public void forgetPwdCommit(View v) {
        JCache.isSmsAction = false;
        if (NetUtils.getJfgNetType(getContext()) == 0) {
            Toast.makeText(getContext(), getString(R.string.NO_NETWORK_4), Toast.LENGTH_SHORT).show();
            return;
        }
//        if (!JCache.isOnline) {
//            Toast.makeText(getContext(), "连接服务器失败", Toast.LENGTH_SHORT).show();
//            return;
//        }
        next();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }


    private void initTitle(final int ret) {
        if (ret == -1)
            return;
        if (ret == JConstant.TYPE_EMAIL)
            rLayoutForgetPwdToolbar.setToolbarTitle(R.string.EMAIL);
        else if (ret == JConstant.TYPE_PHONE) {
            rLayoutForgetPwdToolbar.setToolbarTitle(R.string.NEW_PWD);
        }
    }

    /**
     * 提交邮箱,修改密码，服务端响应之后的view
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
        final String content = String.format(getString(R.string.EMAIL_RESET_PWD),
                ViewUtils.getTextViewContent(etForgetUsername));
        ((TextView) mailView.findViewById(R.id.tv_send_email_content)).setText(content);
        View btn = mailView.findViewById(R.id.tv_email_confirm);
        btn.setEnabled(true);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
                RxBus.getCacheInstance().post(new RxEvent.LoginPopBack(etForgetUsername.getText().toString()));
            }
        });
        vsSetAccountPwd.addView(mailView);
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
        initNewPwdView(phoneNewPwdView);
        initTitle(JConstant.TYPE_PHONE);
        vsSetAccountPwd.addView(phoneNewPwdView);
        vsSetAccountPwd.showNext();
    }

    /**
     * 新密码设置界面
     *
     * @param phoneNewPwdView
     */
    private void initNewPwdView(View phoneNewPwdView) {
        TextView sureBtn = (TextView) phoneNewPwdView.findViewById(R.id.tv_new_pwd_submit);
        ImageView iv_Clear = (ImageView) phoneNewPwdView.findViewById(R.id.iv_new_clear_pwd);
        EditText et_newpass = (EditText) phoneNewPwdView.findViewById(R.id.et_new_pwd_input);
        CheckBox cb_pwd_visiable = (CheckBox) phoneNewPwdView.findViewById(R.id.cb_new_pwd_show);

        et_newpass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                iv_Clear.setVisibility(TextUtils.isEmpty(s) ? View.GONE : View.VISIBLE);
                iv_Clear.setClickable(true);
                sureBtn.setEnabled(TextUtils.isEmpty(s) ? false : true);
            }
        });

        sureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newPwd = et_newpass.getText().toString().trim();
                if (newPwd.length() < 6) {
                    ToastUtil.showToast(getString(R.string.PASSWORD_LESSTHAN_SIX));
                    return;
                }
                if (NetUtils.getNetType(ContextUtils.getContext()) == -1) {
                    ToastUtil.showToast(getString(R.string.OFFLINE_ERR_1));
                    return;
                }
                isCheckAgain = true;
                presenter.submitPhoneNumAndCode(PreferencesUtils.getString(JConstant.SAVE_TEMP_ACCOUNT),PreferencesUtils.getString(JConstant.SAVE_TEMP_CODE));
            }
        });

        iv_Clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_newpass.setText("");
            }
        });

        cb_pwd_visiable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ViewUtils.showPwd(et_newpass, isChecked);
                et_newpass.setSelection(et_newpass.length());
            }
        });
    }

    @Override
    public void submitResult(RequestResetPwdBean bean) {
        final int ret = bean == null ? -1 : bean.ret;
        switch (ret) {
            case JConstant.THIS_ACCOUNT_NOT_REGISTERED:
                tvForgetPwdSubmit.setEnabled(true);
                Toast.makeText(getContext(), getString(R.string.RET_ELOGIN_ACCOUNT_NOT_EXIST), Toast.LENGTH_SHORT).show();
                break;
            case JConstant.AUTHORIZE_MAIL:
                if (bean != null && !TextUtils.equals(bean.content, etForgetUsername.getText())) {
                    tvForgetPwdSubmit.setEnabled(true);
                    Toast.makeText(getContext(), getString(R.string.RET_ELOGIN_ACCOUNT_NOT_EXIST), Toast.LENGTH_SHORT).show();
                    break;
                }
                prepareMailView();
                break;
            case JConstant.AUTHORIZE_PHONE:
                tvForgetPwdSubmit.setEnabled(true);
                preparePhoneView();
                break;
        }
    }

    /**
     * 校验短信的结果
     */
    @Override
    public void checkSmsCodeResult(int code) {
        if (code == 181) {
            ToastUtil.showToast(getString(R.string.RET_ESMS_CODE_TIMEOUT));
        } else if (code == 180){
            ToastUtil.showToast(getString(R.string.RET_ELOGIN_VCODE_ERROR));
        } else if (code == 0) {
            if (!PreferencesUtils.getString(JConstant.SAVE_TEMP_ACCOUNT, "").equals(etForgetUsername.getText().toString().trim())) {
                ToastUtil.showToast(getContext().getResources().getString(R.string.Tap0_wrongcode));
                return;
            }
            if (isCheckAgain){
                presenter.resetPassword(newPwd);
            }else {
                preparePhoneView();
            }
        }
        isCheckAgain = false;
    }

    /**
     * 重置密码的结果
     *
     * @param code
     */
    @Override
    public void resetPwdResult(int code) {
        if (code == JError.ErrorInvalidPass) {
            ToastUtil.showToast(getString(R.string.RET_ECHANGEPASS_OLDPASS_ERROR));
        }else if (code == JError.ErrorSamePass){
            ToastUtil.showNegativeToast(getString(R.string.RET_ECHANGEPASS_SAME));
        }else {
            ToastUtil.showToast(getString(R.string.PWD_OK));
            if (getView() != null) {
                getView().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        RxBus.getCacheInstance().post(new RxEvent.LoginPopBack(PreferencesUtils.getString(JConstant.SAVE_TEMP_ACCOUNT)));
                        ActivityUtils.justPop(getActivity());
                    }
                }, 500);
            }
        }

    }

    @Override
    public void checkIsRegReuslt(int code) {
        if (code == 0) {
            if (isCheckAccAgain){
                if (tempAcc.equals(ViewUtils.getTextViewContent(etForgetUsername))){
                    getArguments().putString(LoginFragment.KEY_TEMP_ACCOUNT, etForgetUsername.getText().toString());
                    if (presenter != null)
                        presenter.submitPhoneNumAndCode(etForgetUsername.getText().toString(), ViewUtils.getTextViewContent(etVerificationInput));
                }else {
                    ToastUtil.showNegativeToast(getString(R.string.RET_ESMS_CODE_TIMEOUT));
                }
                isCheckAccAgain = false;
            }else {
                if (!Patterns.EMAIL_ADDRESS.matcher(ViewUtils.getTextViewContent(etForgetUsername)).find()) {
                    if (!presenter.checkOverCount()){
                        start2HandleVerificationCode();
                    }else {
                        ToastUtil.showNegativeToast(getString(R.string.GetCode_FrequentlyTips));
                        return;
                    }
                }
                presenter.submitAccount(ViewUtils.getTextViewContent(etForgetUsername));
                tempAcc = ViewUtils.getTextViewContent(etForgetUsername);
            }
        } else {
            ToastUtil.showNegativeToast(getString(R.string.INVALID_ACCOUNT));
        }
    }

    @Override
    public void setPresenter(ForgetPwdContract.Presenter presenter) {
        this.presenter = presenter;
    }

    //lazy load 的view 以下不起作用
    @Nullable
    @OnTextChanged(R.id.et_new_pwd_input)
    public void newPwdInputBoxChanged(CharSequence s, final int before, final int count, final int len) {
        final boolean empty = TextUtils.isEmpty(s);
        final boolean isNewPswValid = !empty
                && s.length() >= JConstant.PWD_LEN_MIN
                && s.length() <= JConstant.PWD_LEN_MAX;
        if (tvNewPwdSubmit != null) tvNewPwdSubmit.setEnabled(isNewPswValid);
        if (ivNewClearPwd != null) {
            ivNewClearPwd.setVisibility(empty ? View.GONE : View.VISIBLE);
        }
    }

    @OnCheckedChanged(R.id.cb_new_pwd_show)
    public void onNewPwdCheckBoxState(CompoundButton view, boolean isChecked) {
        ViewUtils.showPwd(etNewPwdInput, isChecked);
        if (etNewPwdInput != null) etNewPwdInput.setSelection(etNewPwdInput.length());
    }

    @OnClick({R.id.iv_new_clear_pwd, R.id.tv_new_pwd_submit})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_new_clear_pwd:
                if (etNewPwdInput != null) etNewPwdInput.setText("");
                break;
            case R.id.tv_new_pwd_submit:
                Toast.makeText(getActivity(), "yes?", Toast.LENGTH_SHORT).show();
                getActivity().getSupportFragmentManager().popBackStack();
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
        if (rLayoutForgetPwdToolbar.getTitle().equals(getString(R.string.FORGOT_PWD))){
            getFragmentManager().popBackStack();
        }else {
            rLayoutForgetPwdToolbar.setToolbarTitle(R.string.FORGOT_PWD);
            vsSetAccountPwd.setInAnimation(getContext(), R.anim.slide_in_left_overshoot);
            vsSetAccountPwd.setOutAnimation(getContext(), R.anim.slide_out_right);
            vsSetAccountPwd.showPrevious();
        }
    }
}
