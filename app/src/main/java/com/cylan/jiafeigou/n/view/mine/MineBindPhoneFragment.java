package com.cylan.jiafeigou.n.view.mine;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.mine.MinePersonalInformationBindPhoneContract;
import com.cylan.jiafeigou.utils.ToastUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 作者：zsl
 * 创建时间：2016/9/1
 * 描述：
 */
public class MineBindPhoneFragment extends Fragment implements MinePersonalInformationBindPhoneContract.View {


    @BindView(R.id.et_input_userphone)
    EditText etInputUserphone;
    @BindView(R.id.tv_get_checkNumber)
    TextView tvGetCheckNumber;
    @BindView(R.id.tv_top_bar_center)
    TextView tvTopBarCenter;
    @BindView(R.id.iv_top_bar_left)
    ImageView ivTopBarLeft;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home_mine_bind_phone, container, false);

        ButterKnife.bind(this, view);

        initToolbarTitle();

        return view;
    }

    public static MineBindPhoneFragment newInstance(Bundle bundle) {
        MineBindPhoneFragment fragment = new MineBindPhoneFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void setPresenter(MinePersonalInformationBindPhoneContract.Presenter presenter) {

    }

    @OnClick({R.id.tv_get_checkNumber, R.id.iv_top_bar_left})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_get_checkNumber:
                ToastUtil.showToast("获取验证码中。。。");
                break;

            case R.id.iv_top_bar_left:
                getFragmentManager().popBackStack();
                break;
        }
    }

    @Override
    public void initToolbarTitle() {
        tvTopBarCenter.setText("修改手机号");
    }

}
