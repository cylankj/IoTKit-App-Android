package com.cylan.jiafeigou.n.view.mine;

import android.databinding.ObservableBoolean;
import android.databinding.ObservableInt;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.injector.component.FragmentComponent;
import com.cylan.jiafeigou.base.wrapper.BaseFragment;
import com.cylan.jiafeigou.databinding.FragmentHomeMineShareContentBinding;
import com.cylan.jiafeigou.databinding.ItemShareContentBinding;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineShareContentContract;
import com.cylan.jiafeigou.n.view.adapter.item.AbstractBindingViewHolder;
import com.cylan.jiafeigou.n.view.adapter.item.ShareContentItem;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.listeners.ClickEventHook;

import java.util.Collections;
import java.util.List;

/**
 * Created by yanzhendong on 2017/5/26.
 */

public class HomeMineShareContentFragment extends BaseFragment<MineShareContentContract.Presenter> implements MineShareContentContract.View, SwipeRefreshLayout.OnRefreshListener {
    private FragmentHomeMineShareContentBinding shareContentBinding;
    private ItemAdapter<ShareContentItem> adapter;
    private LinearLayoutManager manager;
    private ObservableBoolean editMode = new ObservableBoolean(false);
    private ObservableInt selectNumber = new ObservableInt(0);

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
        shareContentBinding.setSelectNumber(selectNumber);
        shareContentBinding.sharedRefresh.setOnRefreshListener(this);
        manager = new LinearLayoutManager(getContext());
        adapter = new ItemAdapter<>();
        FastAdapter<ShareContentItem> fastAdapter = new FastAdapter<>();
        fastAdapter.withSelectable(true);
        fastAdapter.withMultiSelect(true);
        fastAdapter.withAllowDeselection(true);
        fastAdapter.withOnPreClickListener(this::onEnterShareDetail);
        fastAdapter.withSelectWithItemUpdate(true);
        fastAdapter.withItemEvent(new ShareContentItemHook());
        fastAdapter.withSelectionListener((item, selected) -> selectNumber.set(adapter.getFastAdapter().getSelectedItems().size()));
        adapter.wrap(fastAdapter);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        shareContentBinding.sharedContentList.setLayoutManager(manager);
        shareContentBinding.sharedContentList.setAdapter(adapter);
        shareContentBinding.tvMsgFullSelect.setOnClickListener(this::reverseSelection);
        shareContentBinding.tvMsgDelete.setOnClickListener(this::deleteSelection);
    }

    private void deleteSelection(View view) {

    }

    public class ShareContentItemHook extends ClickEventHook<ShareContentItem> {
        @Override
        public void onClick(View v, int position, FastAdapter<ShareContentItem> fastAdapter, ShareContentItem item) {
            unShareWithAlert(item, position);
        }

        @Nullable
        @Override
        public View onBind(@NonNull RecyclerView.ViewHolder viewHolder) {
            ItemShareContentBinding viewDataBinding = ((AbstractBindingViewHolder<ItemShareContentBinding>) viewHolder).getViewDataBinding();
            viewDataBinding.setEditMode(editMode);
            return viewDataBinding.unShareContent;
        }
    }

    private void unShareWithAlert(ShareContentItem item, int position) {
        new AlertDialog.Builder(getContext())
                .setMessage("是否取消分享?")
                .setPositiveButton(R.string.OK, (dialog, which) -> {
                    dialog.dismiss();
                    AppLogger.d("正在取消分享");
                    presenter.unShareContent(Collections.singletonList(item), Collections.singleton(position));
                })
                .setNegativeButton(R.string.CANCEL, null)
                .show();

    }

    @Override
    public void onResume() {
        super.onResume();
        if (!editMode.get()) {
            presenter.loadFromServer(0, true);
        }
    }

    private boolean onEnterShareDetail(View view, IAdapter<ShareContentItem> iAdapter, ShareContentItem iItem, int position) {
        if (!editMode.get()) {
            AppLogger.e("将进入分享详情页面");
            ShareContentH5DetailFragment fragment = new ShareContentH5DetailFragment();

            ActivityUtils.addFragmentSlideInFromRight(getActivity().getSupportFragmentManager(), fragment, android.R.id.content);
        }
        return !editMode.get();
    }

    private void reverseSelection(View view) {
        if (selectNumber.get() > 0) {
            adapter.getFastAdapter().deselect();
        } else {
            adapter.getFastAdapter().select();
        }
    }

    public void onBackAction(View view) {
        AppLogger.e("点击了返回按钮");
        getActivity().getSupportFragmentManager().popBackStack();
    }

    @Override
    protected boolean onBackPressed() {
        if (editMode.get()) {
            editMode.set(false);
            return false;
        } else {
            return super.onBackPressed();
        }
    }

    public void onEditShareContent(View view) {
        AppLogger.d("点击了编辑按钮");
        editMode.set(!editMode.get());
        if (!editMode.get()) {
            adapter.getFastAdapter().deselect();
        }
    }

    @Override
    public void onShareContentResponse(List<ShareContentItem> shareContentItems, boolean refresh) {
        if (refresh) {
            adapter.set(shareContentItems);
        } else {
            adapter.add(shareContentItems);
        }
        shareContentBinding.sharedRefresh.setRefreshing(false);
    }

    @Override
    public void onUnShareContentResponse(int resultCode, Iterable<Integer> selection) {
        if (resultCode == 0) {
            adapter.getFastAdapter().select(selection);
            adapter.getFastAdapter().deleteAllSelectedItems();
        }
    }

    @Override
    public void onRefresh() {
        AppLogger.d("正在刷新");
        if (!editMode.get()) {
            presenter.loadFromServer(0, true);
        }
    }
}
