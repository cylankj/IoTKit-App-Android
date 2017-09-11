package com.cylan.jiafeigou.n.view.mine;

import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.misc.JResultEvent;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineBindPhoneContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.MineBindPhonePresenterImp;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.CustomToolbar;
import com.cylan.jiafeigou.widget.LoadingDialog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

import static com.cylan.jiafeigou.misc.JError.ErrorAccountNotExist;

/**
 * 作者：zsl
 * 创建时间：2016/9/1
 * 描述：
 */
public class MineInfoBindPhoneFragment extends IBaseFragment<MineBindPhoneContract.Presenter> implements MineBindPhoneContract.View {


    private static final long TIME_OUT = 90 * 1000;
    @BindView(R.id.et_mine_bind_phone)
    EditText etMineBindPhone;
    @BindView(R.id.iv_mine_bind_phone_clear)
    ImageView ivMineBindPhoneClear;
    @BindView(R.id.et_verification_input)
    EditText etVerificationInput;
    @BindView(R.id.tv_meter_get_code)
    TextView tvMeterGetCode;
    @BindView(R.id.fLayout_verification_code_input_box)
    FrameLayout fLayoutVerificationCodeInputBox;
    @BindView(R.id.view_mine_personal_information_mailbox)
    View viewMinePersonalInformationMailbox;
    @BindView(R.id.vertify_code_line)
    View vertifyCodeLine;
    @BindView(R.id.custom_toolbar)
    CustomToolbar customToolbar;

    private CountDownTimer countDownTimer;

