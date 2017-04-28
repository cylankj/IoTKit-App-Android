package com.cylan.jiafeigou.n.view.mine;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.n.mvp.contract.home.HomeMineHelpSuggestionContract;
import com.cylan.jiafeigou.n.mvp.impl.home.HomeMineHelpSuggestionImpl;
import com.cylan.jiafeigou.n.mvp.model.MineHelpSuggestionBean;
import com.cylan.jiafeigou.n.view.adapter.HomeMineHelpSuggestionAdapter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.softkeyboard.util.KPSwitchConflictUtil;
import com.cylan.jiafeigou.support.softkeyboard.util.KeyboardUtil;
import com.cylan.jiafeigou.support.softkeyboard.widget.KPSwitchFSPanelLinearLayout;
import com.cylan.jiafeigou.support.superadapter.internal.SuperViewHolder;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.IMEUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.LoadingDialog;
import com.cylan.jiafeigou.widget.dialog.BaseDialog;
import com.cylan.jiafeigou.widget.dialog.SimpleDialogFragment;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * 创建者     谢坤
 * 创建时间   2016/8/8 14:37
 * 描述	      ${TODO}
 * <p>
 * 更新者     $Author$
 * 更新时间   $Date$
 * 更新描述   ${TODO}
 */
public class HomeMineHelpSuggestionFragment extends Fragment implements HomeMineHelpSuggestionContract.View, BaseDialog.BaseDialogAction {

    @BindView(R.id.rv_home_mine_suggestion)
    RecyclerView mRvMineSuggestion;
    @BindView(R.id.et_home_mine_suggestion)
    EditText mEtSuggestion;
    @BindView(R.id.tv_home_mine_suggestion)
    TextView tvHomeMineSuggestion;
    @BindView(R.id.panel_root)
    KPSwitchFSPanelLinearLayout panelRoot;
    @BindView(R.id.iv_loading_rotate)
    ImageView ivLoadingRotate;
    @BindView(R.id.fl_loading_container)
    FrameLayout flLoadingContainer;

    private HomeMineHelpSuggestionAdapter suggestionAdapter;
    private HomeMineHelpSuggestionContract.Presenter presenter;
    private int itemPosition;
    private boolean resendFlag;
    private boolean hasSendLog = false;

    private static final String DIALOG_KEY = "dialogFragment";

    public static HomeMineHelpSuggestionFragment newInstance(Bundle bundle) {
        HomeMineHelpSuggestionFragment fragment = new HomeMineHelpSuggestionFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine_help_suggestion, container, false);
        ButterKnife.bind(this, view);
//        initKeyBoard();
        initPresenter();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initKeyBoard();
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

    @OnClick({R.id.tv_toolbar_icon, R.id.tv_toolbar_right, R.id.tv_home_mine_suggestion})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_toolbar_icon:
                IMEUtils.hide(getActivity());
                getFragmentManager().popBackStack();
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

