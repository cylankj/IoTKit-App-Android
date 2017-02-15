package com.cylan.jiafeigou.n.view.bell;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.cylan.entity.JfgEnum;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.BaseFullScreenActivity;
import com.cylan.jiafeigou.base.module.JFGDoorBellDevice;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.SpacesItemDecoration;
import com.cylan.jiafeigou.n.mvp.contract.bell.DoorBellHomeContract;
import com.cylan.jiafeigou.n.mvp.impl.bell.DBellHomePresenterImpl;
import com.cylan.jiafeigou.n.mvp.model.BellCallRecordBean;
import com.cylan.jiafeigou.n.view.adapter.BellCallRecordListAdapter;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.AnimatorUtils;
import com.cylan.jiafeigou.utils.JFGGlideURL;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.BellTopBackgroundView;
import com.cylan.jiafeigou.widget.ImageViewTip;
import com.cylan.jiafeigou.widget.LoadingDialog;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class DoorBellHomeActivity extends BaseFullScreenActivity<DoorBellHomeContract.Presenter>
        implements DoorBellHomeContract.View,
        BellCallRecordListAdapter.SimpleLongClickListener,
        BellCallRecordListAdapter.SimpleClickListener,
        BellTopBackgroundView.ActionInterface,
        BellCallRecordListAdapter.LoadImageListener,
        ViewTreeObserver.OnGlobalLayoutListener {
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
    private WeakReference<BellSettingFragment> fragmentWeakReference;
    private WeakReference<LBatteryWarnDialog> lBatteryWarnDialog;
    private BellCallRecordListAdapter bellCallRecordListAdapter;
    /**
     * 加载更多
     */
    private boolean endlessLoading = false;
    private boolean mIsLastLoadFinish = true;
    private boolean isFirst = true;


    @Override
    protected void initViewAndListener() {
        ViewUtils.setViewMarginStatusBar(fLayoutTopBarContainer);
        initAdapter();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bellCallRecordListAdapter.getList() == null || bellCallRecordListAdapter.getList().size() == 0) {
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
        imgVTopBarCenter.removeCallbacks(mLoadAction);
    }

    @Override
    protected DoorBellHomeContract.Presenter onCreatePresenter() {
        return new DBellHomePresenterImpl();
    }

    @Override
    protected int getContentViewID() {
        return R.layout.activity_door_bell;
    }

    private void initAdapter() {
        bellCallRecordListAdapter = new BellCallRecordListAdapter(getAppContext(),
                null, R.layout.layout_bell_call_list_item, this);
        bellCallRecordListAdapter.setSimpleClickListener(this);
        bellCallRecordListAdapter.setSimpleLongClickListener(this);
        rvBellList.setAdapter(bellCallRecordListAdapter);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getAppContext(), LinearLayoutManager.HORIZONTAL, false);
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

    private Runnable mLoadAction = () -> {
        if (LoadingDialog.isShowing(getSupportFragmentManager())) {
            LoadingDialog.dismissLoading(getSupportFragmentManager());
            ToastUtil.showNegativeToast(getString(R.string.REQUEST_TIME_OUT));
        }
    };

    private void startLoadData(boolean asc, long version) {
        LoadingDialog.showLoading(getSupportFragmentManager(), getString(R.string.LOADING), true);
        mIsLastLoadFinish = false;
        mPresenter.fetchBellRecordsList(asc, version);
        imgVTopBarCenter.postDelayed(mLoadAction, 10000);
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

    private void initSettingFragment() {
        if (fragmentWeakReference == null || fragmentWeakReference.get() == null) {
            fragmentWeakReference = new WeakReference<>(BellSettingFragment.newInstance(mUUID));
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
        }
    }

    @Override
    public void onBellBatteryDrainOut() {
        initBatteryDialog();
        LBatteryWarnDialog dialog = lBatteryWarnDialog.get();
        dialog.show(getSupportFragmentManager(), "lBattery");
    }

    private void initBatteryDialog() {
        if (lBatteryWarnDialog == null || lBatteryWarnDialog.get() == null)
            lBatteryWarnDialog = new WeakReference<>(LBatteryWarnDialog.newInstance(null));
    }

    @Override
    public void onRecordsListRsp(ArrayList<BellCallRecordBean> beanArrayList) {
        imgVTopBarCenter.removeCallbacks(mLoadAction);
        LoadingDialog.dismissLoading(getSupportFragmentManager());
        if (beanArrayList != null && beanArrayList.size() < 20) endlessLoading = true;
        bellCallRecordListAdapter.addAll(beanArrayList);
        mIsLastLoadFinish = true;
        if (bellCallRecordListAdapter.getList().size() == 0) {//show empty startViewer
            Toast.makeText(this, "暂无数据", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public boolean onLongClick(View v) {
//        if (!TextUtils.isEmpty(mPresenter.getBellInfo().deviceBase.shareAccount))//共享账号不可操作
//            return true;
        final int position = ViewUtils.getParentAdapterPosition(rvBellList, v, R.id.cv_bell_call_item);
        if (position < 0 || position >= bellCallRecordListAdapter.getCount()) {
            AppLogger.d("position is invalid");
            return false;
        }
        //toggle edit mode
        if (bellCallRecordListAdapter.getMode() == 0) {
            AppLogger.d("enter edition mode");
            bellCallRecordListAdapter.setMode(1);
            bellCallRecordListAdapter.reverseItemSelectedState(position);
            tvBellHomeListSelectAll.setText(getString(R.string.SELECT_ALL));
            showEditBar(true);
        }
        return true;
    }

    private void showEditBar(boolean show) {
        AnimatorUtils.slide(fLayoutBellHomeListEdition);
    }

    @Override
    public void onClick(View v) {
        final int position = ViewUtils.getParentAdapterPosition(rvBellList, v, R.id.cv_bell_call_item);
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
            intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, mUUID);
            startActivity(intent);
        }
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
                bellCallRecordListAdapter.remove();
                List<BellCallRecordBean> list = bellCallRecordListAdapter.getSelectedList();
                mPresenter.deleteBellCallRecord(list);
                break;
        }
    }

    @Override
    public void onMakeCall() {
        Intent intent = new Intent(this, BellLiveActivity.class);
        intent.putExtra(JConstant.VIEW_CALL_WAY, JConstant.VIEW_CALL_WAY_VIEWER);
        intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, mUUID);
        startActivity(intent);
    }

    @Override
    public void loadMedia(final BellCallRecordBean item, final ImageView imageView) {
        Glide.with(this)
                .load(new JFGGlideURL(JfgEnum.JFG_URL.WARNING, item.type, item.timeInLong / 1000 + ".jpg", mUUID))
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
                            ((ImageViewTip) imageView).setImageDrawable(circularBitmapDrawable, item.answerState == 0);
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
    public void onShowProperty(JFGDoorBellDevice device) {
        if (device.battery.$() < 20 || (device.battery.$() < 80 && isFirst)) {
            isFirst = false;
            onBellBatteryDrainOut();
        }
        imgVTopBarCenter.setText(TextUtils.isEmpty(device.alias) ? device.uuid : device.alias);
//        cvBellHomeBackground.setState(device.net.$().net);
        cvBellHomeBackground.setState(1);
        cvBellHomeBackground.setActionInterface(this);
    }

}
