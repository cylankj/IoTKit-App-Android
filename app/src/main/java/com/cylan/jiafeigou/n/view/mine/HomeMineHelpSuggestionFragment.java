package com.cylan.jiafeigou.n.view.mine;


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
import android.widget.EditText;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.home.HomeMineHelpSuggestionContract;
import com.cylan.jiafeigou.n.mvp.model.MineHelpSuggestionBean;
import com.cylan.jiafeigou.n.view.adapter.HomeMineHelpSuggestionAdapter;
import com.cylan.jiafeigou.utils.ToastUtil;

import java.util.ArrayList;
import java.util.List;

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
public class HomeMineHelpSuggestionFragment extends Fragment implements HomeMineHelpSuggestionContract.View {

    @BindView(R.id.rv_home_mine_suggestion)
    RecyclerView mRvMineSuggestion;

    @BindView(R.id.et_home_mine_suggestion)
    EditText mEtSuggestion;


    private List<MineHelpSuggestionBean> suggestionList;
    private HomeMineHelpSuggestionAdapter suggestionAdapter;
    private String suggestion;

    private HomeMineHelpSuggestionContract.Presenter presenter;

    //当前点击的时间和上次点击的时间
    private long afterTime;
    private long nowTime;
    private String TAG = "HomeMineHelpSuggestionFragment";

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
        View view = inflater.inflate(R.layout.fragment_home_mine_suggestion, container, false);
        ButterKnife.bind(this, view);
        if (suggestionList == null) {
            initData();
        }
        initRecyclerView();
        return view;
    }

    @OnClick(R.id.iv_home_mine_suggestion_back)
    public void onClick() {
        getFragmentManager().popBackStack();
    }

    /**
     * 模拟数据 添加进去
     */
    private void initData() {
        String server = "亲爱的用户,客户端将于2016年4月1日23:00至00:00进行系统维护升级," +
                "期间对设备正常使用将会造成一定影响,对您造成的不便之处敬请谅解。再次感谢您对加菲狗的支持！";
        String client = "希望你们会做视频下载功能，非常实用呢。";
        suggestionList = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            MineHelpSuggestionBean bean = new MineHelpSuggestionBean();
            if (i == 0) {
                bean.setType(0);
                bean.setText(server);
                bean.setIcon(R.drawable.pic_head);
                bean.setIsShowTime(true);
            } else {
                bean.setType(1);
                bean.setText(client);
                bean.setIcon(R.drawable.img_head);
                bean.setIsShowTime(true);
            }
            suggestionList.add(bean);
        }
    }


    /**
     * 对recyclerView进行初始化的显示
     */

    private void initRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mRvMineSuggestion.setLayoutManager(layoutManager);
        suggestionAdapter = new HomeMineHelpSuggestionAdapter(getContext(), suggestionList, null);
        mRvMineSuggestion.setAdapter(suggestionAdapter);
    }


    @OnClick({R.id.iv_home_mine_suggestion_back, R.id.tv_mine_help_suggestion_clear, R.id.tv_home_mine_suggestion})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_mine_help_suggestion_clear:
                //TODO  点击清空进行集合的清空
                /***********add begin**********/
                //弹出对话框
                showDialog();
                /***********add end**********/
                break;
            case R.id.iv_home_mine_suggestion_back:
                getFragmentManager().popBackStack();
                break;
            case R.id.tv_home_mine_suggestion:
                suggestion = mEtSuggestion.getText().toString();
                if (suggestion.length() >= 10) {
                    nowTime = System.currentTimeMillis();
                    if (nowTime - afterTime > 300000) {
                        //大于300000 表示可以进行自动回复了
                        addClientItem();
                        addAutoReply();
                        suggestionAdapter = new HomeMineHelpSuggestionAdapter(getContext(), suggestionList, null);
                        mRvMineSuggestion.setAdapter(suggestionAdapter);
                        mEtSuggestion.setText("");
                    } else {
                        //表示用户在5分钟之内连续进行反馈，不自动回复
                        addClientItem();
                        suggestionAdapter = new HomeMineHelpSuggestionAdapter(getContext(), suggestionList, null);
                        mRvMineSuggestion.setAdapter(suggestionAdapter);
                        mEtSuggestion.setText("");
                    }
                    afterTime = System.currentTimeMillis();
                } else {
                    ToastUtil.showToast("输入内容不能小于10个字符");
                }
                mRvMineSuggestion.scrollToPosition(suggestionList.size() - 1);      //滚动到集合最后一条显示；
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
                        suggestionAdapter.removeAll(suggestionList);
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
     * 用户点击发送符合条件之后，显示该条目
     */
    private void addClientItem() {
        if (nowTime - afterTime > 300000) {
            MineHelpSuggestionBean suggestionBean = new MineHelpSuggestionBean();
            suggestionBean.setIcon(R.drawable.img_head);
            suggestionBean.setType(1);
            suggestionBean.setText(suggestion);
            suggestionBean.setIsShowTime(true);
            suggestionList.add(suggestionBean);
        } else {
            MineHelpSuggestionBean suggestionBean = new MineHelpSuggestionBean();
            suggestionBean.setIcon(R.drawable.img_head);
            suggestionBean.setType(1);
            suggestionBean.setText(suggestion);
            suggestionBean.setDate("");
            suggestionBean.setIsShowTime(false);
            suggestionList.add(suggestionBean);
        }
    }

    /**
     * 用户进行反馈时添加一个自动回复的条目
     */
    private void addAutoReply() {
        MineHelpSuggestionBean autoReplyBean = new MineHelpSuggestionBean();
        autoReplyBean.setIcon(R.drawable.pic_head);
        autoReplyBean.setType(0);
        autoReplyBean.setText("您的反馈已经收到，我们将会尽快回复"); //TODO 服务端反馈回来的消息
        autoReplyBean.setDate("");
        autoReplyBean.setIsShowTime(false);
        suggestionList.add(autoReplyBean);
    }

    /**
     * 服务端主动推送给客户的消息
     */
    private void addServerItem() {
        if (nowTime - afterTime > 300000) {
            MineHelpSuggestionBean autoReplyBean = new MineHelpSuggestionBean();
            autoReplyBean.setIcon(R.drawable.pic_head);
            autoReplyBean.setType(0);
            autoReplyBean.setText("");
            autoReplyBean.setDate("");
            autoReplyBean.setIsShowTime(true);
            suggestionList.add(autoReplyBean);
        } else {
            MineHelpSuggestionBean autoReplyBean = new MineHelpSuggestionBean();
            autoReplyBean.setIcon(R.drawable.pic_head);
            autoReplyBean.setType(0);
            autoReplyBean.setText("您的反馈已收到，我们将会尽快回复");
            autoReplyBean.setDate("");
            autoReplyBean.setIsShowTime(true);
            suggestionList.add(autoReplyBean);
        }
    }


    @Override
    public void onTalkList(ArrayList<MineHelpSuggestionBean> beanOfArrayList) {

    }

    @Override
    public void onClearAllTalk() {

    }

    @Override
    public void setPresenter(HomeMineHelpSuggestionContract.Presenter presenter) {
        this.presenter = presenter;
    }
}
