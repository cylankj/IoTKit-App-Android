package com.cylan.jiafeigou.n.view.panorama;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.ViewSwitcher;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.injector.component.ActivityComponent;
import com.cylan.jiafeigou.base.module.BaseHttpApiHelper;
import com.cylan.jiafeigou.base.wrapper.BaseActivity;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.widget.video.PanoramicView720_Ext;
import com.cylan.jiafeigou.widget.video.VideoViewFactory;
import com.cylan.panorama.Panoramic720View;
import com.danikula.videocache.HttpProxyCacheServer;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by yanzhendong on 2017/3/16.
 */

public class PanoramaDetailActivity extends BaseActivity<PanoramaDetailContact.Presenter> {

    @BindView(R.id.act_panorama_detail_content_container)
    FrameLayout panoramaContentContainer;
    @BindView(R.id.act_panorama_detail_bottom_panel_switcher)
    ViewSwitcher panoramaPanelSwitcher;
    @BindView(R.id.act_panorama_detail_bottom_video_menu_seek_bar)
    SeekBar panoramaPanelSeekBar;
    @Inject
    HttpProxyCacheServer httpProxy;
    private PanoramicView720_Ext panoramicView720Ext;


    public static Intent getIntent(Context context, String uuid, PanoramaAlbumContact.PanoramaItem item) {
        Intent intent = new Intent(context, PanoramaDetailActivity.class);
        intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
        intent.putExtra("panorama_item", item);
        return intent;
    }

    @Override
    protected void setActivityComponent(ActivityComponent activityComponent) {
        activityComponent.inject(this);
    }

    @Override
    protected int getContentViewID() {
        return R.layout.activity_panorama_detail;
    }

    @Override
    protected void initViewAndListener() {
        super.initViewAndListener();
        PanoramaAlbumContact.PanoramaItem panoramaItem = getIntent().getParcelableExtra("panorama_item");
        initPanoramaView();
        initViewerLayout(panoramaItem.type);
        BaseHttpApiHelper.getInstance().loadPicture(panoramaItem.fileName)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(filePath -> panoramicView720Ext.loadImage(filePath), AppLogger::e);
    }

    private void initPanoramaView() {
        panoramicView720Ext = (PanoramicView720_Ext) VideoViewFactory.CreateRendererExt(VideoViewFactory.RENDERER_VIEW_TYPE.TYPE_PANORAMA_720, this, true);
        panoramicView720Ext.configV720();
        panoramicView720Ext.setId("IVideoView".hashCode());
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        panoramicView720Ext.setLayoutParams(params);
        panoramaContentContainer.addView(panoramicView720Ext);
        panoramicView720Ext.setDisplayMode(Panoramic720View.DM_LittlePlanet);
    }

    private void initViewerLayout(@PanoramaAlbumContact.PanoramaItem.PANORAMA_ITEM_TYPE int type) {
        switch (type) {
            case PanoramaAlbumContact.PanoramaItem.PANORAMA_ITEM_TYPE.TYPE_PICTURE:
                if (panoramaPanelSwitcher.getDisplayedChild() == 0) {
                    panoramaPanelSwitcher.showNext();
                }

                break;
            case PanoramaAlbumContact.PanoramaItem.PANORAMA_ITEM_TYPE.TYPE_VIDEO:
                if (panoramaPanelSwitcher.getDisplayedChild() == 1) {
                    panoramaPanelSwitcher.showPrevious();
                }
                break;
        }
    }

    @OnClick(R.id.act_panorama_detail_bottom_picture_menu_photograph)
    public void clickedPhotograph() {
        AppLogger.d("clickedPhotograph");
        if (panoramicView720Ext != null) {

        }
    }

    @OnClick(R.id.act_panorama_detail_bottom_picture_menu_vr)
    public void clickedVR() {
        AppLogger.d("clickedVR");
        if (panoramicView720Ext != null) {
            panoramicView720Ext.enableVRMode(true);
        }
    }

    @OnClick(R.id.act_panorama_detail_bottom_picture_menu_panorama)
    public void clickedPanorama() {
        AppLogger.d("clickedPanorama");
        if (panoramicView720Ext != null) {
            panoramicView720Ext.enableVRMode(false);
            panoramicView720Ext.setDisplayMode(Panoramic720View.DM_LittlePlanet);
        }
    }

    @OnClick(R.id.act_panorama_detail_bottom_picture_menu_gyroscope)
    public void clickedGyroscope() {
        AppLogger.d("clickedGyroscope");
        if (panoramicView720Ext != null) {
            panoramicView720Ext.enableGyro(true);
            ConnectivityManager manager=null;

        }
    }
}
