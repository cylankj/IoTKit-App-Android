package com.cylan.jiafeigou.n.view.mine;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineFriendScanAddContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.MineFriendScanAddPresenterImp;
import com.cylan.jiafeigou.n.mvp.model.MineAddReqBean;
import com.cylan.jiafeigou.support.zscan.ZXingScannerView;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.LoadingDialog;
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
public class MineFriendScanAddFragment extends Fragment implements ZXingScannerView.ResultHandler, MineFriendScanAddContract.View {

    @BindView(R.id.iv_home_mine_relativesandfriends_scan_add_back)
    ImageView ivHomeMineRelativesandfriendsScanAddBack;
    @BindView(R.id.zxV_scan_add_relativesandfriend)
    ZXingScannerView zxVScanAddRelativesandfriend;
    @BindView(R.id.iv_erweima)
    ImageView ivErweima;
    @BindView(R.id.rl_send_pro_hint)
    RelativeLayout rlSendProHint;
    private MineFriendScanAddContract.Presenter presenter;

    public static MineFriendScanAddFragment newInstance() {
        return new MineFriendScanAddFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = new MineFriendScanAddPresenterImp(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine_friend_scan_add, container, false);
        ButterKnife.bind(this, view);
        initView();
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
    public void setPresenter(MineFriendScanAddContract.Presenter presenter) {
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

    /**
     * 跳转到对方详情页
     */
    @Override
    public void jump2FriendDetailFragment(boolean isFrom, MineAddReqBean bean) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("isFrom", isFrom);
        bundle.putSerializable("addRequestItems", bean);
        MineFriendAddReqDetailFragment addReqDetailFragment = MineFriendAddReqDetailFragment.newInstance(bundle);
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(android.R.id.content, addReqDetailFragment, "addReqDetailFragment")
                .addToBackStack("mineHelpFragment")
                .commit();
    }

    /**
     * 已经是亲友
     */
    @Override
    public void isMineFriendResult() {
        ToastUtil.showToast("已是亲友");
    }

    /**
     * 扫描无结果
     */
    @Override
    public void scanNoResult() {
        ToastUtil.showToast(getString(R.string.EFAMILY_INVALID_DEVICE));
    }

    /**
     * 显示加载进度
     */
    @Override
    public void showLoadingPro() {
        LoadingDialog.showLoading(getFragmentManager(), getString(R.string.LOADING));
    }

    /**
     * 网络状态变化
     *
     * @param state
     */
    @Override
    public void onNetStateChanged(int state) {
        if (state == -1) {
            hideLoadingPro();
            ToastUtil.showNegativeToast(getString(R.string.NO_NETWORK_1));
        }
    }

    /**
     * 隐藏加载进度
     */
    @Override
    public void hideLoadingPro() {
        LoadingDialog.dismissLoading(getFragmentManager());
    }

    @Override
    public void handleResult(final Result rawResult) {
        showLoadingPro();
        if (getView() != null) {
            getView().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (presenter != null) {
                        presenter.checkScanAccount(rawResult.getText());
                    }
                }
            }, 2000);
        }

        // Note:
        // * Wait 2 seconds to resume the preview.
        // * On older devices continuously stopping and resuming camera preview can result in freezing the app.
        // * I don't know why this is the case but I don't have the time to figure out.
        if (getView() != null)
            getView().postDelayed(new Runnable() {
                @Override
                public void run() {
                    zxVScanAddRelativesandfriend.resumeCameraPreview(MineFriendScanAddFragment.this);
                }
            }, 2000);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (presenter != null) presenter.start();
    }

    @Override
    public void onResume() {
        super.onResume();
        zxVScanAddRelativesandfriend.setResultHandler(MineFriendScanAddFragment.this);
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