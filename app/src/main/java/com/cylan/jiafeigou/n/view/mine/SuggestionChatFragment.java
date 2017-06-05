package com.cylan.jiafeigou.n.view.mine;

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
import com.cylan.jiafeigou.cache.db.module.MineHelpSuggestionBean;
import com.cylan.jiafeigou.n.mvp.contract.mine.SuggestionChatContract;
import com.cylan.jiafeigou.n.view.adapter.HomeMineHelpSuggestionAdapter;
import com.cylan.jiafeigou.support.softkeyboard.util.KPSwitchConflictUtil;
import com.cylan.jiafeigou.support.softkeyboard.util.KeyboardUtil;
import com.cylan.jiafeigou.support.softkeyboard.widget.KPSwitchFSPanelLinearLayout;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * 作者：zsl
 * 创建时间：2016/8/30
 * 描述：
 */
public class SuggestionChatFragment extends Fragment implements SuggestionChatContract.View, View.OnClickListener {

    @BindView(R.id.rv_home_mine_suggestion)
    RecyclerView rvHomeMineSuggestion;
    @BindView(R.id.et_home_mine_suggestion)
    EditText etHomeMineSuggestion;
    @BindView(R.id.tv_home_mine_suggestion)
    TextView tvHomeMineSuggestion;
    @BindView(R.id.panel_root)
    KPSwitchFSPanelLinearLayout panelRoot;
    private ArrayList<MineHelpSuggestionBean> mData;
    private HomeMineHelpSuggestionAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine_help_suggestion, container, false);
        ButterKnife.bind(this, view);
        initKeyBoard();
        KeyboardUtil.attach(getActivity(), panelRoot);
        initData();
        initRecycleView();
        initListener();
        return view;
    }

    private void initListener() {
        tvHomeMineSuggestion.setOnClickListener(this);
    }

    private void initData() {
        mData = new ArrayList<>();
    }

    private void initRecycleView() {
        rvHomeMineSuggestion.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new HomeMineHelpSuggestionAdapter(getContext(), mData, null);
        rvHomeMineSuggestion.setAdapter(adapter);
    }

    private void initKeyBoard() {
        KeyboardUtil.attach(getActivity(), panelRoot);
        KPSwitchConflictUtil.attach(panelRoot, tvHomeMineSuggestion, etHomeMineSuggestion,
                switchToPanel -> {
                    if (switchToPanel) {
                        etHomeMineSuggestion.clearFocus();
                    } else {
                        etHomeMineSuggestion.requestFocus();
                    }
                });

        rvHomeMineSuggestion.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    KPSwitchConflictUtil.hidePanelAndKeyboard(panelRoot);
                }
                return false;
            }
        });
    }


    @Override
    public void setPresenter(SuggestionChatContract.Presenter presenter) {

    }

    @Override
    public String getUuid() {
        return null;
    }

    @Override
    public void onClick(View v) {
        MineHelpSuggestionBean suggestionBean = new MineHelpSuggestionBean();
        suggestionBean.setType(1);
        suggestionBean.setText(etHomeMineSuggestion.getText().toString());
        suggestionBean.setIcon("");
        String time = System.currentTimeMillis() + "";
        suggestionBean.setDate(time);
        suggestionBean.isShowTime = true;

        adapter.add(suggestionBean);
        adapter.notifyDataSetHasChanged();
        rvHomeMineSuggestion.scrollToPosition(adapter.getItemCount() - 1);
    }
}
