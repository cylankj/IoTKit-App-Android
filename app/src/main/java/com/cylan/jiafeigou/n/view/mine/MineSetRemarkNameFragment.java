package com.cylan.jiafeigou.n.view.mine;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineSetRemarkNameContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.MineSetRemarkNamePresenterImp;
import com.cylan.jiafeigou.n.view.adapter.item.FriendContextItem;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.CustomToolbar;
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
public class MineSetRemarkNameFragment extends IBaseFragment implements MineSetRemarkNameContract.View {

    @BindView(R.id.et_mine_set_remarkname_new_name)
    EditText etMineSetRemarknameNewName;
    @BindView(R.id.view_mine_personal_set_remarkname_new_name_line)
    View viewMinePersonalSetRemarknameNewNameLine;
    @BindView(R.id.iv_mine_personal_set_remarkname_clear)
    ImageView ivMinePersonalSetRemarknameClear;
    @BindView(R.id.custom_toolbar)
    CustomToolbar customToolbar;


    private MineSetRemarkNameContract.Presenter presenter;

    private OnSetRemarkNameListener listener;
    private FriendContextItem friendItem;

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
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
    private void initPresenter() {
        presenter = new MineSetRemarkNamePresenterImp(this);
    }

    @OnTextChanged(R.id.et_mine_set_remarkname_new_name)
    public void onEditChange(CharSequence s, int start, int before, int count) {
        boolean isEmpty = TextUtils.isEmpty(getEditName());
        if (isEmpty) {
            customToolbar.setTvToolbarRightIcon(R.drawable.icon_finish_disable);
            customToolbar.setTvToolbarRightEnable(false);
            ivMinePersonalSetRemarknameClear.setVisibility(View.GONE);
            viewMinePersonalSetRemarknameNewNameLine.setBackgroundColor(Color.parseColor("#f2f2f2"));
        } else {
            customToolbar.setTvToolbarRightIcon(R.drawable.icon_finish_normal);
            customToolbar.setTvToolbarRightEnable(true);
            ivMinePersonalSetRemarknameClear.setVisibility(View.VISIBLE);
            viewMinePersonalSetRemarknameNewNameLine.setBackgroundColor(Color.parseColor("#36bdff"));
        }
    }

    @OnClick({R.id.tv_toolbar_icon, R.id.tv_toolbar_right, R.id.iv_mine_personal_set_remarkname_clear})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_toolbar_icon:
                getActivity().getSupportFragmentManager().popBackStack();
                break;
            case R.id.tv_toolbar_right:
                if (presenter != null) {
                    presenter.setMarkName(getEditName(), friendItem);
                }
                break;
            case R.id.iv_mine_personal_set_remarkname_clear:
                etMineSetRemarknameNewName.setText("");
                break;
        }
    }

    @Override
    public String getEditName() {
        return etMineSetRemarknameNewName.getText().toString().trim().replace(" ", "");
    }

    /**
     * 初始化页面显示
     */
    @Override
    public void initViewShow() {
        Bundle arguments = getArguments();
        friendItem = arguments.getParcelable("friendItem");
        etMineSetRemarknameNewName.setText(friendItem.friendAccount.markName);
    }

    /**
     * 修改完成结果设置
     */
    @Override
    public void showFinishResult(RxEvent.SetFriendMarkNameBack getFriendInfoCall) {
        if (getFriendInfoCall == null) {
            // TODO: 2017/7/1 timeout
        } else if (getFriendInfoCall.jfgResult.code == JError.ErrorOK) {
            ToastUtil.showPositiveToast(getString(R.string.PWD_OK_2));
            if (listener != null) {
                listener.remarkNameChange(getEditName());
            }
            getActivity().getSupportFragmentManager().popBackStack();
        } else {
            ToastUtil.showPositiveToast(getString(R.string.SUBMIT_FAIL));
        }

    }

    /**
     * 显示正在修改进度提示
     */
    @Override
    public void showSendReqPro() {
        LoadingDialog.showLoading(getActivity(), getString(R.string.is_creating), true);
    }

    /**
     * 隐藏修改进度提示
     */
    @Override
    public void hideSendReqPro() {
        LoadingDialog.dismissLoading();
    }

    /**
     * 网络状态变化
     *
     * @param state
     */
    @Override
    public void onNetStateChanged(int state) {
        if (state == -1) {
            hideSendReqPro();
            ToastUtil.showNegativeToast(getString(R.string.NO_NETWORK_1));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (TextUtils.isEmpty(getEditName())) {
            customToolbar.setTvToolbarRightIcon(R.drawable.icon_finish_disable);
            customToolbar.setTvToolbarRightEnable(false);
            ivMinePersonalSetRemarknameClear.setVisibility(View.GONE);
        } else {
            customToolbar.setTvToolbarRightIcon(R.drawable.icon_finish_normal);
            customToolbar.setTvToolbarRightEnable(true);
            ivMinePersonalSetRemarknameClear.setVisibility(View.VISIBLE);
        }
    }
}
