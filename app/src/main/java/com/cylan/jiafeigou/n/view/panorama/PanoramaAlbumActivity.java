package com.cylan.jiafeigou.n.view.panorama;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.wrapper.BaseActivity;
import com.cylan.jiafeigou.n.mvp.model.PAlbumBean;
import com.cylan.jiafeigou.n.view.adapter.PanoramaAdapter;
import com.cylan.jiafeigou.support.superadapter.OnItemClickListener;
import com.cylan.jiafeigou.support.superadapter.OnItemLongClickListener;
import com.cylan.jiafeigou.utils.RandomUtils;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.pop.RelativePopupWindow;
import com.cylan.jiafeigou.widget.pop.RoundRectPopup;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class PanoramaAlbumActivity extends BaseActivity<PanoramaAlbumContact.Presenter> implements PanoramaAlbumContact.View,
        RadioGroup.OnCheckedChangeListener,
        OnItemClickListener,
        OnItemLongClickListener {
    @BindView(R.id.act_panorama_album_toolbar_container)
    RelativeLayout toolbarContainer;
    @BindView(R.id.act_panorama_album_toolbar_header_title)
    TextView toolbarAlbumViewMode;

    @BindView(R.id.tv_album_delete)
    TextView tvAlbumDelete;


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
        panoramaAdapter = new PanoramaAdapter(this, getTest(), null);
        panoramaAdapter.setOnItemClickListener(this);
        panoramaAdapter.setOnItemLongClickListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(panoramaAdapter);
    }

    private List<PAlbumBean> getTest() {
        List<PAlbumBean> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            PAlbumBean albumBean = new PAlbumBean();
            albumBean.isDate = RandomUtils.getRandom(10) % 2 == 0;
            albumBean.timeInDate = System.currentTimeMillis() / 1000 - RandomUtils.getRandom(10) * 24 * 3600;
            albumBean.from = RandomUtils.getRandom(1, 3);
            albumBean.url = getUrl();
            list.add(albumBean);
        }
        return list;
    }

    String[] ret = {
            "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1490178509&di=faf69d7930c8eaa0d779f25c3aa6e8b5&imgtype=jpg&er=1&src=http%3A%2F%2Fwww.th7.cn%2Fd%2Ffile%2Fp%2F2017%2F01%2F21%2F4cc5e894d9a736f2453387e983180d1e.jpg"
            , "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1490178527&di=cf1d4608f6f9aacdf0e49c2abd9c8579&imgtype=jpg&er=1&src=http%3A%2F%2Ffmn.rrimg.com%2Ffmn057%2Fxiaozhan%2F20120201%2F1335%2Fp%2Fm2w500hq85lt_x_large_XnFv_5fd20000919b125f.jpg"
            , ""
    };

    private String getUrl() {
        return ret[RandomUtils.getRandom(ret.length)];
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
        panoramaAdapter.reverseItemSelectedState(position);
    }

    @Override
    public void onItemLongClick(View itemView, int viewType, int position) {
        new AlertDialog.Builder(this)
                .setMessage("delete item?")
                .setNegativeButton(getString(R.string.OK), (DialogInterface dialog, int which) -> {
                    panoramaAdapter.remove(position);
                })
                .setNegativeButton(getString(R.string.CANCEL), null)
                .create()
                .show();
    }
}
