package com.cylan.jiafeigou.n.view.home;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.cache.LogState;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.home.HomePageListContract;
import com.cylan.jiafeigou.n.mvp.impl.home.HomePageListPresenterImpl;
import com.cylan.jiafeigou.n.view.activity.BindDeviceActivity;
import com.cylan.jiafeigou.n.view.activity.CameraLiveActivity;
import com.cylan.jiafeigou.n.view.activity.NeedLoginActivity;
import com.cylan.jiafeigou.n.view.adapter.HomePageListAdapter;
import com.cylan.jiafeigou.n.view.bell.DoorBellHomeActivity;
import com.cylan.jiafeigou.n.view.panorama.PanoramaCameraActivity;
import com.cylan.jiafeigou.support.block.log.PerformanceUtils;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.superadapter.OnItemClickListener;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.DisableAppBarLayoutBehavior;
import com.cylan.jiafeigou.widget.dialog.BaseDialog;
import com.cylan.jiafeigou.widget.dialog.SimpleDialogFragment;
import com.cylan.jiafeigou.widget.pop.RelativePopupWindow;
import com.cylan.jiafeigou.widget.pop.SimplePopupWindow;
import com.cylan.jiafeigou.widget.wave.SuperWaveView;
import com.google.gson.Gson;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class HomePageListFragmentExt extends IBaseFragment<HomePageListContract.Presenter> implements
        AppBarLayout.OnOffsetChangedListener,
        HomePageListContract.View, SwipeRefreshLayout.OnRefreshListener,
        OnItemClickListener, BaseDialog.BaseDialogAction {

    @BindView(R.id.srLayout_home_page_container)
    SwipeRefreshLayout srLayoutMainContentHolder;
    @BindView(R.id.imgV_add_devices)
    ImageView imgBtnAddDevices;
    @BindView(R.id.rV_devices_list)
    RecyclerView rVDevicesList;//设备列表
    @BindView(R.id.vWaveAnimation)
    SuperWaveView vWaveAnimation;

    @BindView(R.id.tvHeaderLastTitle)
    TextView tvHeaderLastTitle;
    //不是长时间需要,用软引用.
    @BindView(R.id.lLayout_home_greet)
    LinearLayout lLayoutHomeGreet;
    @BindView(R.id.fLayout_home_page_list_header_container)
    FrameLayout fLayoutHomeHeaderContainer;
    @BindView(R.id.tvHeaderNickName)
    TextView tvHeaderNickName;
    @BindView(R.id.tvHeaderPoet)
    TextView tvHeaderPoet;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.appbar)
    AppBarLayout appbar;
    @BindView(R.id.rLayout_network_banner)
    RelativeLayout badNetworkBanner;
    @BindView(R.id.lLayout_home_page_list_empty_view)
    LinearLayout emptyViewState;
    @BindView(R.id.fLayout_header_bg)
    FrameLayout fLayoutHeaderBg;
    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbar;
    private HomePageListAdapter homePageListAdapter;

    public static HomePageListFragmentExt newInstance(Bundle bundle) {
        HomePageListFragmentExt fragment = new HomePageListFragmentExt();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.basePresenter = new HomePageListPresenterImpl(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (basePresenter != null) {
            basePresenter.fetchDeviceList(false);
            basePresenter.checkClientUpdate();
        } else AppLogger.e("presenter is null");
        need2ShowUseCase();
    }

    private void need2ShowUseCase() {
        boolean show = PreferencesUtils.getBoolean(JConstant.NEED_SHOW_BIND_USE_CASE, true);
        if (show) {
            PreferencesUtils.putBoolean(JConstant.NEED_SHOW_BIND_USE_CASE, false);
            imgBtnAddDevices.post(() -> {
                SimplePopupWindow popupWindow = new SimplePopupWindow(getActivity(), R.drawable.add_device_bg_tips, R.string.Tap1_Add_Tips);
                popupWindow.showOnAnchor(imgBtnAddDevices, RelativePopupWindow.VerticalPosition.BELOW,
                        RelativePopupWindow.HorizontalPosition.ALIGN_RIGHT, (int) getResources().getDimension(R.dimen.x10), 0);
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        initWaveAnimation();
        onTimeTick(JFGRules.getTimeRule());
        onNetworkChanged(NetUtils.getJfgNetType(ContextUtils.getContext()) != 0);
        PerformanceUtils.stopTrace("appStart0");
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        homePageListAdapter = new HomePageListAdapter(getContext(), null, null);
        homePageListAdapter.setOnItemClickListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home_page_list_ext, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //添加Handler
        homePageListAdapter.clear();
        appbar.addOnOffsetChangedListener(this);
        srLayoutMainContentHolder.setOnRefreshListener(this);
        enableNestedScroll();
        initProgressBarColor();
        initListAdapter();
        initSomeViewMargin();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //只有app退出后，被调用。
        if (basePresenter != null) {
            basePresenter.stop();
            basePresenter = null;
        }
    }

    private void initListAdapter() {
        rVDevicesList.setLayoutManager(new LinearLayoutManager(getContext()) {
            @Override
            public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
                try {
                    super.onLayoutChildren(recycler, state);
                } catch (IndexOutOfBoundsException e) {
                    AppLogger.e("homepageList" + e.getMessage());
                }
            }
        });
        rVDevicesList.setAdapter(homePageListAdapter);
    }

    private void initSomeViewMargin() {
        ViewUtils.setFitsSystemWindowsCompat(fLayoutHomeHeaderContainer);
        ViewUtils.setViewMarginStatusBar(lLayoutHomeGreet);
        ViewUtils.setViewMarginStatusBar(toolbar);
    }

    /**
     * 水波纹动画初始化
     */
    private void initWaveAnimation() {
        vWaveAnimation.postDelayed(new Runnable() {
            @Override
            public void run() {
                vWaveAnimation.startAnimation();
            }
        }, 500);
    }

    /**
     * 初始化,progressBar的位置.
     */
    private void initProgressBarColor() {
        rVDevicesList.post(new Runnable() {
            @Override
            public void run() {
                srLayoutMainContentHolder.setColorSchemeColors(Color.parseColor("#36BDFF"));
            }
        });
    }

    @OnClick(R.id.imgV_add_devices)
    void onClickAddDevice() {
        if (BaseApplication.getAppComponent().getSourceManager().getLoginState() != LogState.STATE_ACCOUNT_ON) {
            ((NeedLoginActivity) getActivity()).signInFirst(null);
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getActivity().startActivity(new Intent(getActivity(), BindDeviceActivity.class),
                    ActivityOptionsCompat.makeCustomAnimation(getContext(),
                            R.anim.slide_in_right, R.anim.slide_out_left).toBundle());
        } else {
            getActivity().startActivity(new Intent(getActivity(), BindDeviceActivity.class));
        }
    }

    @OnClick(R.id.imgv_close_network_banner)
    public void onClickCloseBanner() {
        badNetworkBanner.setVisibility(View.GONE);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (vWaveAnimation != null) vWaveAnimation.stopAnimation();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void setPresenter(HomePageListContract.Presenter basePresenter) {
        this.basePresenter = basePresenter;
    }

    @UiThread
    @Override
    public void onItemsInsert(List<Device> resultList) {
        homePageListAdapter.clear();//暴力刷新,设备没几个,没关系.
        if (resultList != null && resultList.size() > 0)
            homePageListAdapter.addAll(resultList);
        emptyViewState.setVisibility(homePageListAdapter.getCount() > 0 ? View.GONE : View.VISIBLE);
        onRefreshFinish();
        Log.d("onItemsInsert", "onItemsInsert:" + resultList);
        enableNestedScroll();
    }

    private void enableNestedScroll() {
        boolean enable = homePageListAdapter.getCount() > 4;
        if (appbar.getLayoutParams() != null) {
            CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) appbar.getLayoutParams();
            if (enable) {
                if (!(layoutParams.getBehavior() instanceof AppBarLayout.Behavior))
                    layoutParams.setBehavior(new AppBarLayout.Behavior());
            } else {
                if (!(layoutParams.getBehavior() instanceof DisableAppBarLayoutBehavior))
                    layoutParams.setBehavior(new DisableAppBarLayoutBehavior());
            }
        }
        if (srLayoutMainContentHolder.getLayoutParams() != null) {
            CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) srLayoutMainContentHolder.getLayoutParams();
            CoordinatorLayout.Behavior behavior = layoutParams.getBehavior();
            if (behavior != null && behavior instanceof DisableAppBarLayoutBehavior) {
                ((DisableAppBarLayoutBehavior) behavior).setEnabled(enable);
                Log.d("what", "what 1" + layoutParams.getBehavior() + " ,enable:" + enable);
            }
        }
    }

    @Override
    public void onItemUpdate(int index) {
        if (homePageListAdapter != null
                && MiscUtils.isInRange(0, homePageListAdapter.getCount(), index)) {
            homePageListAdapter.notifyItemChanged(index);
        }
    }

    @Override
    public void onItemDelete(int index) {

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        PreferencesUtils.putBoolean(JConstant.IS_FIRST_PAGE_VIS, isVisibleToUser);
        if (isVisibleToUser && isResumed() && getActivity() != null) {
            srLayoutMainContentHolder.setRefreshing(false);
        }
    }

    @Override
    public void onAccountUpdate(JFGAccount greetBean) {
        tvHeaderNickName.postDelayed(() -> {
            tvHeaderNickName.setText(String.format("Hi %s", getBeautifulAlias(greetBean)));
            tvHeaderPoet.setText(JFGRules.getTimeRule() == JFGRules.RULE_DAY_TIME ? getString(R.string.Tap1_Index_DayGreetings)
                    : getString(R.string.Tap1_Index_NightGreetings));
            tvHeaderNickName.requestLayout();
        }, 100);
        onNetworkChanged(NetUtils.getJfgNetType(ContextUtils.getContext()) != 0);
        AppLogger.d("JFGAccount: " + new Gson().toJson(greetBean));
    }

    /**
     * 根据规则截取字符串
     *
     * @param account
     * @return
     */
    private String getBeautifulAlias(JFGAccount account) {
        if (BaseApplication.getAppComponent().getSourceManager().getLoginState() != LogState.STATE_ACCOUNT_ON) return "";
        if (account == null) return "";
        String temp = TextUtils.isEmpty(account.getAlias()) ? account.getAccount() : account.getAlias();
        return "," + MiscUtils.getBeautifulString(temp, 8);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onTimeTick(final int dayTime) {
        //需要优化
        boolean day = dayTime == JFGRules.RULE_DAY_TIME;
        int count = fLayoutHeaderBg.getChildCount();
        if (count == 1) {
            if (day && fLayoutHeaderBg.getChildAt(0).getId() == R.id.rLayout_home_header_night_bg) {
                fLayoutHeaderBg.removeViewAt(0);
                View v = LayoutInflater.from(getContext()).inflate(R.layout.layout_home_head_day, null, false);
                fLayoutHeaderBg.addView(v);
            }
            if (!day && fLayoutHeaderBg.getChildAt(0).getId() == R.id.rLayout_home_header_day_bg) {
                fLayoutHeaderBg.removeViewAt(0);
                View v = LayoutInflater.from(getContext()).inflate(R.layout.layout_home_head_night, null, false);
                fLayoutHeaderBg.addView(v);
            }
        }
        AppLogger.i("time tick: " + day + " " + (count == 1));
    }

    @Override
    public void onLoginState(boolean state) {
        if (!state) {
            onRefreshFinish();
//            Toast.makeText(getContext(), getString(R.string.UNLOGIN), Toast.LENGTH_SHORT).show();
        } else {
            //setDevice online view
        }
    }

    @Override
    public void onRefreshFinish() {
        srLayoutMainContentHolder.postDelayed(() -> {
            if (srLayoutMainContentHolder.isRefreshing()) {
                srLayoutMainContentHolder.setRefreshing(false);
                srLayoutMainContentHolder.clearAnimation();
                AppLogger.d("stop refreshing ui");
            }
        }, 1500);
    }

    @Override
    public void onNetworkChanged(boolean connected) {
        badNetworkBanner.setVisibility(connected ? View.GONE : View.VISIBLE);
        srLayoutMainContentHolder.setEnabled(connected);
        if (!connected) srLayoutMainContentHolder.setRefreshing(false);
    }

    @Override
    public void autoLoginTip(int code) {
        if (code == JError.LoginTimeOut) {
            ToastUtil.showNegativeToast(getString(R.string.Clear_Sdcard_tips5));
        } else if (code == JError.NoNet) {
            ToastUtil.showNegativeToast(getString(R.string.GLOBAL_NO_NETWORK));
        }
    }

    @Override
    public void clientUpdateDialog(String apkPath) {
        Fragment f = getFragmentManager().findFragmentByTag("update");
        if (f == null){
            Bundle bundle = new Bundle();
            bundle.putString(BaseDialog.KEY_TITLE, getString(R.string.UPGRADE));
            bundle.putString(SimpleDialogFragment.KEY_LEFT_CONTENT, getString(R.string.CANCEL));
            bundle.putString(SimpleDialogFragment.KEY_RIGHT_CONTENT, getString(R.string.UPGRADE_NOW));
            bundle.putBoolean(SimpleDialogFragment.KEY_TOUCH_OUT_SIDE_DISMISS, false);
            SimpleDialogFragment dialogFragment = SimpleDialogFragment.newInstance(bundle);
            dialogFragment.setValue(apkPath);
            dialogFragment.setAction(this);
            dialogFragment.show(this.getFragmentManager(), "update");
        }
    }

    @Override
    public void onRefresh() {
        //不使用post,因为会泄露
        srLayoutMainContentHolder.post(() -> srLayoutMainContentHolder.setRefreshing(true));
//        enableNestedScroll();
        Log.d("refresh", "refresh:initSubscription ");
        if (basePresenter != null)
            basePresenter.fetchDeviceList(true);
    }


    private float preRatio = -1;

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        final float ratio = (appbar.getTotalScrollRange() + verticalOffset) * 1.0f
                / appbar.getTotalScrollRange();
        if (preRatio == ratio) return;
        preRatio = ratio;
        Log.d("HomePageListFragmentExt", "HomePageListFragmentExt: " + verticalOffset);
        updateWaveViewAmplitude(ratio);
    }

    /**
     * 刷新水波纹的振幅
     *
     * @param ratio
     */
    private void updateWaveViewAmplitude(final float ratio) {
        if (vWaveAnimation.getAmplitudeRatio() != ratio && ratio >= 0.0f && ratio < 1.0f) {
            vWaveAnimation.setAmplitudeRatio(ratio);
//            AppLogger.d("ratio: " + ratio);

        }
        float alpha = 1.0f - ratio;
        if (tvHeaderLastTitle.getAlpha() != alpha) {
            if (alpha < 0.02f)
                alpha = 0;//设定一个阀值,以免掉帧导致回调不及时
            tvHeaderLastTitle.setAlpha(alpha);
        }
    }

    @Override
    public void onItemClick(View itemView, int viewType, int position) {
        Device device = homePageListAdapter.getItem(position);
        if (device != null && !TextUtils.isEmpty(device.uuid)) {
            Bundle bundle = new Bundle();
            bundle.putString(JConstant.KEY_DEVICE_ITEM_UUID, device.uuid);
            if (JFGRules.isCamera(device.pid)) {
                startActivity(new Intent(getActivity(), CameraLiveActivity.class)
                        .putExtra(JConstant.KEY_DEVICE_ITEM_UUID, device.uuid));
            } else if (JFGRules.isBell(device.pid)) {
                startActivity(new Intent(getActivity(), DoorBellHomeActivity.class)
                        .putExtra(JConstant.KEY_DEVICE_ITEM_UUID, device.uuid).putExtra("HasNewMsg", true));
            } else if (JFGRules.isVRCam(device.pid)) {
                startActivity(new Intent(getActivity(), PanoramaCameraActivity.class).putExtra(JConstant.KEY_DEVICE_ITEM_UUID, device.uuid));
            }
        } else {
            homePageListAdapter.notifyDataSetChanged();
            AppLogger.e("dis match pid pid: " + position);
        }
    }

    @Override
    public void onDialogAction(int id, Object value) {
        if (id == R.id.tv_dialog_btn_right) {
            if (value != null) {
                String apkPath = (String) value;
                File apkFile = new File(apkPath);
                Uri uri = Uri.fromFile(apkFile);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, "application/vnd.android.package-archive");
                startActivity(intent);
            }
        }

        PreferencesUtils.putBoolean(JConstant.CLIENT_UPDATAE_TAB, true);
        PreferencesUtils.putLong(JConstant.CLIENT_UPDATAE_TIME_TAB, System.currentTimeMillis());
    }
}
