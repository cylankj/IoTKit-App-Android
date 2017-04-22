package com.cylan.jiafeigou.n.view.mine;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineFriendAddByNumContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.MineFriendAddByNumPresenterImp;
import com.cylan.jiafeigou.n.mvp.model.MineAddReqBean;
import com.cylan.jiafeigou.n.mvp.model.RelAndFriendBean;
import com.cylan.jiafeigou.utils.IMEUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
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

    @BindView(R.id.iv_home_mine_friends_add_by_num_back)
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
        addOnTouchListener(view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewUtils.setChineseExclude(etAddByNumber, 65);
        initKeyListener();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (presenter != null) presenter.start();
    }

    private void initPresenter() {
        presenter = new MineFriendAddByNumPresenterImp(this);
    }

    private void initKeyListener() {
        etAddByNumber.requestFocus();
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS);
        etAddByNumber.setOnKeyListener((v, keyCode, event) -> {
            if (KeyEvent.KEYCODE_ENTER == keyCode && KeyEvent.ACTION_DOWN == event.getAction()) {
                String account = BaseApplication.getAppComponent().getSourceManager().getJFGAccount().getAccount();
                if (TextUtils.isEmpty(getInputNum())) {
                    ToastUtil.showNegativeToast(getString(R.string.ACCOUNT_ERR));
                } else if (getInputNum().equals(account)) {
                    ToastUtil.showNegativeToast(getString(R.string.Tap3_FriendsAdd_NotYourself));
                } else {
                    showFindLoading();
                    presenter.checkFriendAccount(getInputNum());
                }
                return true;
            }
            return false;
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

    /**
     * 用来点击空白处隐藏键盘
     *
     * @param view
     */
    public void addOnTouchListener(android.view.View view) {
        view.setOnTouchListener(new android.view.View.OnTouchListener() {
            @Override
            public boolean onTouch(android.view.View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    IMEUtils.hide(getActivity());
                }
                return false;
            }
        });
    }

    @OnClick(R.id.iv_home_mine_friends_add_by_num_back)
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_home_mine_friends_add_by_num_back:
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
        LoadingDialog.showLoading(getFragmentManager(), getString(R.string.LOADING));
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
    public void setFindResult(boolean isFrom, MineAddReqBean bean, boolean isFriend) {
        if (!isFriend) {
            Bundle bundle = new Bundle();
            bundle.putBoolean("isFrom", isFrom);
            bundle.putSerializable("addRequestItems", bean);
            MineFriendAddReqDetailFragment addReqDetailFragment = MineFriendAddReqDetailFragment.newInstance(bundle);
            getFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                            , R.anim.slide_in_left, R.anim.slide_out_right)
                    .add(android.R.id.content, addReqDetailFragment, addReqDetailFragment.getClass().getName())
                    .addToBackStack("AddFlowStack")
                    .commit();
        } else {
            //已是亲友的跳转到分享
            RelAndFriendBean friendBean = new RelAndFriendBean();
            friendBean.account = bean.account;
            friendBean.alias = bean.alias;
            friendBean.markName = "";
            friendBean.iconUrl = bean.iconUrl;
            Bundle bundle = new Bundle();
            bundle.putInt("position", -1);
            bundle.putParcelable("frienditembean", friendBean);
            MineFriendDetailFragment relativeAndFrienDetialFragment = MineFriendDetailFragment.newInstance(bundle);
            getFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                            , R.anim.slide_in_left, R.anim.slide_out_right)
                    .add(android.R.id.content, relativeAndFrienDetialFragment, "relativeAndFrienDetialFragment")
                    .addToBackStack("mineHelpFragment")
                    .commit();
        }

    }

    /**
     * 网络状态变化
     *
     * @param state
     */
    @Override
    public void onNetStateChanged(int state) {
        if (state == -1) {
            hideFindLoading();
            ToastUtil.showNegativeToast(getString(R.string.NO_NETWORK_1));
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (presenter != null) {
            presenter.stop();
            IMEUtils.hide(getActivity());
        }
    }

}
