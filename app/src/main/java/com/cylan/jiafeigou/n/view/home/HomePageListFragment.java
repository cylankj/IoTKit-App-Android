package com.cylan.jiafeigou.n.view.home;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.RxEvent;
import com.cylan.jiafeigou.n.mvp.contract.ActivityResultContract;
import com.cylan.jiafeigou.n.mvp.contract.home.HomePageListContract;
import com.cylan.jiafeigou.n.mvp.impl.ActivityResultPresenterImpl;
import com.cylan.jiafeigou.n.mvp.model.DeviceBean;
import com.cylan.jiafeigou.n.mvp.model.GreetBean;
import com.cylan.jiafeigou.n.view.activity.BindDeviceActivity;
import com.cylan.jiafeigou.n.view.activity.CameraLiveActivity;
import com.cylan.jiafeigou.n.view.activity.MagLiveActivity;
import com.cylan.jiafeigou.n.view.adapter.HomePageListAdapter;
import com.cylan.jiafeigou.n.view.bell.DoorBellActivity;
import com.cylan.jiafeigou.n.view.misc.HomeEmptyView;
import com.cylan.jiafeigou.n.view.misc.IEmptyView;
import com.cylan.jiafeigou.support.rxbus.RxBus;
import com.cylan.jiafeigou.utils.AppLogger;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.dialog.SimpleDialogFragment;
import com.cylan.jiafeigou.widget.sticky.HeaderAnimator;
import com.cylan.jiafeigou.widget.sticky.StickyHeaderBuilder;
import com.cylan.jiafeigou.widget.wave.SuperWaveView;
import com.cylan.utils.RandomUtils;
import com.superlog.SLog;