    private VCode vCode;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        basePresenter = new MineBindPhonePresenterImp(this);
    }

    public OnChangePhoneListener changeAccListener;

    public interface OnChangePhoneListener {
        void onChange(String phone);
    }

    public void setOnChangePhoneListener(OnChangePhoneListener changeAccListener) {
        this.changeAccListener = changeAccListener;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine_info_bind_phone, container, false);
        ButterKnife.bind(this, view);
        initCountDownTime();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewUtils.setChineseExclude(etMineBindPhone, 11);
        customToolbar.setTvToolbarRightEnable(false);
    }

    /**
     * 验证码输入框变化监听
     */
    @OnTextChanged(R.id.et_verification_input)
    public void initCheckCodeListener(CharSequence s, int start, int before, int count) {
        if (s.length() == 6 && JConstant.PHONE_REG.matcher(getInputPhone()).find()) {
            customToolbar.setTvToolbarRightIcon(R.drawable.icon_finish_normal);
            customToolbar.setTvToolbarRightEnable(true);
            vertifyCodeLine.setBackgroundColor(Color.parseColor("#36bdff"));
        } else {
            customToolbar.setTvToolbarRightIcon(R.drawable.icon_finish_disable);
            customToolbar.setTvToolbarRightEnable(false);
            vertifyCodeLine.setBackgroundColor(Color.parseColor("#f2f2f2"));
        }
    }

    private void initCountDownTime(long countDown) {
        if (countDown <= 0) countDown = 1;
        countDownTimer = new CountDownTimer(countDown, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                final String content = millisUntilFinished / 1000 + "s";
                tvMeterGetCode.setText(content);
                tvMeterGetCode.setEnabled(false);
                if (vCode != null) vCode.millisUntilFinished = millisUntilFinished;
            }

            @Override
            public void onFinish() {
                if (isAdded()) {
                    tvMeterGetCode.setText(getString(R.string.Button_ReObtain));
                    tvMeterGetCode.setEnabled(true);
                }
            }
        };
        if (countDown <= TIME_OUT) {
            countDownTimer.cancel();
            countDownTimer.start();
            tvMeterGetCode.setEnabled(false);
        } else tvMeterGetCode.setEnabled(true);
    }

    private void initCountDownTime() {
        long countDown = TIME_OUT;
        if (vCode != null) {
            if (!TextUtils.isEmpty(vCode.account)) {
                etMineBindPhone.setText(vCode.account);
            }
            if (System.currentTimeMillis() - vCode.startTime < TIME_OUT) {
                countDown = vCode.millisUntilFinished;
            }
            initCountDownTime(countDown);
        } else {
            tvMeterGetCode.setEnabled(false);
            tvMeterGetCode.setText(getString(R.string.GET_CODE));
        }
    }

    /**
     * 输入框监听
     */
    @OnTextChanged(R.id.et_mine_bind_phone)
    public void initEditListener(CharSequence s, int start, int before, int count) {
        boolean isValidPhone = JConstant.PHONE_REG.matcher(s).find();
        if (isValidPhone) {
            if (vCode != null && TextUtils.equals(vCode.account, s)) {
                if (System.currentTimeMillis() - vCode.startTime > TIME_OUT) {
                    //是否超时
                    tvMeterGetCode.setEnabled(true);
                    tvMeterGetCode.setText(getString(R.string.GET_CODE));
                } else {//没超时
                    initCountDownTime(TIME_OUT - (System.currentTimeMillis() - vCode.startTime));
                }
            } else {
                tvMeterGetCode.setEnabled(true);
                tvMeterGetCode.setText(getString(R.string.GET_CODE));
            }
        } else {
            if (countDownTimer != null) {
                countDownTimer.cancel();
            }
            tvMeterGetCode.setText(getString(R.string.GET_CODE));
            tvMeterGetCode.setEnabled(false);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        JFGAccount jfgAccount = BaseApplication.getAppComponent().getSourceManager().getJFGAccount();
        if (jfgAccount != null)
            basePresenter.isBindOrChange(jfgAccount);
    }

    public static MineInfoBindPhoneFragment newInstance(Bundle bundle) {
        MineInfoBindPhoneFragment fragment = new MineInfoBindPhoneFragment();
        fragment.setArguments(bundle);
        return fragment;
    }


    @OnClick({R.id.tv_toolbar_icon, R.id.tv_meter_get_code, R.id.iv_mine_bind_phone_clear, R.id.tv_toolbar_right})
    public void onClick(View view) {
        ViewUtils.deBounceClick(view);
        switch (view.getId()) {
            case R.id.tv_meter_get_code:
                if (NetUtils.getJfgNetType() == 0) {
                    ToastUtil.showToast(getString(R.string.NoNetworkTips));
                    return;
                }
                //获取验证码点击
                if (JConstant.PHONE_REG.matcher(getInputPhone()).find()) {
                    AppLogger.d("暂时去掉");
                    // TODO: 2017/7/24 以下这个判断有 bug
//                    JFGAccount jfgAccount = BaseApplication.getAppComponent().getSourceManager().getJFGAccount();
//                    if (jfgAccount != null && (TextUtils.equals(jfgAccount.getPhone(), getInputPhone()) ||
//                            TextUtils.equals(jfgAccount.getAccount(), getInputPhone()))) {
//                        ToastUtil.showToast(getString(R.string.RET_EEDITUSERINFO_SMS_PHONE));
//                        return;
//                    }
                    basePresenter.getVerifyCode(getInputPhone());
                    vCode = new VCode();
                    vCode.account = getInputPhone();
                } else {
                    ToastUtil.showToast(getString(R.string.PHONE_NUMBER_2));
                }
                break;
            case R.id.tv_toolbar_icon:
                getActivity().getSupportFragmentManager().popBackStack();
                break;

            case R.id.iv_mine_bind_phone_clear:
                etMineBindPhone.setText("");
                break;
            case R.id.tv_toolbar_right:
                //点击完成
                if (!JConstant.PHONE_REG.matcher(getInputPhone()).find()) {
                    ToastUtil.showNegativeToast(getString(R.string.PHONE_NUMBER_2));
                    return;
                }
                if (getInputCheckCode().length() != 6) {
                    ToastUtil.showNegativeToast(getString(R.string.Tap0_wrongcode));
                } else {
                    basePresenter.CheckVerifyCode(etMineBindPhone.getText().toString().trim(),
                            getInputCheckCode());
                }
                break;
        }

    }

    /**
     * 跳转到设置密码界面
     */
    private void jump2SetPWDFragment(String account) {
        Bundle bundle = new Bundle();
        bundle.putString("useraccount", account);
        bundle.putString("token", PreferencesUtils.getString(JConstant.KEY_REGISTER_SMS_TOKEN));
        ActivityUtils.addFragmentSlideInFromRight(getActivity().getSupportFragmentManager(),
                MineInfoSetNewPwdFragment.newInstance(bundle), android.R.id.content, "bindStack");
    }

    @Override
    public void onResult(int event, int errId) {
        switch (event) {
            case JConstant.GET_SMS_BACK:
                if (errId == JError.ErrorOK) {
                    vCode.startTime = System.currentTimeMillis();
                    initCountDownTime(TIME_OUT);
                    return;
                }
                break;
            case JResultEvent.JFG_RESULT_CHECK_REGISTER:
                if (errId == JError.ErrorOK) {
                    ToastUtil.showToast(getString(R.string.RET_EEDITUSERINFO_SMS_PHONE));
                    return;
                }
                break;
            case JConstant.CHECK_ACCOUNT:
                if (errId == JError.ErrorAccountAlreadyExist) {
                    ToastUtil.showToast(getString(R.string.REGISTERED));
                    return;
                }
            case JConstant.AUTHORIZE_PHONE_SMS:
                if (errId == JError.ErrorOK) {
                    jump2SetPWDFragment(getInputPhone());
                    return;
                }
                break;
            case JConstant.CHECK_TIMEOUT:
                ToastUtil.showToast(getString(R.string.Request_TimeOut));
                return;
        }

        switch (errId) {
            case JError.ErrorSMSCodeTimeout:
                ToastUtil.showToast(getString(R.string.RET_ESMS_CODE_TIMEOUT));
                break;
            case JError.ErrorSMSCodeNotMatch:
                ToastUtil.showToast(getString(R.string.RET_ELOGIN_VCODE_ERROR));
                break;
            case JError.ErrorInvalidPass:
//                            ToastUtil.showToast(getString(R.string.RET_ECHANGEPASS_OLDPASS_ERROR));
                break;
            case ErrorAccountNotExist:
//                            ToastUtil.showToast(getString(R.string.RET_ESHARE_ACCOUNT_NOT_EXIT));
                break;
            case JError.ErrorConnect:
                ToastUtil.showNegativeToast(getString(R.string.LOGIN_ERR));
                break;
            case JError.ErrorP2PSocket:
                ToastUtil.showNegativeToast(getString(R.string.NoNetworkTips));
                break;
            case JError.ErrorGetCodeTooFrequent:
                ToastUtil.showNegativeToast(getString(R.string.GetCode_FrequentlyTips));
                break;
        }
    }

    @Override
    public void initToolbarTitle(String title) {
        customToolbar.setToolbarLeftTitle(title);
        if (title.equals(getString(R.string.CHANGE_PHONE_NUM))) {
        } else {
        }
    }

    @Override
    public String getInputPhone() {
        return etMineBindPhone.getText().toString().trim();
    }

    /**
     * 获取到输入的验证码
     *
     * @return
     */
    @Override
    public String getInputCheckCode() {
        return etVerificationInput.getText().toString().trim();
    }

    @Override
    public void handlerCheckPhoneResult(RxEvent.CheckRegisterBack registerBack) {
    }


    /**
     * 校验短信验证码的结果
     *
     * @param resultVerifyCode
     */
    @Override
    public void handlerCheckCodeResult(RxEvent.ResultVerifyCode resultVerifyCode) {
        if (resultVerifyCode == null) {
            ToastUtil.showToast(getString(R.string.Request_TimeOut));
        } else if (resultVerifyCode.code == JError.ErrorOK) {
//            basePresenter.sendChangePhoneReq(getInputPhone(), PreferencesUtils.getString(JConstant.KEY_REGISTER_SMS_TOKEN));
        } else if (resultVerifyCode.code == JError.ErrorSMSCodeTimeout) {
            hideLoadingDialog();
            ToastUtil.showToast(getString(R.string.RET_ESMS_CODE_TIMEOUT));
        } else {
            ToastUtil.showToast(getString(R.string.RET_ELOGIN_VCODE_ERROR));
        }
    }

    /**
     * 处理修改结果
     *
     * @param code
     */
    @Override
    public void handlerResetPhoneResult(int code) {
        JFGAccount userinfo = BaseApplication.getAppComponent().getSourceManager().getJFGAccount();
        if (userinfo != null && !TextUtils.isEmpty(getInputPhone())) {
            if (code == JError.ErrorOK) {
                if (basePresenter.isOpenLogin() && TextUtils.isEmpty(userinfo.getEmail())) {
                    //是三方登录
                    jump2SetPWDFragment(userinfo.getAccount());
                    return;
                }
                ToastUtil.showPositiveToast(getString(R.string.SCENE_SAVED));
                if (getView() != null) {
                    if (changeAccListener != null) {
                        changeAccListener.onChange(getInputPhone());
                    }
                    getActivity().getSupportFragmentManager().popBackStack("bindStack", FragmentManager.POP_BACK_STACK_INCLUSIVE);
                }
            } else {
                ToastUtil.showNegativeToast(getString(R.string.SUBMIT_FAIL));
            }
        }
    }

    /**
     * 显示loading
     */
    @Override
    public void showLoadingDialog() {
        LoadingDialog.showLoading(getActivity(), getString(R.string.LOADING), true);
    }

    /**
     * 隐藏loading
     */
    @Override
    public void hideLoadingDialog() {
        LoadingDialog.dismissLoading();
    }

    /**
     * 网络状态变化
     *
     * @param state
     */
    @Override
    public void onNetStateChanged(int state) {
        if (state == -1) {
            hideLoadingDialog();
            ToastUtil.showNegativeToast(getString(R.string.NO_NETWORK_1));
        }
    }

    @Override
    public void getSmsCodeResult(int code) {
        if (code == 192) {
            ToastUtil.showNegativeToast(getString(R.string.GetCode_FrequentlyTips));
        } else {
            ToastUtil.showNegativeToast("error:" + code);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    private static class VCode {
        private String account;
        private long startTime;
        private long millisUntilFinished = TIME_OUT;
    }
}
