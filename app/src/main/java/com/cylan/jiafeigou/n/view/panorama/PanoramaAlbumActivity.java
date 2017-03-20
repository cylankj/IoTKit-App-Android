package com.cylan.jiafeigou.n.view.panorama;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.BuildConfig;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.module.PanoramaEvent;
import com.cylan.jiafeigou.base.wrapper.BaseActivity;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.mvp.model.PAlbumBean;
import com.cylan.jiafeigou.n.view.adapter.PanoramaAdapter;
import com.cylan.jiafeigou.support.superadapter.OnItemClickListener;
import com.cylan.jiafeigou.support.superadapter.OnItemLongClickListener;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.pop.RelativePopupWindow;
import com.cylan.jiafeigou.widget.pop.RoundRectPopup;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;

public class PanoramaAlbumActivity extends BaseActivity<PanoramaAlbumContact.Presenter>
        implements PanoramaAlbumContact.View,
        RadioGroup.OnCheckedChangeListener,
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

    @BindView(R.id.act_panorama_album_lists)
    RecyclerView recyclerView;
    private RoundRectPopup albumModeSelectPop;
    private ALBUM_VIEW_MODE albumViewMode = ALBUM_VIEW_MODE.MODE_BOTH;
    //    private RadioGroup menuContainer;
    private String[] titles = {"相机+手机相册", "全景相册", "手机相册"};
    private PanoramaAdapter panoramaAdapter;

    @Override
    protected PanoramaAlbumContact.Presenter onCreatePresenter() {
        return new PanoramaAlbumPresenter();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresenter.onSetViewUUID(getIntent().getStringExtra(JConstant.KEY_DEVICE_ITEM_UUID));
        panoramaAdapter = new PanoramaAdapter(this, null, null);
        panoramaAdapter.setOnItemClickListener(this);
        panoramaAdapter.setOnItemLongClickListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(panoramaAdapter);
        swipeRefreshLayout.setOnRefreshListener(this);
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
    protected void initViewAndListener() {
        super.initViewAndListener();
    }

    @OnClick(R.id.act_panorama_album_toolbar_header_title)
    public void showAlbumViewModePop() {
        if (albumModeSelectPop == null) {
            albumModeSelectPop = new RoundRectPopup(this, this);
            albumModeSelectPop.setTouchable(true);
            albumModeSelectPop.setOutsideTouchable(false);
            albumModeSelectPop.setFocusable(false);
            albumModeSelectPop.setCheckIndex(modeToResId(albumViewMode, false));
        }
        albumModeSelectPop.showOnAnchor(toolbarAlbumViewMode, RelativePopupWindow.VerticalPosition.ALIGN_TOP, RelativePopupWindow.HorizontalPosition.ALIGN_LEFT);
    }

    public boolean hideAlbumViewModePop() {
        if (albumModeSelectPop != null && albumModeSelectPop.isShowing()) {
            albumModeSelectPop.dismiss();
            return true;
        }
        return false;
    }

    private int modeToResId(ALBUM_VIEW_MODE mode, boolean isPop) {
        switch (mode) {
            case MODE_BOTH:
                return isPop ? R.id.menu_item_album_pop_both : 0;
            case MODE_PANORAMA:
                return isPop ? R.id.menu_item_album_pop_panorama : 1;
            case MODE_PHOTO:
                return isPop ? R.id.menu_item_album_pop_photo : 2;
            default:
                return isPop ? R.id.menu_item_album_pop_both : 0;
        }
    }

    private ALBUM_VIEW_MODE resIdToMode(int resId) {
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
        if (!hideAlbumViewModePop()) {
            super.onBackPressed();
        }
    }

    @OnClick(R.id.tv_album_delete)
    public void albumDelete() {
        String content = tvAlbumDelete.getText().toString();
        if (TextUtils.isEmpty(content) && tvAlbumDelete.isEnabled()) {
            //active state
            tvAlbumDelete.setText(getString(R.string.CANCEL));
            ViewUtils.setDrawablePadding(tvAlbumDelete, -1, 0);
            toggleEditMode(true);
        } else {
            //cancel
            tvAlbumDelete.setText("");
            ViewUtils.setDrawablePadding(tvAlbumDelete, R.drawable.album_delete_selector, 0);
            toggleEditMode(false);
        }
        Bundle bundle = new Bundle();
        bundle.putString("url_item", JConstant.getRoot() + File.separator + "1489906172.jpg");
        Pan720FullFragment fullFragment = Pan720FullFragment.newInstance(bundle);
        ActivityUtils.addFragmentSlideInFromRight(getSupportFragmentManager(), fullFragment, android.R.id.content);
    }

    /**
     * @param toggle
     */
    private void toggleEditMode(boolean toggle) {
        panoramaAdapter.setInEditMode(toggle);
        final int lPos = ((LinearLayoutManager) recyclerView.getLayoutManager())
                .findLastVisibleItemPosition();
        panoramaAdapter.reverseEdition(toggle, lPos);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
        albumViewMode = resIdToMode(checkedId);
        toolbarAlbumViewMode.setText(titles[modeToResId(albumViewMode, false)]);
        hideAlbumViewModePop();
        Log.d("onCheckedChanged", "onCheckedChanged");
        albumDelete();
    }

    @Override
    public void onItemClick(View itemView, int viewType, int position) {
        if (panoramaAdapter.isInEditMode()) {
            panoramaAdapter.reverseItemSelectedState(position);
        } else {
            Bundle bundle = new Bundle();
            bundle.putString("url_item", JConstant.getRoot() + File.separator + "1489906172.jpg");
            Pan720FullFragment fullFragment = Pan720FullFragment.newInstance(bundle);
            ActivityUtils.addFragmentSlideInFromRight(getSupportFragmentManager(), fullFragment, android.R.id.content);
        }
    }

    @Override
    public void onItemLongClick(View itemView, int viewType, int position) {
        new AlertDialog.Builder(this)
                .setMessage("delete item?")
                .setPositiveButton(getString(R.string.OK), (DialogInterface dialog, int which) -> {
                    panoramaAdapter.remove(position);
                })
                .setNegativeButton(getString(R.string.CANCEL), null)
                .create()
                .show();
    }

    @Override
    public void onRefresh() {
        if (swipeRefreshLayout.isRefreshing()) {
            return;
        }
        mPresenter.fresh(false);
    }

    @Override
    public void onAppend(ArrayList<PAlbumBean> resultList) {
        if (resultList != null && resultList.size() > 0)
            panoramaAdapter.addAll(resultList);
        //setEmptyView
        emptyView.setVisibility(panoramaAdapter.getCount() > 0 ? View.GONE : View.VISIBLE);
        swipeRefreshLayout.setEnabled(false);
        swipeRefreshLayout.post(() -> {
            swipeRefreshLayout.setRefreshing(false);
            swipeRefreshLayout.setEnabled(true);
        });
    }

    @Override
    public void onDelete(ArrayList<PAlbumBean> positionList) {
        swipeRefreshLayout.setRefreshing(false);
        if (positionList.size() > 0) {
            panoramaAdapter.removeAll(positionList);
        }
        //setEmptyView
        emptyView.setVisibility(panoramaAdapter.getCount() > 0 ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onUpdate(PanoramaEvent.MsgFile needUpdate, int position) {
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public ArrayList<PAlbumBean> getList() {
        return (ArrayList<PAlbumBean>) panoramaAdapter.getList();
    }

    @Override
    public void onDisconnected() {
        if (BuildConfig.DEBUG)
            ToastUtil.showNegativeToast("sock断开连接");
    }

    @Override
    public void onConnected() {
        if (BuildConfig.DEBUG)
            ToastUtil.showNegativeToast("sock连接成功");
    }

    @Override
    public void onFileState(int state) {

    }
}