import org.msgpack.annotation.NotNullable;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class HomePageListFragment extends Fragment implements
        HomePageListContract.View, SwipeRefreshLayout.OnRefreshListener,
        HomePageListAdapter.DeviceItemClickListener,
        ActivityResultContract.View,
        SimpleDialogFragment.SimpleDialogAction,
        HomePageListAdapter.DeviceItemLongClickListener {
    /**
     * progress 位置
     */
    static int progressBarStartPosition;
    private static final int REFRESH_DELAY = 1500;
    @BindView(R.id.fLayout_main_content_holder)
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
    @BindView(R.id.fLayoutHomeHeaderContainer)
    FrameLayout fLayoutHomeHeaderContainer;
    @BindView(R.id.fLayout_home_page_container)
    FrameLayout fLayoutHomePageContainer;
    @BindView(R.id.tvHeaderNickName)
    TextView tvHeaderNickName;
    @BindView(R.id.tvHeaderPoet)
    TextView tvHeaderPoet;
    private HomePageListContract.Presenter presenter;

    private ActivityResultContract.Presenter activityResultPresenter;
    private HomePageListAdapter homePageListAdapter;
    private SimpleScrollListener simpleScrollListener;

    private EmptyViewState emptyViewState;
    /**
     * 手动完成刷新,自动完成刷新 订阅者.
     */
    private Subscription refreshCompleteSubscription;

    public static HomePageListFragment newInstance(Bundle bundle) {
        HomePageListFragment fragment = new HomePageListFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            SLog.d("save L:" + savedInstanceState);
        }
        Bundle bundle;
        if (getArguments() != null) {
            bundle = getArguments();
        }
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
        if (presenter != null)
            presenter.fetchGreet();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (presenter != null) {
            presenter.start();
            presenter.registerWorker();
        }
        homePageListAdapter = new HomePageListAdapter(getContext(), null, null);
        homePageListAdapter.setDeviceItemClickListener(this);
        homePageListAdapter.setDeviceItemLongClickListener(this);
        initEmptyViewState(context);
        //需要优化.
        activityResultPresenter = new ActivityResultPresenterImpl(this);
        activityResultPresenter.start();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home_page_list, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //添加Handler
        srLayoutMainContentHolder.setOnRefreshListener(this);
        initProgressBarPosition();
        rVDevicesList.setLayoutManager(new LinearLayoutManager(getContext()));
        rVDevicesList.setAdapter(homePageListAdapter);
        initHeaderView();
        initSomeViewMargin();
        initSimpleDialog();
        fLayoutHomePageContainer.postDelayed(new Runnable() {
            @Override
            public void run() {
                emptyViewState.setEmptyViewState(fLayoutHomePageContainer, fLayoutHomeHeaderContainer.getBottom());
                emptyViewState.determineEmptyViewState(homePageListAdapter.getCount());
            }
        }, 20);
    }

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
        ViewUtils.setViewMarginStatusBar(imgBtnAddDevices);
        ViewUtils.setViewMarginStatusBar(lLayoutHomeGreet);
    }

    private void initHeaderView() {
        if (simpleScrollListener == null) {
            simpleScrollListener = new SimpleScrollListener(vWaveAnimation, tvHeaderLastTitle);
        }
        StickyHeaderBuilder.stickTo(rVDevicesList, simpleScrollListener)
                .setHeader(R.id.fLayoutHomeHeaderContainer, (ViewGroup) getView())
                .minHeightHeader((int) (getResources().getDimension(R.dimen.dimens_48dp)
                        + ViewUtils.getCompatStatusBarHeight(getContext())))
                .build();
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
    private void initProgressBarPosition() {
        rVDevicesList.post(new Runnable() {
            @Override
            public void run() {
                if (progressBarStartPosition == 0) {
                    FrameLayout view = fLayoutHomeHeaderContainer;
                    if (view != null) {
                        Drawable drawable = view.getBackground();
                        progressBarStartPosition = drawable.getIntrinsicHeight();
                    }
                }
                srLayoutMainContentHolder.setColorSchemeColors(R.color.color_36bdff);
                srLayoutMainContentHolder.setProgressViewOffset(false, progressBarStartPosition - 100, progressBarStartPosition + 100);
            }
        });
    }

    @OnClick(R.id.imgV_add_devices)
    void onClickAddDevice() {
//        if (!JfgCmd.getJfgCmd(getContext()).isLogined) {
//            if (RxBus.getInstance().hasObservers())
//                RxBus.getInstance().send(new RxEvent.NeedLoginEvent(null));
//            return;
//        }
        if (RandomUtils.getRandom(2) == JFGRules.LOGOUT) {
            if (RxBus.getInstance().hasObservers())
                RxBus.getInstance().send(new RxEvent.NeedLoginEvent(null));
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
        if (presenter != null) presenter.stop();
//        if (waveHelper != null) waveHelper.cancel();
        unRegisterSubscription(refreshCompleteSubscription);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    /**
     * 反注册
     *
     * @param subscriptions
     */
    private void unRegisterSubscription(Subscription... subscriptions) {
        if (subscriptions != null)
            for (Subscription subscription : subscriptions) {
                if (subscription != null)
                    subscription.unsubscribe();
            }
    }

    @Override
    public void setPresenter(HomePageListContract.Presenter presenter) {
        SLog.e("ffff: " + (presenter == null));
        this.presenter = presenter;
    }

    @UiThread
    @Override
    public void onDeviceListRsp(List<DeviceBean> resultList) {
        srLayoutMainContentHolder.setRefreshing(false);
        if (resultList == null || resultList.size() == 0) {
            homePageListAdapter.clear();
            if (isResumed()) {
//                getActivity().findViewById(R.id.vs_empty_view).setVisibility(View.VISIBLE);
            }
            return;
        }
        homePageListAdapter.addAll(resultList);
        emptyViewState.determineEmptyViewState(homePageListAdapter.getCount());
    }

    @Override
    public void onGreetUpdate(GreetBean greetBean) {
        tvHeaderNickName.setText(String.format(getString(R.string.home_nick_name),
                greetBean.nickName));
        tvHeaderPoet.setText(greetBean.poet);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onTimeTick(final int dayTime) {
        //需要优化
        int drawableId = dayTime == JFGRules.RULE_DAY_TIME
                ? R.drawable.bg_home_title_daytime : R.drawable.bg_home_title_night;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            fLayoutHomeHeaderContainer.setBackground(getResources().getDrawable(drawableId, null));
        } else {
            fLayoutHomeHeaderContainer.setBackground(getResources().getDrawable(drawableId));
        }
    }

    @Override
    public void onLoginState(int state) {
        if (state == JFGRules.LOGIN) {
        } else if (state == JFGRules.LOGOUT) {
            srLayoutMainContentHolder.setRefreshing(false);
            Toast.makeText(getContext(), "还没登陆", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onRefresh() {
        if (presenter != null) presenter.fetchDeviceList();
        //不使用post,因为会泄露
        srLayoutMainContentHolder.setRefreshing(true);
        refreshCompleteSubscription = Observable.just(srLayoutMainContentHolder)
                .subscribeOn(Schedulers.newThread())
                .delay(REFRESH_DELAY, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<SwipeRefreshLayout>() {
                    @Override
                    public void call(SwipeRefreshLayout swipeRefreshLayout) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
    }

    @Override
    public void onClick(View v) {
        final int position = v.getTag() == null ? 0 : (int) v.getTag();
        if (position < 0 || position > homePageListAdapter.getCount() - 1)
            return;
        DeviceBean bean = homePageListAdapter.getItem(position);
        if (bean != null) {
            Bundle bundle = new Bundle();
            bundle.putParcelable(JConstant.KEY_DEVICE_ITEM_BUNDLE, bean);
            if (bean.deviceType == JConstant.JFG_DEVICE_CAMERA) {
                startActivity(new Intent(getActivity(), CameraLiveActivity.class)
                        .putExtra(JConstant.KEY_DEVICE_ITEM_BUNDLE, bundle));
            } else if (bean.deviceType == JConstant.JFG_DEVICE_MAG) {
                startActivity(new Intent(getActivity(), MagLiveActivity.class)
                        .putExtra(JConstant.KEY_DEVICE_ITEM_BUNDLE, bundle));
            } else if (bean.deviceType == JConstant.JFG_DEVICE_BELL) {
                startActivity(new Intent(getActivity(), DoorBellActivity.class)
                        .putExtra(JConstant.KEY_DEVICE_ITEM_BUNDLE, bundle));
            }
        }
    }

    @Override
    public boolean onLongClick(View v) {
        final int position = v.getTag() == null ? 0 : (int) v.getTag();
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
        if (id == SimpleDialogFragment.ACTION_NEGATIVE)
            return;
        if (value == null || !(value instanceof Integer)) {
            Toast.makeText(getContext(), "null: ", Toast.LENGTH_SHORT).show();
            return;
        }
        final int position = (int) value;
        Toast.makeText(getContext(), "id: " + id + " value:" + value, Toast.LENGTH_SHORT).show();
        homePageListAdapter.remove((Integer) value);
        //刷新需要剩下的item
        homePageListAdapter.notifyItemRangeChanged(position, homePageListAdapter.getItemCount());
        emptyViewState.determineEmptyViewState(homePageListAdapter.getCount());
    }

    @Override
    public void onActivityResult(@NotNullable RxEvent.ActivityResult result) {
        //这段逻辑 违背MVP，稍后需要修改。
        AppLogger.d("this slice is illegal");
        final Bundle bundle = result.bundle;
        if (bundle == null) {
            AppLogger.d("bundle is null");
            return;
        }
        if (!bundle.containsKey(JConstant.KEY_REMOVE_DEVICE)) {
            AppLogger.d("not the removing");
            return;
        }
        Object o = bundle.getParcelable(JConstant.KEY_DEVICE_ITEM_BUNDLE);
        if (o != null && o instanceof DeviceBean) {
            final String cid = ((DeviceBean) o).cid;
            DeviceBean bean = homePageListAdapter.findTarget(cid);
            if (bean == null) {
                AppLogger.d("bean is null cid: " + cid);
                return;
            }
            homePageListAdapter.remove(bean);
        } else {
            AppLogger.d("woo,bundle is not the type");
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
            lp.topMargin = ViewUtils.dp2px(80)
                    + bottom;
            homePageEmptyView.addView(viewContainer, lp);
        }

        public void determineEmptyViewState(final int count) {
            homePageEmptyView.show(count == 0);
        }
    }

    private static class SimpleScrollListener implements HeaderAnimator.ScrollRationListener {

        private WeakReference<SuperWaveView> weakReference;

        private WeakReference<TextView> fadeTitleWeak;

        public SimpleScrollListener(SuperWaveView superWaveView, TextView textView) {
            weakReference = new WeakReference<>(superWaveView);
            fadeTitleWeak = new WeakReference<>(textView);
        }

        @Override
        public void onScroll(float ratio) {
            if (fadeTitleWeak != null && fadeTitleWeak.get() != null) {
                float alpha = (ratio - 0.8f) / 0.2f;
                if (ratio < 0.8)
                    alpha = 0;
//                Log.d("hunt", "hunt: " + ration + " " + alpha);
                fadeTitleWeak.get().setAlpha(alpha);
            }
            if (weakReference != null && weakReference.get() != null) {
                weakReference.get().setAmplitudeRatio(1.0f - ratio);
            }
        }
    }
}
