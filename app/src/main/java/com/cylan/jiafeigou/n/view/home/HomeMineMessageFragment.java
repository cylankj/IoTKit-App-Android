package com.cylan.jiafeigou.n.view.home;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.home.HomeMineMessageContract;
import com.cylan.jiafeigou.n.mvp.impl.home.HomeMineMessagePresenterImp;
import com.cylan.jiafeigou.n.view.adapter.HomeMineMessageAdapter;

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
    ImageView ivHomeMineMessageBack;
    @BindView(R.id.tv_home_mine_message_clear)
    TextView tvHomeMineMessageClear;
    @BindView(R.id.rcl_home_mine_message_recyclerview)
    RecyclerView rclHomeMineMessageRecyclerview;

    private HomeMineMessageContract.Presenter presenter;
    private HomeMineMessageAdapter messageAdapter;

    public static HomeMineMessageFragment newInstance() {
        return new HomeMineMessageFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_mine_message, container, false);
        ButterKnife.bind(this, view);

        initPresenter();

        showMessageList();

        return view;
    }

    private void initPresenter() {
        presenter = new HomeMineMessagePresenterImp(this);
    }

    @Override
    public void showMessageList() {
        presenter.initMessageData();
        rclHomeMineMessageRecyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        messageAdapter = new HomeMineMessageAdapter(presenter.initMessageData());
        rclHomeMineMessageRecyclerview.setAdapter(messageAdapter);
    }

    @Override
    public void notifyMessageList() {

    }

    @Override
    public void showClearDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("亲！确定要清空吗？").setPositiveButton("清空", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                messageAdapter.getList().clear();
                messageAdapter.notifyDataSetChanged();
            }
        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

    @Override
    public void setPresenter(HomeMineMessageContract.Presenter presenter) {

    }

    @OnClick({R.id.iv_home_mine_message_back, R.id.tv_home_mine_message_clear})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_home_mine_message_back:        //返回
                getFragmentManager().popBackStack();
                break;
            case R.id.tv_home_mine_message_clear:       //清空
                showClearDialog();
                break;
        }
    }
}
