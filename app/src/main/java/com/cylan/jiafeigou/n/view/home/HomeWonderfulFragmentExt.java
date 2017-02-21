package com.cylan.jiafeigou.n.view.home;

import android.app.SharedElementCallback;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.UiThread;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.ColorUtils;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.wrapper.BaseFragment;
import com.cylan.jiafeigou.cache.pool.GlobalDataProxy;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.OnActivityReenterListener;
import com.cylan.jiafeigou.misc.SharedElementCallBackListener;
import com.cylan.jiafeigou.n.mvp.contract.home.HomeWonderfulContract;
import com.cylan.jiafeigou.n.mvp.impl.home.HomeWonderfulPresenterImpl;
import com.cylan.jiafeigou.n.view.activity.MediaActivity;
import com.cylan.jiafeigou.n.view.adapter.HomeWonderfulAdapter;
import com.cylan.jiafeigou.n.view.record.DelayRecordActivity;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.superadapter.internal.SuperViewHolder;
import com.cylan.jiafeigou.utils.AnimatorUtils;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.TimeUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.utils.WonderGlideURL;
import com.cylan.jiafeigou.widget.ShadowFrameLayout;
import com.cylan.jiafeigou.widget.dialog.BaseDialog;
import com.cylan.jiafeigou.widget.dialog.SimpleDialogFragment;
import com.cylan.jiafeigou.widget.textview.WonderfulTitleHead;
import com.cylan.jiafeigou.widget.wheel.WheelView;
import com.cylan.jiafeigou.widget.wheel.WonderIndicatorWheelView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;

import static com.cylan.jiafeigou.dp.DpMsgDefine.DPWonderItem;
import static com.cylan.jiafeigou.n.mvp.contract.record.DelayRecordContract.View.VIEW_LAUNCH_WAY_WONDERFUL;

