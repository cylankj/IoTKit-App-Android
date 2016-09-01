package com.cylan.jiafeigou.n.view.mine;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.mine.SuggestionChatContant;
import com.cylan.jiafeigou.n.mvp.impl.mine.SuggestionPresenterImp;
import com.cylan.jiafeigou.n.view.adapter.SuggestionChatAdapter;
import com.sina.weibo.sdk.utils.LogUtil;


/**
 * 作者：zsl
 * 创建时间：2016/8/30
 * 描述：
 */
public class SuggestionChatFragment extends Fragment implements SuggestionChatContant.View, View.OnClickListener,TextView.OnEditorActionListener {

    private EditText et_content;
    private RecyclerView recyclerView;
    private SuggestionChatContant.Presenter presenter;
    private TextView te_send;
    private SuggestionChatAdapter chatAdapter;
    private TextView tv_clear;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =inflater.inflate(R.layout.fragment_home_mine_suggestion,container,false);

        initView(view);

        initPresenter();

        initLisenter();

        showChatList();

        keyboardListener();

        return view;
    }

    private void initPresenter() {
        presenter = new SuggestionPresenterImp(this,getContext());
    }

    private void initView(View view) {
        te_send = (TextView) view.findViewById(R.id.tv_home_mine_suggestion);
        et_content = (EditText) view.findViewById(R.id.et_home_mine_suggestion);
        tv_clear = (TextView) view.findViewById(R.id.tv_mine_help_suggestion_clear);
        recyclerView = (RecyclerView) view.findViewById(R.id.rv_home_mine_suggestion);

    }

    private void initLisenter() {
        te_send.setOnClickListener(this);
        tv_clear.setOnClickListener(this);
        et_content.setOnEditorActionListener(this);
    }

    @Override
    public void showChatList() {
        presenter.initChatData();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatAdapter = new SuggestionChatAdapter(presenter.initChatData());
        recyclerView.setAdapter(chatAdapter);
    }

    @Override
    public String getEditContent() {
        return et_content.getText().toString();
    }

    @Override
    public String getTime() {

        return System.currentTimeMillis()+"";
    }

    @Override
    public void clearEdit() {
        et_content.setText("");
    }

    @Override
    public void keyboardListener() {
        //添加输入框焦点变化监听
        et_content.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                //获取变化后的列表高度
                recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        //移除监听
                        recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        //列表滑动到最后一条
                        recyclerView.scrollToPosition(chatAdapter.getList().size()-1);
                    }
                });
            }
        });
        //设置输入框点击事件
        et_content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        recyclerView.scrollToPosition(chatAdapter.getList().size()-1);
                        et_content.setEnabled(true);
                    }
                });
            }
        });
    }

    @Override
    public boolean editLessShowDialog() {
        String content = et_content.getText().toString().trim();
        if(content.length() < 10){
            presenter.showToast();
            return true;
        }
        return false;
    }

    @Override
    public void notifyChatList() {
        chatAdapter.notifyDataSetChanged();
    }

    @Override
    public void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("亲！确定要清空吗？").setPositiveButton("清空", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                presenter.clearChatList(chatAdapter.getList());
                notifyChatList();
            }
        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

    @Override
    public void setPresenter(SuggestionChatContant.Presenter presenter) {
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("",""+getActivity());
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){

            case R.id.tv_home_mine_suggestion:
                if(editLessShowDialog()){
                    return;
                }
                presenter.addChatItem(presenter.makeEMMessageBean(getEditContent(),0,getTime()));
                presenter.addChatItem(presenter.testServerData(SystemClock.currentThreadTimeMillis()));
                chatAdapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(chatAdapter.getList().size()-1);
                clearEdit();
                //hideKeyBoard();
                break;

            case R.id.tv_mine_help_suggestion_clear:
                showDialog();
                break;
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        return true;
    }

    public void hideKeyBoard() {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().finish();
    }

}
