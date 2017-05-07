package com.cylan.jiafeigou.n.view.firmware;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.cylan.jiafeigou.NewHomeActivity;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.AlertDialogManager;
import com.cylan.jiafeigou.misc.ClientUpdateManager;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.cam.FUpdatingContract;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.CustomToolbar;
import com.cylan.jiafeigou.widget.LoginButton;
import com.cylan.jiafeigou.widget.SimpleProgressBar;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FirmwareUpdatingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FirmwareUpdatingFragment extends IBaseFragment<FUpdatingContract.Presenter>
        implements FUpdatingContract.View, ClientUpdateManager.FUpdatingListener {


    @BindView(R.id.custom_toolbar)
    CustomToolbar customToolbar;
    @BindView(R.id.tv_loading_percent)
    TextView tvLoadingPercent;
    @BindView(R.id.vs_layout_switch)
    ViewSwitcher vsLayoutSwitch;
    Unbinder unbinder;
    @BindView(R.id.tv_update_result)
    TextView tvUpdateResult;
    @BindView(R.id.btn_update_result)
    LoginButton btnUpdateResult;
    @BindView(R.id.progress_loading)
    SimpleProgressBar progressLoading;

    public FirmwareUpdatingFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static FirmwareUpdatingFragment newInstance(Bundle bundle) {
        FirmwareUpdatingFragment fragment = new FirmwareUpdatingFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        bundle.putString(JConstant.KEY_DEVICE_ITEM_UUID, "200000046267");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_firmware_updating, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewUtils.setViewMarginStatusBar(customToolbar);
        customToolbar.setBackAction(v -> getActivity().getSupportFragmentManager().popBackStack());
    }

    @Override
    public void onStart() {
        super.onStart();
        ClientUpdateManager.FirmWareUpdatingTask task = ClientUpdateManager.getInstance().getUpdatingTask(getUuid());
        if (task != null) {
            if (task.getUpdateState() == JConstant.U.SUCCESS) {
                prepareNextPage(0);
            } else if (task.getUpdateState() < 0) {
                prepareNextPage(task.getUpdateState());
            }
            tvLoadingPercent.setText(task.getSimulatePercent() + "");
        }
        ClientUpdateManager.getInstance().enqueue(getUuid(), new Updating(this));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void start() {
        if (!isAdded()) return;
        AppLogger.d("开始升级");
        progressLoading.post(() -> progressLoading.run());
    }

    @Override
    public void progress(int percent) {
        if (!isAdded()) return;
        Log.d("FirmwareUpdating", "模拟:" + percent);
        tvLoadingPercent.post(() -> tvLoadingPercent.setText(percent + ""));
    }

    @Override
    public void err(int errCode) {
        if (!isAdded()) return;
        progressLoading.post(() -> {
            progressLoading.dismiss();
            Log.d("FirmwareUpdating", "错误:" + errCode);
            if (errCode == JConstant.U.FAILED_30S || errCode == JConstant.U.FAILED_FPING_ERR) {
                prepareNextPage(errCode);
            } else if (errCode == JConstant.U.FAILED_60S || errCode == JConstant.U.FAILED_DEVICE_FAILED) {
                //失败
                showNextSafe();
                tvUpdateResult.setText(getString(R.string.Tap1_FirmwareUpdateFai));
                btnUpdateResult.setText(getString(R.string.TRY_AGAIN));
            }
        });
    }

    @Override
    public void success() {
        if (!isAdded()) return;
        progressLoading.post(() -> {
            progressLoading.dismiss();
            prepareNextPage(JConstant.U.SUCCESS);
        });
    }

    /**
     * 0:成功
     * -1:无响应,弹窗
     * -2:失败,超时
     *
     * @param next
     */
    private void prepareNextPage(int next) {
        if (!isAdded()) return;
        if (next == JConstant.U.FAILED_60S || next == JConstant.U.FAILED_DEVICE_FAILED) {
            tvUpdateResult.setText(getString(R.string.Tap1_FirmwareUpdateFai));
            btnUpdateResult.setText(getString(R.string.TRY_AGAIN));
            showNextSafe();
            progressLoading.dismiss();
        }
        if (next == JConstant.U.FAILED_30S || next == JConstant.U.FAILED_FPING_ERR) {
            showNextSafe();
            progressLoading.dismiss();
            tvUpdateResult.setText(getString(R.string.Tap1_FirmwareUpdateFai));
            btnUpdateResult.setText(getString(R.string.TRY_AGAIN));
            AlertDialogManager.getInstance().showDialog(getActivity(), getString(R.string.UPDATE_DISCONNECT),
                    getString(R.string.UPDATE_DISCONNECT),
                    getString(R.string.OK), (DialogInterface dialog, int which) -> {
                        //重试
                        ClientUpdateManager.getInstance().removeTask(getUuid());
                        ClientUpdateManager.getInstance().enqueue(getUuid(), new Updating(this));
                        startAgain();
                    }, getString(R.string.CANCEL), (DialogInterface dialog, int which) -> {
                        Intent intent = new Intent(getActivity(), NewHomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    });
        }
        if (next == JConstant.U.SUCCESS) {
            customToolbar.setToolbarLeftTitle("");
            tvUpdateResult.setText(getString(R.string.RE_ADD_LABEL));
            btnUpdateResult.setText(getString(R.string.FINISHED));
            showNextSafe();
        }
    }

    @OnClick(R.id.btn_update_result)
    public void clickResult() {
        if (TextUtils.equals(btnUpdateResult.getText(), getString(R.string.FINISHED))) {
            Intent intent = new Intent(getActivity(), NewHomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            AppLogger.d("升级成功,需要清理一些缓存");
        } else {
            ClientUpdateManager.getInstance().removeTask(getUuid());
            ClientUpdateManager.getInstance().enqueue(getUuid(), new Updating(this));
            startAgain();
        }
    }

    private void showPreviousSafe() {
        View v = vsLayoutSwitch.getCurrentView();
        if (v != null && v.getId() == R.id.v_second)
            vsLayoutSwitch.showPrevious();
    }

    private void showNextSafe() {
        View v = vsLayoutSwitch.getCurrentView();
        if (v != null && v.getId() == R.id.v_first)
            vsLayoutSwitch.showNext();
    }

    private void startAgain() {
        showPreviousSafe();
        customToolbar.setToolbarLeftTitle(getString(R.string.DEVICE_UPGRADE));
        progressLoading.post(() -> progressLoading.run());
    }

    private static class Updating implements ClientUpdateManager.FUpdatingListener {

        private WeakReference<ClientUpdateManager.FUpdatingListener> listenerRef;

        public Updating(ClientUpdateManager.FUpdatingListener listener) {
            this.listenerRef = new WeakReference<>(listener);
        }

        @Override
        public void start() {
            if (listenerRef == null || listenerRef.get() == null) return;
            listenerRef.get().start();
        }

        @Override
        public void progress(int percent) {
            if (listenerRef == null || listenerRef.get() == null) return;
            listenerRef.get().progress(percent);
        }

        @Override
        public void err(int errCode) {
            if (listenerRef == null || listenerRef.get() == null) return;
            listenerRef.get().err(errCode);
        }

        @Override
        public void success() {
            if (listenerRef == null || listenerRef.get() == null) return;
            listenerRef.get().success();
        }

    }
}
