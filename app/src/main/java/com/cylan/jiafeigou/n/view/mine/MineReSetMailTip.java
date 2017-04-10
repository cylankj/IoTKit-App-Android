package com.cylan.jiafeigou.n.view.mine;

import android.content.Context;
import android.graphics.Typeface;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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
//            getFragmentManager().popBackStack();
            getActivity().getSupportFragmentManager().popBackStackImmediate(HomeMineInfoMailBoxFragment.class.getName(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        });
    }

    @OnClick(R.id.tv_mail_connect_submit)
    public void onClick() {
//        getFragmentManager().popBackStack();
        int num = getActivity().getSupportFragmentManager().getBackStackEntryCount();
        for (int i = 0; i < num; i++) {
            FragmentManager.BackStackEntry backstatck = getActivity().getSupportFragmentManager().getBackStackEntryAt(i);
            AppLogger.d("Fragment:"+backstatck.getName());
        }
        getActivity().getSupportFragmentManager().popBackStackImmediate(HomeMineInfoMailBoxFragment.class.getName(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }
}
