package com.cylan.jiafeigou.n.view.panorama;

import android.support.annotation.IdRes;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.wrapper.BaseActivity;
import com.cylan.jiafeigou.utils.ViewUtils;

import butterknife.BindView;
import butterknife.OnClick;

public class PanoramaAlbumActivity extends BaseActivity<PanoramaAlbumContact.Presenter> implements PanoramaAlbumContact.View, RadioGroup.OnCheckedChangeListener {
    @BindView(R.id.act_panorama_album_toolbar_container)
    RelativeLayout toolbarContainer;
    @BindView(R.id.act_panorama_album_toolbar_header_title)
    TextView toolbarAlbumViewMode;

    private PopupWindow albumModeSelectPop;
    private ALBUM_VIEW_MODE albumViewMode = ALBUM_VIEW_MODE.MODE_BOTH;
    private RadioGroup menuContainer;
    private String[] titles = {"相机+手机相册", "全景相册", "手机相册"};

    @Override
    protected PanoramaAlbumContact.Presenter onCreatePresenter() {
        return new PanoramaAlbumPresenter();
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
            View content = LayoutInflater.from(this).inflate(R.layout.layout_panorama_album_pop_menu, null);
            content.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            menuContainer = (RadioGroup) content.findViewById(R.id.menu_album_pop_container);
            menuContainer.setOnCheckedChangeListener(this);
            albumModeSelectPop = new PopupWindow(content, content.getMeasuredWidth(), content.getMeasuredHeight());
            albumModeSelectPop.setTouchable(true);
            albumModeSelectPop.setOutsideTouchable(false);
            albumModeSelectPop.setFocusable(false);
        }
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.4f;
        getWindow().setAttributes(lp);
        int xPos = (int) getResources().getDimension(R.dimen.y46);
        int yPos = (int) getResources().getDimension(R.dimen.y30);
        menuContainer.check(modeToResId(albumViewMode, true));
        toolbarAlbumViewMode.setText(titles[modeToResId(albumViewMode, false)]);
        albumModeSelectPop.showAtLocation(toolbarAlbumViewMode, Gravity.TOP | Gravity.START, xPos, yPos);
    }

    public boolean hideAlbumViewModePop() {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 1f;
        getWindow().setAttributes(lp);
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

    @Override
    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
        albumViewMode = resIdToMode(checkedId);
        toolbarAlbumViewMode.setText(titles[modeToResId(albumViewMode, false)]);
        hideAlbumViewModePop();
    }
}
