package com.cylan.jiafeigou.n.view.login;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.mvp.contract.login.SetupPwdContract;
import com.cylan.jiafeigou.n.mvp.model.RequestResetPwdBean;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.IMEUtils;
import com.cylan.jiafeigou.utils.ViewUtils;

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
public class SetupPwdFragment extends android.support.v4.app.Fragment implements SetupPwdContract.View {

    @BindView(R.id.tv_register_pwd_submit)
    TextView tvRegisterPwdSubmit;
    @BindView(R.id.iv_input_box_clear)
    ImageView ivInputBoxClear;
    @BindView(R.id.cb_show_input_box)
    CheckBox cbShowInputBox;
    @BindView(R.id.et_input_box)
    EditText etInputBox;
    @BindView(R.id.iv_top_bar_left)
    ImageView ivLoginTopLeft;
    private SetupPwdContract.Presenter pwdPresenter;

    public SetupPwdFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
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
    public android.view.View onCreateView(LayoutInflater inflater, ViewGroup container,
                                          Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        android.view.View view = inflater.inflate(R.layout.fragment_setup_pwd, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(android.view.View view, @Nullable Bundle savedInstanceState) {
        setupInputBox();
        initTitleBar();
        setupButton();
    }

    private void setupButton() {
        tvRegisterPwdSubmit.setText(getString(R.string.item_confirm));
    }

    private void initTitleBar() {
        FrameLayout layout = (FrameLayout) getView().findViewById(R.id.rLayout_login_top);
        layout.findViewById(R.id.tv_top_bar_right).setVisibility(android.view.View.GONE);
        TextView tvTitle = (TextView) layout.findViewById(R.id.tv_top_bar_center);
        tvTitle.setText("密码");
        ivLoginTopLeft.setImageResource(R.drawable.btn_nav_back);
        ivLoginTopLeft.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                ActivityUtils.justPop(getActivity());
            }
        });
    }

    private void setupInputBox() {
        EditText editText = (EditText) getView().findViewById(R.id.et_input_box);
        editText.setHint(getString(R.string.input_new_pwd));
        editText.setInputType(EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
    }

    @OnTextChanged(R.id.et_input_box)
    public void onInputBoxTextChanged(CharSequence s, int start, int before, int count) {
        final boolean validPws = !TextUtils.isEmpty(s)
                && s.length() >= JConstant.PWD_LEN_MIN
                && s.length() <= JConstant.PWD_LEN_MAX;
        ivInputBoxClear.setVisibility(validPws ? android.view.View.VISIBLE : android.view.View.GONE);
        tvRegisterPwdSubmit.setEnabled(validPws);
    }

    @OnCheckedChanged(R.id.cb_show_input_box)
    public void onShowPwd(CompoundButton buttonView, boolean isChecked) {
        ViewUtils.showPwd(etInputBox, isChecked);
        etInputBox.setSelection(etInputBox.length());
    }

    @OnClick({R.id.iv_input_box_clear, R.id.cb_show_input_box,
            R.id.tv_register_pwd_submit})
    public void onClick(android.view.View view) {
        switch (view.getId()) {
            case R.id.iv_input_box_clear:
                etInputBox.setText("");
                break;
            case R.id.cb_show_input_box:
                break;
            case R.id.tv_register_pwd_submit:
                Bundle bundle = getArguments();
                if (bundle == null) {
                    AppLogger.e("bundle is null");
                    return;
                }
                final String account = bundle.getString(JConstant.KEY_ACCOUNT_TO_SEND);
                final String pwd = bundle.getString(JConstant.KEY_PWD_TO_SEND);
                final String code = bundle.getString(JConstant.KEY_VCODE_TO_SEND);
                if (pwdPresenter != null)
                    pwdPresenter.submitAccountInfo(account, pwd, code);
                IMEUtils.hide(getActivity());
                Toast.makeText(getActivity(), "注册中。。。", Toast.LENGTH_SHORT).show();
                break;
        }
    }


    @Override
    public void setPresenter(SetupPwdContract.Presenter presenter) {
        this.pwdPresenter = presenter;
    }

    @Override
    public void submitResult(RequestResetPwdBean bean) {

    }
}
