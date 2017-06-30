package com.cylan.jiafeigou.n.view.mine;


import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.mine.BindMailContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.BindMailPresenterImpl;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.IMEUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.CustomToolbar;
import com.cylan.jiafeigou.widget.LoadingDialog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

/**
 * 创建者     谢坤
 * 创建时间   2016/8/10 15:37
 * 描述	      ${TODO}
 * <p>
 * 更新者     $Author$
 * 更新时间   $Date$
 * 更新描述   ${TODO}
 */
public class BindMailFragment extends IBaseFragment<BindMailContract.Presenter> implements BindMailContract.View {

    @BindView(R.id.iv_mine_personal_information_mailbox)
    ImageView mIvMailBox;
    @BindView(R.id.view_mine_personal_information_mailbox)
    View mViewMailBox;
    @BindView(R.id.et_mine_personal_information_mailbox)
    EditText mETMailBox;
    @BindView(R.id.custom_toolbar)
    CustomToolbar customToolbar;

    private String mailBox;

    @Override
    public void setPresenter(BindMailContract.Presenter basePresenter) {

    }

    @Override
    public String getUuid() {
        return null;
    }

    @Override
    public void showMailHasBindDialog() {
        ToastUtil.showNegativeToast(getString(R.string.RET_EEDITUSERINFO_EMAIL));
    }

    /**
     * 显示请求发送结果
     */
    @Override
    public void showSendReqResult(int code) {
        if (!isAdded()) return;
        if (!TextUtils.isEmpty(getEditText())) {
            hideSendReqHint();
            if (code == JError.ErrorOK) {
                //区分第三方登录
                if (basePresenter.isOpenLogin()) {
                    JFGAccount userAccount = BaseApplication.getAppComponent().getSourceManager().getJFGAccount();
                    AppLogger.d("bindmail2:" + userAccount.getPhone());
                    if (TextUtils.isEmpty(userAccount.getPhone())) {
                        jump2SetPasswordFragment(getEditText());
                    } else {
                        //绑定成功
                        jump2MailConnectFragment();
                    }
                } else {
                    //绑定成功
                    jump2MailConnectFragment();
                }
            } else {
                //绑定失败
                ToastUtil.showPositiveToast(getString(R.string.SUBMIT_FAIL));
            }
        }
    }

    /**
     * 拿到账号
     */
    @Override
    public void getUserAccountData(JFGAccount userinfo) {
        if (userinfo != null) {
            if (TextUtils.isEmpty(userinfo.getEmail()) || userinfo.getEmail() == null) {
            } else {
                customToolbar.setToolbarLeftTitle(getString(R.string.CHANGE_EMAIL));
            }
        }
    }

