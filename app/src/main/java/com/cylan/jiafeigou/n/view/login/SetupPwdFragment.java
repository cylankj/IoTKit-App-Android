package com.cylan.jiafeigou.n.view.login;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.mvp.contract.login.SetupPwdContract;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.IMEUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.CustomToolbar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnTextChanged;

;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SetupPwdFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SetupPwdFragment extends Fragment implements SetupPwdContract.View {

    @BindView(R.id.tv_register_pwd_submit)
    TextView tvRegisterPwdSubmit;
    @BindView(R.id.iv_input_box_clear)
    ImageView ivInputBoxClear;
    @BindView(R.id.cb_show_input_box)
    CheckBox cbShowInputBox;
    @BindView(R.id.et_input_box)
    EditText etInputBox;
    protected SetupPwdContract.Presenter pwdPresenter;
    @BindView(R.id.vs_set_account_pwd)
    ViewSwitcher vsSetAccountPwd;
    @BindView(R.id.fl_input_container)
    FrameLayout flInputContainer;
    @BindView(R.id.custom_toolbar)
    CustomToolbar customToolbar;

    public SetupPwdFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create activity_cloud_live_mesg_call_out_item new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SetupPwdFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SetupPwdFragment newInstance(Bundle args) {
        SetupPwdFragment fragment = new SetupPwdFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_setup_pwd, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        setupInputBox();
        initTitleBar();
        setupButton();
    }

    private void setupButton() {
        tvRegisterPwdSubmit.setText(getString(R.string.Button_Sure));
    }

    private void initTitleBar() {
        ViewUtils.setViewMarginStatusBar(customToolbar);
        customToolbar.setToolbarTitle(R.string.SET_PWD);
        customToolbar.setTvToolbarIcon(R.drawable.nav_icon_back_gary);
//        initNavigateBack();
    }

    protected void initNavigateBack() {
        customToolbar.setBackAction(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtils.justPop(getActivity());
            }
        });
    }

    private void setupInputBox() {
        EditText editText = (EditText) getView().findViewById(R.id.et_input_box);
        CheckBox checkBox = (CheckBox) getView().findViewById(R.id.cb_show_input_box);
        editText.setHint(getString(R.string.PASSWORD));
        editText.setInputType(EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
        ViewUtils.setChineseExclude(editText, JConstant.PWD_LEN_MAX);
        ViewUtils.showPwd(etInputBox, checkBox.isChecked());
    }

    @OnTextChanged(R.id.et_input_box)
    public void onInputBoxTextChanged(CharSequence s, int start, int before, int count) {
        final boolean validPws = !TextUtils.isEmpty(s);
        ivInputBoxClear.setVisibility(validPws ? View.VISIBLE : View.GONE);
        tvRegisterPwdSubmit.setEnabled(validPws);
    }

    @OnCheckedChanged(R.id.cb_show_input_box)
    public void onShowPwd(CompoundButton buttonView, boolean isChecked) {
        ViewUtils.showPwd(etInputBox, isChecked);
        etInputBox.setSelection(etInputBox.length());
    }

    @OnClick({R.id.iv_input_box_clear, R.id.cb_show_input_box,
            R.id.tv_register_pwd_submit})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_input_box_clear:
                etInputBox.setText("");
                break;
            case R.id.cb_show_input_box:
                break;
            case R.id.tv_register_pwd_submit:
                if (etInputBox.getText().toString().trim().length() < 6) {
                    ToastUtil.showToast(getString(R.string.PASSWORD_LESSTHAN_SIX));
                    return;
                }
                Bundle bundle = getArguments();
                if (bundle == null) {
                    AppLogger.e("bundle is null");
                    return;
                }
                final String account = bundle.getString(JConstant.KEY_ACCOUNT_TO_SEND);
                final String pwd = etInputBox.getText().toString().trim();
                final String code = bundle.getString(JConstant.KEY_VCODE_TO_SEND);
                IMEUtils.hide(getActivity());
                if (pwdPresenter == null) {
                    AppLogger.i("pwdPresenter is null ");
                    return;
                }
                doAction(account, pwd, code);
                break;
        }
    }

    public void doAction(String account, String pwd, String code) {

    }

    @Override
    public void setPresenter(SetupPwdContract.Presenter presenter) {
        this.pwdPresenter = presenter;
    }


    @Override
    public void submitResult(RxEvent.ResultRegister register) {

    }

    @Override
    public void loginResult(int code) {

    }

    @Override
    public boolean isLoginViewVisible() {
        return false;
    }


}
