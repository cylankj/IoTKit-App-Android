package com.cylan.jiafeigou.n.view.home;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.home.HomeMineMessageContract;
import com.cylan.jiafeigou.n.mvp.impl.home.HomeMineMessagePresenterImp;
import com.cylan.jiafeigou.n.mvp.model.MineMessageBean;
import com.cylan.jiafeigou.n.view.adapter.HomeMineMessageAdapter;
import com.cylan.jiafeigou.utils.ViewUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 作者：zsl
 * 创建时间：2016/9/5
 * 描述：
 */
public class HomeMineMessageFragment extends Fragment implements HomeMineMessageContract.View {

    @BindView(R.id.iv_home_mine_message_back)
    TextView ivHomeMineMessageBack;
    @BindView(R.id.tv_home_mine_message_clear)
    TextView tvHomeMineMessageClear;
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
    @BindView(R.id.fLayout_top_bar_container)
    FrameLayout fLayoutTopBarContainer;
    private boolean isCheckAll;

    private HomeMineMessageContract.Presenter presenter;
    private HomeMineMessageAdapter messageAdapter;
    private boolean hasNewMesg;
    private ArrayList<MineMessageBean> hasCheckData;

    public static HomeMineMessageFragment newInstance(Bundle bundle) {
        HomeMineMessageFragment fragment = new HomeMineMessageFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        hasNewMesg = arguments.getBoolean("hasNewMesg");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_mine_message, container, false);
        ButterKnife.bind(this, view);
        initPresenter();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initTopBar();
    }

    private void initPresenter() {
        presenter = new HomeMineMessagePresenterImp(this, hasNewMesg);
    }

    private void initTopBar() {
        ViewUtils.setViewPaddingStatusBar(fLayoutTopBarContainer);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (presenter != null) presenter.start();
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
    public void initRecycleView(ArrayList<MineMessageBean> list) {
        rclHomeMineMessageRecyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        messageAdapter = new HomeMineMessageAdapter(getContext(),list, null);
        rclHomeMineMessageRecyclerview.setAdapter(messageAdapter);
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
    public void setPresenter(HomeMineMessageContract.Presenter presenter) {

    }

    @OnClick({R.id.iv_home_mine_message_back, R.id.tv_home_mine_message_clear, R.id.tv_check_all, R.id.tv_delete})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_home_mine_message_back:        //返回
                getFragmentManager().popBackStack();
                break;
            case R.id.tv_home_mine_message_clear:       //删除
                if (messageAdapter.getItemCount() == 0) return;
                if (tvHomeMineMessageClear.getText().equals(getString(R.string.CANCEL))) {
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
                for (MineMessageBean bean : hasCheckData) {
                    messageAdapter.remove(bean);
                }
                hasCheckData.clear();
                messageAdapter.notifyDataSetHasChanged();
                if (messageAdapter.getItemCount() == 0) {
                    showNoMesgView();
                    rlDeleteDialog.setVisibility(View.GONE);
                    tvHomeMineMessageClear.setText(getString(R.string.DELETE));
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
        tvHomeMineMessageClear.setText(getString(R.string.CANCEL));
        if (hasCheckData == null) {
            hasCheckData = new ArrayList<>();
        }
        messageAdapter.isShowCheck = true;
        rclHomeMineMessageRecyclerview.setAdapter(messageAdapter);
        messageAdapter.setOnDeleteCheckChangeListener(new HomeMineMessageAdapter.OnDeleteCheckChangeListener() {
            @Override
            public void deleteCheck(boolean isCheck, MineMessageBean item) {
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
        tvHomeMineMessageClear.setText(getString(R.string.DELETE));
        hasCheckData.clear();
        hasCheckData = null;
        for (MineMessageBean bean : messageAdapter.getList()) {
            bean.isCheck = 0;
        }
        messageAdapter.notifyDataSetHasChanged();
        messageAdapter.checkAll = false;
        messageAdapter.isShowCheck = false;
        rclHomeMineMessageRecyclerview.setAdapter(messageAdapter);
    }

}
