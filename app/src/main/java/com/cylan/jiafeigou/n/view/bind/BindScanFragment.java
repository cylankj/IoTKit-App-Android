package com.cylan.jiafeigou.n.view.bind;


import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.misc.AlertDialogManager;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.bind.ScanContract;
import com.cylan.jiafeigou.n.mvp.impl.bind.ScanPresenterImpl;
import com.cylan.jiafeigou.n.view.activity.BindBellActivity;
import com.cylan.jiafeigou.n.view.activity.BindCamActivity;
import com.cylan.jiafeigou.n.view.activity.BindDeviceActivity;
import com.cylan.jiafeigou.n.view.activity.BindRsCamActivity;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.zscan.ZXingScannerView;
import com.cylan.jiafeigou.utils.HandlerThreadUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.CustomToolbar;
import com.google.zxing.Result;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

import static android.Manifest.permission.CAMERA;
import static com.cylan.jiafeigou.misc.JConstant.QR_CODE_REG;
import static com.cylan.jiafeigou.misc.JConstant.QR_CODE_REG_WITH_SN;
import static com.cylan.jiafeigou.misc.JError.ErrorCIDBinded;
import static com.cylan.jiafeigou.misc.JError.ErrorCIDNotBind;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BindScanFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
@RuntimePermissions
public class BindScanFragment extends IBaseFragment<ScanContract.Presenter> implements ZXingScannerView.ResultHandler, ScanContract.View {

    @BindView(R.id.zxV_scan)
    ZXingScannerView zxVScan;
    @BindView(R.id.custom_toolbar)
    CustomToolbar customToolbar;

    public BindScanFragment() {
        // Required empty public constructor
    }

