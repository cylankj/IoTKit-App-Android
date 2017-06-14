package com.cylan.jiafeigou.n.view.bind;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.view.panorama.PanoramaCameraActivity;
import com.cylan.jiafeigou.widget.LoginButton;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by yanzhendong on 2017/5/18.
 */

public class ConfigPanoramaWiFiSuccessFragment extends IBaseFragment {

    @BindView(R.id.panorama_mode_picture)
    ImageView modePicture;
    @BindView(R.id.panorama_mode_desc)
    TextView modeDesc;
    @BindView(R.id.panorama_mode_done)
    LoginButton modeDone;
    private boolean success;
    private String mode;
    private String uuid;

    public static ConfigPanoramaWiFiSuccessFragment newInstance(Bundle bundle) {
        ConfigPanoramaWiFiSuccessFragment fragment = new ConfigPanoramaWiFiSuccessFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_config_panorama_wifi_success, container, false);
        ButterKnife.bind(this, view);
        Bundle arguments = getArguments();
        success = arguments.getBoolean("Success");
        mode = arguments.getString("PanoramaConfigure");
        uuid = arguments.getString(JConstant.KEY_DEVICE_ITEM_UUID);
        initView();
        return view;

    }

    private void initView() {
        modeDone.setText(success ? R.string.FINISHED : R.string.TRY_AGAIN);
        modePicture.setImageResource(TextUtils.equals(mode, "Family") ? R.drawable.pic_home_finish : R.drawable.pic_ap_finish);
        modeDesc.setText(TextUtils.equals(mode, "Family") ? R.string.Tap1_HomeMode_Opened : R.string.Tap1_OutdoorMode_Opened);
    }

    @OnClick(R.id.panorama_mode_done)
    public void done() {
        if (success) {
            startActivity(new Intent(getActivity(), PanoramaCameraActivity.class).putExtra(JConstant.KEY_DEVICE_ITEM_UUID, uuid));
        }
    }
}
