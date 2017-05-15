package com.cylan.jiafeigou.n.view.bell;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.cylan.jiafeigou.NewHomeActivity;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.BaseFullScreenActivity;
import com.cylan.jiafeigou.base.injector.component.ActivityComponent;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.AlertDialogManager;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.NotifyManager;
import com.cylan.jiafeigou.misc.SpacesItemDecoration;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.engine.FirmwareCheckerService;
import com.cylan.jiafeigou.n.mvp.contract.bell.DoorBellHomeContract;
import com.cylan.jiafeigou.n.mvp.model.BellCallRecordBean;
import com.cylan.jiafeigou.n.view.adapter.BellCallRecordListAdapter;
import com.cylan.jiafeigou.n.view.firmware.FirmwareUpdateActivity;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.superadapter.OnItemClickListener;
import com.cylan.jiafeigou.support.superadapter.OnItemLongClickListener;
import com.cylan.jiafeigou.utils.AnimatorUtils;
import com.cylan.jiafeigou.utils.JFGGlideURL;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.BellTopBackgroundView;
import com.cylan.jiafeigou.widget.ImageViewTip;
import com.cylan.jiafeigou.widget.LoadingDialog;

import java.lang.ref.WeakReference;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import rx.Subscription;

import static com.cylan.jiafeigou.base.module.DPConstant.NET;

