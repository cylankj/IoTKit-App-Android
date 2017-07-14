package com.cylan.jiafeigou.n.view.mine;

import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
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

/**
 * 作者：zsl
 * 创建时间：2016/9/1
 * 描述：
 */
public class MineInfoBindPhoneFragment extends IBaseFragment<MineBindPhoneContract.Presenter> implements MineBindPhoneContract.View {

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

    private static VCode vCode;

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

        if (s.length() == 6 && getInputPhone().length() == 11) {
            customToolbar.setTvToolbarRightIcon(R.drawable.me_icon_finish_normal);
            customToolbar.setTvToolbarRightEnable(true);
            vertifyCodeLine.setBackgroundColor(Color.parseColor("#36bdff"));
        } else {
            customToolbar.setTvToolbarRightIcon(R.drawable.icon_finish_disable);
            customToolbar.setTvToolbarRightEnable(false);
            vertifyCodeLine.setBackgroundColor(Color.parseColor("#f2f2f2"));
        }
    }

    private void initCountDownTime(long countDown) {
        countDownTimer = new CountDownTimer(countDown, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                final String content = millisUntilFinished / 1000 + "s";
                tvMeterGetCode.setText(content);
                if (vCode != null) vCode.time += 1000;
            }

            @Override
            public void onFinish() {
                if (isAdded()) {
                    tvMeterGetCode.setText(getString(R.string.Button_ReObtain));
                    tvMeterGetCode.setEnabled(true);
                }
            }
        };
        if (countDown != 90 * 1000) {
            countDownTimer.start();
            tvMeterGetCode.setEnabled(false);
        } else tvMeterGetCode.setEnabled(true);
    }

    private void initCountDownTime() {
        long countDown = 90 * 1000;
        if (vCode != null) {
            if (!TextUtils.isEmpty(vCode.account)) {
                etMineBindPhone.setText(vCode.account);
            }
            if (System.currentTimeMillis() - vCode.time < 90 * 1000) {
                countDown = System.currentTimeMillis() - vCode.time;
            }
        }
        initCountDownTime(countDown);

    }

    /**
     * 输入框监听
     */
    @OnTextChanged(R.id.et_mine_bind_phone)
    public void initEditListener(CharSequence s, int start, int before, int count) {
        boolean isValidPhone = JConstant.PHONE_REG.matcher(s).find();
        if (isValidPhone) {
            if (vCode != null && TextUtils.equals(vCode.account, s)) {
                if (System.currentTimeMillis() - vCode.time > 90 * 1000) {
                    //是否超时
                    tvMeterGetCode.setEnabled(true);
                    tvMeterGetCode.setText(getString(R.string.GET_CODE));
                } else {//没超时
                    initCountDownTime(System.currentTimeMillis() - vCode.time);
                }
            } else {
                tvMeterGetCode.setEnabled(true);
                tvMeterGetCode.setText(getString(R.string.GET_CODE));
            }
        } else {
            if (countDownTimer != null) countDownTimer.cancel();
        }
        //        if (s.length() == 0) {
//            tvMeterGetCode.setEnabled(false);
//            ivMineBindPhoneClear.setVisibility(View.INVISIBLE);
//            viewMinePersonalInformationMailbox.setBackgroundColor(Color.parseColor("#f2f2f2"));
//        } else {
//            tvMeterGetCode.setEnabled(true);
//            ivMineBindPhoneClear.setVisibility(View.VISIBLE);
//            viewMinePersonalInformationMailbox.setBackgroundColor(Color.parseColor("#36bdff"));
//        }
//
//        if (s.length() == 11 && getInputCheckCode().length() == 6) {
//            customToolbar.setTvToolbarRightIcon(R.drawable.me_icon_finish_normal);
//            customToolbar.setTvToolbarRightEnable(true);
//            vertifyCodeLine.setBackgroundColor(Color.parseColor("#36bdff"));
//        } else {
//            customToolbar.setTvToolbarRightIcon(R.drawable.icon_finish_disable);
//            customToolbar.setTvToolbarRightEnable(false);
//            vertifyCodeLine.setBackgroundColor(Color.parseColor("#f2f2f2"));
//        }

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
                LoadingDialog.showLoading(getFragmentManager(), getString(R.string.LOADING));
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
                    basePresenter.CheckVerifyCode(etMineBindPhone.getText().toString().trim(), getInputCheckCode(), PreferencesUtils.getString(JConstant.KEY_REGISTER_SMS_TOKEN));
                }
                break;
        }

    }

    private void updateVCode(String account) {
        if (vCode == null) vCode = new VCode();
        vCode.account = account;
        vCode.time = System.currentTimeMillis();
    }

    /**
     * 跳转到设置密码界面
     */
    private void jump2SetPWDFragment(String account) {
        Bundle bundle = new Bundle();
        bundle.putString("useraccount", account);
        bundle.putString("token", PreferencesUtils.getString(JConstant.KEY_REGISTER_SMS_TOKEN));
        ActivityUtils.addFragmentSlideInFromRight(getActivity().getSupportFragmentManager(),
                MineInfoSetNewPwdFragment.newInstance(bundle), android.R.id.content);
    }

    @Override
    public void onResult(int event, int errId) {
        LoadingDialog.dismissLoading(getFragmentManager());
        switch (event) {
            case JConstant.GET_SMS_BACK:
                vCode.time = System.currentTimeMillis();
                initCountDownTime();
                break;
            case JResultEvent.JFG_RESULT_CHECK_REGISTER:
                if (errId == JError.ErrorOK) {
                    ToastUtil.showToast(getString(R.string.RET_EEDITUSERINFO_SMS_PHONE));
                }
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
//        if (getInputPhone().equals(registerBack.account)) {
//            ToastUtil.showNegativeToast(getString(R.string.RET_EEDITUSERINFO_SMS_PHONE));
//        } else {
//            //发送验证码
//            basePresenter.getCheckCode(getInputPhone());
//        }
    }

//    /**
//     * 检测账号是否已经注册的结果
//     *
//     * @param checkAccountCallback
//     */
//    @Override
//    public void handlerCheckPhoneResult(RxEvent.CheckAccountCallback checkAccountCallback) {
//
//    }

    /**
     * 校验短信验证码的结果
     *
     * @param resultVerifyCode
     */
    @Override
    public void handlerCheckCodeResult(RxEvent.ResultVerifyCode resultVerifyCode) {
        if (resultVerifyCode.code == JError.ErrorOK) {
            showLoadingDialog();
            basePresenter.sendChangePhoneReq(getInputPhone(), PreferencesUtils.getString(JConstant.KEY_REGISTER_SMS_TOKEN));
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
        hideLoadingDialog();
        JFGAccount userinfo = BaseApplication.getAppComponent().getSourceManager().getJFGAccount();
        if (userinfo != null && !TextUtils.isEmpty(getInputPhone())) {
            if (code == JError.ErrorOK) {
//                    if (basePresenter.isOpenLogin() && TextUtils.isEmpty(userinfo.getEmail())) {
//                        //是三方登录
//                        jump2SetPWDFragment(userinfo.getAccount());
//                        return;
//                    }
                ToastUtil.showPositiveToast(getString(R.string.SCENE_SAVED));
                if (getView() != null) {
                    if (changeAccListener != null) {
                        changeAccListener.onChange(getInputPhone());
                    }
                    getActivity().getSupportFragmentManager().popBackStack();
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
        LoadingDialog.showLoading(getActivity().getSupportFragmentManager(), getString(R.string.LOADING));
    }

    /**
     * 隐藏loading
     */
    @Override
    public void hideLoadingDialog() {
        LoadingDialog.dismissLoading(getActivity().getSupportFragmentManager());
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
    public void startCountTime() {
        //显示倒计时
        fLayoutVerificationCodeInputBox.setVisibility(View.VISIBLE);
        countDownTimer.start();
        tvMeterGetCode.setEnabled(false);
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
        private long time;
    }
}
