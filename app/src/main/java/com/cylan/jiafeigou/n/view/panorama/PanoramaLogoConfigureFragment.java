package com.cylan.jiafeigou.n.view.panorama;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.injector.component.FragmentComponent;
import com.cylan.jiafeigou.base.wrapper.BaseFragment;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.superadapter.IMulItemViewType;
import com.cylan.jiafeigou.support.superadapter.OnItemClickListener;
import com.cylan.jiafeigou.support.superadapter.SuperAdapter;
import com.cylan.jiafeigou.support.superadapter.internal.SuperViewHolder;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.video.PanoramicView720_Ext;
import com.cylan.jiafeigou.widget.video.VideoViewFactory;
import com.cylan.panorama.Panoramic720View;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;

/**
 * Created by yanzhendong on 2017/3/15.
 */

public class PanoramaLogoConfigureFragment extends BaseFragment<PanoramaLogoConfigureContact.Presenter> implements PanoramaLogoConfigureContact.View, OnItemClickListener {
    @BindView(R.id.fragment_panorama_logo_bottom_panel)
    RecyclerView logoList;
    @BindView(R.id.fragment_panorama_logo_content_container)
    FrameLayout logoContentContainer;
    private List<LogoItem> builtInLogo = Arrays.asList(new LogoItem(LOGO_TYPE.LOGO_TYPE_NONE), new LogoItem(LOGO_TYPE.LOGO_TYPE_WHITE), new LogoItem(LOGO_TYPE.LOGO_TYPE_BLACK), new LogoItem(LOGO_TYPE.LOGO_TYPE_CLOVE_DOG));
    private LogoListAdapter logoListAdapter;
    @LOGO_TYPE
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

    private void initPanoramaView() {
        panoramicView720Ext = (PanoramicView720_Ext) VideoViewFactory.CreateRendererExt(VideoViewFactory.RENDERER_VIEW_TYPE.TYPE_PANORAMA_720, getActivityContext(), true);
        panoramicView720Ext.configV720();
        panoramicView720Ext.setId("IVideoView".hashCode());
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        panoramicView720Ext.setLayoutParams(params);
        logoContentContainer.addView(panoramicView720Ext);
        panoramicView720Ext.setDisplayMode(Panoramic720View.DM_Normal);
        panoramicView720Ext.loadImage(R.drawable.panorama_logo_mask);
    }

    @Override
    protected void setFragmentComponent(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @Override
    protected int getContentViewID() {
        return R.layout.fragment_panorama_logo_configure;
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
        logoListAdapter.notifyItemChanged(currentLogoType);
        ToastUtil.showPositiveToast("切换成功");
    }

    @Override
    public void onChangeLogoTypeError(int position) {
        ToastUtil.showNegativeToast("切换失败");
    }

    private class LogoListAdapter extends SuperAdapter<LogoItem> {

        public LogoListAdapter(Context context, List<LogoItem> items) {
            super(context, items, null);
        }

        @Override
        public void onBind(SuperViewHolder holder, int viewType, int layoutPosition, LogoItem item) {
            switch (item.type) {
                case LOGO_TYPE.LOGO_TYPE_WHITE:
                    holder.setImageResource(R.id.item_panorama_logo_img, R.drawable.logo_white);
                    break;
                case LOGO_TYPE.LOGO_TYPE_BLACK:
                    holder.setImageResource(R.id.item_panorama_logo_img, R.drawable.logo_black);
                    break;
                case LOGO_TYPE.LOGO_TYPE_CLOVE_DOG:
                    holder.setImageResource(R.id.item_panorama_logo_img, R.drawable.logo_clever_dog);
                    break;
            }
            if (layoutPosition == currentLogoType) {
                holder.itemView.setBackgroundResource(R.drawable.logo_selected);
            } else {
                holder.itemView.setBackgroundResource(android.R.color.transparent);
            }
        }

        @Override
        protected IMulItemViewType<LogoItem> offerMultiItemViewType() {
            return new IMulItemViewType<LogoItem>() {
                @Override
                public int getViewTypeCount() {
                    return 2;
                }

                @Override
                public int getItemViewType(int position, LogoItem logoItem) {
                    return logoItem.type != 0 ? 1 : 0;
                }

                @Override
                public int getLayoutId(int viewType) {
                    return viewType == 0 ? R.layout.item_panorama_logo_empty : R.layout.item_panorama_logo;
                }
            };
        }
    }
}
