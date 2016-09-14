package com.cylan.jiafeigou.n.view.home;

import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.FileDescriptorBitmapDecoder;
import com.bumptech.glide.load.resource.bitmap.VideoBitmapDecoder;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.OnActivityReenterListener;
import com.cylan.jiafeigou.misc.SharedElementCallBackListener;
import com.cylan.jiafeigou.n.mvp.contract.home.HomeWonderfulContract;
import com.cylan.jiafeigou.n.mvp.model.MediaBean;
import com.cylan.jiafeigou.n.view.activity.MediaActivity;
import com.cylan.jiafeigou.n.view.adapter.HomeWonderfulAdapter;
import com.cylan.jiafeigou.n.view.misc.HomeEmptyView;
import com.cylan.jiafeigou.n.view.misc.IEmptyView;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.AnimatorUtils;
import com.cylan.jiafeigou.utils.TimeUtils;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.dialog.SimpleDialogFragment;
import com.cylan.jiafeigou.widget.textview.WonderfulTitleHead;
import com.cylan.jiafeigou.widget.wheel.WheelView;
import com.cylan.jiafeigou.widget.wheel.WheelViewDataSet;
import com.cylan.superadapter.internal.SuperViewHolder;
import com.cylan.utils.ListUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class HomeWonderfulFragmentExt extends Fragment implements
        HomeWonderfulContract.View, SwipeRefreshLayout.OnRefreshListener,
        HomeWonderfulAdapter.WonderfulItemClickListener,
        HomeWonderfulAdapter.WonderfulItemLongClickListener,
        ShareDialogFragment.ShareToListener,
        SimpleDialogFragment.SimpleDialogAction,
        WheelView.OnItemChangedListener,
        AppBarLayout.OnOffsetChangedListener,
        HomeWonderfulAdapter.LoadMediaListener,
        SharedElementCallBackListener,
        OnActivityReenterListener {


    @BindView(R.id.fl_date_bg_head_wonder)
    FrameLayout flDateBgHeadWonder;
    @BindView(R.id.rV_wonderful_list)
    RecyclerView rVDevicesList;
    @BindView(R.id.fLayout_main_content_holder)
    SwipeRefreshLayout srLayoutMainContentHolder;
    @BindView(R.id.fLayoutHomeWonderfulHeaderContainer)
    FrameLayout fLayoutHomeHeaderContainer;
    @BindView(R.id.fLayout_date_head_wonder)
    FrameLayout fLayoutDateHeadWonder;
    @BindView(R.id.rl_top_head_wonder)
    RelativeLayout rlTopHeadWonder;
    @BindView(R.id.img_wonderful_title_cover)
    ImageView imgWonderfulTitleCover;
    @BindView(R.id.tv_date_item_head_wonder)
    WonderfulTitleHead tvDateItemHeadWonder;
    @BindView(R.id.imgWonderfulTopBg)
    ImageView imgWonderfulTopBg;
    @BindView(R.id.tv_title_head_wonder)
    TextView tvTitleHeadWonder;
    @BindView(R.id.fLayout_empty_view_container)
    FrameLayout fLayoutWonderfulEmptyContainer;

    WeakReference<WheelView> wheelViewWeakReference;
    WeakReference<ShareDialogFragment> shareDialogFragmentWeakReference;
    WeakReference<SimpleDialogFragment> simpleDialogFragmentWeakReference;
    @BindView(R.id.tv_sec_title_head_wonder)
    TextView tvSecTitleHeadWonder;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbar;
    @BindView(R.id.appbar)
    AppBarLayout appbar;

    /**
     * 加载更多
     */
    private boolean endlessLoading = true;


    private EmptyViewState emptyViewState;
    private HomeWonderfulAdapter homeWonderAdapter;
    private HomeWonderfulContract.Presenter presenter;
    public boolean isShowTimeLine;

    public static HomeWonderfulFragmentExt newInstance(Bundle bundle) {
        HomeWonderfulFragmentExt fragment = new HomeWonderfulFragmentExt();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            AppLogger.d("save L:" + savedInstanceState);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onResume() {
        super.onResume();
        onTimeTick(JFGRules.getTimeRule());
        if (presenter != null) presenter.start();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        homeWonderAdapter = new HomeWonderfulAdapter(getContext(), null, null);
        homeWonderAdapter.setWonderfulItemClickListener(this);
        homeWonderAdapter.setWonderfulItemLongClickListener(this);
        homeWonderAdapter.setLoadMediaListener(this);
        initEmptyViewState(context);
    }

    private void initEmptyViewState(Context context) {
        if (emptyViewState == null)
            emptyViewState = new EmptyViewState(context, R.layout.layout_wonderful_list_empty_view);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home_wonderful_ext, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        appbar.addOnOffsetChangedListener(this);
        srLayoutMainContentHolder.setNestedScrollingEnabled(false);
        initView();

        initProgressBarPosition();

        initHeaderView();

        initSomeViewMargin();

        srLayoutMainContentHolder.postDelayed(new Runnable() {
            @Override
            public void run() {
                emptyViewState.setEmptyViewState(fLayoutWonderfulEmptyContainer,
                        fLayoutHomeHeaderContainer.getBottom());
                emptyViewState.determineEmptyViewState(homeWonderAdapter.getCount());
            }
        }, 20);
    }

    private void initDeleteDialog() {
        if (simpleDialogFragmentWeakReference == null || simpleDialogFragmentWeakReference.get() == null) {
            simpleDialogFragmentWeakReference = new WeakReference<>(SimpleDialogFragment.newInstance(null));
            simpleDialogFragmentWeakReference.get().setAction(this);
        }
    }

    private void initShareDialog() {
        if (shareDialogFragmentWeakReference == null || shareDialogFragmentWeakReference.get() == null) {
            shareDialogFragmentWeakReference = new WeakReference<>(ShareDialogFragment.newInstance(null));
            shareDialogFragmentWeakReference.get().setShareToListener(this);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onPause() {
        super.onPause();
    }


    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (presenter != null) presenter.stop();
        dismissShareDialog();
    }

    private void dismissShareDialog() {
        if (getActivity() != null && getActivity().getSupportFragmentManager() != null) {
            Fragment fragment = getActivity().getSupportFragmentManager().findFragmentByTag("ShareDialogFragment");
            if (fragment != null && fragment instanceof ShareDialogFragment) {
                ((ShareDialogFragment) fragment).dismiss();
            }
        }
    }

    private void initSomeViewMargin() {
        ViewUtils.setViewMarginStatusBar(tvTitleHeadWonder);
        ViewUtils.setViewMarginStatusBar(toolbar);
    }


    private void initView() {
        //添加Handler
        srLayoutMainContentHolder.setOnRefreshListener(this);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        rVDevicesList.setLayoutManager(linearLayoutManager);
        rVDevicesList.setAdapter(homeWonderAdapter);
        rVDevicesList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            int pastVisiblesItems, visibleItemCount, totalItemCount;

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) { //check for scroll down
                    visibleItemCount = linearLayoutManager.getChildCount();
                    totalItemCount = linearLayoutManager.getItemCount();
                    pastVisiblesItems = linearLayoutManager.findFirstVisibleItemPosition();
                    if (endlessLoading) {
                        if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                            endlessLoading = false;
                            if (presenter != null)
                                presenter.startRefresh();
                            AppLogger.v("Last Item Wow !");
                            //Do pagination.. i.e. fetch new data
                        }
                    }
                }
            }
        });
    }

    private void initHeaderView() {
    }


    /**
     * 初始化,progressBar的位置.
     */
    private void initProgressBarPosition() {
        srLayoutMainContentHolder.setColorSchemeColors(R.color.color_36bdff);
    }

    @Override
    public void setPresenter(HomeWonderfulContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @UiThread
    @Override
    public void onMediaListRsp(List<MediaBean> resultList) {
        endlessLoading = true;
        srLayoutMainContentHolder.setRefreshing(false);
        if (resultList == null || resultList.size() == 0) {
            homeWonderAdapter.clear();
            return;
        }
        homeWonderAdapter.addAll(resultList);
        emptyViewState.determineEmptyViewState(homeWonderAdapter.getCount());
        srLayoutMainContentHolder.setNestedScrollingEnabled(homeWonderAdapter.getCount() > 2);
    }

    @Override
    public void onHeadBackgroundChang(int daytime) {
        imgWonderfulTopBg.setBackgroundResource(daytime == 0 ? R.drawable.bg_wonderful_daytime : R.drawable.bg_wonderful_night);
    }

    @Override
    public void timeLineDataUpdate(WheelViewDataSet wheelViewDataSet) {
        View view = getWheelView();
        if (view == null)
            return;
        ((WheelView) view).setDataSet(wheelViewDataSet);
        ((WheelView) view).setOnItemChangedListener(this);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onTimeTick(int dayTime) {
        //需要优化
        int drawableId = dayTime == JFGRules.RULE_DAY_TIME
                ? R.drawable.bg_wonderful_daytime : R.drawable.bg_wonderful_night;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            imgWonderfulTopBg.setBackground(getResources().getDrawable(drawableId, null));
        } else {
            imgWonderfulTopBg.setBackground(getResources().getDrawable(drawableId));
        }
        AppLogger.d("onTimeTick: " + dayTime);
    }

    @Override
    public void onRefresh() {
        if (presenter != null) presenter.startRefresh();
        //不使用post,因为会泄露
        srLayoutMainContentHolder.setRefreshing(true);
    }

    @Override
    public void onClick(View v) {
        final int position = ViewUtils.getParentAdapterPosition(rVDevicesList, v, R.id.lLayout_item_wonderful);
        if (position < 0 || position > homeWonderAdapter.getCount()) {
            AppLogger.d("woo,position is invalid: " + position);
            return;
        }
        switch (v.getId()) {
            case R.id.iv_wonderful_item_content:
                Intent intent = new Intent(getActivity(), MediaActivity.class);
                // Pass data object in the bundle and populate details activity.
                intent.putParcelableArrayListExtra(JConstant.KEY_SHARED_ELEMENT_LIST, (ArrayList<? extends Parcelable>) homeWonderAdapter.getList());
                intent.putExtra(JConstant.KEY_SHARED_ELEMENT_STARTED_POSITION, position);
                getActivity().startActivity(intent,
                        ActivityOptions.makeSceneTransitionAnimation(getActivity(),
                                v, v.getTransitionName()).toBundle());
                AppLogger.d("transition:getName " + ViewCompat.getTransitionName(v));
                break;
            case R.id.tv_wonderful_item_share:
                initShareDialog();
                ShareDialogFragment fragment = shareDialogFragmentWeakReference.get();
                fragment.setArguments(new Bundle());
                fragment.show(getActivity().getSupportFragmentManager(), "ShareDialogFragment");
                break;
            case R.id.tv_wonderful_item_delete:
                initDeleteDialog();
                Toast.makeText(getContext(), "delete: " + position, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        final int position = ViewUtils.getParentAdapterPosition(rVDevicesList, v, R.id.lLayout_item_wonderful);
        if (position < 0 || position > homeWonderAdapter.getCount()) {
            AppLogger.d("woo,position is invalid: " + position);
            return false;
        }
        deleteItem(position);
        return true;
    }


    private void deleteItem(final int position) {
        initDeleteDialog();
        SimpleDialogFragment fragment = simpleDialogFragmentWeakReference.get();
        fragment.setValue(position);
        fragment.show(getActivity().getSupportFragmentManager(), "ShareDialogFragment");
    }

    @OnClick(R.id.fLayout_date_head_wonder)
    public void onClick() {
        View view = getWheelViewContainer();
        if (view != null && !ListUtils.isEmpty(homeWonderAdapter.getList())) {
            AnimatorUtils.slide(view);
            if (wheelViewWeakReference == null || wheelViewWeakReference.get() == null) {
                WheelView wheelView = (WheelView) view.findViewById(R.id.wv_wonderful_timeline);
                wheelViewWeakReference = new WeakReference<>(wheelView);
                wheelView.setOnItemChangedListener(this);
                TextView textView = (TextView) getActivity().findViewById(R.id.tv_time_line_pop);
                WheelViewDataSet dataSet = wheelView.getWheelViewDataSet();
                if (dataSet != null && dataSet.dataSet != null) {
                    textView.setText(TimeUtils.getDateStyle_0(dataSet.dataSet[dataSet.dataSet.length - 1]));
                }
            }
        } else return;
        //显示时间轴
        if (isShowTimeLine) {
            hideTimeLine();
        } else {
            showTimeLine();
        }
        tvDateItemHeadWonder.setTimeLineShow(isShowTimeLine);
        tvDateItemHeadWonder.setBackgroundToRight();
    }

    /**
     * 整个{@link WheelView}的父viewGroup
     *
     * @return
     */
    private RelativeLayout getWheelViewContainer() {
        if (getActivity() != null) {
            return (RelativeLayout) getActivity().findViewById(R.id.fLayout_wonderful_timeline);
        }
        return null;
    }

    /**
     * {@link WheelView}
     *
     * @return
     */
    private WheelView getWheelView() {
        if (getActivity() != null) {
            return (WheelView) getActivity().findViewById(R.id.wv_wonderful_timeline);
        }
        return null;
    }

    private void showTimeLine() {
        //do something presenter.xxx
        isShowTimeLine = true;
    }

    private void hideTimeLine() {
        //do something presenter.xxx
        isShowTimeLine = false;
    }

    @Override
    public void onItemChanged(int position, long timeInLong, String dateInStr) {
        AppLogger.d("date: " + TimeUtils.getDateStyle_0(timeInLong));
        if (getActivity() == null)
            return;
        TextView textView = (TextView) getActivity().findViewById(R.id.tv_time_line_pop);
        if (textView != null) textView.setText(TimeUtils.getDateStyle_0(timeInLong));
    }

    @Override
    public void share(int id) {
        initShareDialog();
        Toast.makeText(getContext(), "share to: " + id, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDialogAction(int id, Object value) {
        if (id == SimpleDialogFragment.ACTION_NEGATIVE)
            return;
        if (value == null || !(value instanceof Integer)) {
            Toast.makeText(getContext(), "null: ", Toast.LENGTH_SHORT).show();
            return;
        }
        homeWonderAdapter.remove((Integer) value);
        Toast.makeText(getContext(), "id: " + id + " value:" + value, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        final float ratio = (appbar.getTotalScrollRange() + verticalOffset) * 1.0f
                / appbar.getTotalScrollRange();
        final float alpha = 1.0f - ratio;
        if (imgWonderfulTitleCover.getAlpha() != alpha) {
            imgWonderfulTitleCover.setAlpha(alpha);
        }
    }

    @Override
    public void loadMedia(int mediaType, String srcUrl, ImageView imageView) {
        //图标
        if (mediaType == MediaBean.TYPE_PIC) {
            Glide.with(this)
                    .load(srcUrl)
                    .placeholder(R.drawable.wonderful_pic_place_holder)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imageView);
        } else {
            BitmapPool bitmapPool = Glide.get(getContext()).getBitmapPool();
            FileDescriptorBitmapDecoder decoder = new FileDescriptorBitmapDecoder(
                    new VideoBitmapDecoder(1000000),
                    bitmapPool,
                    DecodeFormat.PREFER_RGB_565);
            Glide.with(this)
                    .load(srcUrl)
                    .asBitmap()
                    .placeholder(R.drawable.wonderful_pic_place_holder)
                    .videoDecoder(decoder)
                    .into(imageView);
            AppLogger.d("load url: " + srcUrl);
        }
    }

    @Override
    public void onSharedElementCallBack(List<String> names, Map<String, View> sharedElements) {
        if (mTmpReenterState != null) {
            int startingPosition = mTmpReenterState.getInt(JConstant.EXTRA_STARTING_ALBUM_POSITION);
            int currentPosition = mTmpReenterState.getInt(JConstant.EXTRA_CURRENT_ALBUM_POSITION);
            if (startingPosition != currentPosition) {
                // If startingPosition != currentPosition the user must have swiped to a
                // different page in the DetailsActivity. We must update the shared element
                // so that the correct one falls into place.
                String newTransitionName = currentPosition + JConstant.KEY_SHARED_ELEMENT_TRANSITION_NAME_SUFFIX;
                SuperViewHolder holder = (SuperViewHolder) rVDevicesList.findViewHolderForAdapterPosition(currentPosition);
                holder.getView(R.id.iv_wonderful_item_content);
                View newSharedElement = holder.getView(R.id.iv_wonderful_item_content);
                AppLogger.d("transition newTransitionName: " + newTransitionName);
                AppLogger.d("transition newSharedElement: " + newSharedElement);
                if (newSharedElement != null) {
                    names.clear();
                    names.add(newTransitionName);
                    sharedElements.clear();
                    sharedElements.put(newTransitionName, newSharedElement);
                }
            }

            mTmpReenterState = null;
        } else {
            // If mTmpReenterState is null, then the activity is exiting.
//            View navigationBar = findViewById(android.R.id.navigationBarBackground);
//            View statusBar = findViewById(android.R.id.statusBarBackground);
//            if (navigationBar != null) {
//                names.add(navigationBar.getTransitionName());
//                sharedElements.put(navigationBar.getTransitionName(), navigationBar);
//            }
//            if (statusBar != null) {
//                names.add(statusBar.getTransitionName());
//                sharedElements.put(statusBar.getTransitionName(), statusBar);
//            }
        }
    }

    @Override
    public void onActivityReenter(int requestCode, Intent data) {
        AppLogger.d("transition onActivityReenter");
        mTmpReenterState = new Bundle(data.getExtras());
        int startingPosition = mTmpReenterState.getInt(JConstant.EXTRA_STARTING_ALBUM_POSITION);
        int currentPosition = mTmpReenterState.getInt(JConstant.EXTRA_CURRENT_ALBUM_POSITION);
        if (startingPosition != currentPosition) {
//            rVDevicesList.scrollToPosition(currentPosition);
            ((LinearLayoutManager) rVDevicesList.getLayoutManager()).scrollToPositionWithOffset(currentPosition, 0);
        }
        getActivity().postponeEnterTransition();
        rVDevicesList.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                rVDevicesList.getViewTreeObserver().removeOnPreDrawListener(this);
                rVDevicesList.requestLayout();
                getActivity().startPostponedEnterTransition();
                return true;
            }
        });
    }

    private Bundle mTmpReenterState;

    /**
     * 空列表的placeholder
     */
    private static class EmptyViewState {
        private IEmptyView homePageEmptyView;

        public EmptyViewState(Context context, final int layoutId) {
            homePageEmptyView = new HomeEmptyView(context, layoutId);
        }


        public void setEmptyViewState(ViewGroup viewContainer, final int bottom) {
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.gravity = Gravity.CENTER_HORIZONTAL;
            lp.topMargin = bottom;
            homePageEmptyView.addView(viewContainer, lp);
        }

        public void determineEmptyViewState(final int count) {
            homePageEmptyView.show(count == 0);
        }
    }

}
