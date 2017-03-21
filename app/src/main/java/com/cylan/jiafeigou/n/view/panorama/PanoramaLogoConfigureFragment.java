package com.cylan.jiafeigou.n.view.panorama;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.wrapper.BaseFragment;
import com.cylan.jiafeigou.support.superadapter.IMulItemViewType;
import com.cylan.jiafeigou.support.superadapter.OnItemClickListener;
import com.cylan.jiafeigou.support.superadapter.SuperAdapter;
import com.cylan.jiafeigou.support.superadapter.internal.SuperViewHolder;

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
    private List<LogoItem> builtInLogo = Arrays.asList(new LogoItem(0), new LogoItem(1), new LogoItem(2), new LogoItem(3));
    private LogoListAdapter logoListAdapter;


    public static PanoramaLogoConfigureFragment newInstance() {
        PanoramaLogoConfigureFragment fragment = new PanoramaLogoConfigureFragment();
        Bundle bundle = new Bundle();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected PanoramaLogoConfigureContact.Presenter onCreatePresenter() {
        return new PanoramaLogoConfigurePresenter();
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
    }

    @Override
    protected int getContentViewID() {
        return R.layout.fragment_panorama_logo_configure;
    }

    @Override
    public void onItemClick(View itemView, int viewType, int position) {

    }

    private class LogoListAdapter extends SuperAdapter<LogoItem> implements View.OnClickListener {

        public LogoListAdapter(Context context, List<LogoItem> items) {
            super(context, items, null);
        }

        @Override
        public void onBind(SuperViewHolder holder, int viewType, int layoutPosition, LogoItem item) {
            holder.setOnClickListener(R.id.item_panorama_logo_img, this);
            switch (item.type) {
                case 1:
                    holder.setImageResource(R.id.item_panorama_logo_img, R.drawable.logo_white);
                    break;
                case 2:
                    holder.setImageResource(R.id.item_panorama_logo_img, R.drawable.logo_black);
                    break;
                case 3:
                    holder.setImageResource(R.id.item_panorama_logo_img, R.drawable.logo_clever_dog);
                    break;
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

        @Override
        public void onClick(View v) {

        }
    }
}
