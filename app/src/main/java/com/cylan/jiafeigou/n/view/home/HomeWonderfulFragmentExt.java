package com.cylan.jiafeigou.n.view.home;

import android.app.SharedElementCallback;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.injector.component.FragmentComponent;
import com.cylan.jiafeigou.base.wrapper.BaseFragment;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.OnActivityReenterListener;
import com.cylan.jiafeigou.misc.SharedElementCallBackListener;
import com.cylan.jiafeigou.n.mvp.contract.home.HomeWonderfulContract;
import com.cylan.jiafeigou.n.view.activity.MediaActivity;
import com.cylan.jiafeigou.n.view.adapter.HomeWonderfulAdapter;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.share.ShareManager;
import com.cylan.jiafeigou.support.superadapter.internal.SuperViewHolder;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.utils.WonderGlideURL;
import com.cylan.jiafeigou.widget.ShadowFrameLayout;
import com.cylan.jiafeigou.widget.dialog.BaseDialog;
import com.cylan.jiafeigou.widget.dialog.SimpleDialogFragment;
import com.cylan.jiafeigou.widget.wheel.WonderIndicatorWheelView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.cylan.jiafeigou.dp.DpMsgDefine.DPWonderItem;

public class HomeWonderfulFragmentExt extends BaseFragment<HomeWonderfulContract.Presenter> implements
        HomeWonderfulContract.View, SwipeRefreshLayout.OnRefreshListener,
        HomeWonderfulAdapter.WonderfulItemClickListener,
        HomeWonderfulAdapter.WonderfulItemLongClickListener,
        BaseDialog.BaseDialogAction,
        AppBarLayout.OnOffsetChangedListener,
        SharedElementCallBackListener,
        OnActivityReenterListener {
    @BindView(R.id.rV_wonderful_list)
    RecyclerView rVDevicesList;
    @BindView(R.id.fLayout_main_content_holder)
    SwipeRefreshLayout srLayoutMainContentHolder;
    //    @BindView(R.msgId.fLayoutHomeWonderfulHeaderContainer)
//    FrameLayout fLayoutHomeHeaderContainer;
    @BindView(R.id.rl_top_head_wonder)
    RelativeLayout rlTopHeadWonder;
    @BindView(R.id.img_wonderful_title_cover)
    ImageView imgWonderfulTitleCover;
    @BindView(R.id.tv_date_item_head_wonder)
    TextView mHeaderContentTitle;
    @BindView(R.id.tv_title_head_wonder)
    TextView tvTitleHeadWonder;
    @BindView(R.id.fLayout_empty_view_container)
    FrameLayout mWonderfulEmptyViewContainer;
    @BindView(R.id.fragment_wonderful_empty)
    ViewGroup mWonderfulEmptyContainer;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    Unbinder unbinder;
//    @BindView(R.msgId.fragment_wonderful_guide)
//    ViewGroup mWonderfulGuideContainer;

    private WeakReference<SimpleDialogFragment> deleteDialogFragmentWeakReference;

    @BindView(R.id.tv_sec_title_head_wonder)
    TextView tvSecTitleHeadWonder;
    @BindView(R.id.appbar)
    AppBarLayout appbar;
    private HomeWonderfulAdapter homeWonderAdapter;
    private LinearLayoutManager mLinearLayoutManager;
    private boolean mHasMore;
    private boolean isLoading = false;
    private boolean isPrepaper = false;
    private View rootView;

    public static HomeWonderfulFragmentExt newInstance(Bundle bundle) {
        HomeWonderfulFragmentExt fragment = new HomeWonderfulFragmentExt();
        fragment.setArguments(bundle);
        return fragment;
    }


//    @Override
//    public void setUserVisibleHint(boolean isVisibleToUser) {
//        super.setUserVisibleHint(isVisibleToUser);
//        if (getUserVisibleHint()) lazyLoad();
//        else {
//            if (srLayoutMainContentHolder != null)
//                srLayoutMainContentHolder.removeCallbacks(autoLoading);//
//        }
//    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
        if (presenter != null) {
            presenter.onSetContentView();//有些view会根据一定的条件显示不同的view,可以在这个方法中进行条件判断
        }
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
        isPrepaper = true;
//        lazyLoad();
    }

    private Runnable autoLoading = () -> presenter.startRefresh();

    @Override
    public void onStart() {
        super.onStart();
        srLayoutMainContentHolder.setRefreshing(true);
        presenter.startRefresh();
    }

