package com.cylan.jiafeigou.n.view.mine;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineRelativesAndFriendScanAddContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.MineRelativesAndFriendScanAddPresenterImp;
import com.cylan.jiafeigou.support.zscan.ZXingScannerView;
import com.cylan.jiafeigou.utils.PermissionChecker;
import com.cylan.jiafeigou.utils.ToastUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 作者：zsl
 * 创建时间：2016/9/6
 * 描述：
 */
public class MineRelativesAndFriendScanAddFragment extends Fragment implements MineRelativesAndFriendScanAddContract.View {

    @BindView(R.id.iv_home_mine_relativesandfriends_scan_add_back)
    ImageView ivHomeMineRelativesandfriendsScanAddBack;
    @BindView(R.id.zxV_scan_add_relativesandfriend)
    ZXingScannerView zxVScanAddRelativesandfriend;
    private MineRelativesAndFriendScanAddContract.Presenter presenter;

    public static MineRelativesAndFriendScanAddFragment newInstance() {
        return new MineRelativesAndFriendScanAddFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = new MineRelativesAndFriendScanAddPresenterImp();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine_relativesandfriend_scan_add, container, false);
        ButterKnife.bind(this, view);
        initZXScan();
        return view;
    }

    private void initZXScan() {
        zxVScanAddRelativesandfriend.startCamera();
    }

    @Override
    public void setPresenter(MineRelativesAndFriendScanAddContract.Presenter presenter) {

    }

    @OnClick(R.id.iv_home_mine_relativesandfriends_scan_add_back)
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.iv_home_mine_relativesandfriends_scan_add_back:
                getFragmentManager().popBackStack();
                break;
        }

    }
}
