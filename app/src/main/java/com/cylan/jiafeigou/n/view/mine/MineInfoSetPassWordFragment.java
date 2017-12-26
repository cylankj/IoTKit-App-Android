package com.cylan.jiafeigou.n.view.mine;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.entity.jniCall.JFGResult;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.module.LoginHelper;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineInfoSetPassWordContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.MineInfoSetPassWordPresenterImp;
import com.cylan.jiafeigou.utils.MD5Util;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.CustomToolbar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

/**
 * 作者：zsl
 * 创建时间：2016/9/20
 * 描述：
 */
public class MineInfoSetPassWordFragment extends IBaseFragment<MineInfoSetPassWordContract.Presenter> implements MineInfoSetPassWordContract.View {

    @BindView(R.id.et_mine_personal_information_old_password)
    EditText etMinePersonalInformationOldPassword;
    @BindView(R.id.view_mine_personal_information_old_password_line)
    View viewMinePersonalInformationOldPasswordLine;
    @BindView(R.id.iv_mine_personal_information_old_password_clear)
    ImageView ivMinePersonalInformationOldPasswordClear;
    @BindView(R.id.et_mine_personal_information_new_password)
    EditText etMinePersonalInformationNewPassword;
    @BindView(R.id.view_mine_personal_information_new_password_line)
    View viewMinePersonalInformationNewPasswordLine;
    @BindView(R.id.iv_mine_personal_information_new_password_clear)
    ImageView ivMinePersonalInformationNewPasswordClear;
    @BindView(R.id.custom_toolbar)
    CustomToolbar customToolbar;


    public static MineInfoSetPassWordFragment newInstance(Bundle bundle) {
        MineInfoSetPassWordFragment fragment = new MineInfoSetPassWordFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = new MineInfoSetPassWordPresenterImp(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine_info_set_password, container, false);
        ButterKnife.bind(this, view);
        getArgumentData();
        return view;
    }

    /**
     * 获取传递过来的参数
     */
    private void getArgumentData() {
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        ViewUtils.setChineseExclude(etMinePersonalInformationOldPassword, getResources().getInteger(R.integer.max_password_length));
        ViewUtils.setChineseExclude(etMinePersonalInformationNewPassword, getResources().getInteger(R.integer.max_password_length));
        customToolbar.setTvToolbarRightEnable(false);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @OnTextChanged(R.id.et_mine_personal_information_old_password)
    public void onOldPwdUpdate(CharSequence s, int start, int before, int count) {
        boolean isEmpty = s.length() < 6;
        if (isEmpty || getNewPassword().trim().length() < 6) {
            customToolbar.setTvToolbarRightIcon(R.drawable.icon_finish_disable);
            customToolbar.setTvToolbarRightEnable(false);
        } else {
            customToolbar.setTvToolbarRightIcon(R.drawable.icon_finish_normal);
            customToolbar.setTvToolbarRightEnable(true);
        }
        ivMinePersonalInformationOldPasswordClear.setVisibility(TextUtils.isEmpty(s) ? View.GONE : View.VISIBLE);
        viewMinePersonalInformationOldPasswordLine.setBackgroundColor(TextUtils.isEmpty(s) ? getResources().getColor(R.color.color_f2f2f2) : getResources().getColor(R.color.color_36BDFF));
    }

    @OnTextChanged(R.id.et_mine_personal_information_new_password)
    public void onNewPwdUpdate(CharSequence s, int start, int before, int count) {
        boolean isEmpty = s.length() < 6;
        if (isEmpty || getOldPassword().trim().length() < 6) {
            customToolbar.setTvToolbarRightIcon(R.drawable.icon_finish_disable);
            customToolbar.setTvToolbarRightEnable(false);
        } else {
            customToolbar.setTvToolbarRightIcon(R.drawable.icon_finish_normal);
            customToolbar.setTvToolbarRightEnable(true);
        }
        ivMinePersonalInformationNewPasswordClear.setVisibility(TextUtils.isEmpty(s) ? View.GONE : View.VISIBLE);
        viewMinePersonalInformationNewPasswordLine.setBackgroundColor(TextUtils.isEmpty(s) ? getResources().getColor(R.color.color_f2f2f2) : getResources().getColor(R.color.color_36BDFF));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (TextUtils.isEmpty(getNewPassword()) || TextUtils.isEmpty(getOldPassword())) {
            customToolbar.setTvToolbarRightIcon(R.drawable.icon_finish_disable);
        } else {
            customToolbar.setTvToolbarRightIcon(R.drawable.icon_finish_normal);
        }
    }

    @OnClick({R.id.tv_toolbar_icon, R.id.iv_mine_personal_information_old_password_clear,
            R.id.iv_mine_personal_information_new_password_clear, R.id.tv_toolbar_right})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_toolbar_icon:
                getActivity().getSupportFragmentManager().popBackStack();
                break;
            case R.id.iv_mine_personal_information_old_password_clear:
                etMinePersonalInformationOldPassword.setText("");
                break;

            case R.id.iv_mine_personal_information_new_password_clear:
                etMinePersonalInformationNewPassword.setText("");
                break;

            case R.id.tv_toolbar_right:
                saveNewPassword();
                break;
        }
    }

    /**
     * desc：保存新密码
     */
    private void saveNewPassword() {
        if (presenter.checkNewPasswordLength(getNewPassword())) {
            ToastUtil.showToast(getString(R.string.PASSWORD_LESSTHAN_SIX));
            return;
        }
        JFGAccount jfgAccount = DataSourceManager.getInstance().getJFGAccount();
        if (jfgAccount != null) {
            presenter.sendChangePassReq(jfgAccount.getAccount(), getOldPassword(), getNewPassword());
        }

    }

    @Override
    public String getOldPassword() {
        return etMinePersonalInformationOldPassword.getText().toString();
    }

    @Override
    public String getNewPassword() {
        return etMinePersonalInformationNewPassword.getText().toString();
    }

    /**
     * 修改密码的结果
     *
     * @param jfgResult
     */
    @Override
    public void changePwdResult(JFGResult jfgResult) {
        if (jfgResult.code == JError.ErrorInvalidPass) {
            ToastUtil.showToast(getString(R.string.RET_ECHANGEPASS_OLDPASS_ERROR));
        } else if (jfgResult.code == JError.ErrorSamePass) {
            ToastUtil.showToast(getString(R.string.RET_ECHANGEPASS_SAME));
        } else if (jfgResult.code == JError.ErrorOK) {
            ToastUtil.showToast(getString(R.string.PWD_OK_1));
            LoginHelper.saveUser(DataSourceManager.getInstance().getAccount().getAccount(), MD5Util.lowerCaseMD5(getNewPassword()), 1);
            LoginHelper.performAutoLogin().subscribe(ret -> {
            }, error -> {
            });
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }
}
