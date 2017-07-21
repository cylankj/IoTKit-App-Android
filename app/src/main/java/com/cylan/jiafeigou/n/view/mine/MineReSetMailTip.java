package com.cylan.jiafeigou.n.view.mine;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.view.activity.MineInfoActivity;
import com.cylan.jiafeigou.widget.CustomToolbar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 作者：zsl
 * 创建时间：2017/2/20
 * 描述：
 */
public class MineReSetMailTip extends Fragment implements MineInfoActivity.BackInterface {

    public static final String KEY_MAIL = "useraccount";
    @BindView(R.id.custom_toolbar)
    CustomToolbar customToolbar;
    @BindView(R.id.tv_mail_address)
    TextView tvMailAddress;
    @BindView(R.id.tv_mail_connect_submit)
    TextView tvMailConnectSubmit;

    private String mailToVerify;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mailToVerify = getArguments().getString(KEY_MAIL);
        if (getActivity() instanceof MineInfoActivity) {
            ((MineInfoActivity) getActivity()).setBackInterface(this);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (getActivity() instanceof MineInfoActivity) {
            ((MineInfoActivity) getActivity()).setBackInterface(this);
        }
    }

    public static MineReSetMailTip newInstance(Bundle bundle) {
        MineReSetMailTip fragment = new MineReSetMailTip();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine_resetmail_tip, null);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvMailConnectSubmit.setEnabled(true);
        tvMailAddress.setText(mailToVerify);
        tvMailAddress.setTypeface(Typeface.DEFAULT_BOLD);
        customToolbar.setBackAction((View v) -> {
            jump2MineInfoFragment();
        });
    }


    @OnClick(R.id.tv_mail_connect_submit)
    public void onClick() {
        jump2MineInfoFragment();
    }

    public void jump2MineInfoFragment() {
        getActivity().getSupportFragmentManager().popBackStack("bindStack", FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    @Override
    public boolean onBack() {
        if (isDetached() || getActivity() == null) {
            return false;
        }
        jump2MineInfoFragment();
        return true;
    }
}
