package com.cylan.jiafeigou.n.view.panorama;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.injector.component.ActivityComponent;
import com.cylan.jiafeigou.base.wrapper.BaseActivity;
import com.cylan.jiafeigou.databinding.LayoutBottomFooterBinding;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.view.adapter.PanoramaAdapter;
import com.cylan.jiafeigou.support.superadapter.OnItemClickListener;
import com.cylan.jiafeigou.support.superadapter.OnItemLongClickListener;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.pop.RelativePopupWindow;
import com.cylan.jiafeigou.widget.pop.RoundRectPopup;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

import static com.cylan.jiafeigou.dp.DpMsgMap.ID_202_MAC;

public class PanoramaAlbumActivity extends BaseActivity<PanoramaAlbumContact.Presenter>
        implements PanoramaAlbumContact.View,
        OnItemClickListener,
        SwipeRefreshLayout.OnRefreshListener,
        OnItemLongClickListener {
    @BindView(R.id.act_panorama_album_toolbar_container)
    RelativeLayout toolbarContainer;
    @BindView(R.id.act_panorama_album_toolbar_header_title)
    TextView toolbarAlbumViewMode;
    @BindView(R.id.tv_album_delete)
    TextView tvAlbumDelete;
    @BindView(R.id.act_panorama_album_refresh)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.pan_empty_list)
    View emptyView;
    @BindView(R.id.act_panorama_album_back)
    ImageView imgLeftMenu;
    @BindView(R.id.act_panorama_album_lists)
    RecyclerView recyclerView;
    @BindView(R.id.act_panorama_album_bottom_menu_container)
    FrameLayout bottomMenuContainer;
    @BindView(R.id.tv_msg_full_select)
    TextView selectAll;
    @BindView(R.id.tv_msg_delete)
    TextView deleteSelected;

    @ALBUM_VIEW_MODE
    private int albumViewMode = ALBUM_VIEW_MODE.MODE_NONE;
    //    private RadioGroup menuContainer;
    private int[] titles = {R.string.Tap1_File_CameraNPhone, R.string.Tap1_File_Camera, R.string.Tap1_File_Phone};
    private PanoramaAdapter panoramaAdapter;
    private LinearLayoutManager layoutManager;
    private boolean loading;
    private boolean isEditMode = false;
    private RoundRectPopup albumModeSelectPop;
    private LayoutBottomFooterBinding footerBinding;


    @Override
    protected void initViewAndListener() {
        super.initViewAndListener();
        panoramaAdapter = new PanoramaAdapter(uuid, this, null);
        panoramaAdapter.setOnItemClickListener(this);
        panoramaAdapter.setOnItemLongClickListener(this);
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        footerBinding = LayoutBottomFooterBinding.inflate(getLayoutInflater(), recyclerView, false);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    int visibleItemCount = recyclerView.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();
                    //这里需要好好理解
                    if (!loading && totalItemCount - visibleItemCount <= firstVisibleItem && panoramaAdapter.getCount() > 0) {
                        footerBinding.footerProgressBar.setVisibility(View.VISIBLE);
                        footerBinding.footerContentText.setText(R.string.LOADING);
                        //延时一点点,好显示 footerView
                        onLoadMore();
                    }
                }
            }
        });
        recyclerView.setAdapter(panoramaAdapter);
        swipeRefreshLayout.setOnRefreshListener(this);
        String mac = sourceManager.getDevice(uuid).$(ID_202_MAC, "");
        if (mac != null) {
            String routerMac = NetUtils.getRouterMacAddress(getApplication());
            if (TextUtils.equals(mac, routerMac)) {
                toolbarAlbumViewMode.setText(titles[modeToResId(2, false)]);
//                toolbarAlbumViewMode.setEnabled(true);
            } else {
                toolbarAlbumViewMode.setText(titles[modeToResId(0, false)]);
//                toolbarAlbumViewMode.setEnabled(false);
            }
        }
        swipeRefreshLayout.setColorSchemeResources(R.color.color_36BDFF);
        swipeRefreshLayout.setRefreshing(true);
    }

    private void onLoadMore() {
        loading = true;
        PanoramaAlbumContact.PanoramaItem item = panoramaAdapter.getItem(panoramaAdapter.getCount() - 1);
        presenter.fetch(item.time, albumViewMode);
    }

    @Override
    protected int getContentViewID() {
        return R.layout.activity_panorama_album;
    }

    @Override
    protected void onStart() {
        super.onStart();
        ViewUtils.setViewPaddingStatusBar(toolbarContainer);

    }

    @Override
    protected void onStop() {
        super.onStop();
        ViewUtils.clearViewPaddingStatusBar(toolbarContainer);
    }

    @Override
    protected void setActivityComponent(ActivityComponent activityComponent) {
        activityComponent.inject(this);
    }


    @OnClick(R.id.act_panorama_album_toolbar_header_title)
    public void showAlbumViewModePop(View view) {
        ViewUtils.deBounceClick(view);
        if (albumModeSelectPop == null) {
            albumModeSelectPop = new RoundRectPopup(this);
            albumModeSelectPop.setDismissListener((int id) -> {
                albumViewMode = resIdToMode(id);
                toolbarAlbumViewMode.setText(titles[modeToResId(albumViewMode, false)]);
                presenter.fetch(0, albumViewMode);
            });
            albumModeSelectPop.setMode(albumViewMode);
        }
        albumModeSelectPop.setCheckIndex(modeToResId(albumViewMode, false));
        albumModeSelectPop.showOnAnchor(toolbarAlbumViewMode, RelativePopupWindow.VerticalPosition.ALIGN_TOP, RelativePopupWindow.HorizontalPosition.ALIGN_LEFT);
    }

    private int modeToResId(@ALBUM_VIEW_MODE int mode, boolean isPop) {
        switch (mode) {
            case ALBUM_VIEW_MODE.MODE_BOTH:
                return isPop ? R.id.menu_item_album_pop_both : 0;
            case ALBUM_VIEW_MODE.MODE_PANORAMA:
                return isPop ? R.id.menu_item_album_pop_panorama : 1;
            case ALBUM_VIEW_MODE.MODE_PHOTO:
                return isPop ? R.id.menu_item_album_pop_photo : 2;
            default:
                return isPop ? R.id.menu_item_album_pop_both : 0;
        }
    }

    private int resIdToMode(@ALBUM_VIEW_MODE int resId) {
        switch (resId) {
            case R.id.menu_item_album_pop_both:
                return ALBUM_VIEW_MODE.MODE_BOTH;
            case R.id.menu_item_album_pop_panorama:
                return ALBUM_VIEW_MODE.MODE_PANORAMA;
            case R.id.menu_item_album_pop_photo:
                return ALBUM_VIEW_MODE.MODE_PHOTO;
            default:
                return ALBUM_VIEW_MODE.MODE_BOTH;
        }
    }

    @Override
    @OnClick(R.id.act_panorama_album_back)
    public void onBackPressed() {
        if (!isEditMode) {
            super.onBackPressed();
        } else {
            bottomMenuContainer.setVisibility(View.INVISIBLE);
            selectAll.setText(R.string.SELECT_ALL);
            deleteSelected.setEnabled(false);
            tvAlbumDelete.setText("");
            tvAlbumDelete.setBackgroundResource(R.drawable.album_delete_selector);
            toggleEditMode(false);
        }
    }

    @OnClick(R.id.tv_album_delete)
    public void albumDelete() {
        String content = tvAlbumDelete.getText().toString();
        if (TextUtils.isEmpty(content) && tvAlbumDelete.isEnabled()) {
            //active state
            tvAlbumDelete.setText(getString(R.string.CANCEL));
            tvAlbumDelete.setBackground(new ColorDrawable(0));
            toggleEditMode(true);
            bottomMenuContainer.setVisibility(View.VISIBLE);
            deleteSelected.setEnabled(panoramaAdapter.getRemovedList().size() > 0);
        } else {
            //cancel
            tvAlbumDelete.setText("");
            tvAlbumDelete.setBackgroundResource(R.drawable.album_delete_selector);
            toggleEditMode(false);
            bottomMenuContainer.setVisibility(View.INVISIBLE);
        }
    }

    @OnClick(R.id.tv_msg_full_select)
    public void selectAll() {
        if (TextUtils.equals(selectAll.getText(), getString(R.string.SELECT_ALL))) {
            panoramaAdapter.selectAll(layoutManager.findLastVisibleItemPosition());
            deleteSelected.setEnabled(true);
            selectAll.setText(R.string.CANCEL);
        } else {
            panoramaAdapter.selectNone(layoutManager.findLastVisibleItemPosition());
            deleteSelected.setEnabled(false);
            selectAll.setText(R.string.SELECT_ALL);
        }
    }

    @OnClick(R.id.tv_msg_delete)
    public void deleteSelected() {
        isEditMode = false;
        tvAlbumDelete.setText("");
        tvAlbumDelete.setBackgroundResource(R.drawable.album_delete_selector);
        deleteWithAlert(panoramaAdapter.getRemovedList());
    }

    /**
     * @param toggle
     */
    private void toggleEditMode(boolean toggle) {
        this.isEditMode = toggle;
        panoramaAdapter.setInEditMode(toggle);
        final int lPos = ((LinearLayoutManager) recyclerView.getLayoutManager())
                .findLastVisibleItemPosition();
        panoramaAdapter.reverseEdition(toggle, lPos);
    }

    @Override
    public void onItemClick(View itemView, int viewType, int position) {
        if (position == panoramaAdapter.getItemCount() - 1) {
            return;
        }
        PanoramaAlbumContact.PanoramaItem item = panoramaAdapter.getItem(position);
        if (panoramaAdapter.isInEditMode()) {
            panoramaAdapter.reverseItemSelectedState(position);
            deleteSelected.setEnabled(panoramaAdapter.getRemovedList().size() > 0);
        } else {
            Intent intent = PanoramaDetailActivity.getIntent(this, uuid, item, albumViewMode);
            startActivity(intent);
        }
    }

    @Override
    public void onItemLongClick(View itemView, int viewType, int position) {
        deleteWithAlert(Collections.singletonList(panoramaAdapter.getList().get(position)));
    }

    private void deleteWithAlert(List<PanoramaAlbumContact.PanoramaItem> items) {
        boolean hasDownloading = false;
        for (PanoramaAlbumContact.PanoramaItem item : items) {
            if (item.downloadInfo != null && item.downloadInfo.getState() > 0 && item.downloadInfo.getState() < 4) {
                hasDownloading = true;
                break;
            }
        }
        if (hasDownloading) {
            new AlertDialog.Builder(this)
                    .setMessage("视频正在下载中，是否删除?")
                    .setCancelable(false)
                    .setPositiveButton(R.string.DELETE, (dialog, which) -> {
                        bottomMenuContainer.setVisibility(View.INVISIBLE);
                        presenter.deletePanoramaItem(items, albumViewMode);
                    })
                    .setNegativeButton(R.string.CANCEL, null)
                    .show();
        } else {
            new AlertDialog.Builder(this)
                    .setMessage(albumViewMode == 2 ? R.string.Tap1_DeletedCameraNCellphoneFileTips : R.string.Tips_SureDelete)
                    .setPositiveButton(getString(R.string.OK), (DialogInterface dialog, int which) -> {
                        bottomMenuContainer.setVisibility(View.INVISIBLE);
                        presenter.deletePanoramaItem(items, albumViewMode);
                    })
                    .setNegativeButton(getString(R.string.CANCEL), null)
                    .show();
        }
    }

    @Override
    public void onRefresh() {
        presenter.fetch(0, albumViewMode);
    }

    @Override
    public void onAppend(List<PanoramaAlbumContact.PanoramaItem> resultList, boolean isRefresh, boolean loadFinish, int fetchLocation) {
        loading = !loadFinish;
        if (isRefresh) {
            panoramaAdapter.clear();
        }
        if (resultList != null && resultList.size() > 0) {
            panoramaAdapter.addAll(resultList);
            if (!panoramaAdapter.hasFooterView()) {
                panoramaAdapter.addFooterView(footerBinding.getRoot());
            }
            if (isRefresh) {
                PreferencesUtils.putString(JConstant.PANORAMA_THUMB_PICTURE + ":" + uuid, resultList.get(0).fileName);
            }
        }
        boolean hasMore = resultList != null && resultList.size() == 20;
        footerBinding.footerProgressBar.setVisibility(hasMore ? View.VISIBLE : View.GONE);
        footerBinding.footerContentText.setText(hasMore ? R.string.LOADING : R.string.Loaded);
        //setEmptyView
        emptyView.setVisibility(panoramaAdapter.getCount() > 0 ? View.GONE : View.VISIBLE);
        if (panoramaAdapter.getCount() == 0) {
            panoramaAdapter.removeFooterView();
        }
        swipeRefreshLayout.setEnabled(false);
        swipeRefreshLayout.post(() -> {
            swipeRefreshLayout.setRefreshing(false);
            swipeRefreshLayout.setEnabled(true);
        });
        tvAlbumDelete.setEnabled(panoramaAdapter.getItemCount() > 0);
    }

    @Override
    public void onDelete(List<PanoramaAlbumContact.PanoramaItem> positionList) {
        swipeRefreshLayout.setRefreshing(false);
        if (positionList.size() > 0) {
            panoramaAdapter.removeAll(positionList);
        }
        //setEmptyView
        emptyView.setVisibility(panoramaAdapter.getCount() > 0 ? View.GONE : View.VISIBLE);
        tvAlbumDelete.setEnabled(panoramaAdapter.getCount() > 0);
        panoramaAdapter.setInEditMode(false);
        if (panoramaAdapter.getCount() == 0) {
            panoramaAdapter.removeFooterView();
        }
    }

    @Override
    public void onUpdate(PanoramaAlbumContact.PanoramaItem needUpdate, int position) {
        swipeRefreshLayout.setRefreshing(false);
        panoramaAdapter.notifyItemChanged(position);
        tvAlbumDelete.setEnabled(panoramaAdapter.getItemCount() > 0);
    }

    @Override
    public List<PanoramaAlbumContact.PanoramaItem> getList() {
        return panoramaAdapter.getList();
    }

    @Override
    public void onViewModeChanged(int mode, boolean report) {
        panoramaAdapter.notifyDataSetChanged();
        if (albumViewMode == mode) return;
        if (report) {
            ToastUtil.showNegativeToast(getString(R.string.Tap1_Disconnected));
        }
        presenter.fetch(0, albumViewMode = mode);
        toolbarAlbumViewMode.setText(titles[modeToResId(albumViewMode, false)]);
        if (albumModeSelectPop != null && report) {
            albumModeSelectPop.setMode(mode);
        }
    }

    @Override
    public void onSDCardCheckResult(int has_sdcard) {
        if (has_sdcard == 1) {
        } else {
            new AlertDialog.Builder(this)
                    .setMessage(R.string.MSG_SD_OFF)
                    .setCancelable(false)
                    .setPositiveButton(R.string.OK, (dialog, which) -> {
                        onViewModeChanged(0, false);
                    })
                    .show();
        }
    }
}
