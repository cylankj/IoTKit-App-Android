package com.cylan.jiafeigou.n.view.panorama;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.view.JFGPresenter;
import com.cylan.jiafeigou.base.wrapper.BaseActivity;
import com.cylan.jiafeigou.utils.ViewUtils;

import butterknife.BindView;
import butterknife.OnClick;

public class PanoramaAlbumActivity extends BaseActivity {
    @BindView(R.id.act_panorama_album_toolbar_container)
    RelativeLayout toolbarContainer;
    @BindView(R.id.act_panorama_album_toolbar_header_title)
    TextView toolbarAlbumViewMode;

    private PopupWindow albumModeSelectPop;

    @Override
    protected JFGPresenter onCreatePresenter() {
        return null;
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

    @OnClick(R.id.act_panorama_album_toolbar_header_title)
    public void showAlbumViewModePop() {
        if (albumModeSelectPop == null) {
            View content = LayoutInflater.from(this).inflate(R.layout.layout_panorama_album_pop_menu, null);
            content.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            albumModeSelectPop = new PopupWindow(content, content.getMeasuredWidth(), content.getMeasuredHeight());
            albumModeSelectPop.setTouchable(true);
            albumModeSelectPop.setOutsideTouchable(true);
            albumModeSelectPop.setFocusable(true);
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.alpha = 0.4f;
            getWindow().setAttributes(lp);
        }
        int xPos = (int) getResources().getDimension(R.dimen.y46);
        int yPos = (int) getResources().getDimension(R.dimen.y30);
        albumModeSelectPop.showAtLocation(toolbarAlbumViewMode, Gravity.TOP | Gravity.START, xPos, yPos);
    }

    public void hideAlbumViewModePop() {
        if (albumModeSelectPop != null && albumModeSelectPop.isShowing()) {
            albumModeSelectPop.dismiss();
        }
    }

    @Override
    @OnClick(R.id.act_panorama_album_back)

    public void onBackPressed() {
        super.onBackPressed();
    }
}
