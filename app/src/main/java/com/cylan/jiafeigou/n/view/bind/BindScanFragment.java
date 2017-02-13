package com.cylan.jiafeigou.n.view.bind;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.bind.ScanContract;
import com.cylan.jiafeigou.n.mvp.impl.bind.ScanPresenterImpl;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.zscan.ZXingScannerView;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.LoadingDialog;
import com.cylan.jiafeigou.widget.dialog.BaseDialog;
import com.cylan.jiafeigou.widget.dialog.SimpleDialogFragment;
import com.google.zxing.Result;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.cylan.jiafeigou.misc.JConstant.EFAMILY_QR_CODE_REG;
import static com.cylan.jiafeigou.misc.JConstant.EFAMILY_URL_PREFIX;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BindScanFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BindScanFragment extends IBaseFragment<ScanContract.Presenter> implements ZXingScannerView.ResultHandler, ScanContract.View {

    @BindView(R.id.zxV_scan)
    ZXingScannerView zxVScan;
    @BindView(R.id.imgV_nav_back)
    ImageView imgVNavBack;
    @BindView(R.id.fLayout_top_bar)
    FrameLayout fLayoutTopBar;
    private String uuid;
    private SimpleDialogFragment rebindDialog;
    private Bundle bindBundle;

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
        zxVScan.startCamera();
        zxVScan.setResultHandler(BindScanFragment.this);
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
        ViewUtils.setViewMarginStatusBar(fLayoutTopBar);
    }


    @Override
    public void handleResult(Result rawResult) {// Note:
        // * Wait 2 seconds to resume the preview.
        // * On older devices continuously stopping and resuming camera preview can result in freezing the app.
        // * I don't know why this is the case but I don't have the startTime to figure out.
        if (EFAMILY_QR_CODE_REG.matcher(rawResult.getText().replace(JConstant.EFAMILY_URL_PREFIX, "")).find()) {
            try {
                String tmp = rawResult.getText().replace(EFAMILY_URL_PREFIX, "").replace("&", "");
                int indexCid = tmp.indexOf("cid=");
                int indexMac = tmp.indexOf("mac=");
                String cid = tmp.substring(indexCid + 4, indexCid + 16);
                String mac = tmp.substring(indexMac + 4, tmp.length());
                uuid = cid;
                bindBundle = new Bundle();
                bindBundle.putString("cid", cid);
                bindBundle.putString("mac", mac);
                bindBundle.putInt("bindWay", 0);
                bindBundle.putString("alias", getString(R.string.DOOR_MAGNET_NAME));
                basePresenter.submit(bindBundle);
                zxVScan.stop();
                bindBundle.putString(JConstant.KEY_DEVICE_ITEM_UUID, cid);
                LoadingDialog.showLoading(getActivity().getSupportFragmentManager(),
                        getString(R.string.PLEASE_WAIT_2), true);
                AppLogger.i("" + rawResult.getText());
            } catch (Exception e) {
                AppLogger.e("" + e.getLocalizedMessage());
            }
        } else if (getView() != null) {
            ToastUtil.showNegativeToast(getString(R.string.EFAMILY_INVALID_DEVICE));
            getView().postDelayed(() -> {
                zxVScan.resumeCameraPreview(BindScanFragment.this);
            }, 2000);
        }
    }


    @OnClick(R.id.imgV_nav_back)
    public void onClick() {
        if (getActivity() != null)
            getActivity().getSupportFragmentManager().popBackStack();
    }

    @Override
    public void onScanRsp(int state) {
        LoadingDialog.dismissLoading(getActivity().getSupportFragmentManager());
        Log.d(this.getClass().getSimpleName(), "bindResult: " + state);
        if (state == 0 && getActivity() != null) {
            ToastUtil.showPositiveToast(getString(R.string.Added_successfully));
            getActivity().getSupportFragmentManager().popBackStack();
        } else if (state == 8) {
            //需要重复绑定
            if (rebindDialog != null && rebindDialog.isResumed())
                return;
            Bundle bundle = new Bundle();
            bundle.putString(BaseDialog.KEY_TITLE, "");
            bundle.putString(SimpleDialogFragment.KEY_LEFT_CONTENT, getString(R.string.OK));
            bundle.putString(SimpleDialogFragment.KEY_RIGHT_CONTENT, getString(R.string.CANCEL));
            bundle.putString(SimpleDialogFragment.KEY_CONTENT_CONTENT,
                    this.getString(R.string.RET_EISBIND_BYOTHER));
            rebindDialog = SimpleDialogFragment.newInstance(bundle);
            rebindDialog.setAction((int id, Object value) -> {
                bindBundle.putInt("bindWay", 1);
                basePresenter.submit(bindBundle);
            });
            rebindDialog.show(getActivity().getSupportFragmentManager(), "rebindDialog");
        }
    }

    @Override
    public void onStartScan() {
        zxVScan.startCamera();
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    @Override
    public void setPresenter(ScanContract.Presenter presenter) {
        this.basePresenter = presenter;
    }
}
