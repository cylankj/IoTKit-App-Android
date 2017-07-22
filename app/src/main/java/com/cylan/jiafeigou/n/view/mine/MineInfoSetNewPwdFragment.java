package com.cylan.jiafeigou.n.view.mine;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.AlertDialogManager;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineInfoSetNewPwdContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.MineInfoSetNewPwdPresenterImp;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.IMEUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.CustomToolbar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnTextChanged;

/**
 * 作者：zsl
 * 创建时间：2016/12/28
 * 描述：
 */
public class MineInfoSetNewPwdFragment extends IBaseFragment<MineInfoSetNewPwdContract.Presenter> implements
        MineInfoSetNewPwdContract.View {

    @BindView(R.id.et_mine_set_newpwd)
    EditText etMineSetNewpwd;
    @BindView(R.id.iv_mine_new_pwd_clear)
    ImageView ivMineNewPwdClear;
    @BindView(R.id.cb_new_pwd_show)
    CheckBox cbNewPwdShow;
    @BindView(R.id.custom_toolbar)
    CustomToolbar customToolbar;
    private String userAccount;
    private String token;

    public static MineInfoSetNewPwdFragment newInstance(Bundle bundle) {
        MineInfoSetNewPwdFragment fragment = new MineInfoSetNewPwdFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        userAccount = arguments.getString("useraccount");
        token = arguments.getString("token");
        this.basePresenter = new MineInfoSetNewPwdPresenterImp(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine_info_set_pwd, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewUtils.setChineseExclude(etMineSetNewpwd, getResources().getInteger(R.integer.max_password_length));
        etMineSetNewpwd.requestFocus();//默认为可输入状态
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void setPresenter(MineInfoSetNewPwdContract.Presenter presenter) {

    }

    @OnTextChanged(R.id.et_mine_set_newpwd)
    public void onNewPwdChance(CharSequence s, int start, int before, int count) {
        boolean isEmpty = TextUtils.isEmpty(s);
        ivMineNewPwdClear.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        customToolbar.setTvToolbarRightIcon(isEmpty ? R.drawable.icon_finish_disable : R.drawable.me_icon_finish_normal);
        customToolbar.setTvToolbarRightEnable(!isEmpty);
    }

    @OnCheckedChanged(R.id.cb_new_pwd_show)
    public void onPwdCheckChange(CompoundButton buttonView, boolean isChecked) {
        ViewUtils.showPwd(etMineSetNewpwd, isChecked);
        etMineSetNewpwd.setSelection(etMineSetNewpwd.length());
    }

    @OnClick({R.id.tv_toolbar_icon, R.id.tv_toolbar_right, R.id.iv_mine_new_pwd_clear})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_toolbar_icon:
                backDialog();
                break;
            case R.id.tv_toolbar_right:
                if (getNewPwd().length() < 6) {
                    ToastUtil.showToast(getString(R.string.PASSWORD_LESSTHAN_SIX));
                    return;
                }

                if (basePresenter.checkIsOverTime()) {
                    ToastUtil.showNegativeToast(getString(R.string.Tips_Device_TimeoutRetry));
                    //跳转到个人信息页
                    jump2MineInfoFragment();
                    return;
                }
                basePresenter.openLoginRegister(userAccount, getNewPwd(), token);
                break;
            case R.id.iv_mine_new_pwd_clear:
                etMineSetNewpwd.setText("");
                break;
        }
    }

    /**
     * 获取设置的密码
     */
    public String getNewPwd() {
        return etMineSetNewpwd.getText().toString().trim();
    }

    /**
     * 注册结果
     *
     * @param code
     */
    @Override
    public void registerResult(int code) {
        AppLogger.d("open_bind:" + code);
        //just for test
//        code = JError.ErrorOK;
        if (code == JError.ErrorOK) {
            IMEUtils.hide(getActivity());
            if (TextUtils.isEmpty(token)) {
                //增加邮箱验证界面
                jump2MailConnectFragment();
            } else {
                //绑定手机
                ToastUtil.showPositiveToast(getString(R.string.Added_successfully));
                jump2MineInfoFragment();
            }
        } else if (code == JError.ErrorSetPassTimeout) {
            ToastUtil.showToast(getString(R.string.SUBMIT_FAIL));
        } else {
            ToastUtil.showToast(getString(R.string.Tips_Device_TimeoutRetry));
        }
    }

    @Override
    public void jump2MailConnectFragment() {
        Bundle bundle = new Bundle();
        bundle.putString("useraccount", userAccount);
        MineReSetMailTip fragment = MineReSetMailTip.newInstance(bundle);
        getActivity().getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(android.R.id.content, fragment, "bindStack")
                .addToBackStack("bindStack")
                .commit();
    }

    public void backDialog() {
        AlertDialogManager.getInstance().showDialog(getActivity(),
                getString(R.string.Tap3_logout_tips), getString(R.string.Tap3_logout_tips),
                getString(R.string.Button_Yes), (DialogInterface dialog, int which) -> {
                    jump2MineInfoFragment();
                    IMEUtils.hide(getActivity());
                }, getString(R.string.Button_No), null, false);
    }

    public void jump2MineInfoFragment() {
//        if (getActivity() != null) {
//            int count = getActivity().getSupportFragmentManager().getBackStackEntryCount();
//            for (int i = 0; i < count; i++) {
//                getActivity().getSupportFragmentManager().popBackStack();
//            }
//        }
        getActivity().getSupportFragmentManager().popBackStack("bindStack", FragmentManager.POP_BACK_STACK_INCLUSIVE);
//        HomeMineInfoFragment personalInfoFragment = (HomeMineInfoFragment) getActivity().getSupportFragmentManager().findFragmentByTag("personalInformationFragment");
//        MineInfoSetNewPwdFragment setNewPwdFragment = (MineInfoSetNewPwdFragment) getActivity().getSupportFragmentManager().findFragmentByTag("MineInfoSetNewPwdFragment");
//        MineInfoBindPhoneFragment bindPhoneFragment = (MineInfoBindPhoneFragment) getActivity().getSupportFragmentManager().findFragmentByTag("bindPhoneFragment");
//        BindMailFragment mailBoxFragment = (BindMailFragment) getActivity().getSupportFragmentManager().findFragmentByTag("mailBoxFragment");
//
//        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
//        if (personalInfoFragment != null) {
//            AppLogger.d("infoFrag不为空");
//            if (setNewPwdFragment != null) {
//                ft.remove(setNewPwdFragment);
//            }
//            if (mailBoxFragment != null) {
//                ft.remove(mailBoxFragment);
//                mailBoxFragment.getActivity().getSupportFragmentManager().popBackStack();
//            }
//            if (bindPhoneFragment != null) {
//                ft.remove(bindPhoneFragment);
//                bindPhoneFragment.getActivity().getSupportFragmentManager().popBackStack();
//            }
//            ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
//                    , R.anim.slide_in_left, R.anim.slide_out_right)
//                    .show(personalInfoFragment)
//                    .commit();
//        } else {
//            AppLogger.d("infoFrag为空");
//            HomeMineInfoFragment fragment = HomeMineInfoFragment.newInstance();
//            ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
//                    , R.anim.slide_in_left, R.anim.slide_out_right)
//                    .add(android.R.id.content, fragment, "mineReSetMailTip")
//                    .addToBackStack("personalInformationFragment")
//                    .commit();
//        }
//        if (setNewPwdFragment != null) {
//            ft.remove(setNewPwdFragment);
//        }
    }
}
