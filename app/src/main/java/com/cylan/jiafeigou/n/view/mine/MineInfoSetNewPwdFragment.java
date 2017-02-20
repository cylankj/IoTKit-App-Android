package com.cylan.jiafeigou.n.view.mine;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineInfoSetNewPwdContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.MineInfoSetNewPwdPresenterImp;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;

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
public class MineInfoSetNewPwdFragment extends IBaseFragment implements MineInfoSetNewPwdContract.View {

    @BindView(R.id.iv_mine_set_newpwd_back)
    ImageView ivMineSetNewpwdBack;
    @BindView(R.id.tv_top_title)
    TextView tvTopTitle;
    @BindView(R.id.iv_mine_info_set_newpwd_able)
    ImageView ivMineInfoSetNewpwdAble;
    @BindView(R.id.et_mine_set_newpwd)
    EditText etMineSetNewpwd;
    @BindView(R.id.iv_mine_new_pwd_clear)
    ImageView ivMineNewPwdClear;
    @BindView(R.id.cb_new_pwd_show)
    CheckBox cbNewPwdShow;
    @BindView(R.id.rl_tab_bar_container)
    FrameLayout rlTabBarContainer;
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
        ViewUtils.setViewPaddingStatusBar(rlTabBarContainer);
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
        ivMineInfoSetNewpwdAble.setImageDrawable(isEmpty ? getResources().getDrawable(R.drawable.icon_finish_disable) : getResources().getDrawable(R.drawable.me_icon_finish_normal));
        ivMineInfoSetNewpwdAble.setEnabled(isEmpty ? false : true);
    }

    @OnCheckedChanged(R.id.cb_new_pwd_show)
    public void onPwdCheckChange(CompoundButton buttonView, boolean isChecked) {
        ViewUtils.showPwd(etMineSetNewpwd, isChecked);
        etMineSetNewpwd.setSelection(etMineSetNewpwd.length());
    }

    @OnClick({R.id.iv_mine_set_newpwd_back, R.id.iv_mine_info_set_newpwd_able, R.id.iv_mine_new_pwd_clear})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_mine_set_newpwd_back:
                getFragmentManager().popBackStack();
                break;
            case R.id.iv_mine_info_set_newpwd_able:
                if (getNewPwd().length() < 6) {
                    ToastUtil.showToast(getString(R.string.PASSWORD_LESSTHAN_SIX));
                    return;
                }
                // TODO 调用SDK 注册?????
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
        if (code == JError.ErrorOK) {
            //增加邮箱验证界面
            jump2MailConnectFragment();
        } else {
            ToastUtil.showToast(getString(R.string.Tips_Device_TimeoutRetry));
        }
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
}
