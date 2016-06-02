package com.cylan.jiafeigou.n.view.home;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
import com.cylan.jiafeigou.n.view.login.LoginFrament;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.ImageViewTip;
import com.cylan.sdkjni.JfgCmd;
import com.cylan.utils.ArrayUtils;
import com.cylan.utils.BitmapUtil;
import com.cylan.utils.FastBlurUtil;
import com.readystatesoftware.viewbadger.BadgeView;
import com.superlog.SLog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class HomeMineFragment extends Fragment
        implements HomeMineContract.View {

    private static final String TAG = "HomeMineFragment";

    @BindView(R.id.tv_home_mine_portrait)
    TextView tvMinePortrait;
    @BindView(R.id.iv_home_mine_msg)
    TextView ivMsg;

    @BindView(R.id.rLayout_home_mine_top)
    RelativeLayout rLayout;


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
//        BadgeView badgeView = new BadgeView(getContext(), ivMsg);
//        badgeView.setText("2");
//        badgeView.setTextSize(10);
//        badgeView.setBadgePosition(BadgeView.POSITION_TOP_RIGHT);
//        badgeView.show();
        testBlurBackground();
        return view;
    }

    private void testBlurBackground() {
        long time = System.currentTimeMillis();
        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.bg_mine_top_defult_background);
        bm = FastBlurUtil.blur(bm, 8, 10);
        rLayout.setBackground(new BitmapDrawable(getResources(), bm));
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

    @OnClick(R.id.iv_home_mine_msg)
    public void onClickMsg() {
        if (needStartLoginFragment()) return;
        ToastUtil.showToast(getContext(), "xiao xi");
    }

    @OnClick(R.id.tv_home_mine_portrait)
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
        LoginFrament frament = LoginFrament.newInstance(null);
        ActivityUtils.addFragmentToActivity(getActivity().getSupportFragmentManager(),
                frament, R.id.rLayout_new_home_container);
    }

    @OnClick(R.id.home_mine_item_share)
    public void onClickShareItem(View view) {
        if (needStartLoginFragment()) return;
        SLog.i("It's Login,can do something!");
    }

    @OnClick(R.id.home_mine_item_settings)
    public void onClickSettingsItem(View view) {
        if (needStartLoginFragment()) return;
        SLog.i("It's Login,can do something!");
    }

    @OnClick(R.id.home_mine_item_help)
    public void onClickHelpItem(View view) {
        if (needStartLoginFragment()) return;
        SLog.i("It's Login,can do something!");
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
        tvMinePortrait.setText(url);
    }


    private boolean needStartLoginFragment() {
        if (!JfgCmd.getJfgCmd(getContext()).isLogined) {
//            ToastUtil.showToast(getContext(), "Not login.....");
//            SLog.i("Not login.....");
//            LoginFrament fragment = LoginFrament.newInstance(null);
//            FragmentManager manager = getFragmentManager();
//            FragmentTransaction transaction = manager.beginTransaction();
//            transaction.hide(this);
//            transaction.add(R.id.rLayout_new_home_container, fragment, "login");
//            transaction.commit();
            return true;
        }
        return false;
    }
}
