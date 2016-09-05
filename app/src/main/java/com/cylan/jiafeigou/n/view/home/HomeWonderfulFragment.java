package com.cylan.jiafeigou.n.view.home;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Fade;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.transition.DetailsTransition;
import com.cylan.jiafeigou.n.mvp.contract.home.HomeWonderfulContract;
import com.cylan.jiafeigou.n.mvp.model.MediaBean;
import com.cylan.jiafeigou.n.view.adapter.HomeWonderAdapter;
import com.cylan.jiafeigou.n.view.media.WonderfulBigPicFragment;
import com.cylan.jiafeigou.n.view.misc.HomeEmptyView;
import com.cylan.jiafeigou.n.view.misc.IEmptyView;
import com.cylan.jiafeigou.utils.AnimatorUtils;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.TimeUtils;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.dialog.SimpleDialogFragment;
import com.cylan.jiafeigou.widget.sticky.HeaderAnimator;
import com.cylan.jiafeigou.widget.sticky.StickyHeaderBuilder;
import com.cylan.jiafeigou.widget.textview.WonderfulTitleHead;
import com.cylan.jiafeigou.widget.wheel.WheelView;
import com.cylan.jiafeigou.widget.wheel.WheelViewDataSet;
import com.cylan.utils.ListUtils;
;

