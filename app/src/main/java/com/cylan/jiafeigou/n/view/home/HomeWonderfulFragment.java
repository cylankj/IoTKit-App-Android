package com.cylan.jiafeigou.n.view.home;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.model.MediaBean;
import com.cylan.jiafeigou.n.mvp.contract.home.HomeWonderfulContract;
import com.cylan.jiafeigou.n.view.adapter.HomeWondereAdapter;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.dialog.HomeMenuDialog;
import com.cylan.jiafeigou.widget.sticky.HeaderAnimator;
import com.cylan.jiafeigou.widget.sticky.StickyHeaderBuilder;
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

public class HomeWonderfulFragment extends Fragment implements
        HomeWonderfulContract.View, SwipeRefreshLayout.OnRefreshListener,
        HomeWondereAdapter.DeviceItemClickListener,
        HomeWondereAdapter.DeviceItemLongClickListener {


    /**
     * progress 位置
     */
    private int progressBarStartPosition;
    private static final int REFRESH_DELAY = 1500;

    @BindView(R.id.fl_date_bg_head_wonder)
    FrameLayout flDateBgHeadWonder;
    @BindView(R.id.rV_wonderful_list)
    RecyclerView rVDevicesList;
    @BindView(R.id.fLayout_main_content_holder)
    SwipeRefreshLayout srLayoutMainContentHolder;
    @BindView(R.id.rLayoutHomeWonderfulHeaderContainer)
    RelativeLayout rLayoutHomeHeaderContainer;
    @BindView(R.id.fLayout_date_head_wonder)
    FrameLayout fLayoutDateHeadWonder;
    @BindView(R.id.rl_top_head_wonder)
    RelativeLayout rlTopHeadWonder;
    @BindView(R.id.img_cover)
    ImageView imgCover;
    @BindView(R.id.tv_date_item_head_wonder)
    TextView tvDateItemHeadWonder;


    //不是长时间需要,用软引用.
    private WeakReference<HomeMenuDialog> homeMenuDialogWeakReference;
    private HomeWonderfulContract.Presenter presenter;

    private HomeWondereAdapter homeWondereAdapter;
    private SimpleScrollListener simpleScrollListener;

    /**
     * 手动完成刷新,自动完成刷新 订阅者.
     */
    private Subscription refreshCompleteSubscription;

    public static HomeWonderfulFragment newInstance(Bundle bundle) {
        HomeWonderfulFragment fragment = new HomeWonderfulFragment();
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
        homeWondereAdapter = new HomeWondereAdapter(getContext(), null, null);
        homeWondereAdapter.setDeviceItemClickListener(this);
        homeWondereAdapter.setDeviceItemLongClickListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home_wonderful, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView();

        initProgressBarPosition();

        initHeaderView();
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void initView() {
        //添加Handler
        srLayoutMainContentHolder.setOnRefreshListener(this);

        rVDevicesList.setLayoutManager(new LinearLayoutManager(getContext()));
        rVDevicesList.setAdapter(homeWondereAdapter);

    }

    private void initHeaderView() {

        if (simpleScrollListener == null)
            simpleScrollListener = new SimpleScrollListener(imgCover, fLayoutDateHeadWonder);
        StickyHeaderBuilder.stickTo(rVDevicesList, simpleScrollListener)
                .setHeader(R.id.rLayoutHomeWonderfulHeaderContainer, (ViewGroup) getView())
                .minHeightHeaderDim(R.dimen.dimens_48dp)
                .build();
    }


    /**
     * 初始化,progressBar的位置.
     */
    private void initProgressBarPosition() {
        rVDevicesList.post(new Runnable() {
            @Override
            public void run() {
                if (progressBarStartPosition == 0) {
                    ImageView view = (ImageView) getView().findViewById(R.id.imgWonderfulTopBg);
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


    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (presenter != null) presenter.stop();

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
    public void setPresenter(HomeWonderfulContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @UiThread
    @Override
    public void onDeviceListRsp(List<MediaBean> resultList) {
        srLayoutMainContentHolder.setRefreshing(false);
        if (resultList == null || resultList.size() == 0) {
            homeWondereAdapter.clear();
            if (isResumed()) {
//                getActivity().findViewById(R.id.vs_empty_view).setVisibility(View.VISIBLE);
            }
            return;
        }
        homeWondereAdapter.addAll(resultList);
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
                            if (homeWondereAdapter != null && homeWondereAdapter.getCount() > position) {
                                final MediaBean bean = homeWondereAdapter.getItem(position);
                                homeWondereAdapter.remove(position);
                                //              presenter.onDeleteItem(bean);
                            }
                        }
                    });
            homeMenuDialogWeakReference = new WeakReference<>(dialog);
        }
        homeMenuDialogWeakReference.get().show();
    }

    @OnClick(R.id.fLayout_date_head_wonder)
    public void onClick() {
        //显示时间轴
        if (isShowTimeLine) {
            hideTimeLine();
        } else {
            showTimeLine();
        }
    }

    boolean isShowTimeLine;

    private void showTimeLine() {

        tvDateItemHeadWonder.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_date_uptag_wonderful, 0);
        isShowTimeLine = true;
    }

    private void hideTimeLine() {

        tvDateItemHeadWonder.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_date_downtag_wonderful, 0);
        isShowTimeLine = false;
    }


    private static class SimpleScrollListener implements HeaderAnimator.ScrollRationListener {

        private WeakReference<ImageView> fadeTopHeadCover;
        private final FrameLayout mTitleBackgroundRef;
        private TextView tvDateColor;

        public SimpleScrollListener(ImageView relativeLayout, FrameLayout frameLayout) {
            fadeTopHeadCover = new WeakReference<ImageView>(relativeLayout);
            mTitleBackgroundRef = new WeakReference<>(frameLayout).get();
        }

        @Override
        public void onScroll(float ration) {

            if (fadeTopHeadCover != null && fadeTopHeadCover.get() != null && mTitleBackgroundRef != null) {
                if (tvDateColor == null)
                    tvDateColor = (TextView) mTitleBackgroundRef.getChildAt(1);

                float alpha = (ration - 0.8f) / 0.2f;
                if (ration < 0.8) {
                    alpha = 0;
                    tvDateColor.setTextColor(Color.rgb(78, 82, 91));
                } else {
                    tvDateColor.setTextColor(Color.WHITE);
                }
                mTitleBackgroundRef.getChildAt(0).setAlpha(1 - alpha);
                fadeTopHeadCover.get().setAlpha(alpha);
            }

        }
    }
}
