package com.cylan.jiafeigou.n.view.mine;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.db.module.Account;
import com.cylan.jiafeigou.databinding.FragmentMineFriendScanAddBinding;
import com.cylan.jiafeigou.misc.LinkManager;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineFriendScanAddContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.MineFriendScanAddPresenterImp;
import com.cylan.jiafeigou.n.view.adapter.item.FriendContextItem;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.zscan.Qrcode;
import com.cylan.jiafeigou.support.zscan.ZXingScannerView;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.LoadingDialog;
import com.google.zxing.Result;

import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 作者：zsl
 * 创建时间：2016/9/6
 * 描述：
 */
public class MineFriendQRScanFragment extends IBaseFragment<MineFriendScanAddContract.Presenter> implements ZXingScannerView.ResultHandler, MineFriendScanAddContract.View {

    private MineFriendScanAddContract.Presenter presenter;
    private FragmentMineFriendScanAddBinding scanAddBinding;

    public static MineFriendQRScanFragment newInstance() {
        return new MineFriendQRScanFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = new MineFriendScanAddPresenterImp(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        scanAddBinding = FragmentMineFriendScanAddBinding.inflate(inflater, container, false);
        ButterKnife.bind(this, scanAddBinding.customToolbar);
        return scanAddBinding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        scanAddBinding.qrScanView.startCamera();
        Account account = DataSourceManager.getInstance().getAccount();
        if (account != null && account.getAccount() != null) {
            rx.Observable.just("go")
                    .subscribeOn(Schedulers.io())
                    .map(ret -> Qrcode.createQRImage(LinkManager.getQrCodeLink(), ViewUtils.dp2px(78), ViewUtils.dp2px(78), null))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(ret -> scanAddBinding.qrCodePicture.setImageBitmap(ret), AppLogger::e);
        }
    }

    private void initView() {       //控制显示的宽和高
//        int screenWidth = getActivity().getWindowManager().getDefaultDisplay().getWidth();
//        int screenHeight = getActivity().getWindowManager().getDefaultDisplay().getHeight();
//
//        ViewGroup.LayoutParams scanlayoutParams = zxVScanAddRelativesandfriend.getLayoutParams();
//        scanlayoutParams.height = (int) (screenHeight * 0.41 + 0.5);
//        scanlayoutParams.width = (int) (screenWidth * 0.72 + 0.5);
//        zxVScanAddRelativesandfriend.setLayoutParams(scanlayoutParams);
//        //二维码 qrCode
//        ViewGroup.LayoutParams erWeimalayoutParams = ivErweima.getLayoutParams();
//        erWeimalayoutParams.height = (int) (screenHeight * 0.135 + 0.5);
//        erWeimalayoutParams.width = (int) (screenWidth * 0.24 + 0.5);
//        ivErweima.setLayoutParams(erWeimalayoutParams);
    }

    @OnClick(R.id.tv_toolbar_icon)
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_toolbar_icon:
                getActivity().getSupportFragmentManager().popBackStack();
                break;
        }

    }

    public void enterFriendInformationFragment(FriendContextItem friendContextItem) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("friendItem", friendContextItem);
        MineFriendInformationFragment friendInformationFragment = MineFriendInformationFragment.newInstance(bundle);
        friendInformationFragment.setCallBack(t -> {
            scanAddBinding.qrScanView.startCamera();
            scanAddBinding.qrScanView.resumeCameraPreview(this);
        });
        ActivityUtils.addFragmentSlideInFromRight(getActivity().getSupportFragmentManager(), friendInformationFragment, android.R.id.content, MineFriendInformationFragment.class.getSimpleName());
    }

    @Override
    public void showLoading(int resId, String... args) {
        LoadingDialog.showLoading(getActivity(), getString(resId, (Object[]) args), true);
    }

    @Override
    public void hideLoading() {
        LoadingDialog.dismissLoading();
    }

    /**
     * 网络状态变化
     *
     * @param state
     */
    @Override
    public void onNetStateChanged(int state) {
        if (state == -1) {
            hideLoading();
            ToastUtil.showNegativeToast(getString(R.string.NO_NETWORK_1));
        }
    }

    @Override
    public void onCheckFriendAccountResult(FriendContextItem friendContextItem) {
        if (friendContextItem == null) {
            //未获取到账号信息
            ToastUtil.showToast(getString(R.string.RET_ELOGIN_ACCOUNT_NOT_EXIST));
        } else {
            //已经是好友了
            scanAddBinding.qrScanView.removeCallbacks(resumeRunnable);
            scanAddBinding.qrScanView.stop();
            enterFriendInformationFragment(friendContextItem);
        }
    }

    private Runnable resumeRunnable = () -> scanAddBinding.qrScanView.resumeCameraPreview(MineFriendQRScanFragment.this);

    @Override
    public void handleResult(final Result rawResult) {
        scanAddBinding.qrScanView.postDelayed(resumeRunnable, 2000);
        String account = DataSourceManager.getInstance().getJFGAccount().getAccount();

        if (NetUtils.getJfgNetType() == 0) {
            ToastUtil.showNegativeToast(getString(R.string.OFFLINE_ERR_1));
            return;
        }
        final String tag = "id=";
        final String result = rawResult.getText();
        final int start = result.indexOf(tag);
        if (start < 0 || start > result.length()) {
            //无效二维码
            ToastUtil.showNegativeToast(getString(R.string.EFAMILY_INVALID_DEVICE));
            return;
        }
        final String targetAccount = result.substring(start).replace(tag, "").trim();
        AppLogger.d("扫描结果: " + targetAccount);
        if (TextUtils.equals(targetAccount, account)) {
            ToastUtil.showNegativeToast(getString(R.string.Tap3_FriendsAdd_NotYourself));
        } else {
            presenter.checkFriendAccount(targetAccount);
        }

        // Note:
        // * Wait 2 seconds to resume the preview.
        // * On older devices continuously stopping and resuming camera preview can result in freezing the app.
        // * I don't know why this is the case but I don't have the startTime to figure out.

    }

    @Override
    public void onResume() {
        super.onResume();
        scanAddBinding.qrScanView.setResultHandler(MineFriendQRScanFragment.this);
    }

    @Override
    public void onPause() {
        super.onPause();
        scanAddBinding.qrScanView.stopCamera();
    }

}