    public static BindMailFragment newInstance(Bundle bundle) {
        BindMailFragment fragment = new BindMailFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (basePresenter != null) {
            basePresenter.start();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (basePresenter != null) basePresenter.stop();
        IMEUtils.hide(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        customToolbar.setTvToolbarRightIcon(R.drawable.icon_finish_disable);
        customToolbar.setTvToolbarRightEnable(false);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine_info_mailbox, container, false);
        ButterKnife.bind(this, view);
        initPresenter();
        initListener();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewUtils.setChineseExclude(mETMailBox, 65);
        initKeyListener();
        initMailEdit();
    }

    private void initMailEdit() {
        JFGAccount jfgAccount = BaseApplication.getAppComponent().getSourceManager().getJFGAccount();
        if (jfgAccount != null)
            mETMailBox.setText(jfgAccount.getEmail());
    }

    private void initKeyListener() {
//        mETMailBox.setOnKeyListener((v, keyCode, event) -> {
//            if (KeyEvent.KEYCODE_ENTER == keyCode && KeyEvent.ACTION_DOWN == event.getAction()) {
//                mailBox = getEditText();
//                if (TextUtils.isEmpty(mailBox)) {
//                    return false;
//                } else if (!basePresenter.checkEmail(mailBox)) {
//                    ToastUtil.showNegativeToast(getString(R.string.EMAIL_2));
//                    return false;
//                } else {
//                    basePresenter.isEmailBind(mailBox);
//                }
//            }
//            return false;
//        });
    }

    private void initPresenter() {
        basePresenter = new BindMailPresenterImpl(this);
    }

    @OnTextChanged(R.id.et_mine_personal_information_mailbox)
    public void onEditChange(CharSequence s, int start, int before, int count) {
        boolean isEmpty = TextUtils.isEmpty(s);
        mIvMailBox.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        mViewMailBox.setBackgroundColor(isEmpty ? getResources().getColor(R.color.color_f2f2f2) : getResources().getColor(R.color.color_36BDFF));
        customToolbar.setTvToolbarRightIcon(isEmpty ? R.drawable.icon_finish_disable : R.drawable.me_icon_finish_normal);
        customToolbar.setTvToolbarRightEnable(!isEmpty);
    }

    /**
     * 监听输入框内容的变化
     */
    private void initListener() {
        //设置输入框，不可输入回车
        mETMailBox.setOnEditorActionListener((v, actionId, event) -> (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER));

        /**
         * 当输入有内容的时候，点击右侧xx便吧 editText内容清空
         */
        mIvMailBox.setOnClickListener(v -> mETMailBox.setText(""));
    }

    @OnClick({R.id.tv_toolbar_icon, R.id.tv_toolbar_right})
    public void onClick(View v) {
        switch (v.getId()) {
            //返回上一个fragment
            case R.id.tv_toolbar_icon:
                IMEUtils.hide((Activity) getContext());
                getActivity().getSupportFragmentManager().popBackStack();
                break;
            //绑定邮箱
            case R.id.tv_toolbar_right:
                mailBox = getEditText();
                if (TextUtils.isEmpty(mailBox)) {
                    return;
                } else if (!basePresenter.checkEmail(mailBox)) {
                    ToastUtil.showNegativeToast(getString(R.string.EMAIL_2));
                    return;
                } else {
                    basePresenter.sendSetAccountReq(mailBox);
                }
                break;
        }
    }


    @Override
    public void showSendReqHint() {
        LoadingDialog.showLoading(getActivity().getSupportFragmentManager(), getString(R.string.upload));
    }

    @Override
    public void hideSendReqHint() {
        LoadingDialog.dismissLoading(getActivity().getSupportFragmentManager());
    }

    /**
     * 获取到输入的内容
     *
     * @return
     */
    @Override
    public String getEditText() {
        return mETMailBox.getText().toString();
    }

    /**
     * 网络状态变化
     *
     * @param state
     */
    @Override
    public void onNetStateChanged(int state) {
        if (!isAdded() || getView() == null) return;
        getView().post(() -> {
            if (state == 0) {
                hideSendReqHint();
                ToastUtil.showNegativeToast(getString(R.string.NO_NETWORK_1));
            }
        });
    }

    @Override
    public void jump2MailConnectFragment() {
        Bundle bundle = new Bundle();
        bundle.putString(MineReSetMailTip.KEY_MAIL, getEditText());
        MineReSetMailTip fragment = MineReSetMailTip.newInstance(bundle);
        ActivityUtils.addFragmentSlideInFromRight(getActivity().getSupportFragmentManager(),
                fragment, android.R.id.content);
        IMEUtils.hide(getActivity());
    }

    /**
     * 跳转到设置密码界面
     */
    private void jump2SetPasswordFragment(String account) {
        Bundle bundle = new Bundle();
        bundle.putString("useraccount", account);
        bundle.putString("token", "");
        MineInfoSetNewPwdFragment fragment = MineInfoSetNewPwdFragment.newInstance(bundle);
        ActivityUtils.addFragmentSlideInFromRight(getActivity().getSupportFragmentManager(),
                fragment, android.R.id.content);
    }

}
