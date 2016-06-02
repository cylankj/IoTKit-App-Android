package com.cylan.jiafeigou.n.view.home;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.model.DeviceBean;
import com.cylan.jiafeigou.n.model.GreetBean;
import com.cylan.jiafeigou.n.mvp.contract.home.HomePageListContract;
import com.cylan.jiafeigou.n.view.adapter.HomePageListAdapter;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.dialog.HomeMenuDialog;
import com.cylan.jiafeigou.widget.sticky.HeaderAnimator;
import com.cylan.jiafeigou.widget.sticky.StickyHeaderBuilder;
import com.cylan.jiafeigou.widget.wave.WaveHelper;
import com.cylan.jiafeigou.widget.wave.WaveView;
import com.superlog.SLog;

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
        HomePageListAdapter.DeviceItemLongClickListener {
    /**
     * progress 位置
     */
    static int progressBarStartPosition;
    private static final int REFRESH_DELAY = 1500;
    @BindView(R.id.fLayout_main_content_holder)
    SwipeRefreshLayout srLayoutMainContentHolder;
    @BindView(R.id.imgBtn_add_devices)
    ImageButton imgBtnAddDevices;
    @BindView(R.id.rV_devices_list)
    RecyclerView rVDevicesList;//设备列表
    @BindView(R.id.vWaveAnimation)
    WaveView vWaveAnimation;
    @BindView(R.id.rLayout_home_top)
    RelativeLayout rLayoutHomeTop;

    @BindView(R.id.tvHeaderLastTitle)
    TextView tvHeaderLastTitle;
    //不是长时间需要,用软引用.
    private WeakReference<HomeMenuDialog> homeMenuDialogWeakReference;
    private HomePageListContract.Presenter presenter;

    private HomePageListAdapter homePageListAdapter;
    private SimpleScrollListener simpleScrollListener;
    private WaveHelper waveHelper;
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
        if (presenter != null) presenter.start();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        homePageListAdapter = new HomePageListAdapter(getContext(), null, null);
        homePageListAdapter.setDeviceItemClickListener(this);
        homePageListAdapter.setDeviceItemLongClickListener(this);
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
        initWaveAnimation();
        initHeaderView();
    }

    private void initHeaderView() {
        if (simpleScrollListener == null)
            simpleScrollListener = new SimpleScrollListener(waveHelper, tvHeaderLastTitle);
        StickyHeaderBuilder.stickTo(rVDevicesList, simpleScrollListener)
                .setHeader(R.id.rLayoutHomeHeaderContainer, (ViewGroup) getView())
                .minHeightHeaderDim(R.dimen.dimens_48dp)
                .build();
    }

    /**
     * 水波纹动画初始化
     */
    private void initWaveAnimation() {
        if (waveHelper == null)
            waveHelper = new WaveHelper(vWaveAnimation);
        vWaveAnimation.post(new Runnable() {
            @Override
            public void run() {
                waveHelper.start();
            }
        });
    }

    /**
     * 初始化,progressBar的位置.
     */
    private void initProgressBarPosition() {
        rVDevicesList.post(new Runnable() {
            @Override
            public void run() {
                if (progressBarStartPosition == 0) {
                    ImageView view = (ImageView) getView().findViewById(R.id.imgHomeTopBg);
                    if (view != null) {
                        Drawable drawable = view.getBackground();
                        progressBarStartPosition = drawable.getIntrinsicHeight();
                    }
                }
                srLayoutMainContentHolder.setProgressViewOffset(false, progressBarStartPosition - 100, progressBarStartPosition + 100);
            }
        });
    }

    @OnClick(R.id.imgBtn_add_devices)
    void onClickAddDevice() {
        ToastUtil.showToast(getContext(), "add devices");
    }

    @Override
    public void onStop() {
        super.onStop();
        if (waveHelper != null) waveHelper.start();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (presenter != null) presenter.stop();
        if (waveHelper != null) waveHelper.cancel();
        unRegisterSubscription(refreshCompleteSubscription);
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
    }

    @Override
    public void onGreetUpdate(GreetBean greetBean) {

    }


    @Override
    public void onRefresh() {
        if (presenter != null) presenter.startRefresh();
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
        ToastUtil.showToast(getContext(), "click: " + position);
    }

    @Override
    public boolean onLongClick(View v) {
        final int position = v.getTag() == null ? 0 : (int) v.getTag();
        deleteItem(position);
        return true;
    }


    private void deleteItem(final int position) {
        if (homeMenuDialogWeakReference == null || homeMenuDialogWeakReference.get() == null) {
            HomeMenuDialog dialog = new HomeMenuDialog(getActivity(),
                    null,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (homePageListAdapter != null && homePageListAdapter.getCount() > position) {
                                final DeviceBean bean = homePageListAdapter.getItem(position);
                                homePageListAdapter.remove(position);
                                presenter.onDeleteItem(bean);
                            }
                        }
                    });
            homeMenuDialogWeakReference = new WeakReference<>(dialog);
        }
        homeMenuDialogWeakReference.get().show();
    }

    private static class SimpleScrollListener implements HeaderAnimator.ScrollRationListener {

        private WeakReference<WaveHelper> weakReference;

        private WeakReference<TextView> fadeTitleWeak;

        public SimpleScrollListener(WaveHelper helper, TextView textView) {
            weakReference = new WeakReference<>(helper);
            fadeTitleWeak = new WeakReference<>(textView);
        }

        @Override
        public void onScroll(float ration) {
            if (fadeTitleWeak != null && fadeTitleWeak.get() != null) {
                float alpha = (ration - 0.8f) / 0.2f;
                if (ration < 0.8)
                    alpha = 0;
//                Log.d("hunt", "hunt: " + ration + " " + alpha);
                fadeTitleWeak.get().setAlpha(alpha);
            }
            if (weakReference != null && weakReference.get() != null) {
                weakReference.get().updateAmplitudeRatio(ration);
            }
        }
    }
}
