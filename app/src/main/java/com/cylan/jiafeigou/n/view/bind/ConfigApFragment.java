package com.cylan.jiafeigou.n.view.bind;


import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.ScanResult;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.SmartcallActivity;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.bind.UdpConstant;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.bind.ConfigApContract;
import com.cylan.jiafeigou.n.mvp.impl.bind.ConfigApPresenterImpl;
import com.cylan.jiafeigou.n.mvp.model.BeanWifiList;
import com.cylan.jiafeigou.n.view.activity.BindDeviceActivity;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.BindUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.CustomToolbar;
import com.cylan.jiafeigou.widget.LoginButton;

import java.lang.ref.WeakReference;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ConfigApFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ConfigApFragment extends IBaseFragment<ConfigApContract.Presenter>
        implements ConfigApContract.View, WiFiListDialogFragment.ClickCallBack {

    @BindView(R.id.iv_wifi_clear_pwd)
    ImageView ivWifiClearPwd;
    @BindView(R.id.cb_wifi_pwd)
    CheckBox cbWifiPwd;
    @BindView(R.id.tv_wifi_pwd_submit)
    LoginButton tvWifiPwdSubmit;
    @BindView(R.id.et_wifi_pwd)
    EditText etWifiPwd;
    @BindView(R.id.tv_config_ap_name)
    TextView tvConfigApName;

    WiFiListDialogFragment fiListDialogFragment;
    @BindView(R.id.rLayout_wifi_pwd_input_box)
    RelativeLayout rLayoutWifiPwdInputBox;
    @BindView(R.id.vs_show_content)
    ViewSwitcher vsShowContent;
    @BindView(R.id.custom_toolbar)
    CustomToolbar customToolbar;
    private AlertDialog reBingDialog;

    private List<ScanResult> cacheList;

    public ConfigApFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create activity_cloud_live_mesg_call_out_item new instance of
     * this fragment using the provided parameters.
     *
     * @param bundle Parameter 2.
     * @return A new instance of fragment ConfigApFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ConfigApFragment newInstance(Bundle bundle) {
        ConfigApFragment fragment = new ConfigApFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initFragment();
        this.basePresenter = new ConfigApPresenterImpl(this);
        basePresenter.clearConnection();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_config_ap, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (cacheList != null && cacheList.size() > 0) {
            tvConfigApName.setText(cacheList.get(0).SSID);
            tvConfigApName.setTag(new BeanWifiList(cacheList.get(0)));
        }
        //默认显示
        ViewUtils.showPwd(etWifiPwd, true);
        customToolbar.setBackAction(v -> getActivity().onBackPressed());
        cbWifiPwd.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            ViewUtils.showPwd(etWifiPwd, isChecked);
            etWifiPwd.setSelection(etWifiPwd.length());
        });
    }

    /**
     * 明文/密文 密码
     */


    @Override
    public void onResume() {
        super.onResume();
        JConstant.ConfigApStep = 2;
        if (basePresenter != null) {
            basePresenter.refreshWifiList();
            basePresenter.check3GDogCase();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        JConstant.ConfigApStep = 3;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (fragmentWeakReference != null
                && fragmentWeakReference.get() != null
                && fragmentWeakReference.get().isResumed()) {
            fragmentWeakReference.get().dismiss();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        dismissDialog();
    }


    private void createDialog() {
        if (reBingDialog == null) {
            reBingDialog = new AlertDialog.Builder(getActivity())
                    .setMessage(getString(R.string.Tap1_AddDevice_disconnected))
                    .setNegativeButton(getString(R.string.CANCEL), (DialogInterface dialog, int which) -> {
                        if (getActivity() != null && getActivity() instanceof BindDeviceActivity) {
                            ((BindDeviceActivity) getActivity()).finishExt();
                        }
                    })
                    .setPositiveButton(getString(R.string.OK), (DialogInterface dialog, int which) -> {
                        ((BindDeviceActivity) getActivity()).finishExt();
                        Intent intent = new Intent(getActivity(), BindDeviceActivity.class);
                        intent.putExtra(JConstant.KEY_AUTO_SHOW_BIND, JConstant.KEY_AUTO_SHOW_BIND);
                        startActivity(intent);
                    })
                    .create();
        }
        if (reBingDialog.isShowing()) return;
        reBingDialog.show();
    }

    private void dismissDialog() {
        if (reBingDialog != null && reBingDialog.isShowing()) reBingDialog.dismiss();
    }

    @OnTextChanged(R.id.et_wifi_pwd)
    public void onPwdUpdate(CharSequence s, int start, int before, int count) {
        boolean flag = TextUtils.isEmpty(s);
        ivWifiClearPwd.setVisibility(flag ? View.INVISIBLE : View.VISIBLE);
        tvWifiPwdSubmit.setEnabled(s.length() > 6);
    }


    @OnClick({R.id.iv_wifi_clear_pwd, R.id.tv_wifi_pwd_submit, R.id.tv_config_ap_name})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_wifi_clear_pwd:
                etWifiPwd.setText("");
                break;
            case R.id.tv_wifi_pwd_submit:
                ViewUtils.deBounceClick(view);
                int currentNet = NetUtils.getJfgNetType(getActivity());
                if (currentNet != ConnectivityManager.TYPE_WIFI) {
                    createDialog();
                    return;
                }
                String ssid = tvConfigApName.getText().toString();
                String pwd = ViewUtils.getTextViewContent(etWifiPwd);
                int type = 0;
                if (TextUtils.isEmpty(ssid)) {
//                    ToastUtil.showNegativeToast("没有文案:请选择wifi");
                    return;
                }
                Object o = tvConfigApName.getTag();
                if (o != null && o instanceof BeanWifiList) {
                    type = BindUtils.getSecurity(((BeanWifiList) o).result);
                }
                if (type != 0 && pwd.length() < 8) {
                    ToastUtil.showNegativeToast(getString(R.string.ENTER_PWD_1));
                    return;
                }
                if (basePresenter != null)
                    basePresenter.sendWifiInfo(ViewUtils.getTextViewContent(tvConfigApName),
                            ViewUtils.getTextViewContent(etWifiPwd), type);
                if (getArguments().getBoolean("ReSetBell", false)) {
                    ToastUtil.showPositiveToast(getString(R.string.DOOR_SET_WIFI_MSG));
                    Intent intent = new Intent(getActivity(), SmartcallActivity.class);
                    startActivity(intent);
                    return;
                } else {
                    tvWifiPwdSubmit.viewZoomSmall();
                }
                break;
            case R.id.tv_config_ap_name:
                if (getView() != null)
                    ViewUtils.deBounceClick(getView().findViewById(R.id.tv_config_ap_name));
                initFragment();
                fiListDialogFragment = fragmentWeakReference.get();
                fiListDialogFragment.setClickCallBack(this);
                fiListDialogFragment.updateList(cacheList, tvConfigApName.getTag());
                fiListDialogFragment.show(getActivity().getSupportFragmentManager(), "WiFiListDialogFragment");
                if (basePresenter != null) {
                    basePresenter.refreshWifiList();
                }
                break;
        }
    }

    private void initFragment() {
        if (fragmentWeakReference == null || fragmentWeakReference.get() == null)
            fragmentWeakReference = new WeakReference<>(WiFiListDialogFragment.newInstance(new Bundle()));
    }

    private WeakReference<WiFiListDialogFragment> fragmentWeakReference;

    @Override
    public void onNetStateChanged(int state) {
        if (state != ConnectivityManager.TYPE_WIFI)
            ToastUtil.showNegativeToast(getString(R.string.NoNetworkTips));
        else {
            dismissDialog();
        }
    }

    @Override
    public void onWiFiResult(List<ScanResult> resultList) {
        final int count = resultList == null ? 0 : resultList.size();
        if (count == 0) {
            if (Build.VERSION.SDK_INT >= 23) {
                ToastUtil.showNegativeToast(getString(R.string.GetWifiList_FaiTips));
            } else {
//                ToastUtil.showNegativeToast("请尝试手动开关wifi");
            }
            return;
        }
        cacheList = resultList;
        if (fiListDialogFragment != null)
            fiListDialogFragment.updateList(cacheList, tvConfigApName.getTag());
        Object object = tvConfigApName.getTag();
        if (object == null) {
            tvConfigApName.setTag(new BeanWifiList(resultList.get(0)));
            tvConfigApName.setText(resultList.get(0).SSID);
        }
    }

    @Override
    public void onSetWifiFinished(UdpConstant.UdpDevicePortrait o) {
        Bundle bundle = getArguments();
        bundle.putString(JConstant.KEY_DEVICE_ITEM_UUID, o.uuid);
        SubmitBindingInfoFragment fragment = SubmitBindingInfoFragment.newInstance(bundle);
        ActivityUtils.addFragmentSlideInFromRight(getActivity().getSupportFragmentManager(),
                fragment, android.R.id.content);
        if (basePresenter != null) {
            basePresenter.finish();
        }
    }

    @Override
    public void lossDogConnection() {
        if (ActivityUtils.isFragmentInTop(getActivity(), R.id.fLayout_submit_bind_info))
            return;
    }


    @Override
    public void upgradeDogState(int state) {
        if (vsShowContent.getCurrentView().getId() == R.id.fragment_config_ap_pre) {
            vsShowContent.showNext();
        }
    }

    @Override
    public void pingFailed() {
        ToastUtil.showNegativeToast(getString(R.string.ADD_FAILED));
        tvWifiPwdSubmit.viewZoomBig();
    }

    @Override
    public void setPresenter(ConfigApContract.Presenter basePresenter) {
        this.basePresenter = basePresenter;
    }

    @Override
    public void onDismiss(ScanResult scanResult) {
        if (isResumed()) {
            tvConfigApName.setTag(new BeanWifiList(scanResult));
            tvConfigApName.setText(scanResult.SSID);
            rLayoutWifiPwdInputBox.setVisibility(BindUtils.getSecurity(scanResult) != 0
                    ? View.VISIBLE : View.GONE);
        }
    }
}