public class HomeWonderfulFragmentExt extends BaseFragment<HomeWonderfulContract.Presenter> implements
        HomeWonderfulContract.View, SwipeRefreshLayout.OnRefreshListener,
        HomeWonderfulAdapter.WonderfulItemClickListener,
        HomeWonderfulAdapter.WonderfulItemLongClickListener,
        BaseDialog.BaseDialogAction,
        AppBarLayout.OnOffsetChangedListener,
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
    FrameLayout mWonderfulEmptyViewContainer;
    @BindView(R.id.fragment_wonderful_empty)
    ViewGroup mWonderfulEmptyContainer;
    @BindView(R.id.fragment_wonderful_guide)
    ViewGroup mWonderfulGuideContainer;

    private WeakReference<WonderIndicatorWheelView> wheelViewWeakReference;
    private ShareDialogFragment shareDialogFragment;
    private WeakReference<SimpleDialogFragment> deleteDialogFragmentWeakReference;

    @BindView(R.id.tv_sec_title_head_wonder)
    TextView tvSecTitleHeadWonder;
    @BindView(R.id.appbar)
    AppBarLayout appbar;
    private HomeWonderfulAdapter homeWonderAdapter;
    private LinearLayoutManager mLinearLayoutManager;
    private ShadowFrameLayout mParent;
    private boolean isScrollShow = false;
    private boolean mCanRefresh = true;
    private boolean mHasMore;

    public static HomeWonderfulFragmentExt newInstance(Bundle bundle) {
        HomeWonderfulFragmentExt fragment = new HomeWonderfulFragmentExt();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void initViewAndListener() {
        homeWonderAdapter = new HomeWonderfulAdapter(getContext(), null, null);
        homeWonderAdapter.setWonderfulItemClickListener(this);
        homeWonderAdapter.setWonderfulItemLongClickListener(this);
        appbar.addOnOffsetChangedListener(this);
        srLayoutMainContentHolder.setNestedScrollingEnabled(false);
        initView();

        initProgressBarPosition();

        initHeaderView();

        initSomeViewMargin();
    }

    private SimpleDialogFragment initDeleteDialog() {
        if (deleteDialogFragmentWeakReference == null || deleteDialogFragmentWeakReference.get() == null) {
            //为删除dialog设置提示信息
            Bundle args = new Bundle();
            args.putString(BaseDialog.KEY_TITLE, "");
            args.putString(SimpleDialogFragment.KEY_LEFT_CONTENT, "");
            args.putString(SimpleDialogFragment.KEY_RIGHT_CONTENT, "");
            args.putString(SimpleDialogFragment.KEY_CONTENT_CONTENT,
                    this.getString(R.string.Tips_SureDelete));
            deleteDialogFragmentWeakReference = new WeakReference<>(SimpleDialogFragment.newInstance(args));
            deleteDialogFragmentWeakReference.get().setAction(this);
        }
        return deleteDialogFragmentWeakReference.get();
    }

    private ShareDialogFragment initShareDialog() {
        if (shareDialogFragment == null) {
            shareDialogFragment = ShareDialogFragment.newInstance();
        }
        return shareDialogFragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        onTimeTick(JFGRules.getTimeRule());
    }

    @Override
    protected boolean onBackPressed() {
        if (getWheelView().isShown()) {
            tvDateItemHeadWonder.setTimeLineShow(!getWheelView().isShown());
            tvDateItemHeadWonder.setBackgroundToRight();
            AnimatorUtils.slide(getWheelView());
            return true;//消费该事件
        }
        return false;
    }

    @Override
    protected HomeWonderfulContract.Presenter onCreatePresenter() {
        return new HomeWonderfulPresenterImpl();
    }

    @Override
    protected int getContentViewID() {
        return R.layout.fragment_home_wonderful_ext;
    }

    @Override
    public void onDetach() {
        super.onDetach();
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


    private void showWheelView() {
        if (!getWheelView().isShown()) {
            tvDateItemHeadWonder.setTimeLineShow(!getWheelView().isShown());
            tvDateItemHeadWonder.setBackgroundToRight();
            AnimatorUtils.slide(getWheelView(), new AnimatorUtils.OnEndListener() {
                @Override
                public void onAnimationEnd(boolean gone) {
                    getWheelView().scrollPositionToCenter();
                }

                @Override
                public void onAnimationStart(boolean gone) {
                }
            });
        }
    }

    private void hideWheelView() {
        if (getWheelView().isShown()) {
            tvDateItemHeadWonder.setTimeLineShow(!getWheelView().isShown());
            tvDateItemHeadWonder.setBackgroundToRight();
            AnimatorUtils.slide(getWheelView());
        }
    }

    private void initView() {
        //添加Handler
        srLayoutMainContentHolder.setOnRefreshListener(this);
        mLinearLayoutManager = new LinearLayoutManager(getContext());
        rVDevicesList.setLayoutManager(mLinearLayoutManager);
        rVDevicesList.setAdapter(homeWonderAdapter);
        rVDevicesList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int pastVisibleItems = mLinearLayoutManager.findFirstVisibleItemPosition();
                int visibleItemCount = mLinearLayoutManager.getChildCount();
                int totalItemCount = mLinearLayoutManager.getItemCount();
                if (dy > 0) { //check for scroll down
                    if ((visibleItemCount + pastVisibleItems) >= totalItemCount && homeWonderAdapter.getItem(totalItemCount - 1).msgType != DPWonderItem.TYPE_LOAD) {

                        if (!getWheelView().isShown()) {
                            isScrollShow = true;
                            showWheelView();
                        }
                    }
                } else {
                    if (isScrollShow) {
                        isScrollShow = false;
                        hideWheelView();
                    }
                }
            }
        });
    }

    private void initHeaderView() {
        tvDateItemHeadWonder.post(() -> tvDateItemHeadWonder.setText(TimeUtils.getTodayString()));
    }


    /**
     * 初始化,progressBar的位置.
     */
    private void initProgressBarPosition() {
        srLayoutMainContentHolder.setColorSchemeColors(Color.parseColor("#36BDFF"));
    }

    @UiThread
    @Override
    public void onQueryTimeLineSuccess(List<DPWonderItem> resultList, boolean isRefresh) {
        srLayoutMainContentHolder.setRefreshing(false);
        if (resultList == null || resultList.size() == 0) {
            ToastUtil.showNegativeToast("暂无数据!");
            return;
        }
        if (isRefresh) {
            homeWonderAdapter.addAll(0, resultList);
            rVDevicesList.scrollToPosition(0);
        } else {
            int lastPosition = homeWonderAdapter.getCount() - 1;
            DPWonderItem last = homeWonderAdapter.getItem(lastPosition);
            if (last.msgType == 2) {
                homeWonderAdapter.remove(last);
            }
            mHasMore = resultList.size() == 21;
            if (mHasMore) {
                homeWonderAdapter.addAll(resultList.subList(0, 20));
                homeWonderAdapter.add(DPWonderItem.getEmptyLoadTypeBean());
            } else {
                homeWonderAdapter.addAll(resultList);
            }
            homeWonderAdapter.notifyItemRangeChanged(lastPosition, 1);
        }
        homeWonderAdapter.notifyItemChanged(homeWonderAdapter.getCount() - 1);
        srLayoutMainContentHolder.setNestedScrollingEnabled(true);
    }

    @Override
    public void onHeadBackgroundChang(int daytime) {
        imgWonderfulTopBg.setBackgroundResource(daytime == 0 ? R.drawable.bg_wonderful_daytime : R.drawable.wonderful_bg_top_night);
    }

    @Override
    public void onTimeLineRsp(long dayStartTime, boolean b1, boolean b) {
        getWheelView().notify(dayStartTime, b1, b);
    }

    @Override
    public void onTimeLineInit(List<WonderIndicatorWheelView.WheelItem> list) {
        getWheelView().init(list);
    }

    @Override
    public void onDeleteWonderSuccess(int position) {
        homeWonderAdapter.remove(position);
        if (position < homeWonderAdapter.getCount())
            homeWonderAdapter.notifyItemChanged(position);
    }

    @Override
    public void onQueryTimeLineTimeOut() {
        ToastUtil.showNegativeToast(ContextUtils.getContext().getString(R.string.Clear_Sdcard_tips5));
        srLayoutMainContentHolder.setRefreshing(false);
    }

    @Override
    public void onChangeTimeLineDaySuccess(List<DPWonderItem> items) {
        if (getWheelView().isShown()) {
            getWheelView().scrollPositionToCenter();
        }
        homeWonderAdapter.clear();
        if (items.size() > 0)
            tvDateItemHeadWonder.setText(TimeUtils.getDayString(items.get(0).version));
        onQueryTimeLineSuccess(items, true);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onTimeTick(int dayTime) {

        //需要优化
        int drawableId = dayTime == JFGRules.RULE_DAY_TIME
                ? R.drawable.bg_wonderful_daytime : R.drawable.wonderful_bg_top_night;
        Glide.with(this)
                .load(drawableId)
                .asBitmap()
                .format(DecodeFormat.PREFER_ARGB_8888)
                .into(imgWonderfulTopBg);
        AppLogger.d("onTimeTick: " + dayTime);
    }

    @Override
    public void onPageScrolled() {
        final WonderIndicatorWheelView view = getWheelView();
        if (view != null && view.isShown()) {
            AppLogger.e("onPageScrolled");
            AnimatorUtils.slide(view);
        }
        if (srLayoutMainContentHolder != null)
            srLayoutMainContentHolder.setRefreshing(false);
    }

    @Override
    public void onWechatCheckRsp(boolean installed) {

    }

    @OnClick(R.id.item_wonderful_to_start)
    public void openWonderful() {
        if (GlobalDataProxy.getInstance().isOnline()) {//在线表示已登录
            Intent intent = new Intent(getActivityContext(), DelayRecordActivity.class);
            intent.putExtra(JConstant.VIEW_CALL_WAY, VIEW_LAUNCH_WAY_WONDERFUL);
            intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, mUUID);
            startActivity(intent);
        } else {//不在线表示还未登录
            RxBus.getCacheInstance().post(new RxEvent.NeedLoginEvent(null));
        }
    }

    @Override
    public void onLoginStateChanged(boolean online) {
        super.onLoginStateChanged(online);
        srLayoutMainContentHolder.setRefreshing(false);
    }

    @OnClick(R.id.tv_wonderful_item_share)
    public void shareWonderful() {//分享官方演示视频
        DPWonderItem bean = DPWonderItem.getGuideBean();
        onShareWonderfulContent(bean);
    }

    @OnClick(R.id.iv_wonderful_item_content)
    public void viewWonderful(View view) {
        DPWonderItem bean = DPWonderItem.getGuideBean();
        ArrayList<Parcelable> list = new ArrayList<>();
        list.add(bean);
        onEnterWonderfulContent(list, 0, view);
    }

    @OnClick(R.id.tv_wonderful_item_delete)
    public void removeAnymore() {
        deleteItem(-1);
    }

    @Override
    public void chooseEmptyView(int type) {
        switch (type) {
            case VIEW_TYPE_HIDE: {//hide
                mWonderfulEmptyViewContainer.setVisibility(View.GONE);
                mWonderfulGuideContainer.setVisibility(View.GONE);
                mWonderfulEmptyContainer.setVisibility(View.GONE);
                mCanRefresh = true;
            }
            break;
            case VIEW_TYPE_EMPTY: {//empty
                mCanRefresh = true;
                mWonderfulEmptyViewContainer.setVisibility(View.VISIBLE);
                mWonderfulGuideContainer.setVisibility(View.GONE);
                mWonderfulEmptyContainer.setVisibility(View.VISIBLE);

            }
            break;
            case VIEW_TYPE_GUIDE: {//guide
                mCanRefresh = false;
                mWonderfulEmptyViewContainer.setVisibility(View.VISIBLE);
                mWonderfulEmptyContainer.setVisibility(View.GONE);
                mWonderfulGuideContainer.setVisibility(View.VISIBLE);
            }
            break;
        }
        srLayoutMainContentHolder.setRefreshing(false);
    }

    @Override
    public void onRefresh() {
        if (mCanRefresh) {
            mPresenter.startRefresh();
            srLayoutMainContentHolder.setRefreshing(true);
        } else {
            srLayoutMainContentHolder.setRefreshing(false);
        }
    }

    private void onEnterWonderfulContent(ArrayList<? extends Parcelable> list, int position, View v) {
        final Intent intent = new Intent(getActivity(), MediaActivity.class);
        // Pass data object in the bundle and populate details activity.
        intent.putParcelableArrayListExtra(JConstant.KEY_SHARED_ELEMENT_LIST, list);
        intent.putExtra(JConstant.KEY_SHARED_ELEMENT_STARTED_POSITION, position);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mParent = (ShadowFrameLayout) v.getParent();
            mParent.adjustSize(true);
            ActivityOptionsCompat compat = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), v, v.getTransitionName());
            startActivity(intent, compat.toBundle());
        } else {
            startActivity(intent);
        }
        AppLogger.d("transition:getName " + ViewCompat.getTransitionName(v));
    }

    private void onShareWonderfulContent(DPWonderItem bean) {
        ShareDialogFragment fragment = initShareDialog();
        fragment.setPictureURL(new WonderGlideURL(bean));
        if (bean.msgType == DPWonderItem.TYPE_VIDEO) {
            fragment.setVideoURL(bean.fileName);
        }
        fragment.show(getActivity().getSupportFragmentManager(), "ShareDialogFragment");
    }

    private void onDeleteWonderfulContent(DPWonderItem bean, int position) {
        SimpleDialogFragment deleteF = initDeleteDialog();
        deleteF.setValue(position);
        deleteF.show(getActivity().getSupportFragmentManager(), "deleteDialogFragment");
    }

    @Override
    public void onClick(final View v) {
        final int position = ViewUtils.getParentAdapterPosition(rVDevicesList, v, R.id.lLayout_item_wonderful);
        if (position < 0 || position > homeWonderAdapter.getCount()) {
            AppLogger.d("woo,position is invalid: " + position);
            return;
        }
        switch (v.getId()) {
            case R.id.iv_wonderful_item_content:
                ArrayList<? extends Parcelable> list = (ArrayList<? extends Parcelable>) homeWonderAdapter.getList();
                onEnterWonderfulContent(list, position, v);
                break;
            case R.id.tv_wonderful_item_share:
                DPWonderItem bean = homeWonderAdapter.getItem(position);
                onShareWonderfulContent(bean);
                break;
            case R.id.tv_wonderful_item_delete:
                onDeleteWonderfulContent(null, position);
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
        SimpleDialogFragment fragment = deleteDialogFragmentWeakReference.get();
        fragment.setValue(position);
        fragment.show(getActivity().getSupportFragmentManager(), "ShareDialogFragment");
    }

    @OnClick(R.id.fLayout_date_head_wonder)
    public void onClick() {
        final WonderIndicatorWheelView view = getWheelView();
        if (view != null && view.hasInit()) {
            if (view.isShown()) {
                hideWheelView();
            } else {
                showWheelView();
            }
        }
    }


    /**
     * {@link WheelView}
     *
     * @return
     */
    private WonderIndicatorWheelView getWheelView() {
        if (wheelViewWeakReference == null || wheelViewWeakReference.get() == null) {
            if (getActivity() != null) {
                WonderIndicatorWheelView wheelView = (WonderIndicatorWheelView) getActivity().findViewById(R.id.act_main_wonder_indicator_view);
                wheelView.setListener(time -> {
                    isScrollShow = false;
                    tvDateItemHeadWonder.setText(TimeUtils.getDayString(time));
                    homeWonderAdapter.clear();
                    mPresenter.loadSpecificDay(time);
                });
                wheelViewWeakReference = new WeakReference<>(wheelView);
            }
        }
        return wheelViewWeakReference.get();
    }

    @Override
    public void onDialogAction(int id, Object value) {
        if (id == R.id.tv_dialog_btn_right)
            return;
        if (value == null || !(value instanceof Integer)) {
            AppLogger.i("value is null :" + value);
            return;
        }
        final int position = (int) value;
        if (position >= 0 && position < homeWonderAdapter.getCount()) {
            mPresenter.deleteTimeline(position);
        } else if (position == -1) {
            mPresenter.removeGuideAnymore();
        }
    }

    private float preRatio = -1;

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        final float ratio = (appbar.getTotalScrollRange() + verticalOffset) * 1.0f
                / appbar.getTotalScrollRange();
        if (preRatio == ratio) return;
        preRatio = ratio;
        final float alpha = 1.0f - ratio;
        if (imgWonderfulTitleCover.getAlpha() != alpha) {
            imgWonderfulTitleCover.setAlpha(alpha);
        }
        flDateBgHeadWonder.setAlpha(ratio);
        int i = ColorUtils.blendARGB(Color.WHITE, Color.parseColor("#788291"), ratio);
        tvDateItemHeadWonder.setTextColor(i);
        tvDateItemHeadWonder.setTitleHeadIsTop(ratio < 0.1);
        tvDateItemHeadWonder.setBackgroundToRight();
    }


    @Override
    public void onSharedElementCallBack(List<String> names, Map<String, View> sharedElements) {
        if (mTmpReenterState != null) {
            int startingPosition = mTmpReenterState.getInt(JConstant.EXTRA_STARTING_ALBUM_POSITION);
            int currentPosition = mTmpReenterState.getInt(JConstant.EXTRA_CURRENT_ALBUM_POSITION);

//            if (startingPosition != currentPosition) {
            // If startingPosition != currentPosition the user must have swiped to activity_cloud_live_mesg_call_out_item
            // different page in the DetailsActivity. We must setDevice the shared element
            // so that the correct one falls into place.
            String newTransitionName = currentPosition + JConstant.KEY_SHARED_ELEMENT_TRANSITION_NAME_SUFFIX;
            SuperViewHolder holder = (SuperViewHolder) rVDevicesList.findViewHolderForAdapterPosition(currentPosition);
            View newSharedElement;
            if (holder != null) {
                newSharedElement = holder.getView(R.id.iv_wonderful_item_content);
            } else {
                newSharedElement = mWonderfulGuideContainer.findViewById(R.id.iv_wonderful_item_content);
            }
            if (newSharedElement == null) return;
            ShadowFrameLayout parent = (ShadowFrameLayout) newSharedElement.getParent();
            if (mParent != parent) {
                mParent.adjustSize(false);
                int position = ViewUtils.getParentAdapterPosition(rVDevicesList, newSharedElement, R.id.lLayout_item_wonderful);
                homeWonderAdapter.notifyItemChanged(position);
            }
            AppLogger.d("transition newTransitionName: " + newTransitionName);
            AppLogger.d("transition newSharedElement: " + newSharedElement);
            names.clear();
            names.add(newTransitionName);
            sharedElements.clear();
            sharedElements.put(newTransitionName, newSharedElement);
            mTmpReenterState = null;
        }
    }

    @Override
    public void onSharedElementEnd(List<String> sharedElementNames, List<View> sharedElements, List<View> sharedElementSnapshots) {
        if (sharedElementNames.size() == 0) {
            if (homeWonderAdapter.getCount() == 0) {
                mPresenter.startRefresh();
            }
            return;
        }
        View view = sharedElements.get(0);
        if (view != null) {
            final ShadowFrameLayout parent = (ShadowFrameLayout) view.getParent();
            parent.post(() -> {
                parent.adjustSize(false);
                int position = ViewUtils.getParentAdapterPosition(rVDevicesList, view, R.id.lLayout_item_wonderful);
                if (position >= 0 && position < homeWonderAdapter.getCount()) {
                    homeWonderAdapter.notifyItemChanged(position);
                }
            });
        }
    }

    @Override
    public void onSharedElementArrived(List<String> sharedElementNames, List<View> sharedElements, SharedElementCallback.OnSharedElementsReadyListener listener) {

    }

    @Override
    public void onActivityReenter(int requestCode, Intent data) {
        AppLogger.d("transition onActivityReenter");
        mTmpReenterState = new Bundle(data.getExtras());
        int startingPosition = mTmpReenterState.getInt(JConstant.EXTRA_STARTING_ALBUM_POSITION);
        int currentPosition = mTmpReenterState.getInt(JConstant.EXTRA_CURRENT_ALBUM_POSITION);
        if (startingPosition != currentPosition) {
            ((LinearLayoutManager) rVDevicesList.getLayoutManager()).scrollToPositionWithOffset(currentPosition, 0);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getActivity().postponeEnterTransition();
        }
        rVDevicesList.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                rVDevicesList.getViewTreeObserver().removeOnPreDrawListener(this);
                rVDevicesList.requestLayout();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    getActivity().startPostponedEnterTransition();
                }
                return true;
            }
        });
    }

    private Bundle mTmpReenterState;
}
