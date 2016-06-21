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
import com.cylan.sdkjni.JfgCmd;
import com.cylan.utils.BitmapUtil;
import com.cylan.utils.FastBlurUtil;
import com.superlog.SLog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class HomeMineFragment extends Fragment
        implements HomeMineContract.View {


    @BindView(R.id.iv_home_mine_portrait)
    ImageView ivMinePortrait;

    @BindView(R.id.tv_home_mine_nick)
    TextView tvNick;

    @BindView(R.id.iv_mine_msg)
    ImageView ivMsg;

    @BindView(R.id.rLayout_home_mine_top)
    RelativeLayout rLayout;

    @BindView(R.id.tv_home_mine_msg_count)
    TextView tvUnReadMsgCount;


    private HomeMineContract.Presenter presenter;

    public static HomeMineFragment newInstance(Bundle bundle) {
        HomeMineFragment fragment = new HomeMineFragment();
        fragment.setArguments(bundle);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle;
        if (getArguments() != null) {
            bundle = getArguments();
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home_mine, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewUtils.setViewMarginStatusBar(ivMsg);
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
        ivMinePortrait.setImageDrawable(new BitmapDrawable(getResources(), b));
        b = FastBlurUtil.blur(b, 20, 2);
        rLayout.setBackgroundDrawable(new BitmapDrawable(getResources(), b));
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


    @OnClick(R.id.iv_home_mine_portrait)
    public void onClickPortrait() {
        if (needStartLoginFragment()) return;
        ToastUtil.showToast(getContext(), "推荐fragment");
        AccountInfoFragment fragment = (AccountInfoFragment) AccountInfoFragment.getInstance();
        ActivityUtils.addFragmentToActivity(getActivity().getSupportFragmentManager(),
                fragment, R.id.rLayout_new_home_container, 0);
        new AccountInfoPresenterImpl(fragment);
    }

    @OnClick(R.id.home_mine_item_friend)
    public void onClickFriendItem(View view) {
        if (needStartLoginFragment()) return;
        SLog.i("It's Login,can do something!");
    }

    @OnClick(R.id.home_mine_item_share)
    public void onClickShareItem(View view) {
        if (needStartLoginFragment()) return;
    }

    @OnClick(R.id.home_mine_item_settings)
    public void onClickSettingsItem(View view) {
        if (needStartLoginFragment()) return;
    }

    @OnClick(R.id.home_mine_item_help)
    public void onClickHelpItem(View view) {
        if (needStartLoginFragment()) return;
    }

    @OnClick(R.id.rLayout_home_mine_top)
    public void onClickblurPic(View view) {
        if (needStartLoginFragment()) return;
    }

    @Override
    public void setPresenter(HomeMineContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void onPortraitUpdate(String url) {
        if (getActivity() != null) {
            testBlurBackground(R.drawable.clouds);
            tvUnReadMsgCount.post(new Runnable() {
                @Override
                public void run() {
                    tvUnReadMsgCount.setText("99+");
                    tvUnReadMsgCount.setBackgroundResource(R.drawable.shape_mine_msg_count_rectangle);
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
}
