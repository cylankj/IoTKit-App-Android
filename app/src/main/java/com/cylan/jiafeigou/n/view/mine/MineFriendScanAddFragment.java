package com.cylan.jiafeigou.n.view.mine;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.cache.db.module.FriendBean;
import com.cylan.jiafeigou.misc.LinkManager;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineFriendScanAddContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.MineFriendScanAddPresenterImp;
import com.cylan.jiafeigou.cache.db.module.FriendsReqBean;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.zscan.Qrcode;
import com.cylan.jiafeigou.support.zscan.ZXingScannerView;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
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
//        initView();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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

    @Override
    public String getUuid() {
        return null;
    }

    @OnClick(R.id.tv_toolbar_icon)
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_toolbar_icon:
                getActivity().getSupportFragmentManager().popBackStack();
                break;
        }

    }

    @Override
    public void onStartScan() {
        zxVScanAddRelativesandfriend.startCamera();
    }

    @Override
    public void showQrCode(String account) {
        ivErweima.setImageBitmap(Qrcode.createQRImage(LinkManager.getQrCodeLink(), ViewUtils.dp2px(78), ViewUtils.dp2px(78), null));
    }

    /**
     * 跳转到对方详情页
     */
    @Override
    public void jump2FriendDetailFragment(boolean isFrom, FriendsReqBean bean, boolean isFriend) {
        if (!isFriend) {
            Bundle bundle = new Bundle();
            bundle.putBoolean("isFrom", isFrom);
            bundle.putSerializable("addRequestItems", bean);
            AddFriendReqDetailFragment addReqDetailFragment = AddFriendReqDetailFragment.newInstance(bundle);
            getActivity().getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                            , R.anim.slide_in_left, R.anim.slide_out_right)
                    .add(android.R.id.content, addReqDetailFragment, "addReqDetailFragment")
                    .addToBackStack("AddFlowStack")
                    .commit();
        } else {
            //已是亲友的跳转到分享
            FriendBean friendBean = new FriendBean();
            friendBean.account = bean.account;
            friendBean.alias = bean.alias;
            friendBean.markName = "";
            friendBean.iconUrl = bean.iconUrl;
            Bundle bundle = new Bundle();
            bundle.putInt("position", -1);
            bundle.putParcelable("frienditembean", friendBean);
            MineFriendDetailFragment relativeAndFrienDetialFragment = MineFriendDetailFragment.newInstance(bundle);
            getActivity().getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                            , R.anim.slide_in_left, R.anim.slide_out_right)
                    .add(android.R.id.content, relativeAndFrienDetialFragment, "relativeAndFrienDetialFragment")
                    .addToBackStack("mineHelpFragment")
                    .commit();
        }

    }

    /**
     * 已经是亲友
     */
    @Override
    public void isMineFriendResult() {
        ToastUtil.showToast(getString(R.string.Tap3_FriendsAdd_NotYourself));
    }

    /**
     * 扫描无结果
     */
    @Override
    public void scanNoResult() {
        ToastUtil.showToast(getString(R.string.RET_ELOGIN_ACCOUNT_NOT_EXIST));
    }

    /**
     * 显示加载进度
     */
    @Override
    public void showLoadingPro() {
        LoadingDialog.showLoading(getActivity().getSupportFragmentManager(), getString(R.string.LOADING));
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
        LoadingDialog.dismissLoading(getActivity().getSupportFragmentManager());
    }

    @Override
    public void handleResult(final Result rawResult) {
        String account = BaseApplication.getAppComponent().getSourceManager().getJFGAccount().getAccount();

        if (NetUtils.getJfgNetType() == 0) {
            ToastUtil.showNegativeToast(getString(R.string.OFFLINE_ERR_1));
            return;
        }
        if (getView() != null) {
            if (presenter != null) {
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
                    presenter.checkScanAccount(targetAccount);
                }
            }
        }

        // Note:
        // * Wait 2 seconds to resume the preview.
        // * On older devices continuously stopping and resuming camera preview can result in freezing the app.
        // * I don't know why this is the case but I don't have the startTime to figure out.
        if (getView() != null)
            getView().postDelayed(() -> zxVScanAddRelativesandfriend.resumeCameraPreview(MineFriendScanAddFragment.this), 2000);
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
                }, AppLogger::e);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (presenter != null) presenter.stop();
    }

}