        if (suggestionAdapter.getItemCount() == 1 || suggestionAdapter.getItemCount() == 2) {
            addAutoReply();
        } else {
            if (presenter.checkOverTime(suggestionAdapter.getItem(suggestionAdapter.getItemCount() - 2).getDate())) {
                addAutoReply();
            }
        }
    }

    /**
     * 弹出对话框
     */
    private void showDialog() {
        Fragment f = getActivity().getSupportFragmentManager().findFragmentByTag(DIALOG_KEY);
        if (f == null) {
            Bundle bundle = new Bundle();
            bundle.putString(BaseDialog.KEY_TITLE, getString(R.string.Tap3_Feedback_ClearTips));
            bundle.putString(SimpleDialogFragment.KEY_LEFT_CONTENT, getString(R.string.Tap3_Feedback_Clear));
            bundle.putString(SimpleDialogFragment.KEY_RIGHT_CONTENT, getString(R.string.CANCEL));
            bundle.putBoolean(SimpleDialogFragment.KEY_TOUCH_OUT_SIDE_DISMISS, false);
            SimpleDialogFragment dialogFragment = SimpleDialogFragment.newInstance(bundle);
            dialogFragment.setValue("clearLocal");
            dialogFragment.setAction(this);
            dialogFragment.show(getActivity().getSupportFragmentManager(), DIALOG_KEY);
        }
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

        if (suggestionAdapter.getItemCount() == 0 || suggestionAdapter.getItemCount() == 1) {
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
            presenter.saveIntoDb(suggestionBean);
            suggestionAdapter.add(suggestionBean);
            suggestionAdapter.notifyDataSetHasChanged();
            mRvMineSuggestion.scrollToPosition(suggestionAdapter.getItemCount() - 1);
            return;
        } else {
            suggestionBean.pro_falag = 0;
        }
        suggestionAdapter.add(suggestionBean);
        suggestionAdapter.notifyDataSetHasChanged();
        mRvMineSuggestion.scrollToPosition(suggestionAdapter.getItemCount() - 1);
        presenter.sendFeedBack(suggestionBean);
    }

    @Override
    public void showLoadingDialog() {
        LoadingDialog.showLoading(getFragmentManager());
    }

    @Override
    public void hideLoadingDialog() {
        LoadingDialog.dismissLoading(getFragmentManager());
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
        RxBus.getCacheInstance().removeStickyEvent(RxEvent.GetFeedBackRsp.class);

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
        if (code != JError.ErrorOK) {
            if (resendFlag) {
                suggestionAdapter.getItem(itemPosition).pro_falag = 1;
                resendFlag = false;
                mRvMineSuggestion.setAdapter(suggestionAdapter);
                presenter.saveIntoDb(suggestionAdapter.getItem(itemPosition));
            } else {
                suggestionAdapter.getItem(suggestionAdapter.getItemCount() - 1).pro_falag = 1;
                mRvMineSuggestion.setAdapter(suggestionAdapter);
                presenter.saveIntoDb(suggestionAdapter.getItem(suggestionAdapter.getItemCount() - 1));
            }
        } else {
            if (resendFlag) {
                suggestionAdapter.getItem(itemPosition).pro_falag = 2;
                presenter.saveIntoDb(suggestionAdapter.getItem(itemPosition));
                resendFlag = false;
            } else {
                suggestionAdapter.getItem(suggestionAdapter.getItemCount() - 1).pro_falag = 2;
                presenter.saveIntoDb(suggestionAdapter.getItem(suggestionAdapter.getItemCount() - 1));
            }
            autoReply();
            mRvMineSuggestion.setAdapter(suggestionAdapter);

            //系统后台回复
            presenter.getSystemAutoReply();
        }
        mRvMineSuggestion.scrollToPosition(suggestionAdapter.getItemCount() - 1); //滚动到集合最后一条显示；
    }

    /**
     * 上传日志的结果
     */
    @Override
    public void sendLogResult(int code) {
//        hideLoadingDialog();
        hasSendLog = true;
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
            bean.icon = presenter.getUserPhotoUrl();
        }
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mRvMineSuggestion.setLayoutManager(layoutManager);
        suggestionAdapter = new HomeMineHelpSuggestionAdapter(getContext(), list, null);
        mRvMineSuggestion.setAdapter(suggestionAdapter);
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
        AlertDialog.Builder b = new AlertDialog.Builder(getContext());
        b.setTitle(getString(R.string.ANEW_SEND));
        b.setNegativeButton(getString(R.string.Button_No), (DialogInterface dialog, int which) -> {
            dialog.dismiss();
        });
        b.setPositiveButton(getString(R.string.Button_Yes), (DialogInterface dialog, int which) -> {
            itemPosition = position;
            resendFlag = true;
//            ImageView send_pro = (ImageView) holder.itemView.findViewById(R.id.iv_send_pro);
//            send_pro.setImageDrawable(getContext().getResources().getDrawable(R.drawable.listview_loading));
            item.setPro_falag(0);
            suggestionAdapter.notifyDataSetHasChanged();
            presenter.sendFeedBack(item);
            presenter.deleteOnItemFromDb(item);
            dialog.dismiss();
        }).show();
    }

    @Override
    public void setPresenter(HomeMineHelpSuggestionContract.Presenter presenter) {
        this.presenter = presenter;
    }

    private void initKeyBoard() {
        KeyboardUtil.attach(getActivity(), panelRoot, isShowing -> {
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

    @Override
    public void onDialogAction(int id, Object value) {
        Fragment f = getActivity()
                .getSupportFragmentManager()
                .findFragmentByTag(DIALOG_KEY);
        if (f != null && f.isVisible()) {
            ((SimpleDialogFragment) f).dismiss();
        }
        if (id == R.id.tv_dialog_btn_left) {
            presenter.onClearAllTalk();
            suggestionAdapter.clear();
            suggestionAdapter.notifyDataSetHasChanged();
        }
    }
}
