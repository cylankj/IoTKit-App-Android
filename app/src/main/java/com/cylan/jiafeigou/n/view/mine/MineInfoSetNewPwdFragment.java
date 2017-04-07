package com.cylan.jiafeigou.n.view.mine;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineInfoSetNewPwdContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.MineInfoSetNewPwdPresenterImp;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.CustomToolbar;
import com.cylan.jiafeigou.widget.dialog.BaseDialog;
import com.cylan.jiafeigou.widget.dialog.SimpleDialogFragment;

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
public class MineInfoSetNewPwdFragment extends IBaseFragment implements MineInfoSetNewPwdContract.View, BaseDialog.BaseDialogAction {

    @BindView(R.id.et_mine_set_newpwd)
    EditText etMineSetNewpwd;
    @BindView(R.id.iv_mine_new_pwd_clear)
    ImageView ivMineNewPwdClear;
    @BindView(R.id.cb_new_pwd_show)
    CheckBox cbNewPwdShow;
    @BindView(R.id.custom_toolbar)
    CustomToolbar customToolbar;
    private MineInfoSetNewPwdContract.Presenter presenter;
    private String useraccount;
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
        useraccount = arguments.getString("useraccount");
        token = arguments.getString("token");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine_info_set_pwd, container, false);
        ButterKnife.bind(this, view);
        initPresenter();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewUtils.setChineseExclude(etMineSetNewpwd, 65);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (presenter != null)presenter.start();
    }

    @Override
    public void onStop() {
        super.onStop();
        presenter.stop();
    }

    private void initPresenter() {
        presenter = new MineInfoSetNewPwdPresenterImp(this);
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
                presenter.openLoginRegister(useraccount, getNewPwd(), token);
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
     * @param code
     */
    @Override
    public void registerResult(int code) {
        AppLogger.d("open_bind:"+code);
        if (code == JError.ErrorOK) {
            if (TextUtils.isEmpty(token)){
                //增加邮箱验证界面
                jump2MailConnectFragment();
            }else {
                //绑定手机
                ToastUtil.showPositiveToast(getString(R.string.Added_successfully));
                getFragmentManager().popBackStack();
            }
        } else if (code == JError.ErrorSetPassTimeout){
            ToastUtil.showToast(getString(R.string.SUBMIT_FAIL));
        }else {
            ToastUtil.showToast(getString(R.string.Tips_Device_TimeoutRetry));
        }
    }

    /**
     * 跳转到个人信息
     */
    private void jump2MineInfoFragment() {

    }

    @Override
    public void jump2MailConnectFragment() {
        Bundle bundle = new Bundle();
        bundle.putString("useraccount", useraccount);
        MineReSetMailTip fragment = MineReSetMailTip.newInstance(bundle);
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(android.R.id.content, fragment, "mineInfoSetNewPwdFragment")
                .addToBackStack("personalInformationFragment")
                .commit();

    }

    public void backDialog(){
        Fragment f = getActivity().getSupportFragmentManager().findFragmentByTag("backPress");
        if (f == null) {
            Bundle bundle = new Bundle();
            bundle.putString(BaseDialog.KEY_TITLE, getString(R.string.Tap3_logout_tips));
            bundle.putString(SimpleDialogFragment.KEY_LEFT_CONTENT, getString(R.string.Button_No));
            bundle.putString(SimpleDialogFragment.KEY_RIGHT_CONTENT, getString(R.string.Button_Yes));
            bundle.putBoolean(SimpleDialogFragment.KEY_TOUCH_OUT_SIDE_DISMISS, false);
            SimpleDialogFragment dialogFragment = SimpleDialogFragment.newInstance(bundle);
            dialogFragment.setAction(this);
            dialogFragment.show(getActivity().getSupportFragmentManager(), "backPress");
        }
    }

    @Override
    public void onDialogAction(int id, Object value) {
        if (id == R.id.tv_dialog_btn_right){
            getFragmentManager().popBackStackImmediate(HomeMineInfoFragment.class.getName(),FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }
}
