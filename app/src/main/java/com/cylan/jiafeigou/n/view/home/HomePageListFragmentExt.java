package com.cylan.jiafeigou.n.view.home;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.entity.jniCall.JFGDevice;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.cache.pool.GlobalDataProxy;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.home.HomePageListContract;
import com.cylan.jiafeigou.n.mvp.impl.home.HomePageListPresenterImpl;
import com.cylan.jiafeigou.n.view.activity.BindDeviceActivity;
import com.cylan.jiafeigou.n.view.activity.CameraLiveActivity;
import com.cylan.jiafeigou.n.view.activity.CloudLiveActivity;
import com.cylan.jiafeigou.n.view.activity.MagLiveActivity;
import com.cylan.jiafeigou.n.view.adapter.HomePageListAdapter;
import com.cylan.jiafeigou.n.view.bell.DoorBellHomeActivity;
import com.cylan.jiafeigou.n.view.misc.HomeEmptyView;
import com.cylan.jiafeigou.n.view.misc.IEmptyView;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.dialog.BaseDialog;
import com.cylan.jiafeigou.widget.dialog.SimpleDialogFragment;
import com.cylan.jiafeigou.widget.wave.SuperWaveView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class HomePageListFragmentExt extends IBaseFragment<HomePageListContract.Presenter> implements
        AppBarLayout.OnOffsetChangedListener,
        HomePageListContract.View, SwipeRefreshLayout.OnRefreshListener,
        HomePageListAdapter.DeviceItemClickListener,
        BaseDialog.BaseDialogAction,
        HomePageListAdapter.DeviceItemLongClickListener {

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
    WeakReference<SimpleDialogFragment> simpleDialogFragmentWeakReference;
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
    //    @BindView(R.id.collapsing_toolbar)
//    CollapsingToolbarLayout collapsingToolbar;
    @BindView(R.id.fLayout_empty_view_container)
    FrameLayout fLayoutEmptyViewContainer;
    @BindView(R.id.img_home_page_header_bg)
    ImageView imgHomePageHeaderBg;
    private HomePageListAdapter homePageListAdapter;

    private EmptyViewState emptyViewState;


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
    public void onResume() {
        super.onResume();
        initWaveAnimation();
        onTimeTick(JFGRules.getTimeRule());
        homePageListAdapter.notifyDataSetChanged();
        if (basePresenter != null) {
            basePresenter.fetchGreet();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        homePageListAdapter = new HomePageListAdapter(getContext(), null, null);
        homePageListAdapter.setDeviceItemClickListener(this);
        homePageListAdapter.setDeviceItemLongClickListener(this);
        initEmptyViewState(context);
        //需要优化.
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
        srLayoutMainContentHolder.setNestedScrollingEnabled(false);
        initProgressBarColor();
        initListAdapter();
        initSomeViewMargin();
        addEmptyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //只有app退出后，被调用。
        if (basePresenter != null) {
            basePresenter.stop();
            basePresenter.unRegisterWorker();
            basePresenter = null;
        }
    }

    private void initListAdapter() {
        rVDevicesList.setLayoutManager(new LinearLayoutManager(getContext()));
        rVDevicesList.setAdapter(homePageListAdapter);
    }

    /**
     * 添加
     */
    private void addEmptyView() {
        srLayoutMainContentHolder.post(new Runnable() {
            @Override
            public void run() {
                emptyViewState.setEmptyViewState(fLayoutEmptyViewContainer, 0);
                emptyViewState.determineEmptyViewState(homePageListAdapter.getCount());
            }
        });
    }

    /**
     * 初始化Layout,Inflation一个layout,可在attach的时候做。
     *
     * @param context
     */
    private void initEmptyViewState(Context context) {
        if (emptyViewState == null)
            emptyViewState = new EmptyViewState(context, R.layout.layout_home_page_list_empty_view);
    }

    private void initSimpleDialog() {
        if (simpleDialogFragmentWeakReference == null || simpleDialogFragmentWeakReference.get() == null) {
            simpleDialogFragmentWeakReference = new WeakReference<>(SimpleDialogFragment.newInstance(null));
            simpleDialogFragmentWeakReference.get().setAction(this);
        }
    }

    private void initSomeViewMargin() {
        ViewUtils.setFitsSystemWindowsCompat(fLayoutHomeHeaderContainer);
//        ViewUtils.setViewMarginStatusBar(imgBtnAddDevices);
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
                srLayoutMainContentHolder.setColorSchemeColors(R.color.color_36bdff);
            }
        });
    }

    @OnClick(R.id.imgV_add_devices)
    void onClickAddDevice() {
        if (!GlobalDataProxy.getInstance().isOnline()) {
            if (RxBus.getCacheInstance().hasObservers())
                RxBus.getCacheInstance().post(new RxEvent.NeedLoginEvent(null));
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            getActivity().startActivity(new Intent(getActivity(), BindDeviceActivity.class),
                    ActivityOptionsCompat.makeCustomAnimation(getContext(),
                            R.anim.slide_in_right, R.anim.slide_out_left).toBundle());
        } else {
            getActivity().startActivity(new Intent(getActivity(), BindDeviceActivity.class));
        }
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
    public void onItemsInsert(List<String> resultList) {
        srLayoutMainContentHolder.setRefreshing(false);
        if (resultList == null || resultList.size() == 0) {
            homePageListAdapter.clear();
            srLayoutMainContentHolder.setNestedScrollingEnabled(false);
            return;
        }
        homePageListAdapter.addAll(resultList);
        emptyViewState.determineEmptyViewState(homePageListAdapter.getCount());
        srLayoutMainContentHolder.setNestedScrollingEnabled(homePageListAdapter.getCount() > JFGRules.NETSTE_SCROLL_COUNT);
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
    public ArrayList<String> getUuidList() {
        return homePageListAdapter == null ? null : (ArrayList<String>) homePageListAdapter.getList();
    }

    @Override
    public void onAccountUpdate(JFGAccount greetBean) {
        tvHeaderNickName.setText(String.format("Hi,%s",
                greetBean.getAccount()));
        tvHeaderPoet.setText(JFGRules.getTimeRule() == JFGRules.RULE_DAY_TIME ? getString(R.string.Tap1_Index_DayGreetings)
                : getString(R.string.Tap1_Index_NightGreetings));
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onTimeTick(final int dayTime) {
        //需要优化
        int drawableId = dayTime == JFGRules.RULE_DAY_TIME
                ? R.drawable.bg_home_title_daytime : R.drawable.bg_home_title_night;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            imgHomePageHeaderBg.setBackground(getResources().getDrawable(drawableId, null));
//        } else {
//            imgHomePageHeaderBg.setBackground(getResources().getDrawable(drawableId));
//        }
        Glide.with(this)
                .load(drawableId)
                .asBitmap()
                .format(DecodeFormat.PREFER_ARGB_8888)
                .into(imgHomePageHeaderBg);
    }

    @Override
    public void onLoginState(boolean state) {
        if (!state) {
            onRefreshFinish();
            Toast.makeText(getContext(), "还没登陆", Toast.LENGTH_SHORT).show();
        } else {
            //setDevice online view
        }
    }

    @Override
    public void onRefreshFinish() {
        srLayoutMainContentHolder.setRefreshing(false);
    }

    @Override
    public void onRefresh() {
        if (basePresenter != null)
            basePresenter.fetchDeviceList(true);
        //不使用post,因为会泄露
        srLayoutMainContentHolder.setRefreshing(true);
    }

    @Override
    public void onClick(View v) {
        final int position = ViewUtils.getParentAdapterPosition(rVDevicesList,
                v,
                R.id.rLayout_device_item);
        if (position < 0 || position > homePageListAdapter.getCount() - 1) {
            AppLogger.d("woo,position is invalid: " + position);
            return;
        }
        String uuid = homePageListAdapter.getItem(position);
        JFGDevice device = GlobalDataProxy.getInstance().fetch(uuid);
        int pid = device == null ? 0 : device.pid;
        if (uuid != null) {
            Bundle bundle = new Bundle();
            bundle.putString(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
            if (JConstant.isCamera(pid)) {
                startActivity(new Intent(getActivity(), CameraLiveActivity.class)
                        .putExtra(JConstant.KEY_DEVICE_ITEM_UUID, uuid));
            } else if (JConstant.isMag(pid)) {
                startActivity(new Intent(getActivity(), MagLiveActivity.class)
                        .putExtra(JConstant.KEY_DEVICE_ITEM_UUID, uuid));
            } else if (JConstant.isBell(pid)) {
                startActivity(new Intent(getActivity(), DoorBellHomeActivity.class)
                        .putExtra(JConstant.KEY_DEVICE_ITEM_UUID, uuid));
            } else if (JConstant.isEFamily(pid)) {
                startActivity(new Intent(getActivity(), CloudLiveActivity.class)
                        .putExtra(JConstant.KEY_DEVICE_ITEM_UUID, uuid));
            }
        }
    }

    @Override
    public boolean onLongClick(View v) {
        final int position = ViewUtils.getParentAdapterPosition(rVDevicesList, v, R.id.rLayout_device_item);
        if (position < 0 || position > homePageListAdapter.getCount()) {
            AppLogger.d("woo,position is invalid: " + position);
            return false;
        }
        deleteItem(position);
        return true;
    }

    //删除一个Item
    private void deleteItem(final int position) {
        initSimpleDialog();
        SimpleDialogFragment fragment = simpleDialogFragmentWeakReference.get();
        fragment.setValue(position);
        fragment.show(getActivity().getSupportFragmentManager(), "ShareDialogFragment");
    }

    @Override
    public void onDialogAction(int id, Object value) {
        if (id == R.id.tv_dialog_btn_right)
            return;
        if (value == null || !(value instanceof Integer)) {
            Toast.makeText(getContext(), "null: ", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(getContext(), "id: " + id + " value:" + value, Toast.LENGTH_SHORT).show();
        homePageListAdapter.remove((Integer) value);
        //刷新需要剩下的item
        emptyViewState.determineEmptyViewState(homePageListAdapter.getCount());
        srLayoutMainContentHolder.setNestedScrollingEnabled(homePageListAdapter.getCount() > JFGRules.NETSTE_SCROLL_COUNT);
    }


    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        srLayoutMainContentHolder.setEnabled(verticalOffset == 0);
        final float ratio = (appbar.getTotalScrollRange() + verticalOffset) * 1.0f
                / appbar.getTotalScrollRange();
//        AppLogger.d("verticalOffset: " + " " + verticalOffset + "   " + ratio);
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

    private static class EmptyViewState {
        private IEmptyView homePageEmptyView;

        public EmptyViewState(Context context, final int layoutId) {
            homePageEmptyView = new HomeEmptyView(context, layoutId);
        }


        public void setEmptyViewState(ViewGroup viewContainer, final int bottom) {
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.gravity = Gravity.CENTER_HORIZONTAL;
            lp.topMargin = ViewUtils.dp2px(80);
            homePageEmptyView.addView(viewContainer, lp);
        }

        public void determineEmptyViewState(final int count) {
            homePageEmptyView.show(count == 0);
        }
    }

}
