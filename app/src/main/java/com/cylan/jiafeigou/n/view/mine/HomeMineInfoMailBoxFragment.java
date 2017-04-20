package com.cylan.jiafeigou.n.view.mine;


import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineInfoBindMailContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.MineInfoBineMailPresenterImp;
import com.cylan.jiafeigou.support.log.AppLogger;
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
public class HomeMineInfoMailBoxFragment extends Fragment implements MineInfoBindMailContract.View {

    @BindView(R.id.iv_mine_personal_information_mailbox)
    ImageView mIvMailBox;
    @BindView(R.id.view_mine_personal_information_mailbox)
    View mViewMailBox;
    @BindView(R.id.et_mine_personal_information_mailbox)
    EditText mETMailBox;
    @BindView(R.id.custom_toolbar)
    CustomToolbar customToolbar;

    private String mailBox;
    private MineInfoBindMailContract.Presenter presenter;
    private OnBindMailBoxListener onBindMailBoxListener;
    private JFGAccount userinfo;
    private boolean bindOrChange = false;       //绑定或者修改邮箱

    @Override
    public void setPresenter(MineInfoBindMailContract.Presenter presenter) {

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
        if (!TextUtils.isEmpty(getEditText())) {
            hideSendReqHint();
            if (code == JError.ErrorOK) {
                //区分第三方登录
                if (presenter.isOpenLogin()){
                    JFGAccount userAccount = BaseApplication.getAppComponent().getSourceManager().getJFGAccount();
                    AppLogger.d("bindmail2:"+userAccount.getPhone());
                    if (TextUtils.isEmpty(userAccount.getPhone())) {
                        jump2SetPasswordFragment(getEditText());
                    }else {
                        //绑定成功
                        jump2MailConnectFragment();
                    }
                }else {
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
    public void getUserAccountData(JFGAccount account) {
        userinfo = account;
        if (userinfo != null) {
            if (TextUtils.isEmpty(userinfo.getEmail()) || userinfo.getEmail() == null) {
                bindOrChange = true;
            } else {
                bindOrChange = false;
                customToolbar.setToolbarLeftTitle(getString(R.string.CHANGE_EMAIL));
            }
        }
    }

    public interface OnBindMailBoxListener {
        void mailBoxChange(String content);
    }

    public void setListener(OnBindMailBoxListener mListener) {
        this.onBindMailBoxListener = mListener;
    }

    public static HomeMineInfoMailBoxFragment newInstance(Bundle bundle) {
        HomeMineInfoMailBoxFragment fragment = new HomeMineInfoMailBoxFragment();
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
        if (presenter != null) {
            presenter.start();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (presenter != null) presenter.stop();
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
        Bundle arguments = getArguments();
        JFGAccount jfgAccount = (JFGAccount) arguments.getSerializable("userinfo");
        if (jfgAccount != null)
            mETMailBox.setText(jfgAccount.getEmail());
    }

    private void initKeyListener() {
        mETMailBox.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (KeyEvent.KEYCODE_ENTER == keyCode && KeyEvent.ACTION_DOWN == event.getAction()) {
                    mailBox = getEditText();
                    if (TextUtils.isEmpty(mailBox)) {
                        return false;
                    } else if (!presenter.checkEmail(mailBox)) {
                        ToastUtil.showNegativeToast(getString(R.string.EMAIL_2));
                        return false;
                    } else {
                        presenter.checkEmailIsBinded(mailBox);
                    }
                }
                return false;
            }
        });
    }

    private void initPresenter() {
        presenter = new MineInfoBineMailPresenterImp(this);
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
        mETMailBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER);
            }
        });

        /**
         * 当输入有内容的时候，点击右侧xx便吧 editText内容清空
         */
        mIvMailBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mETMailBox.setText("");
            }
        });
    }

    @OnClick({R.id.tv_toolbar_icon, R.id.tv_toolbar_right})
    public void onClick(View v) {
        switch (v.getId()) {
            //返回上一个fragment
            case R.id.tv_toolbar_icon:
                IMEUtils.hide((Activity) getContext());
                getFragmentManager().popBackStack();
                break;
            //绑定邮箱
            case R.id.tv_toolbar_right:
                mailBox = getEditText();
                if (TextUtils.isEmpty(mailBox)) {
                    return;
                } else if (!presenter.checkEmail(mailBox)) {
                    ToastUtil.showNegativeToast(getString(R.string.EMAIL_2));
                    return;
                } else {
                    presenter.checkEmailIsBinded(mailBox);
                }
                break;
        }
    }

    /**
     * 账号未注册过
     */
    @Override
    public void showAccountUnReg() {
        presenter.sendSetAccountReq(getEditText());
    }

    @Override
    public void showSendReqHint() {
        LoadingDialog.showLoading(getFragmentManager(), getString(R.string.upload));
    }

    @Override
    public void hideSendReqHint() {
        LoadingDialog.dismissLoading(getFragmentManager());
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
        if (state == -1) {
            hideSendReqHint();
            ToastUtil.showNegativeToast(getString(R.string.NO_NETWORK_1));
        }
    }

    @Override
    public void jump2MailConnectFragment() {
        Bundle bundle = new Bundle();
        bundle.putString("useraccount", getEditText());
        MineReSetMailTip fragment = MineReSetMailTip.newInstance(bundle);
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(android.R.id.content, fragment, "MineReSetMailTip")
//                .addToBackStack("personalInformationFragment")
                .commit();

    }

    /**
     * 跳转到设置密码界面
     */
    private void jump2SetPasswordFragment(String account) {
        Bundle bundle = new Bundle();
        bundle.putString("useraccount", account);
        bundle.putString("token", "");
        MineInfoSetNewPwdFragment fragment = MineInfoSetNewPwdFragment.newInstance(bundle);
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(android.R.id.content, fragment, "MineInfoSetNewPwdFragment")
//                .addToBackStack("personalInformationFragment")
                .commit();

    }

}
