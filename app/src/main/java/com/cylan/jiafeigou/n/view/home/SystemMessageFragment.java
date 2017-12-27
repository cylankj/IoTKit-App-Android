package com.cylan.jiafeigou.n.view.home;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.cache.db.module.SysMsgBean;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.home.SysMessageContract;
import com.cylan.jiafeigou.n.mvp.impl.home.SysMessagePresenterImp;
import com.cylan.jiafeigou.n.view.adapter.HomeMineMessageAdapter;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.badge.Badge;
import com.cylan.jiafeigou.widget.CustomToolbar;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 作者：zsl
 * 创建时间：2016/9/5
 * 描述：
 */
@Badge(parentTag = "HomeMineFragment")
public class SystemMessageFragment extends IBaseFragment implements SysMessageContract.View {

    @BindView(R.id.rcl_home_mine_message_recyclerview)
    RecyclerView rlSystemMessages;
    @BindView(R.id.ll_no_mesg)
    LinearLayout llNoMesg;
    @BindView(R.id.tv_check_all)
    TextView tvCheckAll;
    @BindView(R.id.tv_delete)
    TextView tvDelete;
    @BindView(R.id.rl_delete_dialog)
    RelativeLayout rlDeleteDialog;
    @BindView(R.id.custom_toolbar)
    CustomToolbar customToolbar;
    private SysMessageContract.Presenter presenter;
    private HomeMineMessageAdapter messageAdapter;
    private LinearLayoutManager layoutManager;

    public static SystemMessageFragment newInstance(Bundle bundle) {
        SystemMessageFragment fragment = new SystemMessageFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private boolean isLoading = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_mine_message, container, false);
        ButterKnife.bind(this, view);
        layoutManager = new LinearLayoutManager(getContext());
        rlSystemMessages.setLayoutManager(layoutManager);
        messageAdapter = new HomeMineMessageAdapter(getContext(), null, null);
        rlSystemMessages.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int visibleItemPosition = layoutManager.findFirstVisibleItemPosition();
                if (visibleItemPosition >= 0) {
                    if (dy > 0) { //check for scroll down
                        int visibleItemCount = layoutManager.getChildCount();
                        int totalItemCount = layoutManager.getItemCount();
                        if (!isLoading) {
                            if ((visibleItemCount + visibleItemPosition) >= totalItemCount) {
                                isLoading = true;
                                onLoadMore();
                            }
                        }
                    }
                }
            }
        });

        rlSystemMessages.setAdapter(messageAdapter);
        messageAdapter.setSelectionListener(new HomeMineMessageAdapter.SelectionListener() {
            @Override
            public void onSelectionChanged(int position, boolean isChecked) {
                if (messageAdapter.isEditMode()) {
                    updateBottomLayout();
                }
            }
        });
        initPresenter();
        presenter.loadSystemMessageFromServer(0, 0);
        return view;
    }

    private void updateBottomLayout() {
        List<SysMsgBean> selectedItems = messageAdapter.getSelectedItems();
        tvDelete.setEnabled(selectedItems.size() > 0);
        if (selectedItems.size() < messageAdapter.getList().size()) {
            tvCheckAll.setText(R.string.SELECT_ALL);
        } else {
            tvCheckAll.setText(R.string.CANCEL);
        }
    }


    private void onLoadMore() {
        List<SysMsgBean> msgBeanList = messageAdapter.getList();
        long v601 = Long.MAX_VALUE;
        long v701 = Long.MAX_VALUE;
        if (msgBeanList != null) {
            for (SysMsgBean bean : msgBeanList) {
                if (bean.type == 701) {
                    v701 = Math.min(v701, bean.time);
                } else if (bean.type == 601) {
                    v601 = Math.min(v601, bean.time);
                }
            }
        }
        presenter.loadSystemMessageFromServer(v601, v701);
    }

    private void initPresenter() {
        presenter = new SysMessagePresenterImp(this);
    }


    @OnClick({R.id.tv_toolbar_icon, R.id.tv_toolbar_right, R.id.tv_check_all, R.id.tv_delete})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_toolbar_icon:        //返回
                getActivity().getSupportFragmentManager().popBackStack();
                break;
            case R.id.tv_toolbar_right:       //删除
                if (messageAdapter == null) {
                    return;
                }
                if (messageAdapter.getItemCount() == 0) {
                    return;
                }
                if (customToolbar.getTvToolbarRight().getText().equals(getString(R.string.CANCEL))) {
                    handleCancel();
                    return;
                }
                handleDelete();
                break;
            case R.id.tv_check_all:
                List<SysMsgBean> selectedItems = messageAdapter.getSelectedItems();
                List<SysMsgBean> beans = messageAdapter.getList();
                int size = beans == null ? 0 : beans.size();
                messageAdapter.select(selectedItems.size() < size);
                break;

            case R.id.tv_delete:
                customToolbar.setToolbarRightTitle(getString(R.string.DELETE));
                presenter.deleteSystemMessageFromServer(messageAdapter.getSelectedItems());
                messageAdapter.notifyDataSetHasChanged();
                if (messageAdapter.getItemCount() == 0) {
                    rlDeleteDialog.setVisibility(View.GONE);
                }
                break;
        }
    }

    /**
     * 处理删除操作
     */
    private void handleDelete() {
        rlDeleteDialog.setVisibility(View.VISIBLE);
        customToolbar.setToolbarRightTitle(getString(R.string.CANCEL));
        messageAdapter.setEditMode(true);
        tvDelete.setEnabled(messageAdapter.getSelectedItems().size() > 0);
    }

    /**
     * 处理取消操作
     */
    private void handleCancel() {
        rlDeleteDialog.setVisibility(View.GONE);
        customToolbar.setToolbarRightTitle(getString(R.string.DELETE));
        for (SysMsgBean bean : messageAdapter.getList()) {
            bean.isCheck = 0;
        }
        messageAdapter.notifyDataSetHasChanged();
        messageAdapter.setEditMode(false);
        rlSystemMessages.setAdapter(messageAdapter);
    }

    @Override
    public void onQuerySystemMessageRsp(ArrayList<SysMsgBean> list) {
        isLoading = false;
        messageAdapter.addAll(list);
        decideEmptyView();
    }

    private void decideEmptyView() {
        boolean isEmpty = messageAdapter.getList() == null || messageAdapter.getList().size() == 0;
        rlSystemMessages.setVisibility(isEmpty ? View.INVISIBLE : View.VISIBLE);
        llNoMesg.setVisibility(isEmpty ? View.VISIBLE : View.INVISIBLE);

    }

    @Override
    public void onDeleteSystemMessageRsp(RxEvent.DeleteDataRsp rsp) {
        if (rsp != null && rsp.resultCode == 0) {
            messageAdapter.removeAll(messageAdapter.getSelectedItems());
        }
        decideEmptyView();
    }

}
