package com.cylan.jiafeigou.n.view.home;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.design.widget.AppBarLayout;
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
import com.cylan.jiafeigou.cache.db.module.DPEntity;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.cache.db.view.DBAction;
import com.cylan.jiafeigou.misc.AlertDialogManager;
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
import com.cylan.jiafeigou.n.view.adapter.item.HomeItem;
import com.cylan.jiafeigou.n.view.panorama.PanoramaCameraActivity;
import com.cylan.jiafeigou.support.block.log.PerformanceUtils;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.Functions;
import com.cylan.jiafeigou.utils.ListUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.DisableAppBarLayoutBehavior;
import com.cylan.jiafeigou.widget.ImageViewTip;
import com.cylan.jiafeigou.widget.LoadingDialog;
import com.cylan.jiafeigou.widget.dialog.BaseDialog;
import com.cylan.jiafeigou.widget.dialog.SimpleDialogFragment;
import com.cylan.jiafeigou.widget.wave.SuperWaveView;
import com.google.gson.Gson;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.commons.utils.DiffCallbackImpl;
import com.mikepenz.fastadapter.commons.utils.FastAdapterDiffUtil;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class HomePageListFragmentExt extends IBaseFragment<HomePageListContract.Presenter> implements
        AppBarLayout.OnOffsetChangedListener,
        HomePageListContract.View, SwipeRefreshLayout.OnRefreshListener,
        FastAdapter.OnClickListener<HomeItem>, FastAdapter.OnLongClickListener<HomeItem>, BaseDialog.BaseDialogAction {

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
    private ItemAdapter<HomeItem> mItemAdapter;
    private boolean refreshFinish = true;
    private AnimatorSet set;

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
    public void onStart() {
        super.onStart();
        if (basePresenter != null) {
//            PreferencesUtils.putBoolean(JConstant.IS_FIRST_PAGE_VIS, true);
//            srLayoutMainContentHolder.setRefreshing(false);
//            enableNestedScroll();
//            if (isVisibleToUser && isResumed() && getActivity() != null) {
//            }        appbar.addOnOffsetChangedListener(this);
            srLayoutMainContentHolder.setOnRefreshListener(this);
            onItemsRsp(BaseApplication.getAppComponent().getSourceManager().getAllDevice());
            updateAccount.run();
            basePresenter.fetchDeviceList(false);
        } else {
            AppLogger.e("presenter is null");
        }
    }

    private void need2ShowUseCase() {
        boolean showUserCase = PreferencesUtils.getBoolean(JConstant.NEED_SHOW_BIND_USE_CASE, true);
        boolean showTipAnimation = PreferencesUtils.getBoolean(JConstant.NEED_SHOW_BIND_ANIMATION, true);
//        JFGSourceManager sourceManager = BaseApplication.getAppComponent().getSourceManager();
//        Account account = sourceManager.getAccount();
//        if (account != null && account.isAvailable()) {
        if (showUserCase) {
            PreferencesUtils.putBoolean(JConstant.NEED_SHOW_BIND_USE_CASE, false);
            HomePageCoverFragment fragment = new HomePageCoverFragment();
            imgBtnAddDevices.post(() -> getActivity().getSupportFragmentManager()
                    .beginTransaction().add(android.R.id.content, fragment)
                    .addToBackStack("HomePageCoverFragment")
                    .commitAllowingStateLoss());
        }

        if (showTipAnimation) {
            // TODO: 2017/7/11 先放着
            imgBtnAddDevices.setImageResource(R.drawable.icon_add_to);
            if (set != null) {
                set.cancel();
            }
            set = new AnimatorSet();
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(imgBtnAddDevices, "scaleX", 1.0f, 1.2f, 1.0f);
            scaleX.setDuration(2000);
            scaleX.setRepeatMode(ValueAnimator.REVERSE);
            scaleX.setRepeatCount(ValueAnimator.INFINITE);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(imgBtnAddDevices, "scaleY", 1.0f, 1.2f, 1.0f);
            scaleY.setDuration(2000);
            scaleY.setRepeatMode(ValueAnimator.REVERSE);
            scaleY.setRepeatCount(ValueAnimator.INFINITE);
            ObjectAnimator alpha = ObjectAnimator.ofFloat(imgBtnAddDevices, "alpha", 1.0f, 0.5f, 1.0f);
            alpha.setDuration(2000);
            alpha.setRepeatMode(ValueAnimator.REVERSE);
            alpha.setRepeatCount(ValueAnimator.INFINITE);
            set.playTogether(scaleX, scaleY, alpha);
            set.start();
        } else {
            imgBtnAddDevices.setScaleY(1);
            imgBtnAddDevices.setScaleX(1);
            imgBtnAddDevices.setImageResource(R.drawable.btn_common_add);
        }
//        }

    }


    @Override
    public void onResume() {
        super.onResume();
        initWaveAnimation();
        need2ShowUseCase();
        onTimeTick(JFGRules.getTimeRule());
        PerformanceUtils.stopTrace("NewHomeActivityStart");
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        PerformanceUtils.startTrace("homePage layout");
        View view = inflater.inflate(R.layout.fragment_home_page_list_ext, container, false);
        PerformanceUtils.stopTrace("homePage layout");
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //添加Handler

        initListAdapter();
        initProgressBarColor();
        initSomeViewMargin();

//        view.post(updateAccount);
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
                    AppLogger.w("IndexOutOfBoundsException");
                    rVDevicesList.postDelayed(() -> mItemAdapter.notifyDataSetChanged(), 500);
                }
            }
        });
        mItemAdapter = new ItemAdapter<>();
        FastAdapter<HomeItem> itemFastAdapter = new FastAdapter<>();
        itemFastAdapter.withOnClickListener(this);
        itemFastAdapter.withOnLongClickListener(this);
        mItemAdapter.withComparator(null);
        rVDevicesList.setAdapter(mItemAdapter.wrap(itemFastAdapter));
        enableNestedScroll();
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
        vWaveAnimation.postDelayed(() -> vWaveAnimation.startAnimation(), 500);
    }

    /**
     * 初始化,progressBar的位置.
     */
    private void initProgressBarColor() {
        srLayoutMainContentHolder.setColorSchemeColors(rVDevicesList.getContext().getResources().getColor(R.color.color_36BDFF));
    }

    @OnClick(R.id.imgV_add_devices)
    void onClickAddDevice() {
        if (BaseApplication.getAppComponent().getSourceManager().getLoginState() != LogState.STATE_ACCOUNT_ON) {
            ((NeedLoginActivity) getActivity()).signInFirst(null);
            return;
        }
        PreferencesUtils.putBoolean(JConstant.NEED_SHOW_BIND_ANIMATION, false);
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
        if (set != null) {
            set.cancel();
            set = null;
            imgBtnAddDevices.setAlpha(1.0f);
            imgBtnAddDevices.setScaleX(1.0f);
            imgBtnAddDevices.setScaleY(1.0f);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (vWaveAnimation != null) {
            vWaveAnimation.stopAnimation();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void setPresenter(HomePageListContract.Presenter basePresenter) {
        this.basePresenter = basePresenter;
    }

    private List<Device> resultList;

    private void updateImmediately() {
        try {
            if (getView() != null) {
                //mItemAdapter.clear();//别暴力刷新,存在闪烁.不推荐.
                List<HomeItem> uiList = mItemAdapter.getAdapterItems();
                List<HomeItem> newList = MiscUtils.getHomeItemListFromDevice(resultList);
                List<HomeItem> toRemoveList = ListUtils.getDiff(uiList, newList);
                if (toRemoveList != null) {
                    for (HomeItem item : toRemoveList) {
                        int index = mItemAdapter.getAdapterItems().indexOf(item);
                        if (index == -1) {
                            continue;
                        }
                        mItemAdapter.remove(index);
                        Log.d("xxxxx", "removexxxx:" + index);
                    }
                }
                int size = ListUtils.getSize(resultList);
                if (size > 0) {
                    for (int i = 0; i < size; i++) {
                        int count = mItemAdapter.getAdapterItemCount();
                        if (i < count) {
                            mItemAdapter.getAdapterItems().set(i, newList.get(i));
                            mItemAdapter.notifyItemChanged(i);
                        } else {
                            //应该一次性加载
                            mItemAdapter.add(newList.get(i));
                            Log.d("xxxxx", "xxxx:" + count);
                        }
                    }
                }
                onRefreshFinish();
//                enableNestedScroll();
                emptyViewState.setVisibility(mItemAdapter.getItemCount() > 0 ? View.GONE : View.VISIBLE);
                Log.d("onItemsRsp", "onItemsRsp:" + resultList);
            }
        } catch (Exception e) {
            AppLogger.e("err:" + MiscUtils.getErr(e));
        }
    }

    private Runnable runnable = this::updateImmediately;

    @UiThread
    @Override
    public void onItemsRsp(List<Device> resultList) {
        refreshFinish = true;
//        this.resultList = resultList;
//        if (!getUserVisibleHint()) return;
        ItemAdapter<HomeItem> set = FastAdapterDiffUtil.set(mItemAdapter, MiscUtils.getHomeItemListFromDevice(resultList), new DiffCallbackImpl<>(), true);
        onRefreshFinish();
        emptyViewState.setVisibility(mItemAdapter.getItemCount() > 0 ? View.GONE : View.VISIBLE);
    }

    private void enableNestedScroll() {
        if (getView() != null) {
            getView().post(() -> {
//            boolean enable = mItemAdapter.getItemCount() > 4;
                if (appbar.getLayoutParams() != null) {
                    CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) appbar.getLayoutParams();
                    if (mItemAdapter != null && mItemAdapter.getItemCount() > 4) {
                        CoordinatorLayout.Behavior behavior = layoutParams.getBehavior();
                        if (!(behavior instanceof AppBarLayout.Behavior)) {
                            layoutParams.setBehavior(new AppBarLayout.Behavior());
                        } else {
                            ((AppBarLayout.Behavior) behavior).setDragCallback(new AppBarLayout.Behavior.DragCallback() {
                                @Override
                                public boolean canDrag(@NonNull AppBarLayout appBarLayout) {
                                    return !srLayoutMainContentHolder.isRefreshing();
                                }
                            });
                        }
                    } else {
                        if (!(layoutParams.getBehavior() instanceof DisableAppBarLayoutBehavior)) {
                            layoutParams.setBehavior(new DisableAppBarLayoutBehavior());
                        }
                    }
                }
                if (srLayoutMainContentHolder.getLayoutParams() != null) {
                    CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) srLayoutMainContentHolder.getLayoutParams();
                    CoordinatorLayout.Behavior behavior = layoutParams.getBehavior();
                    if (behavior != null && behavior instanceof DisableAppBarLayoutBehavior) {
                        ((DisableAppBarLayoutBehavior) behavior).setCanDragChecker(() ->
                                mItemAdapter != null && mItemAdapter.getItemCount() > 4 && !srLayoutMainContentHolder.isRefreshing());
                        Log.d("what", "what 1," + layoutParams.getBehavior() + " ,enable:" + (mItemAdapter != null && mItemAdapter.getItemCount() > 4));
                    }
                }
            });
        }
    }

    @Override
    public void onItemUpdate(int index) {
        if (mItemAdapter != null
                && MiscUtils.isInRange(0, mItemAdapter.getItemCount(), index)) {
            mItemAdapter.notifyItemChanged(index);
        }
    }

    @Override
    public void onItemDelete(int index) {

    }

    @Override
    public List<HomeItem> getUuidList() {
        return mItemAdapter == null ? null : mItemAdapter.getAdapterItems();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        PreferencesUtils.putBoolean(JConstant.IS_FIRST_PAGE_VIS, isVisibleToUser);
        if (isVisibleToUser && isResumed() && getActivity() != null) {
            srLayoutMainContentHolder.setRefreshing(false);
            enableNestedScroll();
        }
    }


    private Runnable updateAccount = new Runnable() {
        @Override
        public void run() {
//            if (!isAdded()) return;

            JFGAccount greetBean = BaseApplication.getAppComponent().getSourceManager().getJFGAccount();
            tvHeaderNickName.setText(String.format("Hi %s", getBeautifulAlias(greetBean)));
            tvHeaderPoet.setText(JFGRules.getTimeRule() == JFGRules.RULE_DAY_TIME ? getString(R.string.Tap1_Index_DayGreetings)
                    : getString(R.string.Tap1_Index_NightGreetings));
            tvHeaderNickName.requestLayout();
            Functions.INSTANCE.runOnDebug(() -> AppLogger.w("JFGAccount: " + new Gson().toJson(greetBean)));
        }
    };

    @Override
    public void onAccountUpdate(JFGAccount greetBean) {
        if (isResumed() && getView() != null && getUserVisibleHint()) {
            getView().removeCallbacks(updateAccount);
            getView().post(updateAccount);
        }
    }

    /**
     * 根据规则截取字符串
     *
     * @param account
     * @return
     */
    private String getBeautifulAlias(JFGAccount account) {
        if (BaseApplication.getAppComponent().getSourceManager().getLoginState() != LogState.STATE_ACCOUNT_ON) {
            return "";
        }
        if (account == null) {
            return "";
        }
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
        AppLogger.w("time tick: " + day + " " + (count == 1));
    }

    @Override
    public void onLoginState(boolean state) {
        onRefreshFinish();
        if (!state) {
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
                enableNestedScroll();
                AppLogger.w("stop refreshing ui");
            }

        }, 1500);
    }

    @Override
    public void onNetworkChanged(boolean connected) {
        badNetworkBanner.setVisibility(connected ? View.GONE : View.VISIBLE);
        srLayoutMainContentHolder.setEnabled(connected);
        if (!connected) {
            srLayoutMainContentHolder.setRefreshing(false);
        }
    }

    @Override
    public void autoLoginTip(int code) {
        if (code == JError.ERROR_LOGIN_TIME_OUT) {
            if (BaseApplication.getAppComponent().getSourceManager().isOnline()) {
                ToastUtil.showNegativeToast(getString(R.string.Clear_Sdcard_tips5));
            } else {
                ToastUtil.showNegativeToast(getString(R.string.GLOBAL_NO_NETWORK));
            }
        }
    }

    @Override
    public void clientUpdateDialog(String apkPath) {
        Fragment f = getFragmentManager().findFragmentByTag("update");
        if (f == null) {
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
    public void onRefreshDeviceList() {
        mItemAdapter.notifyDataSetChanged();
    }

    @Override
    public void onRefresh() {
        //不使用post,因为会泄露

//        List<PropertyItem> all = BaseApplication.getPropertyItemBox().getAll();
//        Log.e("AAAA", all.toString());
        if (refreshFinish) {
            srLayoutMainContentHolder.post(() -> srLayoutMainContentHolder.setRefreshing(true));

            Log.d("fetch", "fetch:initSubscription ");
            if (basePresenter != null) {
                basePresenter.fetchDeviceList(true);
            }
        }

//        basePresenter.refreshDevices();

        refreshFinish = false;
    }


    private float preRatio = -1;

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        final float ratio = (appbar.getTotalScrollRange() + verticalOffset) * 1.0f
                / appbar.getTotalScrollRange();
        if (preRatio == ratio) {
            return;
        }
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
            if (alpha < 0.02f) {
                alpha = 0;//设定一个阀值,以免掉帧导致回调不及时
            }
            tvHeaderLastTitle.setAlpha(alpha);
        }
    }

    private void prepareNext(View itemView, int position) {
        Device device = mItemAdapter.getItem(position).getDevice();
        if (device != null && !TextUtils.isEmpty(device.uuid)) {
            Bundle bundle = new Bundle();
            bundle.putString(JConstant.KEY_DEVICE_ITEM_UUID, device.uuid);
            if (JFGRules.isPan720(device.pid)) {
                Intent intent = new Intent(getActivity(), PanoramaCameraActivity.class);
                intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, device.uuid);
                View tip = itemView.findViewById(R.id.img_device_icon);
                if (tip != null && tip instanceof ImageViewTip) {
                    if (((ImageViewTip) tip).isShowDot()) {
                        intent.putExtra(JConstant.KEY_JUMP_TO_MESSAGE, JConstant.KEY_JUMP_TO_MESSAGE);
                    }
                }
                startActivity(intent);
            } else /**if (JFGRules.isCamera(device.pid))/**/ {
                Intent in = new Intent(getActivity(), CameraLiveActivity.class);
                View tip = itemView.findViewById(R.id.img_device_icon);
                if (tip != null && tip instanceof ImageViewTip) {
                    if (((ImageViewTip) tip).isShowDot()) {
                        in.putExtra(JConstant.KEY_JUMP_TO_MESSAGE, JConstant.KEY_JUMP_TO_MESSAGE);
                    }
                }
                in.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, device.uuid);
                startActivity(in);
            }
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

    @Override
    public boolean onClick(View v, IAdapter<HomeItem> adapter, HomeItem item, int position) {
        if (position != -1) {
            prepareNext(v, position);
        } else {
            AppLogger.e("dis match position : " + position);
        }
        return true;
    }

    @Override
    public boolean onLongClick(View v, IAdapter<HomeItem> adapter, HomeItem item, int position) {
        final String alias = ((TextView) v.findViewById(R.id.tv_device_alias)).getText().toString();
        AlertDialogManager.getInstance().showDialog(getActivity(), "deleteItem",
                getString(R.string.SURE_DELETE_1, alias), getString(R.string.OK), (dialog, which) -> {
                    LoadingDialog.showLoading(getActivity(), getString(R.string.DELETEING), true);
                    Subscription subscribe = Observable.just(new DPEntity()
                            .setUuid(item.getUUid())
                            .setAction(DBAction.UNBIND))
                            .subscribeOn(Schedulers.io())
                            .observeOn(Schedulers.io())
                            .flatMap(i -> BaseApplication.getAppComponent().getTaskDispatcher().perform(i))
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(rsp -> {
                                ToastUtil.showToast(getString(R.string.DELETED_SUC));
                                if (basePresenter != null) {
                                    basePresenter.fetchDeviceList(true);
                                }
                            }, e -> {
                                ToastUtil.showToast(getString(R.string.Tips_DeleteFail));
                                AppLogger.e("err: " + MiscUtils.getErr(e));
                            }, () -> LoadingDialog.dismissLoading());
                    basePresenter.addSubscription("unbind", subscribe);
                }, getString(R.string.CANCEL), null);
        return true;
    }
}