    public static BindScanFragment newInstance(Bundle bundle) {
        BindScanFragment fragment = new BindScanFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.basePresenter = new ScanPresenterImpl(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        BindScanFragmentPermissionsDispatcher.onCameraPermissionWithCheck(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        BindScanFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
        if (permissions.length == 1) {
            if (TextUtils.equals(permissions[0], CAMERA) && grantResults[0] > -1) {
                BindScanFragmentPermissionsDispatcher.onCameraPermissionWithCheck(this);
            }
        }
    }

    @OnPermissionDenied(Manifest.permission.CAMERA)
    public void onCameraPermissionDenied() {
        if (getActivity() != null && getActivity() instanceof BindDeviceActivity) {
            ((BindDeviceActivity) getActivity()).finishExt();
        }
    }

    @OnNeverAskAgain(Manifest.permission.CAMERA)
    public void onNeverAskAgainCameraPermission() {
        AlertDialogManager.getInstance().showDialog(getActivity(),
                getString(R.string.permission_auth, getString(R.string.CAMERA)),
                getString(R.string.permission_auth, getString(R.string.CAMERA)),
                getString(R.string.OK), (DialogInterface dialog, int which) -> {
                    startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 0);
                },
                getString(R.string.CANCEL), (DialogInterface dialog, int which) -> {
                    if (getActivity() != null && getActivity() instanceof BindDeviceActivity) {
                        ((BindDeviceActivity) getActivity()).finishExt();
                    }
                });
    }

    @NeedsPermission(Manifest.permission.CAMERA)
    public void onCameraPermission() {
        customToolbar.setVisibility(View.VISIBLE);
        zxVScan.startCamera();
        zxVScan.setResultHandler(BindScanFragment.this);
    }


    @OnShowRationale(Manifest.permission.CAMERA)
    public void showRationaleForCamera(PermissionRequest request) {
        // NOTE: Show activity_cloud_live_mesg_call_out_item rationale to explain why the permission is needed, e.g. with activity_cloud_live_mesg_call_out_item dialog.
        // Call proceed() or cancel() on the provided PermissionRequest to continue or abort
        AppLogger.d(JConstant.LOG_TAG.PERMISSION + "showRationaleForCamera");
        onNeverAskAgainCameraPermission();
    }


    @Override
    public void onPause() {
        super.onPause();
        Log.d("onResume", "onResume: " + Thread.currentThread().getId());
        zxVScan.stopCamera();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_bind_scan, null, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        ViewUtils.setViewMarginStatusBar(customToolbar);
        customToolbar.setVisibility(View.GONE);
        customToolbar.setBackAction(v -> {
            if (getActivity() != null)
                getActivity().getSupportFragmentManager().popBackStack();
        });
    }


    @Override
    public void handleResult(Result rawResult) {// Note:
        // * Wait 2 seconds to resume the preview.
        // * On older devices continuously stopping and resuming camera preview can result in freezing the app.
        // * I don't know why this is the case but I don't have the startTime to figure out.
        String result = rawResult.getText().replace(JConstant.EFAMILY_URL_PREFIX, "");
        if (QR_CODE_REG_WITH_SN.matcher(result).find()) {
            try {
                handleScanResult(result);
            } catch (Exception e) {
                AppLogger.e("" + e.getLocalizedMessage());
            }
        } else if (QR_CODE_REG.matcher(result).find()) {
            ToastUtil.showNegativeToast(getString(R.string.Tap1_AddDevice_QR_Fail));
            zxVScan.stopCamera();
            if (getActivity() instanceof BindDeviceActivity)
                ((BindDeviceActivity) getActivity()).finishExt();
        } else {
            ToastUtil.showToast(getString(R.string.EFAMILY_INVALID_DEVICE));
            if (getActivity() instanceof BindDeviceActivity)
                ((BindDeviceActivity) getActivity()).finishExt();
        }
    }

    private static final Pattern vidReg = Pattern.compile("vid=[0-9a-zA-Z]{0,12}");
    private static final Pattern pidReg = Pattern.compile("pid=\\d{0,12}");
    private static final Pattern snReg = Pattern.compile("sn=[0-9a-zA-Z]{0,64}");

    private void handleScanResult(String content) {
        Matcher matcher = vidReg.matcher(content);
        String vid = null, pid = null, sn = null;
        if (matcher.find()) {
            vid = matcher.group().replace("vid=", "");
        }
        matcher = pidReg.matcher(content);
        if (matcher.find()) {
            pid = matcher.group().replace("pid=", "");
        }
        matcher = snReg.matcher(content);
        if (matcher.find()) {
            sn = matcher.group().replace("sn=", "");
        }
        if (TextUtils.isEmpty(vid) || TextUtils.isEmpty(pid) || TextUtils.isEmpty(sn)) {
            ToastUtil.showToast(getString(R.string.EFAMILY_INVALID_DEVICE));
            return;
        }
        int net = NetUtils.getJfgNetType(getActivity());
        if (net == 0) {
            ToastUtil.showToast(getString(R.string.NoNetworkTips));
            return;
        }
        zxVScan.stopCamera();
        try {
            Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(sn);
            if (device != null && device.available()) {
                ToastUtil.showNegativeToast(getString(R.string.Tap1_AddedDeviceTips));
                HandlerThreadUtils.postDelay(() -> {
                    if (zxVScan != null) zxVScan.stop();
                    if (getActivity() != null && getActivity().getSupportFragmentManager() != null)
                        getActivity().getSupportFragmentManager().popBackStack();
                    zxVScan.stop();
                }, 2000);
            } else {
                getActivity().getSupportFragmentManager().beginTransaction().remove(this)
                        .commitAllowingStateLoss();//不需要动画.
                int iPid = Integer.parseInt(pid);
                if (JFGRules.isRS(iPid)) {
                    if (getActivity() != null)
                        startActivity(new Intent(getActivity(), BindRsCamActivity.class));
                } else if (JFGRules.isCamera(iPid)) {
                    if (getActivity() != null)
                        startActivity(new Intent(getActivity(), BindCamActivity.class));
                } else if (JFGRules.isBell(iPid)) {
                    if (getActivity() != null)
                        startActivity(new Intent(getActivity(), BindBellActivity.class));
                } else {
                    AppLogger.d("不支持的设备类型");
                    ToastUtil.showNegativeToast(getString(R.string.Tap1_AddDevice_QR_Fail));
                }
                zxVScan.stop();
            }
        } catch (Exception e) {

        }
    }

    @Override
    public void onScanRsp(int state) {
        Log.d(this.getClass().getSimpleName(), "bindResult: " + state);
        if (state == 0) {
            ToastUtil.showPositiveToast(getString(R.string.Added_successfully));
        } else if (state == -1) {
            ToastUtil.showToast(getString(R.string.ADD_FAILED));
        } else if (state == ErrorCIDBinded) {
            ToastUtil.showToast(getString(R.string.RET_EISBIND_BYSELF));
        } else if (state == ErrorCIDNotBind) {
            ToastUtil.showToast(getString(R.string.RET_ECID_INVALID));
        }
        getActivity().getSupportFragmentManager().popBackStack();
        //默认强绑
    }

    @Override
    public void onStartScan() {
        zxVScan.startCamera();
    }


    @Override
    public void setPresenter(ScanContract.Presenter presenter) {
        this.basePresenter = presenter;
    }
}
