package com.cylan.jiafeigou.n.view.mine;

import android.databinding.ObservableBoolean;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cylan.jiafeigou.base.injector.component.FragmentComponent;
import com.cylan.jiafeigou.base.wrapper.BaseFragment;
import com.cylan.jiafeigou.databinding.FragmentHomeMineShareContentBinding;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineShareContentContract;
import com.cylan.jiafeigou.n.view.adapter.item.ShareContentItem;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;

import java.util.List;

/**
 * Created by yanzhendong on 2017/5/26.
 */

public class HomeMineShareContentFragment extends BaseFragment<MineShareContentContract.Presenter> implements MineShareContentContract.View {
    private FragmentHomeMineShareContentBinding shareContentBinding;
    private ItemAdapter<ShareContentItem> adapter;
    private LinearLayoutManager manager;
    private ObservableBoolean editMode = new ObservableBoolean(false);

    @Override
    protected void setFragmentComponent(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        shareContentBinding = FragmentHomeMineShareContentBinding.inflate(inflater);
        return shareContentBinding.getRoot();
    }

    public static HomeMineShareContentFragment newInstance(Bundle bundle) {
        HomeMineShareContentFragment fragment = new HomeMineShareContentFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected void initViewAndListener() {
        super.initViewAndListener();
        shareContentBinding.setBackAction(this::onBackAction);
        shareContentBinding.setRightAction(this::onEditShareContent);
        shareContentBinding.setEditMode(editMode);
        manager = new LinearLayoutManager(getContext());
        adapter = new ItemAdapter<>();
        FastAdapter<ShareContentItem> fastAdapter = new FastAdapter<>();
        fastAdapter.withSelectable(true);
        fastAdapter.withMultiSelect(true);
        fastAdapter.withAllowDeselection(true);
        fastAdapter.withOnClickListener(this::onEnterShareDetail);
        adapter.wrap(fastAdapter);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        shareContentBinding.sharedContentList.setLayoutManager(manager);
        shareContentBinding.sharedContentList.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.loadFromServer(0, true);
    }

    private boolean onEnterShareDetail(View view, IAdapter<ShareContentItem> iAdapter, ShareContentItem iItem, int position) {
        AppLogger.e("将进入分享详情页面");
        return false;
    }

    public void onBackAction(View view) {
        AppLogger.e("点击了返回按钮");
    }

    public void onEditShareContent(View view) {
        AppLogger.d("点击了编辑按钮");
        editMode.set(!editMode.get());
    }

    @Override
    public void onShareContentResponse(List<ShareContentItem> shareContentItems, boolean refresh) {
        if (refresh) {
            adapter.set(shareContentItems);
        } else {
            adapter.add(shareContentItems);
        }
    }

    @Override
    public void onUnShareContentResponse(int resultCode, int position) {
        if (resultCode == 0) {
            adapter.remove(position);
        }
    }
}
