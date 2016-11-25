package com.cylan.jiafeigou.n.view.mine;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.home.HomeMineHelpSuggestionContract;
import com.cylan.jiafeigou.n.mvp.impl.home.HomeMineHelpSuggestionImpl;
import com.cylan.jiafeigou.n.mvp.model.MineHelpSuggestionBean;
import com.cylan.jiafeigou.n.view.adapter.HomeMineHelpSuggestionAdapter;
import com.cylan.jiafeigou.support.softkeyboard.util.KPSwitchConflictUtil;
import com.cylan.jiafeigou.support.softkeyboard.util.KeyboardUtil;
import com.cylan.jiafeigou.support.softkeyboard.widget.KPSwitchFSPanelLinearLayout;
import com.cylan.jiafeigou.utils.ToastUtil;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * 创建者     谢坤
 * 创建时间   2016/8/8 14:37
 * 描述	      ${TODO}
 * <p/>
 * 更新者     $Author$
 * 更新时间   $Date$
 * 更新描述   ${TODO}
 */
public class HomeMineHelpSuggestionFragment extends Fragment implements HomeMineHelpSuggestionContract.View{

    @BindView(R.id.rv_home_mine_suggestion)
    RecyclerView mRvMineSuggestion;
    @BindView(R.id.et_home_mine_suggestion)
    EditText mEtSuggestion;
    @BindView(R.id.tv_mine_help_suggestion_clear)
    TextView tvMineHelpSuggestionClear;
    @BindView(R.id.tv_home_mine_suggestion)
    TextView tvHomeMineSuggestion;
    @BindView(R.id.panel_root)
    KPSwitchFSPanelLinearLayout panelRoot;

    private HomeMineHelpSuggestionAdapter suggestionAdapter;
    private String suggestion;
    private HomeMineHelpSuggestionContract.Presenter presenter;

    public static HomeMineHelpSuggestionFragment newInstance(Bundle bundle) {
        HomeMineHelpSuggestionFragment fragment = new HomeMineHelpSuggestionFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine_help_suggestion, container, false);
        ButterKnife.bind(this, view);
        initKeyBoard();
        initPresenter();
        return view;
    }

    private void initPresenter() {
        presenter = new HomeMineHelpSuggestionImpl(this);
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


    @OnClick({R.id.tv_mine_help_suggestion_clear, R.id.tv_home_mine_suggestion})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_mine_help_suggestion_clear:
                //弹出对话框
                showDialog();
                break;
            case R.id.tv_home_mine_suggestion:
                addInputItem();
                if (suggestionAdapter.getItemCount() != 1) {
                    if (presenter.checkOverTime(suggestionAdapter.getItem(suggestionAdapter.getItemCount() - 1).getDate())) {
                        addAutoReply();
                    }
                } else {
                    addAutoReply();
                }
                mRvMineSuggestion.scrollToPosition(suggestionAdapter.getItemCount() - 1); //滚动到集合最后一条显示；
                mEtSuggestion.setText("");
                break;
        }
    }

    /**
     * 弹出对话框
     */
    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getView().getContext());
        builder.setTitle("清空消息？")
                .setPositiveButton("清空", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        presenter.onClearAllTalk();
                        suggestionAdapter.clear();
                        suggestionAdapter.notifyDataSetHasChanged();
                        ToastUtil.showToast("消息已清空");
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    /**
     * 用户进行反馈时添加一个自动回复的条目
     */
    @Override
    public void addAutoReply() {
        MineHelpSuggestionBean autoReplyBean = new MineHelpSuggestionBean();
        autoReplyBean.setType(0);
        autoReplyBean.setText("您的反馈已经收到，我们将会尽快回复");
        autoReplyBean.setDate(System.currentTimeMillis() + "");
        suggestionAdapter.add(autoReplyBean);
        suggestionAdapter.notifyDataSetHasChanged();
        presenter.saveIntoDb(autoReplyBean);
    }

    /**
     * recycleview显示用户输入的条目
     */
    @Override
    public void addInputItem() {
        suggestion = mEtSuggestion.getText().toString();
        if (suggestion.length() >= 10) {
            MineHelpSuggestionBean suggestionBean = new MineHelpSuggestionBean();
            suggestionBean.setType(1);
            suggestionBean.setText(suggestion);
            suggestionBean.setIcon(presenter.getUserPhotoUrl());
            String time = System.currentTimeMillis() + "";
            suggestionBean.setDate(time);

            if (suggestionAdapter.getItemCount() != 0) {
                if (presenter.checkOver20s(suggestionAdapter.getList().get(suggestionAdapter.getItemCount() - 1).getDate())) {
                    suggestionBean.isShowTime = true;
                } else {
                    suggestionBean.isShowTime = false;
                }
            } else {
                suggestionBean.isShowTime = true;
            }
            suggestionAdapter.add(suggestionBean);
            suggestionAdapter.notifyDataSetHasChanged();
            presenter.saveIntoDb(suggestionBean);
        } else {
            ToastUtil.showToast("输入内容不能小于10个字符");
            return;
        }
    }

    /**
     * 初始化显示列表
     *
     * @param list
     */
    @Override
    public void initRecycleView(ArrayList<MineHelpSuggestionBean> list) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mRvMineSuggestion.setLayoutManager(layoutManager);
        suggestionAdapter = new HomeMineHelpSuggestionAdapter(getContext(), list, null);
        mRvMineSuggestion.setAdapter(suggestionAdapter);
    }

    @Override
    public void setPresenter(HomeMineHelpSuggestionContract.Presenter presenter) {
        this.presenter = presenter;
    }

    private void initKeyBoard() {
        KeyboardUtil.attach(getActivity(), panelRoot, new KeyboardUtil.OnKeyboardShowingListener() {
            @Override
            public void onKeyboardShowing(boolean isShowing) {
                mRvMineSuggestion.scrollToPosition(suggestionAdapter.getItemCount()-1);
            }
        });
        KPSwitchConflictUtil.attach(panelRoot, mEtSuggestion,
                new KPSwitchConflictUtil.SwitchClickListener() {
                    @Override
                    public void onClickSwitch(boolean switchToPanel) {
                        if (switchToPanel) {
                            mEtSuggestion.clearFocus();
                        } else {
                            mEtSuggestion.requestFocus();
                        }
                    }
                });

        mRvMineSuggestion.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    KPSwitchConflictUtil.hidePanelAndKeyboard(panelRoot);
                }
                return false;
            }
        });
    }
}
