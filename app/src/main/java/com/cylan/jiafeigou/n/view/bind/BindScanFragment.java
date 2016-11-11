package com.cylan.jiafeigou.n.view.bind;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.bind.ScanContract;
import com.cylan.jiafeigou.n.mvp.impl.bind.ScanContractImpl;
import com.cylan.jiafeigou.support.zscan.ZXingScannerView;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.google.zxing.Result;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

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
        this.basePresenter = new ScanContractImpl(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        zxVScan.setResultHandler(BindScanFragment.this);
    }

    @Override
    public void onPause() {
        super.onPause();
        Observable.just(null)
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        Log.d("onResume", "onResume: " + Thread.currentThread().getId());
                        zxVScan.stopCamera();
                    }
                });
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_bind_scan, null, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        ViewUtils.setViewMarginStatusBar(imgVNavBack);
    }


    @Override
    public void handleResult(Result rawResult) {
        Toast.makeText(getActivity(), "Contents = " + rawResult.getText() +
                ", Format = " + rawResult.getBarcodeFormat().name(), Toast.LENGTH_SHORT).show();
        // Note:
        // * Wait 2 seconds to resume the preview.
        // * On older devices continuously stopping and resuming camera preview can result in freezing the app.
        // * I don't know why this is the case but I don't have the time to figure out.
        if (getView() != null)
            getView().postDelayed(new Runnable() {
                @Override
                public void run() {
                    zxVScan.resumeCameraPreview(BindScanFragment.this);
                }
            }, 2000);
    }


    @OnClick(R.id.imgV_nav_back)
    public void onClick() {
        if (getActivity() != null)
            getActivity().getSupportFragmentManager().popBackStack();
    }

    @Override
    public void onScanRsp(int state) {

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
