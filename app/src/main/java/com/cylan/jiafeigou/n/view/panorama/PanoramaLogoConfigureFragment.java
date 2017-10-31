package com.cylan.jiafeigou.n.view.panorama;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.wrapper.BaseFragment;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.superadapter.OnItemClickListener;
import com.cylan.jiafeigou.support.superadapter.SuperAdapter;
import com.cylan.jiafeigou.support.superadapter.internal.SuperViewHolder;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.video.PanoramicView720_Ext;
import com.cylan.jiafeigou.widget.video.VideoViewFactory;
import com.cylan.panorama.Panoramic720View;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by yanzhendong on 2017/3/15.
 */

public class PanoramaLogoConfigureFragment extends BaseFragment<PanoramaLogoConfigureContact.Presenter> implements PanoramaLogoConfigureContact.View, OnItemClickListener {
    @BindView(R.id.fragment_panorama_logo_bottom_panel)
    RecyclerView logoList;
    @BindView(R.id.fragment_panorama_logo_content_container)
    FrameLayout logoContentContainer;
    @BindView(R.id.fragment_panorama_logo_toolbar)
    FrameLayout logoToobarContainer;
    private List<LogoItem> builtInLogo = Arrays.asList(
            new LogoItem(R.drawable.logo_no_watermark),
            new LogoItem(R.drawable.logo_white),
            new LogoItem(R.drawable.logo_black),
            new LogoItem(R.drawable.logo_clever_dog)
    );
    private LogoListAdapter logoListAdapter;
    private int currentLogoType = 0;
    private PanoramicView720_Ext panoramicView720Ext;


    public static PanoramaLogoConfigureFragment newInstance() {
        PanoramaLogoConfigureFragment fragment = new PanoramaLogoConfigureFragment();
        Bundle bundle = new Bundle();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected void initViewAndListener() {
        super.initViewAndListener();
        LinearLayoutManager manager = new LinearLayoutManager(logoList.getContext());
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        logoList.setLayoutManager(manager);
        logoListAdapter = new LogoListAdapter(logoList.getContext(), builtInLogo);
        logoListAdapter.setOnItemClickListener(this);
        logoList.setAdapter(logoListAdapter);
        initPanoramaView();
    }

    @Override
    public void onStart() {
        super.onStart();
        ViewUtils.setViewPaddingStatusBar(logoToobarContainer);
    }

    @Override
    public void onStop() {
        super.onStop();
        ViewUtils.clearViewPaddingStatusBar(logoToobarContainer);
    }

    private void initPanoramaView() {
        panoramicView720Ext = (PanoramicView720_Ext) VideoViewFactory.CreateRendererExt(VideoViewFactory.RENDERER_VIEW_TYPE.TYPE_PANORAMA_720, getActivity(), true);
        panoramicView720Ext.configV720();
        panoramicView720Ext.setId("IVideoView".hashCode());
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        panoramicView720Ext.setLayoutParams(params);
        logoContentContainer.addView(panoramicView720Ext);
        panoramicView720Ext.setDisplayMode(Panoramic720View.DM_Normal);
        panoramicView720Ext.loadImage(R.drawable.panorama_logo_mask);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate( R.layout.fragment_panorama_logo_configure,container,false);
    }

    @Override
    public void onItemClick(View itemView, int viewType, int position) {
        presenter.changeLogoType(position);

    }

    @Override
    public void onHttpConnectionToDeviceError() {
        AppLogger.d("onHttpConnectionToDeviceError");
    }

    @Override
    public void onChangeLogoTypeSuccess(int logtype) {
        currentLogoType = logtype;
        logoListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onChangeLogoTypeError(int position) {
        ToastUtil.showNegativeToast("切换失败");
    }

    private class LogoListAdapter extends SuperAdapter<LogoItem> {

        public LogoListAdapter(Context context, List<LogoItem> items) {
            super(context, items, R.layout.item_panorama_logo);
        }

        @Override
        public void onBind(SuperViewHolder holder, int viewType, int layoutPosition, LogoItem item) {
            holder.setBackgroundResource(R.id.item_panorama_logo_background, currentLogoType == layoutPosition ? R.drawable.logo_selected : android.R.color.transparent);
            holder.setBackgroundResource(R.id.item_panorama_logo_picture, item.logoResId);
            holder.setVisibility(R.id.item_panorama_logo_text, item.logoResId == R.drawable.logo_no_watermark ? View.VISIBLE : View.GONE);
        }
    }

    @OnClick(R.id.tv_top_bar_left)
    public void clickedTopBackMenu() {
        getActivity().onBackPressed();
    }
}
