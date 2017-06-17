package com.cylan.jiafeigou.n.view.mine;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.cache.db.module.MineHelpSuggestionBean;
import com.cylan.jiafeigou.misc.AlertDialogManager;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.n.BaseFullScreenFragmentActivity;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.home.FeedBackContract;
import com.cylan.jiafeigou.n.mvp.impl.home.FeedBackImpl;
import com.cylan.jiafeigou.n.view.adapter.HomeMineHelpSuggestionAdapter;
import com.cylan.jiafeigou.support.badge.Badge;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.softkeyboard.util.KPSwitchConflictUtil;
import com.cylan.jiafeigou.support.softkeyboard.util.KeyboardUtil;
import com.cylan.jiafeigou.support.softkeyboard.widget.KPSwitchFSPanelLinearLayout;
import com.cylan.jiafeigou.support.superadapter.internal.SuperViewHolder;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.IMEUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.CustomToolbar;
import com.cylan.jiafeigou.widget.LoadingDialog;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

@Badge(parentTag = "HomeMineHelpFragment")
public class FeedbackActivity extends BaseFullScreenFragmentActivity<FeedBackContract.Presenter>
        implements FeedBackContract.View {

    @BindView(R.id.custom_toolbar)
    CustomToolbar customToolbar;
    @BindView(R.id.rv_home_mine_suggestion)
    RecyclerView mRvMineSuggestion;
    @BindView(R.id.iv_loading_rotate)
    ImageView ivLoadingRotate;
    @BindView(R.id.fl_loading_container)
    FrameLayout flLoadingContainer;
    @BindView(R.id.et_home_mine_suggestion)
    EditText mEtSuggestion;
    @BindView(R.id.tv_home_mine_suggestion)
    TextView tvHomeMineSuggestion;
    @BindView(R.id.rl_home_mine_suggestion_bottom)
    RelativeLayout rlHomeMineSuggestionBottom;
    @BindView(R.id.panel_root)
    KPSwitchFSPanelLinearLayout panelRoot;

    private HomeMineHelpSuggestionAdapter suggestionAdapter;
    private FeedBackContract.Presenter presenter;
    private boolean resendFlag;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_mine_help_suggestion);
        ButterKnife.bind(this);
        basePresenter = new FeedBackImpl(this);
        initKeyBoard();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mRvMineSuggestion.setLayoutManager(layoutManager);
        suggestionAdapter = new HomeMineHelpSuggestionAdapter(getContext(), null, null);
        mRvMineSuggestion.setAdapter(suggestionAdapter);
        BaseApplication.getAppComponent().getTreeHelper().markNodeRead(HomeMineHelpFragment.class.getSimpleName());
    }

    @Override
    public void onBackPressed() {
        finishExt();
    }

    @OnClick({R.id.tv_toolbar_icon, R.id.tv_toolbar_right, R.id.tv_home_mine_suggestion})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_toolbar_icon:
                IMEUtils.hide(this);
                onBackPressed();
                break;
            case R.id.tv_toolbar_right:
                //弹出对话框
                if (suggestionAdapter.getItemCount() == 0) {
                    return;
                }
                showDialog();
                break;
            case R.id.tv_home_mine_suggestion:
                if (mEtSuggestion.getText().toString().length() < 10) {
                    ToastUtil.showNegativeToast(getString(R.string.Tap3_Feedback_TextFail));
                    return;
                }
                addInputItem();
                mEtSuggestion.setText("");
                break;
        }
    }

    /**
     * 自动回复
     */
    private void autoReply() {
        if (suggestionAdapter.getItemCount() == 0) {
            return;
        }

        if ((suggestionAdapter.getItemCount() == 1 || suggestionAdapter.getItemCount() == 2) || (suggestionAdapter.getCount() > 2 && suggestionAdapter.getItem(suggestionAdapter.getCount() - 1).type != 0)) {
            addAutoReply();
        }
    }

    /**
     * 弹出对话框
     */
    private void showDialog() {
        AlertDialogManager.getInstance().showDialog(this, getString(R.string.Tap3_Feedback_ClearTips), getString(R.string.Tap3_Feedback_ClearTips),
                getString(R.string.Tap3_Feedback_Clear), (DialogInterface dialog, int which) -> {
                    presenter.onClearAllTalk();
                    suggestionAdapter.clear();
                    suggestionAdapter.notifyDataSetHasChanged();
                }, getString(R.string.CANCEL), null);
    }

    /**
     * 用户进行反馈时添加一个自动回复的条目
     */
    @Override
    public void addAutoReply() {
        MineHelpSuggestionBean autoReplyBean = new MineHelpSuggestionBean();
        autoReplyBean.setType(0);
        autoReplyBean.setText(getString(R.string.Tap3_Feedback_AutoReply));
        autoReplyBean.setDate(System.currentTimeMillis() + "");
        suggestionAdapter.add(autoReplyBean);
        presenter.saveIntoDb(autoReplyBean);
    }

    /**
     * recycleview显示用户输入的条目
     */
    @Override
    public void addInputItem() {
        String suggestion = mEtSuggestion.getText().toString();
        MineHelpSuggestionBean suggestionBean = new MineHelpSuggestionBean();
        suggestionBean.setType(1);

        suggestionBean.setText(suggestion);
        suggestionBean.setIcon(presenter.getUserPhotoUrl());
        String time = System.currentTimeMillis() + "";
        suggestionBean.setDate(time);

        if (suggestionAdapter == null || suggestionAdapter.getItemCount() == 0 || suggestionAdapter.getItemCount() == 1) {
            suggestionBean.isShowTime = true;
        } else {
            if (presenter.checkOver20Min(suggestionAdapter.getList().get(suggestionAdapter.getItemCount() - 1).getDate())) {
                suggestionBean.isShowTime = true;
            } else {
                suggestionBean.isShowTime = false;
            }
        }

        if (NetUtils.getNetType(ContextUtils.getContext()) == -1) {
            suggestionBean.pro_falag = 1;
        } else {
            suggestionBean.pro_falag = 0;
        }
        presenter.saveIntoDb(suggestionBean);
        suggestionAdapter.add(suggestionBean);
        suggestionAdapter.notifyDataSetHasChanged();
        mRvMineSuggestion.scrollToPosition(suggestionAdapter.getItemCount() - 1);
        if (NetUtils.getNetType(ContextUtils.getContext()) != -1) {
            presenter.sendFeedBack(suggestionBean);
        }
    }

    @Override
    public void showLoadingDialog() {
        LoadingDialog.showLoading(getSupportFragmentManager());
    }

    @Override
    public void hideLoadingDialog() {
        LoadingDialog.dismissLoading(getSupportFragmentManager());
    }

    /**
     * 系统的回复
     *
     * @param time
     * @param content
     */
    @Override
    public void addSystemAutoReply(long time, String content) {
        MineHelpSuggestionBean autoReplyBean = new MineHelpSuggestionBean();
        autoReplyBean.setType(0);
        autoReplyBean.setText(content);
        autoReplyBean.setDate(System.currentTimeMillis() + "");
        suggestionAdapter.add(autoReplyBean);
        suggestionAdapter.notifyDataSetHasChanged();
        mRvMineSuggestion.scrollToPosition(suggestionAdapter.getItemCount() - 1); //滚动到集合最后一条显示；
        presenter.saveIntoDb(autoReplyBean);
//        RxBus.getCacheInstance().removeStickyEvent(RxEvent.GetFeedBackRsp.class);
    }

    public MineHelpSuggestionBean addSystemAutoReply() {
        MineHelpSuggestionBean autoReplyBean = new MineHelpSuggestionBean();
        autoReplyBean.setType(0);
        autoReplyBean.isShowTime = true;
        autoReplyBean.setText(getString(R.string.Tap3_Feedback_AutoTips));
        autoReplyBean.setDate(System.currentTimeMillis() + "");
        presenter.saveIntoDb(autoReplyBean);
        return autoReplyBean;
    }


    @Override
    public void refrshRecycleView(int code) {
        MineHelpSuggestionBean mineHelpSuggestionBean = null;
        for (MineHelpSuggestionBean bean : suggestionAdapter.getList()) {
            if (bean.getPro_falag() != 2) {
                mineHelpSuggestionBean = bean;
                break;
            }
        }
        if (mineHelpSuggestionBean == null) return;
        if (code != JError.ErrorOK) {
            if (resendFlag) {
                mineHelpSuggestionBean.pro_falag = 1;
                resendFlag = false;
                mRvMineSuggestion.setAdapter(suggestionAdapter);
                presenter.saveIntoDb(mineHelpSuggestionBean);
            } else {
                suggestionAdapter.getItem(suggestionAdapter.getItemCount() - 1).pro_falag = 1;
                mRvMineSuggestion.setAdapter(suggestionAdapter);
                presenter.saveIntoDb(suggestionAdapter.getItem(suggestionAdapter.getItemCount() - 1));
            }
        } else {
            if (resendFlag) {
                mineHelpSuggestionBean.pro_falag = 2;
                presenter.saveIntoDb(mineHelpSuggestionBean);
                resendFlag = false;
            } else {
                suggestionAdapter.getItem(suggestionAdapter.getItemCount() - 1).pro_falag = 2;
                presenter.saveIntoDb(suggestionAdapter.getItem(suggestionAdapter.getItemCount() - 1));
            }
            mRvMineSuggestion.setAdapter(suggestionAdapter);
            autoReply();

            //系统后台回复
//            presenter.getSystemAutoReply();
        }
        mRvMineSuggestion.scrollToPosition(suggestionAdapter.getItemCount() - 1); //滚动到集合最后一条显示；
    }

    /**
     * 上传日志的结果
     */
    @Override
    public void sendLogResult(int code) {
//        hideLoadingDialog();
    }

    /**
     * 初始化显示列表
     *
     * @param list
     */
    @Override
    public void initRecycleView(ArrayList<MineHelpSuggestionBean> list) {
        if (list.size() == 0) {
            //列表为空插入一条系统提示
            list.add(addSystemAutoReply());
        }
        for (MineHelpSuggestionBean bean : list) {
            if (bean.pro_falag == 0) bean.pro_falag = 2;
        }
        suggestionAdapter.clear();
        suggestionAdapter.addAll(list);
        //从最后一行显示
        mRvMineSuggestion.scrollToPosition(suggestionAdapter.getItemCount() - 1);

        suggestionAdapter.setOnResendFeedBack((holder, item, position) -> {
            if (NetUtils.getNetType(ContextUtils.getContext()) == -1) {
                ToastUtil.showToast(getString(R.string.NO_NETWORK_4));
                return;
            }
            showResendFeedBackDialog(holder, item, position);
        });
    }

    private void showResendFeedBackDialog(SuperViewHolder holder, MineHelpSuggestionBean item, int position) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle(getString(R.string.ANEW_SEND));
        b.setNegativeButton(getString(R.string.Button_No), (DialogInterface dialog, int which) -> {
            dialog.dismiss();
        });
        b.setPositiveButton(getString(R.string.Button_Yes), (DialogInterface dialog, int which) -> {
            resendFlag = true;
//            ImageView send_pro = (ImageView) holder.itemView.findViewById(R.id.iv_send_pro);
//            send_pro.setImageDrawable(getContext().getResources().getDrawable(R.drawable.listview_loading));
            item.setPro_falag(0);
            suggestionAdapter.notifyItemChanged(position);
            presenter.update(item);
            presenter.sendFeedBack(item);
            dialog.dismiss();
        });
        AlertDialogManager.getInstance().showDialog("showResendFeedBackDialog", this, b);
    }

    @Override
    public void setPresenter(FeedBackContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public String getUuid() {
        return null;
    }

    private void initKeyBoard() {
        KeyboardUtil.attach(this, panelRoot, isShowing -> {
            if (suggestionAdapter == null) return;
            mRvMineSuggestion.scrollToPosition(suggestionAdapter.getItemCount() - 1);
        });
        KPSwitchConflictUtil.attach(panelRoot, mEtSuggestion,
                switchToPanel -> {
                    AppLogger.d("KPSwitchConflictUtil:" + switchToPanel);
                    if (switchToPanel) {
                        mEtSuggestion.clearFocus();
                    } else {
                        mEtSuggestion.requestFocus();
                    }
                });

        mRvMineSuggestion.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                KPSwitchConflictUtil.hidePanelAndKeyboard(panelRoot);
            }
            return false;
        });
    }

}
