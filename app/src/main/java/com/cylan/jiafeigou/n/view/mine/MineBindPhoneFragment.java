package com.cylan.jiafeigou.n.view.mine;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.RxEvent;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineBindPhoneContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.MineBindPhonePresenterImp;
import com.cylan.jiafeigou.utils.ToastUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 作者：zsl
 * 创建时间：2016/9/1
 * 描述：
 */
public class MineBindPhoneFragment extends Fragment implements MineBindPhoneContract.View {


    @BindView(R.id.et_input_userphone)
    EditText etInputUserphone;
    @BindView(R.id.tv_get_checkNumber)
    TextView tvGetCheckNumber;
    @BindView(R.id.tv_top_bar_center)
    TextView tvTopBarCenter;
    @BindView(R.id.iv_top_bar_left)
    ImageView ivTopBarLeft;

    private JFGAccount userinfo;

    private boolean isBindOrChange;
    private MineBindPhoneContract.Presenter presenter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home_mine_bind_phone, container, false);
        ButterKnife.bind(this, view);
        getArgumentData();
        initPresenter();
        initEditListener();
        return view;
    }

    /**
     * 输入框监听
     */
    private void initEditListener() {
        etInputUserphone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0){
                    tvGetCheckNumber.setEnabled(false);
                }else {
                    tvGetCheckNumber.setEnabled(true);
                }
            }
        });
    }

    private void initPresenter() {
        presenter = new MineBindPhonePresenterImp(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (presenter != null)presenter.isBindOrChange(userinfo);
    }


    /**
     * 获取传递过来的参数
     */
    private void getArgumentData() {
        Bundle arguments = getArguments();
        userinfo = (JFGAccount) arguments.getSerializable("userinfo");
    }

    public static MineBindPhoneFragment newInstance(Bundle bundle) {
        MineBindPhoneFragment fragment = new MineBindPhoneFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void setPresenter(MineBindPhoneContract.Presenter presenter) {

    }

    @OnClick({R.id.tv_get_checkNumber, R.id.iv_top_bar_left})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_get_checkNumber:
                if ( JConstant.PHONE_REG.matcher(getInputPhone()).find()){
                    presenter.checkPhoneIsBind(getInputPhone());
                }else {
                    ToastUtil.showToast("请求输入有效手机号");
                }
                break;

            case R.id.iv_top_bar_left:
                getFragmentManager().popBackStack();
                break;
        }
    }

    @Override
    public void initToolbarTitle(String title) {
        tvTopBarCenter.setText(title);
    }

    @Override
    public String getInputPhone() {
        return etInputUserphone.getText().toString().trim();
    }

    /**
     * 检测账号是否已经注册的结果
     * @param checkAccountCallback
     */
    @Override
    public void handlerCheckPhoneResult(RxEvent.CheckAccountCallback checkAccountCallback) {
        if (getInputPhone().equals(checkAccountCallback.s)){
            AlertDialog.Builder hasBineDialog = new AlertDialog.Builder(getContext())
                    .setTitle("该手机号已经被绑定")
                    .setPositiveButton("我知道了", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            hasBineDialog.show();
        }else {
            // TODO 显示倒计时

            //发送验证码
            presenter.getCheckCode(getInputPhone());
        }
    }

}
