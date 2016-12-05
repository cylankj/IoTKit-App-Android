package com.cylan.jiafeigou.n.view.mine;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineSetRemarkNameContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.MineSetRemarkNamePresenterImp;
import com.cylan.jiafeigou.n.mvp.model.RelAndFriendBean;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.LoadingDialog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

/**
 * 作者：zsl
 * 创建时间：2016/9/23
 * 描述：
 */
public class MineSetRemarkNameFragment extends Fragment implements MineSetRemarkNameContract.View {

    @BindView(R.id.iv_top_bar_left_back)
    ImageView ivTopBarLeftBack;
    @BindView(R.id.iv_mine_set_remarkname_bind)
    ImageView ivMineSetRemarknameBind;
    @BindView(R.id.et_mine_set_remarkname_new_name)
    EditText etMineSetRemarknameNewName;
    @BindView(R.id.view_mine_personal_set_remarkname_new_name_line)
    View viewMinePersonalSetRemarknameNewNameLine;
    @BindView(R.id.iv_mine_personal_set_remarkname_clear)
    ImageView ivMinePersonalSetRemarknameClear;


    private MineSetRemarkNameContract.Presenter presenter;

    private OnSetRemarkNameListener listener;
    private RelAndFriendBean friendBean;

    public interface OnSetRemarkNameListener {
        void remarkNameChange(String name);
    }

    public void setOnSetRemarkNameListener(OnSetRemarkNameListener listener) {
        this.listener = listener;
    }

    public static MineSetRemarkNameFragment newInstance(Bundle bundle) {
        MineSetRemarkNameFragment fragment = new MineSetRemarkNameFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_set_remark_name, container, false);
        ButterKnife.bind(this, view);
        initPresenter();
        initViewShow();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (presenter != null)presenter.start();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (presenter != null)presenter.stop();
    }

    private void initPresenter() {
        presenter = new MineSetRemarkNamePresenterImp(this);
    }

    @Override
    public void setPresenter(MineSetRemarkNameContract.Presenter presenter) {

    }

    @OnTextChanged(R.id.et_mine_set_remarkname_new_name)
    public void onEditChange(CharSequence s, int start, int before, int count) {
        boolean isEmpty = TextUtils.isEmpty(getEditName());
        if (isEmpty) {
            ivMineSetRemarknameBind.setImageDrawable(getResources().getDrawable(R.drawable.icon_finish_disable));
            ivMineSetRemarknameBind.setEnabled(false);
            ivMinePersonalSetRemarknameClear.setVisibility(View.GONE);
        } else {
            ivMineSetRemarknameBind.setImageDrawable(getResources().getDrawable(R.drawable.icon_finish));
            ivMineSetRemarknameBind.setEnabled(true);
            ivMinePersonalSetRemarknameClear.setVisibility(View.VISIBLE);
        }
    }

    @OnClick({R.id.iv_top_bar_left_back, R.id.iv_mine_set_remarkname_bind, R.id.iv_mine_personal_set_remarkname_clear})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_top_bar_left_back:
                getFragmentManager().popBackStack();
                break;
            case R.id.iv_mine_set_remarkname_bind:
                if (presenter != null){
                    presenter.sendSetmarkNameReq(getEditName(),friendBean);
                }
                break;
            case R.id.iv_mine_personal_set_remarkname_clear:
                etMineSetRemarknameNewName.setText("");
                break;
        }
    }

    @Override
    public String getEditName() {
        return etMineSetRemarknameNewName.getText().toString().trim();
    }

    /**
     * 初始化页面显示
     */
    @Override
    public void initViewShow() {
        Bundle arguments = getArguments();
        friendBean = arguments.getParcelable("friendBean");
        etMineSetRemarknameNewName.setText(friendBean.markName);
    }

    /**
     * 修改完成结果设置
     */
    @Override
    public void showFinishResult(RxEvent.GetFriendInfoCall getFriendInfoCall) {
        if (getFriendInfoCall.i == JError.ErrorOK && getEditName().equals(getFriendInfoCall.jfgFriendAccount.markName)){
            ToastUtil.showPositiveToast(getString(R.string.PWD_OK_2));
            if (listener != null) {
                listener.remarkNameChange(getEditName());
            }
            getFragmentManager().popBackStack();
        }else {
            ToastUtil.showPositiveToast(getString(R.string.SUBMIT_FAIL));
        }

    }

    /**
     * 显示正在修改进度提示
     */
    @Override
    public void showSendReqPro() {
        LoadingDialog.showLoading(getFragmentManager(),getString(R.string.is_creating));
    }

    /**
     * 隐藏修改进度提示
     */
    @Override
    public void hideSendReqPro() {
        LoadingDialog.dismissLoading(getFragmentManager());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (TextUtils.isEmpty(getEditName())) {
            ivMineSetRemarknameBind.setImageDrawable(getResources().getDrawable(R.drawable.icon_finish_disable));
            ivMineSetRemarknameBind.setEnabled(false);
            ivMinePersonalSetRemarknameClear.setVisibility(View.GONE);
        } else {
            ivMineSetRemarknameBind.setImageDrawable(getResources().getDrawable(R.drawable.icon_finish));
            ivMineSetRemarknameBind.setEnabled(true);
            ivMinePersonalSetRemarknameClear.setVisibility(View.VISIBLE);
        }
    }
}
