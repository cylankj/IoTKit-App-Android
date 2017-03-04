package com.cylan.jiafeigou.n.view.bind;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.bind.SubmitBindingInfoContract;
import com.cylan.jiafeigou.n.mvp.impl.bind.SubmitBindingInfoContractImpl;
import com.cylan.jiafeigou.n.view.activity.BindDeviceActivity;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.BindUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.CustomToolbar;
import com.cylan.jiafeigou.widget.LoginButton;
import com.cylan.jiafeigou.widget.SimpleProgressBar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by cylan-hunt on 16-11-12.
 */

public class SubmitBindingInfoFragment extends IBaseFragment<SubmitBindingInfoContract.Presenter>
        implements SubmitBindingInfoContract.View {

    @BindView(R.id.progress_loading)
    SimpleProgressBar progressLoading;
    @BindView(R.id.tv_loading_percent)
    TextView tvLoadingPercent;
    @BindView(R.id.btn_bind_failed_repeat)
    LoginButton btnBindRepeat;
    @BindView(R.id.vs_layout_switch)
    ViewSwitcher vsLayoutSwitch;
    @BindView(R.id.custom_toolbar)
    CustomToolbar customToolbar;


    private AlertDialog needRebindDialog;
    private AlertDialog nullCidDialog;

    public static SubmitBindingInfoFragment newInstance(Bundle bundle) {
        SubmitBindingInfoFragment fragment = new SubmitBindingInfoFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.basePresenter = new SubmitBindingInfoContractImpl(this, getArguments().getString(JConstant.KEY_DEVICE_ITEM_UUID));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_submit_binding_info, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adjustViewSize();
        if (basePresenter != null)
            basePresenter.startCounting();
        customToolbar.setBackAction(v -> getActivity().onBackPressed());
    }

    private void adjustViewSize() {
        ViewGroup.LayoutParams l = progressLoading.getLayoutParams();
        int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        l.height = l.width = (int) (screenWidth * 0.6f);
        progressLoading.setLayoutParams(l);
    }

    private AlertDialog getDialog() {
        if (needRebindDialog == null)
            needRebindDialog = new AlertDialog.Builder(getActivity())
                    .setMessage(getString(R.string.BIND_NEED_REBIND))
                    .setPositiveButton(getString(R.string.OK), null)
                    .setNegativeButton(getString(R.string.CANCEL), (DialogInterface dialog, int which) -> {
                        if (getActivity() != null && getActivity() instanceof BindDeviceActivity) {
                            ((BindDeviceActivity) getActivity()).finishExt();
                        }
                    })
                    .create();
        needRebindDialog.setOnShowListener(dialog -> {
            Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view -> {
                int net = NetUtils.getJfgNetType(getActivity());
                if (net == 0) {
                    ToastUtil.showToast(getString(R.string.NoNetworkTips));
                    return;
                }
                basePresenter.startCounting();
                basePresenter.setBindState(BindUtils.BIND_PREPARED);
                basePresenter.start();
                ToastUtil.showToast("还没有强绑接口");
            });
        });
        return needRebindDialog;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (needRebindDialog != null && needRebindDialog.isShowing()) return;
        int result = basePresenter.getBindState();
        if (result == BindUtils.BIND_NEED_REBIND) {
            needRebindDialog = getDialog();
            needRebindDialog.show();
        }
    }

    @Override
    public void bindState(int state) {
        if (state == BindUtils.BIND_FAILED) {//失败
            //绑定失败
            vsLayoutSwitch.showNext();
            if (needRebindDialog != null && needRebindDialog.isShowing())
                needRebindDialog.dismiss();
        } else if (state == BindUtils.BIND_NEED_REBIND) {//强绑
            if (needRebindDialog != null && needRebindDialog.isShowing())
                needRebindDialog.dismiss();
            getDialog();
            needRebindDialog.show();
            basePresenter.endCounting();
        } else if (state == BindUtils.BIND_SUC) {//成功
            progressLoading.setVisibility(View.INVISIBLE);
            if (basePresenter != null)
                basePresenter.stop();
            SetDeviceAliasFragment fragment = SetDeviceAliasFragment.newInstance(getArguments());
            ActivityUtils.addFragmentSlideInFromRight(getActivity().getSupportFragmentManager(),
                    fragment, android.R.id.content);
            if (basePresenter != null)
                basePresenter.stop();
        } else if (state == BindUtils.BIND_NULL) {
            if (nullCidDialog != null && nullCidDialog.isShowing()) return;
            nullCidDialog = new AlertDialog.Builder(getActivity())
                    .setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (getActivity() != null && getActivity() instanceof BindDeviceActivity) {
                                ((BindDeviceActivity) getActivity()).finishExt();
                            }
                        }
                    })
                    .setNegativeButton(getString(R.string.CANCEL), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (getActivity() != null && getActivity() instanceof BindDeviceActivity) {
                                ((BindDeviceActivity) getActivity()).finishExt();
                            }
                        }
                    })
                    .create();
            nullCidDialog.show();
        }
    }

    @Override
    public void onCounting(int percent) {
        Log.d("SubmitBindingInfo", "SubmitBindingInfo: " + percent);
        tvLoadingPercent.setText(percent + "");
    }

    @Override
    public void setPresenter(SubmitBindingInfoContract.Presenter presenter) {
        this.basePresenter = presenter;
    }

    @OnClick(R.id.btn_bind_failed_repeat)
    public void onClick() {
        getActivity().finish();
        Intent intent = new Intent(getActivity(), BindDeviceActivity.class);
        intent.putExtra(JConstant.KEY_AUTO_SHOW_BIND, JConstant.KEY_AUTO_SHOW_BIND);
        startActivity(intent);
    }
}