//    private void lazyLoad() {
//        if (getUserVisibleHint() && isPrepaper && sourceManager.getAccount() != null && sourceManager.getAccount().isAvailable()) {
//            srLayoutMainContentHolder.setRefreshing(true);
//            srLayoutMainContentHolder.postDelayed(autoLoading, 100);//避免刷新过快
//        }
//    }

    private SimpleDialogFragment initDeleteDialog() {
        if (deleteDialogFragmentWeakReference == null || deleteDialogFragmentWeakReference.get() == null) {
            //为删除dialog设置提示信息
            Bundle args = new Bundle();
            args.putString(BaseDialog.KEY_TITLE, "");
            args.putString(SimpleDialogFragment.KEY_LEFT_CONTENT, getString(R.string.OK));
            args.putString(SimpleDialogFragment.KEY_RIGHT_CONTENT, getString(R.string.CANCEL));
            args.putString(SimpleDialogFragment.KEY_CONTENT_CONTENT,
                    this.getString(R.string.Tips_SureDelete));
            deleteDialogFragmentWeakReference = new WeakReference<>(SimpleDialogFragment.newInstance(args));
            deleteDialogFragmentWeakReference.get().setAction(this);
        }
        return deleteDialogFragmentWeakReference.get();
    }

    @Override
    public void onResume() {
        super.onResume();
        onTimeTick(JFGRules.getTimeRule());
    }

    @Override
    protected int getContentViewID() {
        return R.layout.fragment_home_wonderful_ext;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    protected void setFragmentComponent(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }


    private void initSomeViewMargin() {
//        ViewUtils.setFitsSystemWindowsCompat(appbar);
//        ViewUtils.setViewPaddingStatusBar(appbar);
    }

    private void initView() {
        //添加Handler
        srLayoutMainContentHolder.setOnRefreshListener(this);
        mLinearLayoutManager = new LinearLayoutManager(getContext()) {
            @Override
            public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
                try {
                    super.onLayoutChildren(recycler, state);
                } catch (IndexOutOfBoundsException e) {
                    AppLogger.e(e.getMessage());
                }
            }
        };
        rVDevicesList.setLayoutManager(mLinearLayoutManager);
        rVDevicesList.setAdapter(homeWonderAdapter);
        rVDevicesList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int pastVisibleItems = mLinearLayoutManager.findFirstVisibleItemPosition();
                int visibleItemCount = mLinearLayoutManager.getChildCount();
                int totalItemCount = mLinearLayoutManager.getItemCount();
                if (dy > 0) { //check for scroll down
                    if (pastVisibleItems + visibleItemCount >= totalItemCount && mHasMore && getUserVisibleHint() && !isLoading) {
                        isLoading = true;
                        presenter.startLoadMore();
                    }
                }
            }
        });
        ViewUtils.setViewMarginStatusBar(toolbar);
    }

    private void initHeaderView() {
//        tvDateItemHeadWonder.post(() -> tvDateItemHeadWonder.setText(TimeUtils.getTodayString()));
        imgWonderfulTitleCover.setBackgroundResource(JFGRules.getTimeRule() == 0 ? R.color.color_0ba8cf : R.color.color_23344e);
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
        isLoading = false;
        if (!getUserVisibleHint()) return;
        srLayoutMainContentHolder.setRefreshing(false);
        mHasMore = resultList.size() == 15;
        int lastPosition = homeWonderAdapter.getCount() - 1;
        if (isRefresh) {
            homeWonderAdapter.clear();
            homeWonderAdapter.addAll(resultList);

        } else {
            DPWonderItem last = homeWonderAdapter.getItem(lastPosition);
            if (last.msgType == 2 || last.msgType == 3) {
                homeWonderAdapter.remove(last);
            }
            homeWonderAdapter.addAll(resultList);

        }
        if (mHasMore) {
            homeWonderAdapter.add(DPWonderItem.getEmptyLoadTypeBean());

        } else {
            homeWonderAdapter.add(DPWonderItem.getNoMoreTypeBean());
        }
        homeWonderAdapter.notifyItemRangeChanged(lastPosition, 1);
        homeWonderAdapter.notifyItemChanged(homeWonderAdapter.getCount() - 1);
        if (homeWonderAdapter.getCount() > 0) {
            srLayoutMainContentHolder.setNestedScrollingEnabled(true);
        }
    }

    @Override
    public void onHeadBackgroundChang(int daytime) {
        appbar.setBackgroundResource(daytime == 0 ? R.drawable.bg_wonderful_daytime : R.drawable.wonderful_bg_top_night);
    }

    @Override
    public void onTimeLineRsp(long dayStartTime, boolean b1, boolean b) {

    }

    @Override
    public void onTimeLineInit(List<WonderIndicatorWheelView.WheelItem> list) {

    }

    @Override
    public void onDeleteWonderSuccess(int position) {
        homeWonderAdapter.remove(position);
        if (position < homeWonderAdapter.getCount())
            homeWonderAdapter.notifyItemChanged(position);
        int pastVisibleItems = mLinearLayoutManager.findFirstVisibleItemPosition();
        int visibleItemCount = mLinearLayoutManager.getChildCount();
        int totalItemCount = mLinearLayoutManager.getItemCount();
        if (pastVisibleItems + visibleItemCount >= totalItemCount && mHasMore) {
            presenter.startLoadMore();

        }
    }

    @Override
    public void onQueryTimeLineTimeOut() {
        srLayoutMainContentHolder.setRefreshing(false);
    }

    @Override
    public void onChangeTimeLineDaySuccess(List<DPWonderItem> items) {
        homeWonderAdapter.clear();
        if (items.size() > 0)
            onQueryTimeLineSuccess(items, true);
    }

    @Override
    public void onQueryTimeLineCompleted() {
        srLayoutMainContentHolder.setRefreshing(false);
        isLoading = false;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onTimeTick(int dayTime) {
        //需要优化
        int drawableId = dayTime == JFGRules.RULE_DAY_TIME
                ? R.drawable.wonderful_bg_top_daytime : R.drawable.wonderful_bg_top_night;
        appbar.setBackgroundResource(drawableId);
        AppLogger.d("onTimeTick: " + dayTime);
    }

    @Override
    public void onPageScrolled() {
        if (srLayoutMainContentHolder != null)
            srLayoutMainContentHolder.setRefreshing(false);
    }

    @Override
    public void onWechatCheckRsp(boolean installed) {

    }


    @Override
    public void onLoginStateChanged(boolean online) {
        super.onLoginStateChanged(online);
        srLayoutMainContentHolder.setRefreshing(false);
    }

    @Override
    public void chooseEmptyView(int type) {
        switch (type) {
            case VIEW_TYPE_HIDE: {//hide
                mWonderfulEmptyViewContainer.setVisibility(View.GONE);
//                mWonderfulGuideContainer.setVisibility(View.GONE);
                mWonderfulEmptyContainer.setVisibility(View.GONE);
            }
            break;
            case VIEW_TYPE_EMPTY: {//empty
                mWonderfulEmptyViewContainer.setVisibility(View.VISIBLE);
//                mWonderfulGuideContainer.setVisibility(View.GONE);
                mWonderfulEmptyContainer.setVisibility(View.VISIBLE);

            }
            break;
            case VIEW_TYPE_GUIDE: {//guide
                mWonderfulEmptyViewContainer.setVisibility(View.VISIBLE);
                mWonderfulEmptyContainer.setVisibility(View.GONE);
//                mWonderfulGuideContainer.setVisibility(View.VISIBLE);
            }
            break;
        }
        srLayoutMainContentHolder.setRefreshing(false);
    }

    @Override
    public void onRefresh() {
        if (sourceManager.getAccount() != null && sourceManager.getAccount().isAvailable()) {
            srLayoutMainContentHolder.removeCallbacks(autoLoading);
            presenter.startRefresh();
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
            ((ShadowFrameLayout) v.getParent()).adjustSize(true);
            ActivityOptionsCompat compat = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), v, v.getTransitionName());
            startActivity(intent, compat.toBundle());
        } else {
            startActivity(intent);
        }
        AppLogger.d("transition:getName " + ViewCompat.getTransitionName(v));
    }

    private void onShareWonderfulContent(DPWonderItem bean) {
//        ShareOptionMenuDialog fragment = initShareDialog();
//        fragment.setPictureURL(new WonderGlideURL(bean));
//        if (bean.msgType == DPWonderItem.TYPE_VIDEO) {
//            fragment.setVideoURL(bean.fileName);
//        }
//        fragment.show(getActivity().getSupportFragmentManager(), "ShareOptionMenuDialog");
        new WonderGlideURL(bean).fetchFile(filePath -> {
            ShareManager.byImg(getActivity())
                    .withImg(filePath)
                    .share();
//            Intent intent = new Intent(getActivity(), ShareMediaActivity.class);
//            intent.putExtra(ShareConstant.SHARE_CONTENT_PICTURE_EXTRA_IMAGE_PATH, filePath);
//            intent.putExtra(ShareConstant.SHARE_CONTENT, ShareConstant.SHARE_CONTENT_PICTURE);
//            startActivity(intent);
        });
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
                List<DPWonderItem> wonderItems = homeWonderAdapter.getList();
                if (wonderItems != null && wonderItems.size() > 0) {
                    DPWonderItem item = wonderItems.get(wonderItems.size() - 1);
                    if (item.msgType != DPWonderItem.TYPE_PIC && item.msgType != DPWonderItem.TYPE_VIDEO) {
                        wonderItems.remove(item);
                    }
                    ArrayList<? extends Parcelable> list = (ArrayList<? extends Parcelable>) wonderItems;
                    onEnterWonderfulContent(list, position, v);
                }
                break;
            case R.id.tv_wonderful_item_share:
                if (NetUtils.isNetworkAvailable(getContext())) {
                    DPWonderItem bean = homeWonderAdapter.getItem(position);
                    onShareWonderfulContent(bean);
                } else {
                    ToastUtil.showNegativeToast(getString(R.string.OFFLINE_ERR_1));
                }
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
        fragment.show(getActivity().getSupportFragmentManager(), "ShareOptionMenuDialog");
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
            presenter.deleteTimeline(position);
        } else if (position == -1) {
            presenter.removeGuideAnymore();
        }
    }

    private float preRatio = -1;

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        final float ratio = (appbar.getTotalScrollRange() + verticalOffset) * 1.0f
                / appbar.getTotalScrollRange();
        if (preRatio == ratio) return;
        preRatio = ratio;
        Log.d("WonderfulFragmentExt", "WonderfulFragmentExt: " + verticalOffset);
        final float alpha = 1.0f - ratio;
        if (imgWonderfulTitleCover.getAlpha() != alpha) {
            imgWonderfulTitleCover.setAlpha(alpha);
            mHeaderContentTitle.setAlpha(alpha);
        }
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
            View newSharedElement = null;
            if (holder != null) {
                newSharedElement = holder.getView(R.id.iv_wonderful_item_content);
            } else {
//                newSharedElement = mWonderfulGuideContainer.findViewById(R.msgId.iv_wonderful_item_content);
            }
            if (newSharedElement == null) return;
            ((ShadowFrameLayout) newSharedElement.getParent()).adjustSize(true);
            SuperViewHolder holders = (SuperViewHolder) rVDevicesList.findViewHolderForAdapterPosition(currentPosition);
            View oldView = null;
            if (holders != null) {
                oldView = holders.getView(R.id.iv_wonderful_item_content);
            } else {
//                oldView = mWonderfulGuideContainer.findViewById(R.msgId.iv_wonderful_item_content);
            }
            ((ShadowFrameLayout) oldView.getParent()).adjustSize(false);
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
        if (sharedElements.size() > 0)
            mWonderfulEmptyViewContainer.post(() -> ((ShadowFrameLayout) sharedElements.get(0).getParent()).adjustSize(false));
    }

    @Override
    public void onSharedElementArrived(List<String> sharedElementNames, List<View> sharedElements, SharedElementCallback.OnSharedElementsReadyListener listener) {

    }

    @Override
    public void onSharedElementStart(List<String> sharedElementNames, List<View> sharedElements, List<View> sharedElementSnapshots) {
        ((ShadowFrameLayout) sharedElements.get(0)).adjustSize(true);
    }

    @Override
    public void onActivityReenter(int requestCode, Intent data) {
        AppLogger.d("transition onActivityReenter");
        mTmpReenterState = new Bundle(data.getExtras());
        int startingPosition = mTmpReenterState.getInt(JConstant.EXTRA_STARTING_ALBUM_POSITION);
        int currentPosition = mTmpReenterState.getInt(JConstant.EXTRA_CURRENT_ALBUM_POSITION);
//        if (startingPosition != currentPosition) {
        rVDevicesList.getLayoutManager().scrollToPosition(currentPosition);
//        }
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO: inflate a fragment view
        if (rootView == null) {
            rootView = super.onCreateView(inflater, container, savedInstanceState);
            initViewAndListener();
        }
        unbinder = ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
