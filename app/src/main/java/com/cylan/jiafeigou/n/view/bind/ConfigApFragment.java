package com.cylan.jiafeigou.n.view.bind;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.utils.ViewUtils;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnTextChanged;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ConfigApFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ConfigApFragment extends Fragment {

    @BindView(R.id.iv_wifi_clear_pwd)
    ImageView ivWifiClearPwd;
    @BindView(R.id.cb_wifi_pwd)
    CheckBox cbWifiPwd;
    @BindView(R.id.tv_wifi_pwd_submit)
    TextView tvWifiPwdSubmit;
    @BindView(R.id.et_wifi_pwd)
    EditText etWifiPwd;
    @BindView(R.id.tv_config_ap_name)
    TextView tvConfigApName;

    WiFiListDialogFragment fiListDialogFragment;

    public ConfigApFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_config_ap, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @OnTextChanged(R.id.et_wifi_pwd)
    public void onPwdUpdate(CharSequence s, int start, int before, int count) {
        boolean flag = TextUtils.isEmpty(s);
        ivWifiClearPwd.setVisibility(flag ? View.INVISIBLE : View.VISIBLE);
        tvWifiPwdSubmit.setEnabled(s.length() > 6);
    }

    /**
     * 明文/密文 密码
     *
     * @param buttonView
     * @param isChecked
     */
    @OnCheckedChanged(R.id.cb_wifi_pwd)
    public void onShowPwd(CompoundButton buttonView, boolean isChecked) {
        ViewUtils.showPwd(etWifiPwd, isChecked);
        etWifiPwd.setSelection(etWifiPwd.length());
    }

    @OnClick({R.id.iv_wifi_clear_pwd, R.id.cb_wifi_pwd, R.id.tv_wifi_pwd_submit, R.id.tv_config_ap_name})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_wifi_clear_pwd:
                etWifiPwd.setText("");
                break;
            case R.id.tv_wifi_pwd_submit:

                break;
            case R.id.tv_config_ap_name:
                if (getView() != null)
                    ViewUtils.deBounceClick(getView().findViewById(R.id.tv_config_ap_name));
                initFragment();
                fiListDialogFragment = fragmentWeakReference.get();
                if (fiListDialogFragment != null && fiListDialogFragment.isResumed())
                    return;
                fiListDialogFragment.show(getActivity().getSupportFragmentManager(), "WiFiListDialogFragment");
                break;
        }
    }

    private void initFragment() {
        if (fragmentWeakReference == null || fragmentWeakReference.get() == null)
            fragmentWeakReference = new WeakReference<>(WiFiListDialogFragment.newInstance(new Bundle()));
    }

    private WeakReference<WiFiListDialogFragment> fragmentWeakReference;

}
