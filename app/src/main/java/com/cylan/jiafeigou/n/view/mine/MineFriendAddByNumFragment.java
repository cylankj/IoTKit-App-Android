package com.cylan.jiafeigou.n.view.mine;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineFriendAddByNumContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.MineFriendAddByNumPresenterImp;
import com.cylan.jiafeigou.n.mvp.model.MineAddReqBean;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.LoadingDialog;
import com.cylan.jiafeigou.widget.roundedimageview.RoundedImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 作者：zsl
 * 创建时间：2016/9/7
 * 描述：
 */
public class MineFriendAddByNumFragment extends Fragment implements MineFriendAddByNumContract.View {

    @BindView(R.id.iv_home_mine_relativesandfriends_add_by_num_back)
    ImageView ivHomeMineRelativesandfriendsAddByNumBack;
    @BindView(R.id.et_add_by_number)
    EditText etAddByNumber;
    @BindView(R.id.iv_userhead)
    RoundedImageView ivUserhead;
    @BindView(R.id.tv_username)
    TextView tvUsername;
    @BindView(R.id.tv_user_phone)
    TextView tvUserPhone;
    @BindView(R.id.rl_relative_and_friend_container)
    RelativeLayout rlRelativeAndFriendContainer;
    @BindView(R.id.fl_display_find_result)
    FrameLayout flDisplayFindResult;
    @BindView(R.id.rl_home_mine_relativesandfriends_add_by_num)
    RelativeLayout rlHomeMineRelativesandfriendsAddByNum;
    @BindView(R.id.ll_no_friend)
    LinearLayout llNoFriend;

    private MineFriendAddByNumContract.Presenter presenter;

    public static MineFriendAddByNumFragment newInstance() {
        return new MineFriendAddByNumFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine_friend_add_by_num, container, false);
        ButterKnife.bind(this, view);
        initPresenter();
        initKeyListener();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (presenter != null)presenter.start();
    }

    private void initPresenter() {
        presenter = new MineFriendAddByNumPresenterImp(this);
    }

    private void initKeyListener() {
        etAddByNumber.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (KeyEvent.KEYCODE_ENTER == keyCode && KeyEvent.ACTION_DOWN == event.getAction()) {
                    showFindLoading();
                    presenter.checkFriendAccount(getInputNum());
                    return true;
                }
                return false;
            }
        });

        etAddByNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                hideFindNoResult();
            }
        });
    }

    @Override
    public void setPresenter(MineFriendAddByNumContract.Presenter presenter) {

    }

    @OnClick(R.id.iv_home_mine_relativesandfriends_add_by_num_back)
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_home_mine_relativesandfriends_add_by_num_back:
                getFragmentManager().popBackStack();
                break;
        }
    }

    @Override
    public String getInputNum() {
        return etAddByNumber.getText().toString().trim();
    }

    @Override
    public void showFindResult(MineAddReqBean bean) {
        if (bean == null) {
            showFindNoResult();
        } else {
            presenter.checkIsSendAddReqToMe(bean);
        }
    }

    @Override
    public void showFindLoading() {
        LoadingDialog.showLoading(getFragmentManager(),getString(R.string.LOADING));
    }

    @Override
    public void hideFindLoading() {
        LoadingDialog.dismissLoading(getFragmentManager());
    }

    /**
     * 显示无结果
     */
    @Override
    public void showFindNoResult() {
        rlRelativeAndFriendContainer.setVisibility(View.GONE);
        llNoFriend.setVisibility(View.VISIBLE);
    }

    /**
     * 隐藏显示无结果
     */
    @Override
    public void hideFindNoResult() {
        llNoFriend.setVisibility(View.GONE);
    }

    @Override
    public void setFindResult(boolean isFrom,MineAddReqBean bean) {
            Bundle bundle = new Bundle();
            bundle.putBoolean("isFrom",isFrom);
            bundle.putSerializable("addRequestItems", bean);
            MineFriendAddReqDetailFragment addReqDetailFragment = MineFriendAddReqDetailFragment.newInstance(bundle);
            getFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                            , R.anim.slide_in_left, R.anim.slide_out_right)
                    .add(android.R.id.content, addReqDetailFragment, "addReqDetailFragment")
                    .addToBackStack("mineHelpFragment")
                    .commit();
    }

    /**
     * 网络状态变化
     * @param state
     */
    @Override
    public void onNetStateChanged(int state) {
        if (state == -1){
            hideFindLoading();
            ToastUtil.showNegativeToast(getString(R.string.NO_NETWORK_1));
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (presenter != null) {
            presenter.stop();
        }
    }
}
