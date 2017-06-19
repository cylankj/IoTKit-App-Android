package com.cylan.jiafeigou.n.view.home;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.cylan.jiafeigou.n.mvp.contract.home.SysMessageContract;
import com.cylan.jiafeigou.n.mvp.impl.home.SysMessagePresenterImp;
import com.cylan.jiafeigou.n.view.adapter.HomeMineMessageAdapter;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.badge.Badge;
import com.cylan.jiafeigou.widget.CustomToolbar;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 作者：zsl
 * 创建时间：2016/9/5
 * 描述：
 */
@Badge(parentTag = "HomeMineFragment")
public class SystemMessageFragment extends Fragment implements SysMessageContract.View {

    @BindView(R.id.rcl_home_mine_message_recyclerview)
    RecyclerView rclHomeMineMessageRecyclerview;
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

    private boolean isCheckAll;

    private SysMessageContract.Presenter presenter;
    private HomeMineMessageAdapter messageAdapter;
    private ArrayList<SysMsgBean> hasCheckData;
    private ArrayList<Integer> serviceDelRsp;

    public static SystemMessageFragment newInstance(Bundle bundle) {
        SystemMessageFragment fragment = new SystemMessageFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_mine_message, container, false);
        ButterKnife.bind(this, view);
        rclHomeMineMessageRecyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        messageAdapter = new HomeMineMessageAdapter(getContext(), null, null);
        rclHomeMineMessageRecyclerview.setAdapter(messageAdapter);
        initPresenter();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (presenter != null) presenter.start();
    }

    private void initPresenter() {
        presenter = new SysMessagePresenterImp(this);
    }

    @Override
    public void onStart() {
        super.onStart();
//        if (presenter != null) presenter.initSubscription();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (presenter != null) presenter.stop();
    }

    /**
     * 初始化列表显示
     *
     * @param list
     */
    @Override
    public void initRecycleView(ArrayList<SysMsgBean> list) {
        messageAdapter.addAll(list);
    }

    /**
     * 消息为空显示
     */
    @Override
    public void showNoMesgView() {
        rclHomeMineMessageRecyclerview.setVisibility(View.INVISIBLE);
        llNoMesg.setVisibility(View.VISIBLE);
    }

    /**
     * 消息不为空显示
     */
    @Override
    public void hideNoMesgView() {
        rclHomeMineMessageRecyclerview.setVisibility(View.VISIBLE);
        llNoMesg.setVisibility(View.GONE);
    }


    @Override
    public void setPresenter(SysMessageContract.Presenter presenter) {

    }

    @Override
    public String getUuid() {
        return null;
    }

    @OnClick({R.id.tv_toolbar_icon, R.id.tv_toolbar_right, R.id.tv_check_all, R.id.tv_delete})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_toolbar_icon:        //返回
                getActivity().getSupportFragmentManager().popBackStack();
                break;
            case R.id.tv_toolbar_right:       //删除
                if (messageAdapter == null) return;
                if (messageAdapter.getItemCount() == 0) return;
                if (customToolbar.getTvToolbarRight().getText().equals(getString(R.string.CANCEL))) {
                    handleCancle();
                    return;
                }
                handleDelete();
                break;
            case R.id.tv_check_all:
                if (isCheckAll) {
                    messageAdapter.checkAll = true;
                    rclHomeMineMessageRecyclerview.setAdapter(messageAdapter);
                    if (hasCheckData == null) {
                        hasCheckData = new ArrayList<>();
                    }
                    hasCheckData.clear();
                    hasCheckData.addAll(messageAdapter.getList());
                } else {
                    hasCheckData.clear();
                    messageAdapter.checkAll = false;
                    rclHomeMineMessageRecyclerview.setAdapter(messageAdapter);
                }
                isCheckAll = !isCheckAll;
                break;

            case R.id.tv_delete:
                if (hasCheckData.size() == 0) {
                    return;
                }
                for (SysMsgBean bean : hasCheckData) {
                    messageAdapter.remove(bean);
                    if (bean.type == 601) {
                        presenter.deleteServiceMsg(bean.type, bean.getTime());
                    }
                    presenter.deleteOneItem(bean);
                }
                hasCheckData.clear();
                messageAdapter.notifyDataSetHasChanged();
                if (messageAdapter.getItemCount() == 0) {
                    showNoMesgView();
                    rlDeleteDialog.setVisibility(View.GONE);
                    customToolbar.setToolbarRightTitle(getString(R.string.DELETE));
                }
                break;
        }
    }

    /**
     * 处理删除操作
     */
    private void handleDelete() {
        isCheckAll = true;
        rlDeleteDialog.setVisibility(View.VISIBLE);
        customToolbar.setToolbarRightTitle(getString(R.string.CANCEL));
        if (hasCheckData == null) {
            hasCheckData = new ArrayList<>();
        }
        messageAdapter.isShowCheck = true;
        rclHomeMineMessageRecyclerview.setAdapter(messageAdapter);
        messageAdapter.setOnDeleteCheckChangeListener(new HomeMineMessageAdapter.OnDeleteCheckChangeListener() {
            @Override
            public void deleteCheck(boolean isCheck, SysMsgBean item) {
                if (isCheck) {
                    if (!hasCheckData.contains(item)) {
                        hasCheckData.add(item);
                    }
                } else {
                    hasCheckData.remove(item);
                }
            }
        });
    }

    /**
     * 处理取消操作
     */
    private void handleCancle() {
        isCheckAll = false;
        rlDeleteDialog.setVisibility(View.GONE);
        customToolbar.setToolbarRightTitle(getString(R.string.DELETE));
        hasCheckData.clear();
        hasCheckData = null;
        for (SysMsgBean bean : messageAdapter.getList()) {
            bean.isCheck = 0;
        }
        messageAdapter.notifyDataSetHasChanged();
        messageAdapter.checkAll = false;
        messageAdapter.isShowCheck = false;
        rclHomeMineMessageRecyclerview.setAdapter(messageAdapter);
    }

    @Override
    public void deleteMesgReuslt(RxEvent.DeleteDataRsp rsp) {
        if (serviceDelRsp == null) {
            serviceDelRsp = new ArrayList<>();
        }
        serviceDelRsp.add(rsp.resultCode);
        //TODO
    }

}