import java.lang.ref.WeakReference;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class HomeWonderfulFragment extends Fragment implements
        HomeWonderfulContract.View, SwipeRefreshLayout.OnRefreshListener,
        HomeWonderAdapter.WonderfulItemClickListener,
        HomeWonderAdapter.WonderfulItemLongClickListener,
        ShareDialogFragment.ShareToListener,
        SimpleDialogFragment.SimpleDialogAction,
        WheelView.OnItemChangedListener {


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
    WonderfulTitleHead tvDateItemHeadWonder;
    @BindView(R.id.imgWonderfulTopBg)
    ImageView imgWonderfulTopBg;
    @BindView(R.id.tv_title_head_wonder)
    TextView tvTitleHeadWonder;
    @BindView(R.id.fLayout_wonderful_container)
    FrameLayout fLayoutWonderfulContainer;

    WeakReference<WheelView> wheelViewWeakReference;
    WeakReference<ShareDialogFragment> shareDialogFragmentWeakReference;
    WeakReference<SimpleDialogFragment> simpleDialogFragmentWeakReference;
    /**
     * progress 位置
     */
    private int progressBarStartPosition;
    //不是长时间需要,用软引用.
//    private WeakReference<HomeMenuDialog> homeMenuDialogWeakReference;
    private HomeWonderfulContract.Presenter presenter;
    private HomeWonderAdapter homeWonderAdapter;
    private SimpleScrollListener simpleScrollListener;
    public boolean isShowTimeLine;
    private EmptyViewState emptyViewState;

    public static HomeWonderfulFragment newInstance(Bundle bundle) {
        HomeWonderfulFragment fragment = new HomeWonderfulFragment();
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
        if (presenter != null) presenter.start();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        homeWonderAdapter = new HomeWonderAdapter(getContext(), null, null);
        homeWonderAdapter.setWonderfulItemClickListener(this);
        homeWonderAdapter.setWonderfulItemLongClickListener(this);
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

        initSomeViewMargin();

        initShareDialog();

        initDeleteDialog();

        srLayoutMainContentHolder.postDelayed(new Runnable() {
            @Override
            public void run() {
                emptyViewState.setEmptyViewState(fLayoutWonderfulContainer, rLayoutHomeHeaderContainer.getBottom());
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
    }

    private void initView() {
        //添加Handler
        srLayoutMainContentHolder.setOnRefreshListener(this);

        rVDevicesList.setLayoutManager(new LinearLayoutManager(getContext()));
        rVDevicesList.setAdapter(homeWonderAdapter);

    }

    private void initHeaderView() {

        if (simpleScrollListener == null)
            simpleScrollListener = new SimpleScrollListener(imgCover, fLayoutDateHeadWonder);
        StickyHeaderBuilder.stickTo(rVDevicesList, simpleScrollListener)
                .setHeader(R.id.rLayoutHomeWonderfulHeaderContainer,
                        (ViewGroup) getView())
                .minHeightHeader((int) (getResources().getDimension(R.dimen.dimens_48dp)
                        + ViewUtils.getCompatStatusBarHeight(getContext())))
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
                    Drawable drawable = imgWonderfulTopBg.getBackground();
                    progressBarStartPosition = drawable.getIntrinsicHeight();
                }
                srLayoutMainContentHolder.setColorSchemeColors(R.color.color_36bdff);
                srLayoutMainContentHolder.setProgressViewOffset(false, progressBarStartPosition - 100, progressBarStartPosition + 100);
            }
        });
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
            homeWonderAdapter.clear();
            return;
        }
        homeWonderAdapter.addAll(resultList);
        emptyViewState.determineEmptyViewState(homeWonderAdapter.getCount());
    }

    @Override
    public void onHeadBackgroundChang(int daytime) {
        imgWonderfulTopBg.setBackgroundResource(daytime == 0 ? R.drawable.bg_head_daytime_wonderful : R.drawable.bg_head_night_wonderful);
    }

    @Override
    public void timeLineDataUpdate(WheelViewDataSet wheelViewDataSet) {
        View view = getWheelView();
        if (view == null)
            return;
        ((WheelView) view).setDataSet(wheelViewDataSet);
        ((WheelView) view).setOnItemChangedListener(this);
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
                WonderfulBigPicFragment picDetailsFragment = WonderfulBigPicFragment.newInstance(null);

//                // Note that we need the API version check here because the actual transition classes (e.g. Fade)
//                // are not in the support library and are only available in API 21+. The methods we are calling on the Fragment
//                // ARE available in the support library (though they don't do anything on API < 21)
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                    picDetailsFragment.setSharedElementEnterTransition(new DetailsTransition());
////                    picDetailsFragment.setEnterTransition(new Fade());
//                    picDetailsFragment.setSharedElementReturnTransition(new DetailsTransition());
//                    setSharedElementReturnTransition(new Fade());
//                    setExitTransition(new Fade());
//                }


                setExitTransition(new Fade());
                picDetailsFragment.setEnterTransition(new Fade());
                picDetailsFragment.setSharedElementEnterTransition(new DetailsTransition());
                picDetailsFragment.setSharedElementReturnTransition(new DetailsTransition());

                getFragmentManager()
                        .beginTransaction()
                        .addSharedElement(v, "picDetailsFragment")
                        .replace(R.id.fLayout_wonderful_container, picDetailsFragment)
                        .addToBackStack("picDetailsFragment")
                        .commit();
                break;
            case R.id.tv_wonderful_item_share:
                initShareDialog();
                ShareDialogFragment fragment = shareDialogFragmentWeakReference.get();
                fragment.setArguments(new Bundle());
                fragment.show(getActivity().getSupportFragmentManager(), "ShareDialogFragment");
                break;
            case R.id.tv_wonderful_item_delete:
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

        private WeakReference<ImageView> fadeTopHeadCover;
        private final FrameLayout mTitleBackgroundRef;
        private WonderfulTitleHead tvDateColor;

        public SimpleScrollListener(ImageView relativeLayout, FrameLayout frameLayout) {
            fadeTopHeadCover = new WeakReference<>(relativeLayout);
            mTitleBackgroundRef = new WeakReference<>(frameLayout).get();
        }


        @Override
        public void onScroll(float ration) {

            if (fadeTopHeadCover != null && fadeTopHeadCover.get() != null && mTitleBackgroundRef != null) {
                if (tvDateColor == null)
                    tvDateColor = (WonderfulTitleHead) mTitleBackgroundRef.getChildAt(1);

                float alpha = (ration - 0.8f) / 0.2f;
                if (ration < 0.8) {
                    alpha = 0;
                    tvDateColor.setTextColor(Color.rgb(78, 82, 91));
                    tvDateColor.setTitleHeadIsTop(false);
                } else {
                    tvDateColor.setTextColor(Color.WHITE);
                    tvDateColor.setTitleHeadIsTop(true);
                }
                if (alpha < 0.99f)
                    tvDateColor.setBackgroundToRight();
                mTitleBackgroundRef.getChildAt(0).setAlpha(1 - alpha);
                fadeTopHeadCover.get().setAlpha(alpha);
            }
        }
    }
}
