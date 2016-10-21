package com.cylan.jiafeigou.n.view.mine;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineRelativesAndFriendScanAddContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.MineRelativesAndFriendScanAddPresenterImp;
import com.cylan.jiafeigou.support.zscan.ZXingScannerView;
import com.google.zxing.Result;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * 作者：zsl
 * 创建时间：2016/9/6
 * 描述：
 */
public class MineRelativesAndFriendScanAddFragment extends Fragment implements ZXingScannerView.ResultHandler, MineRelativesAndFriendScanAddContract.View {

    @BindView(R.id.iv_home_mine_relativesandfriends_scan_add_back)
    ImageView ivHomeMineRelativesandfriendsScanAddBack;
    @BindView(R.id.zxV_scan_add_relativesandfriend)
    ZXingScannerView zxVScanAddRelativesandfriend;
    @BindView(R.id.iv_erweima)
    ImageView ivErweima;
    private MineRelativesAndFriendScanAddContract.Presenter presenter;

    public static MineRelativesAndFriendScanAddFragment newInstance() {
        return new MineRelativesAndFriendScanAddFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = new MineRelativesAndFriendScanAddPresenterImp(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine_relativesandfriend_scan_add, container, false);
        ButterKnife.bind(this, view);
        initView();
        showQrCode(presenter.encodeAsBitmap("1234", presenter.getDimension()));
        return view;
    }

    private void initView() {       //控制显示的宽和高
        int screenWidth = getActivity().getWindowManager().getDefaultDisplay().getWidth();
        int screenHeight = getActivity().getWindowManager().getDefaultDisplay().getHeight();

        ViewGroup.LayoutParams scanlayoutParams = zxVScanAddRelativesandfriend.getLayoutParams();
        scanlayoutParams.height = (int) (screenHeight * 0.41 + 0.5);
        scanlayoutParams.width = (int) (screenWidth * 0.72 + 0.5);
        zxVScanAddRelativesandfriend.setLayoutParams(scanlayoutParams);
        //二维码 qrCode
        ViewGroup.LayoutParams erWeimalayoutParams = ivErweima.getLayoutParams();
        erWeimalayoutParams.height = (int) (screenHeight * 0.135 + 0.5);
        erWeimalayoutParams.width = (int) (screenWidth * 0.24 + 0.5);
        ivErweima.setLayoutParams(erWeimalayoutParams);
    }

    @Override
    public void setPresenter(MineRelativesAndFriendScanAddContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @OnClick(R.id.iv_home_mine_relativesandfriends_scan_add_back)
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_home_mine_relativesandfriends_scan_add_back:
                getFragmentManager().popBackStack();
                break;
        }

    }

    @Override
    public void onStartScan() {
        zxVScanAddRelativesandfriend.startCamera();
    }

    @Override
    public void showQrCode(Bitmap bitmap) {
        ivErweima.setImageBitmap(bitmap);
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
                    zxVScanAddRelativesandfriend.resumeCameraPreview(MineRelativesAndFriendScanAddFragment.this);
                }
            }, 2000);
    }

    @Override
    public void onResume() {
        super.onResume();
        zxVScanAddRelativesandfriend.setResultHandler(MineRelativesAndFriendScanAddFragment.this);
        if (presenter != null)
            presenter.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        Observable.just(null)
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        zxVScanAddRelativesandfriend.stopCamera();
                    }
                });
    }

    @Override
    public void onStop() {
        super.onStop();
        if (presenter != null) presenter.stop();
    }

}
