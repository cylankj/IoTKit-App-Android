package com.cylan.jiafeigou.n.view.mine;

import android.content.Context;
import android.graphics.Typeface;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.widget.CustomToolbar;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 作者：zsl
 * 创建时间：2017/2/20
 * 描述：
 */
public class MineReSetMailTip extends Fragment {

    @BindView(R.id.custom_toolbar)
    CustomToolbar customToolbar;
    @BindView(R.id.tv_mail_address)
    TextView tvMailAddress;
    @BindView(R.id.tv_mail_connect_submit)
    TextView tvMailConnectSubmit;

    private String useraccount;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        useraccount = getArguments().getString("useraccount");
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
        tvMailAddress.setText(useraccount);
        tvMailAddress.setTypeface(Typeface.DEFAULT_BOLD);
        initTopBar();
    }

    private void initTopBar() {
        customToolbar.setBackAction((View v) -> {
            jump2MineInfoFragment();
        });
    }

    @OnClick(R.id.tv_mail_connect_submit)
    public void onClick() {
        jump2MineInfoFragment();
    }

    public void jump2MineInfoFragment(){
        HomeMineInfoFragment personalInfoFragment = (HomeMineInfoFragment) getFragmentManager().findFragmentByTag("personalInformationFragment");
        MineReSetMailTip mailTip = (MineReSetMailTip) getFragmentManager().findFragmentByTag("MineReSetMailTip");
        MineInfoSetNewPwdFragment setNewPwdFragment = (MineInfoSetNewPwdFragment) getFragmentManager().findFragmentByTag("MineInfoSetNewPwdFragment");
        HomeMineInfoMailBoxFragment mailBoxFragment = (HomeMineInfoMailBoxFragment) getFragmentManager().findFragmentByTag("mailBoxFragment");

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        if (personalInfoFragment != null){
            AppLogger.d("infoFrag不为空");
                    if (setNewPwdFragment != null){ft.remove(setNewPwdFragment);}
                    if (mailBoxFragment != null){ft.remove(mailBoxFragment);}
                    ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                            , R.anim.slide_in_left, R.anim.slide_out_right)
                    .show(personalInfoFragment)
                    .commit();
        }else {
            AppLogger.d("infoFrag为空");
            HomeMineInfoFragment fragment = HomeMineInfoFragment.newInstance();
                    ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                            , R.anim.slide_in_left, R.anim.slide_out_right)
                    .add(android.R.id.content, fragment, "mineReSetMailTip")
                    .addToBackStack("personalInformationFragment")
                    .commit();
        }
        if (mailTip != null){ft.remove(mailTip);}
    }
}