public class DoorBellHomeActivity extends BaseFullScreenActivity<DoorBellHomeContract.Presenter>
        implements DoorBellHomeContract.View,
        OnItemLongClickListener,
        OnItemClickListener,
        BellTopBackgroundView.ActionInterface,
        BellCallRecordListAdapter.LoadImageListener,
        ViewTreeObserver.OnGlobalLayoutListener, FragmentManager.OnBackStackChangedListener {
    @BindView(R.id.tv_top_bar_left)
    TextView imgVTopBarCenter;
    @BindView(R.id.fLayout_top_bar_container)
    FrameLayout fLayoutTopBarContainer;
    @BindView(R.id.rv_bell_list)
    RecyclerView rvBellList;
    @BindView(R.id.fLayout_bell_list_container)
    FrameLayout fLayoutBellListContainer;
    @BindView(R.id.tv_bell_home_list_cancel)
    TextView tvBellHomeListCancel;
    @BindView(R.id.tv_bell_home_list_select_all)
    TextView tvBellHomeListSelectAll;
    @BindView(R.id.tv_bell_home_list_delete)
    TextView tvBellHomeListDelete;
    @BindView(R.id.fLayout_bell_home_list_edition)
    FrameLayout fLayoutBellHomeListEdition;
    @BindView(R.id.cv_bell_home_background)
    BellTopBackgroundView cvBellHomeBackground;
    @BindView(R.id.fragment_bell_home_empty)
    ViewGroup mEmptyView;
    private WeakReference<BellSettingFragment> fragmentWeakReference;
    private WeakReference<LBatteryWarnDialog> lBatteryWarnDialog;
    private BellCallRecordListAdapter bellCallRecordListAdapter;
    /**
     * 加载更多
     */
    private boolean endlessLoading = false;
    private boolean mIsLastLoadFinish = true;
    private boolean mIsShardAccount = false;
    private long mLastEnterTime;
    private boolean mHasLoadInitFinished = false;
    private Subscription pageSub;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirmwareCheckerService.checkVersion(uuid);
    }

    @Override
    protected void initViewAndListener() {
        ViewUtils.setViewMarginStatusBar(fLayoutTopBarContainer);
        cvBellHomeBackground.setActionInterface(this);
        getSupportFragmentManager().addOnBackStackChangedListener(this);
        initAdapter();
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerNetWorkObserver();
        mLastEnterTime = PreferencesUtils.getLong(JConstant.KEY_BELL_LAST_ENTER_TIME_PREFIX + uuid, 0);
        int notifyId = (uuid + "bell").hashCode();
        NotifyManager.getNotifyManager().clearNotify(notifyId);

        try {
            sourceManager.clearValue(uuid, 1004, 1005);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        if (!mHasLoadInitFinished) {
            startLoadData(false, 0);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (lBatteryWarnDialog != null
                && lBatteryWarnDialog.get() != null
                && lBatteryWarnDialog.get().isResumed())
            lBatteryWarnDialog.get().dismiss();
        if (myReceiver != null) {
            unregisterReceiver(myReceiver);
        }
        PreferencesUtils.putLong(JConstant.KEY_BELL_LAST_ENTER_TIME_PREFIX + uuid, System.currentTimeMillis());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (pageSub != null && pageSub.isUnsubscribed()) {
            pageSub.unsubscribe();
            pageSub = null;
        }
    }

    @Override
    protected void setActivityComponent(ActivityComponent activityComponent) {
        activityComponent.inject(this);
    }

    @Override
    public void showFirmwareDialog() {
        AlertDialogManager.getInstance().showDialog(this,
                getString(R.string.Tap1_Device_UpgradeTips), getString(R.string.Tap1_Device_UpgradeTips),
                getString(R.string.OK), (DialogInterface dialog, int which) -> {
                    Intent intent = new Intent(this, FirmwareUpdateActivity.class);
                    intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
                    startActivity(intent);
                }, getString(R.string.CANCEL), null);
    }

    @Override
    protected int getContentViewID() {
        return R.layout.activity_door_bell;
    }

    private void initAdapter() {
        bellCallRecordListAdapter = new BellCallRecordListAdapter(getAppContext(),
                null, R.layout.layout_bell_call_list_item, this);
        bellCallRecordListAdapter.setOnItemClickListener(this);
        bellCallRecordListAdapter.setOnItemLongClickListener(this);
        rvBellList.setAdapter(bellCallRecordListAdapter);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getAppContext(), LinearLayoutManager.HORIZONTAL, false) {
            @Override
            public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
                try {
                    super.onLayoutChildren(recycler, state);
                } catch (IndexOutOfBoundsException e) {
                    AppLogger.e(e.getMessage());
                }
            }
        };
        rvBellList.setLayoutManager(linearLayoutManager);
        rvBellList.addItemDecoration(new SpacesItemDecoration(new Rect(ViewUtils.dp2px(10), ViewUtils.dp2px(15), 0, 0)));
        rvBellList.getViewTreeObserver().addOnGlobalLayoutListener(this);
        rvBellList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            int pastVisibleItems, visibleItemCount, totalItemCount;

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dx > 0) { //check for scroll down
                    visibleItemCount = linearLayoutManager.getChildCount();
                    totalItemCount = linearLayoutManager.getItemCount();
                    pastVisibleItems = linearLayoutManager.findFirstVisibleItemPosition();
                    if (!endlessLoading && mIsLastLoadFinish) {
                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                            List<BellCallRecordBean> list = bellCallRecordListAdapter.getList();
                            BellCallRecordBean bean = list.get(list.size() - 1);
                            startLoadData(false, bean.version);
                        }
                    }
                }
            }
        });
    }

    private void startLoadData(boolean asc, long version) {
        LoadingDialog.showLoading(getSupportFragmentManager(), getString(R.string.LOADING), false);
        mIsLastLoadFinish = false;
        presenter.fetchBellRecordsList(asc, version);
    }

    @OnClick({R.id.tv_top_bar_left, R.id.imgv_toolbar_right})
    public void onElementClick(View v) {
        switch (v.getId()) {
            case R.id.imgv_toolbar_right:
                ViewUtils.deBounceClick(v);
                initSettingFragment();
                BellSettingFragment fragment = fragmentWeakReference.get();
                getSupportFragmentManager().beginTransaction()
                        //如果需要动画，可以把动画添加进来
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                                , R.anim.slide_in_left, R.anim.slide_out_right)
                        .add(android.R.id.content, fragment, fragment.getClass().getSimpleName())
                        .addToBackStack(fragment.getClass().getSimpleName())
                        .commit();
                break;
            case R.id.tv_top_bar_left:
                onBackPressed();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        presenter.cancelFetch();
        LoadingDialog.dismissLoading(getSupportFragmentManager());
    }

    private void initSettingFragment() {
        if (fragmentWeakReference == null || fragmentWeakReference.get() == null) {
            fragmentWeakReference = new WeakReference<>(BellSettingFragment.newInstance(uuid));
        }
    }

    @Override
    protected boolean shouldExit() {
        return !checkExtraChildFragment() && !checkExtraFragment() && !reverseEditionMode();
    }

    @Override
    protected void onPrepareToExit(Action action) {
        finishExt();
        action.actionDone();
    }

    /**
     * 反转编辑模式
     *
     * @return
     */
    private boolean reverseEditionMode() {
        if (bellCallRecordListAdapter.getMode() == 1) {
            bellCallRecordListAdapter.setMode(0);
            final int lPos = ((LinearLayoutManager) rvBellList.getLayoutManager())
                    .findLastVisibleItemPosition();
            bellCallRecordListAdapter.reverseEdition(true, lPos);
            showEditBar(false);
            return true;
        }
        return false;
    }

    @Override
    public void onLoginStateChanged(boolean online) {
        super.onLoginStateChanged(online);
        if (!online) {
            LoadingDialog.dismissLoading(getSupportFragmentManager());
            mEmptyView.postDelayed(() -> LoadingDialog.dismissLoading(getSupportFragmentManager()), 300);//防止 loadingDialog还没添加数据就已经返回了导致dismiss 不掉
        }
    }

    @Override
    public void onBellBatteryDrainOut() {
        if (JFGRules.isShareDevice(uuid)) return;
        initBatteryDialog();
        LBatteryWarnDialog dialog = lBatteryWarnDialog.get();
        if (!dialog.isAdded())
            dialog.show(getSupportFragmentManager(), "lBattery");
    }

    private void initBatteryDialog() {
        if (lBatteryWarnDialog == null || lBatteryWarnDialog.get() == null)
            lBatteryWarnDialog = new WeakReference<>(LBatteryWarnDialog.newInstance(null));
    }

    @Override
    public void onRecordsListRsp(List<BellCallRecordBean> beanArrayList) {
        mHasLoadInitFinished = true;
        LoadingDialog.dismissLoading(getSupportFragmentManager());
        mEmptyView.postDelayed(() -> LoadingDialog.dismissLoading(getSupportFragmentManager()), 300);//防止 loadingDialog还没添加数据就已经返回了导致dismiss 不掉
        if (beanArrayList != null && beanArrayList.size() < 20) endlessLoading = true;
        bellCallRecordListAdapter.addAll(beanArrayList);
        mIsLastLoadFinish = true;
        boolean isEmpty = bellCallRecordListAdapter.getList().size() == 0;
        mEmptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onQueryRecordListTimeOut() {
        if (LoadingDialog.isShowing(getSupportFragmentManager())) {
            LoadingDialog.dismissLoading(getSupportFragmentManager());
            ToastUtil.showNegativeToast(getString(R.string.Request_TimeOut));
        }
    }

    @Override
    public void onDeleteBellRecordSuccess(List<BellCallRecordBean> list) {
        ToastUtil.showPositiveToast("刪除成功");
        LoadingDialog.dismissLoading(getSupportFragmentManager());
        for (BellCallRecordBean bean : list) {
            bellCallRecordListAdapter.remove(bean);
        }
        boolean isEmpty = bellCallRecordListAdapter.getList().size() == 0;
        mEmptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDeleteBellCallRecordFailed() {
        ToastUtil.showNegativeToast("刪除失敗");
    }

    @Override
    public void onBellRecordCleared() {
        bellCallRecordListAdapter.clear();
        mEmptyView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDeviceUnBind() {
        AppLogger.d("当前设备已解绑");
        new AlertDialog.Builder(this).setCancelable(false)
                .setPositiveButton(getString(R.string.OK), (dialog, which) -> {
                    finish();
                    Intent intent = new Intent(this, NewHomeActivity.class);
                    startActivity(intent);
                })
                .setMessage(getString(R.string.Tap1_device_deleted))
                .show();
    }

    @Override
    public void onFinish() {
        finish();
    }


    private void showEditBar(boolean show) {
        AnimatorUtils.slide(fLayoutBellHomeListEdition);
    }


    @OnClick({R.id.tv_bell_home_list_cancel, R.id.tv_bell_home_list_select_all, R.id.tv_bell_home_list_delete})
    public void onEditBarClick(View view) {
        final int lPos = ((LinearLayoutManager) rvBellList.getLayoutManager())
                .findLastVisibleItemPosition();
        switch (view.getId()) {
            case R.id.tv_bell_home_list_cancel:
                bellCallRecordListAdapter.reverseEdition(true, lPos);
                bellCallRecordListAdapter.setMode(0);
                showEditBar(false);
                break;
            case R.id.tv_bell_home_list_select_all:
                if (TextUtils.equals(tvBellHomeListSelectAll.getText(), getString(R.string.SELECT_ALL))) {
                    bellCallRecordListAdapter.selectAll(lPos);
//                    tvBellHomeListSelectAll.setText(getString(R.string.SELECT_NONE));
                } else {
                    bellCallRecordListAdapter.selectNone(lPos);
                    tvBellHomeListSelectAll.setText(getString(R.string.SELECT_ALL));
                }
                break;
            case R.id.tv_bell_home_list_delete:
                ViewUtils.deBounceClick(view);
                new AlertDialog.Builder(this)
                        .setMessage(getString(R.string.DOOR_COMFIRETOCLEAR))
                        .setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                List<BellCallRecordBean> list = bellCallRecordListAdapter.getSelectedList();
                                presenter.deleteBellCallRecord(list);
                                bellCallRecordListAdapter.setMode(0);
                                showEditBar(false);
                                LoadingDialog.showLoading(getSupportFragmentManager(), getString(R.string.DELETEING));
                            }
                        })
                        .setNegativeButton(getString(R.string.CANCEL), null)
                        .create().show();
                break;
        }
    }

    @Override
    public void onMakeCall() {
        Intent intent = new Intent(this, BellLiveActivity.class);
        intent.putExtra(JConstant.VIEW_CALL_WAY, JConstant.VIEW_CALL_WAY_VIEWER);
        intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
        startActivity(intent);
    }

    @Override
    public void loadMedia(final BellCallRecordBean item, final ImageView imageView) {
        ((ImageViewTip) imageView).setShowDot(item.answerState == 0 && item.timeInLong > mLastEnterTime);
        Glide.with(this)
                .load(new JFGGlideURL(uuid, item.timeInLong / 1000 + ".jpg"))
                .asBitmap()
                .placeholder(R.drawable.pic_head_normal240px)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(new BitmapImageViewTarget(imageView) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(getAppContext().getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        if (imageView instanceof ImageViewTip) {
                            //顺便实现了红点。
                            ((ImageViewTip) imageView).setImageDrawable(circularBitmapDrawable, item.answerState == 0 && item.timeInLong > mLastEnterTime);
                        }
                    }
                });
    }

    @Override
    public void onGlobalLayout() {
        if (bellCallRecordListAdapter != null && bellCallRecordListAdapter.getCount() > 0) {
            int w = rvBellList.getLayoutManager().getChildAt(0).getMeasuredWidth();
            int h = rvBellList.getLayoutManager().getChildAt(0).getMeasuredHeight();
            bellCallRecordListAdapter.setItemHeight(h);
            bellCallRecordListAdapter.setItemWidth(w);
            rvBellList.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        }
    }

    @Override
    public void onShowProperty(Device device) {
        imgVTopBarCenter.setText(TextUtils.isEmpty(device.alias) ? device.uuid : device.alias);
        if (isNetworkConnected(this)) {
            DpMsgDefine.DPNet net = device.$(NET, new DpMsgDefine.DPNet());
            cvBellHomeBackground.setState(net.net);
        } else {
            cvBellHomeBackground.setState(2);
        }
        mIsShardAccount = !TextUtils.isEmpty(device.shareAccount);
    }

    private void registerNetWorkObserver() {
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        myReceiver = new ConnectionChangeReceiver();
        this.registerReceiver(myReceiver, filter);
    }

    private ConnectionChangeReceiver myReceiver;

    @Override
    public void onItemClick(View itemView, int viewType, int position) {
        if (position < 0 || position >= bellCallRecordListAdapter.getCount()) {
            AppLogger.d("position is invalid");
            return;
        }
        if (bellCallRecordListAdapter.getMode() == 1) {//编辑模式下的点击事件
            bellCallRecordListAdapter.reverseItemSelectedState(position);
            int count = bellCallRecordListAdapter.getSelectedList().size();
            if (count == 0) {
                bellCallRecordListAdapter.setMode(0);
                showEditBar(false);
            }
        } else {//普通模式下的点击事件,即查看大图模式
            Intent intent = new Intent(this, BellRecordDetailActivity.class);
            intent.putExtra(JConstant.KEY_DEVICE_ITEM_BUNDLE, bellCallRecordListAdapter.getItem(position));
            intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
            startActivity(intent);
        }

    }


    @Override
    public void onItemLongClick(View itemView, int viewType, int position) {
        if (mIsShardAccount)//共享账号不可操作
            return;
        if (position < 0 || position >= bellCallRecordListAdapter.getCount()) {
            AppLogger.d("position is invalid");
            return;
        }
        //toggle edit mode
        if (bellCallRecordListAdapter.getMode() == 0) {
            AppLogger.d("enter edition mode");
            bellCallRecordListAdapter.setMode(1);
            bellCallRecordListAdapter.reverseItemSelectedState(position);
            tvBellHomeListSelectAll.setText(getString(R.string.SELECT_ALL));
            showEditBar(true);
        }
    }

    @Override
    public void onBackStackChanged() {
        final int count = getSupportFragmentManager().getBackStackEntryCount();
        if (count == 0) {
            presenter.onStart();
        } else {
            for (Fragment fragment : getSupportFragmentManager().getFragments()) {
                if (fragment instanceof BellSettingFragment) {
                    fragment.onStart();
                }
            }
        }
    }

    public class ConnectionChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mobNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            NetworkInfo wifiNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (!mobNetInfo.isConnected() && !wifiNetInfo.isConnected()) {
                cvBellHomeBackground.setState(2);
                //改变背景或者 处理网络的全局变量
            } else {
                //改变背景或者 处理网络的全局变量
                DpMsgDefine.DPNet net = sourceManager.getValue(uuid, DpMsgMap.ID_201_NET, new DpMsgDefine.DPNet());
                if (net != null) {
                    cvBellHomeBackground.setState(net.net);
                } else {
                    cvBellHomeBackground.setState(0);
                }
            }
        }
    }

    public boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }
}
