package com.cylan.jiafeigou.n.view.home;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.home.HomeMineContract;
import com.cylan.jiafeigou.n.mvp.impl.setting.AccountInfoPresenterImpl;
import com.cylan.jiafeigou.n.view.fragment.AccountInfoFragment;
import com.cylan.jiafeigou.n.view.login.LoginModelActivity;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.HomeMineItemView;
import com.cylan.jiafeigou.widget.roundedimageview.RoundedImageView;
import com.cylan.sdkjni.JfgCmd;
import com.cylan.utils.BitmapUtil;
import com.cylan.utils.FastBlurUtil;
import com.superlog.SLog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class HomeMineFragment extends Fragment
        implements HomeMineContract.View, View.OnClickListener {


    @BindView(R.id.iv_home_mine_portrait)
    RoundedImageView ivHomeMinePortrait;

    @BindView(R.id.tv_home_mine_nick)
    TextView tvHomeMineNick;

    @BindView(R.id.iv_mine_msg)
    ImageView ivMineMsg;
    @BindView(R.id.tv_home_mine_msg_count)
    TextView tvHomeMineMsgCount;
    @BindView(R.id.rLayout_home_mine_top)
    RelativeLayout rLayoutHomeMineTop;
    @BindView(R.id.home_mine_item_friend)
    HomeMineItemView homeMineItemFriend;
    @BindView(R.id.home_mine_item_share)
    HomeMineItemView homeMineItemShare;
    @BindView(R.id.home_mine_item_help)
    HomeMineItemView homeMineItemHelp;
    @BindView(R.id.home_mine_item_settings)
    HomeMineItemView homeMineItemSettings;
    private HomeMineContract.Presenter presenter;

    public static HomeMineFragment newInstance(Bundle bundle) {
        HomeMineFragment fragment = new HomeMineFragment();
        fragment.setArguments(bundle);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_mine, container, false);
        ButterKnife.bind(this, view);
        setLinstener();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewUtils.setViewMarginStatusBar(ivMineMsg);
    }

    /**
     * 测试高斯模糊背景
     *
     * @param resId
     */
    private void testBlurBackground(int resId) {
        long time = System.currentTimeMillis();
        Bitmap bm = BitmapFactory.decodeResource(getResources(), resId);
        Bitmap b = BitmapUtil.zoomBitmap(bm, 160, 160);
        ivHomeMinePortrait.setImageDrawable(new BitmapDrawable(getResources(), b));
        b = FastBlurUtil.blur(b, 20, 2);
        rLayoutHomeMineTop.setBackgroundDrawable(new BitmapDrawable(getResources(), b));
        SLog.e("usetime:%d ms", System.currentTimeMillis() - time);
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (presenter != null) {
            presenter.start();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (presenter != null) {
            presenter.stop();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    public void portrait() {
        if (needStartLoginFragment()) return;
        ToastUtil.showToast(getContext(), "推荐fragment");
        AccountInfoFragment fragment = (AccountInfoFragment) AccountInfoFragment.getInstance();
        ActivityUtils.addFragmentToActivity(getActivity().getSupportFragmentManager(),
                fragment, R.id.rLayout_new_home_container, 0);
        new AccountInfoPresenterImpl(fragment);
    }

    public void friendItem(View view) {
        if (needStartLoginFragment()) return;
        SLog.i("It's Login,can do something!");
    }

    public void shareItem(View view) {
        if (needStartLoginFragment()) return;
        SLog.i("It's Login,can do something!");
    }

    public void settingsItem(View view) {
        if (needStartLoginFragment()) return;
        SLog.i("It's Login,can do something!");
    }

    public void helpItem(View view) {
        if (needStartLoginFragment()) return;
        SLog.i("It's Login,can do something!");
    }

    public void blurPic(View view) {
        if (needStartLoginFragment()) return;
        SLog.i("It's Login,can do something!");
    }

    @Override
    public void setPresenter(HomeMineContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void onPortraitUpdate(String url) {
        if (getActivity() != null) {
            testBlurBackground(R.drawable.clouds);
            tvHomeMineMsgCount.post(new Runnable() {
                @Override
                public void run() {
                    tvHomeMineMsgCount.setText("99+");
                    tvHomeMineMsgCount.setBackgroundResource(R.drawable.shape_mine_msg_count_rectangle);
                }
            });
        }
    }


    private boolean needStartLoginFragment() {
        if (!JfgCmd.getJfgCmd(getContext()).isLogined) {
            getActivity().startActivity(new Intent(getContext(), LoginModelActivity.class));
            return true;
        }
        return false;
    }

    @OnClick({R.id.iv_home_mine_portrait, R.id.tv_home_mine_nick,
            R.id.iv_mine_msg, R.id.tv_home_mine_msg_count, R.id.rLayout_home_mine_top})
    public void onButterknifeClick(View view) {
        switch (view.getId()) {
            case R.id.iv_home_mine_portrait:
                portrait();
                break;
            case R.id.tv_home_mine_nick:
                SLog.e("tv_home_mine_nick");
                break;
            case R.id.iv_mine_msg:
                SLog.e("iv_mine_msg");
                break;
            case R.id.tv_home_mine_msg_count:
                SLog.e("tv_home_mine_msg_count");
                break;
            case R.id.rLayout_home_mine_top:
                SLog.e("rLayout_home_mine_top");
                break;
//            case R.id.home_mine_item_friend:
//                SLog.e("home_mine_item_friend");
//                friendItem(view);
//                break;
//            case R.id.home_mine_item_share:
//                SLog.e("home_mine_item_share");
//                shareItem(view);
//                break;
//            case R.id.home_mine_item_help:
//                SLog.e("home_mine_item_help");
//                helpItem(view);
//                break;
//            case R.id.home_mine_item_settings:
//                SLog.e("home_mine_item_settings");
//                settingsItem(view);
//                break;
        }
    }


    private void setLinstener() {
        homeMineItemFriend.setOnClickListener(this);
        homeMineItemHelp.setOnClickListener(this);
        homeMineItemSettings.setOnClickListener(this);
        homeMineItemShare.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.home_mine_item_friend:
                SLog.e("home_mine_item_friend");
                friendItem(view);
                break;
            case R.id.home_mine_item_share:
                SLog.e("home_mine_item_share");
                shareItem(view);
                break;
            case R.id.home_mine_item_help:
                SLog.e("home_mine_item_help");
                helpItem(view);
                break;
            case R.id.home_mine_item_settings:
                SLog.e("home_mine_item_settings");
                settingsItem(view);
                break;
        }
    }
}
