package com.cylan.jiafeigou.n.view.bind;


import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.bind.BindDeviceContract;
import com.cylan.jiafeigou.n.view.BaseTitleFragment;
import com.cylan.jiafeigou.utils.AnimatorUtils;
import com.cylan.utils.ListUtils;
import com.nineoldandroids.animation.Animator;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BindCameraFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BindCameraFragment extends BaseTitleFragment implements BindDeviceContract.View {


    @BindView(R.id.imgV_camera_wifi_light_flash)
    ImageView imgVCameraWifiLightFlash;
    @BindView(R.id.imgV_camera_hand)
    ImageView imgVCameraHand;
    @BindView(R.id.imgV_camera_red_dot)
    ImageView imgVCameraRedDot;

    private BindDeviceContract.Presenter presenter;

    public BindCameraFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param bundle Parameter 2.
     * @return A new instance of fragment BindCameraFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BindCameraFragment newInstance(Bundle bundle) {
        BindCameraFragment fragment = new BindCameraFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateNavBackIcon(R.drawable.btn_nav_back);
        initAnimation();
    }

    Animator animator;

    @Override
    protected int getSubContentViewId() {
        return R.layout.fragment_bind_camera;
    }


    private void initAnimation() {
        animator = AnimatorUtils.onHandMoveAndFlash(imgVCameraHand, imgVCameraRedDot, imgVCameraWifiLightFlash);
        animator.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (animator != null && animator.isRunning())
            animator.cancel();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO: inflate a fragment view
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @OnClick(R.id.tv_bind_camera_tip)
    public void onClick() {
        Toast.makeText(getContext(), "startScan", Toast.LENGTH_SHORT).show();
        presenter.scanDevices();
    }

    @Override
    public void onDevicesRsp(List<ScanResult> resultList) {
        final int count = ListUtils.getSize(resultList);
        if (count == 0) {
            Toast.makeText(getContext(), "没发现设备", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void setPresenter(BindDeviceContract.Presenter presenter) {
        this.presenter = presenter;
    }
}
