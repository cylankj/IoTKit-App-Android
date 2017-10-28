package com.cylan.jiafeigou.n.view.mine;

import android.content.Context;
import android.databinding.ObservableBoolean;
import android.os.Bundle;
import android.support.annotation.Nullable;
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

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.databinding.FragmentMineFriendAddByNumBinding;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineFriendSearchContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.MineFriendAddByNumPresenterImp;
import com.cylan.jiafeigou.n.view.adapter.item.FriendContextItem;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.IMEUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.LoadingDialog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.support.v4.app.FragmentManager.POP_BACK_STACK_INCLUSIVE;

/**
 * 作者：zsl
 * 创建时间：2016/9/7
 * 描述：
 */
public class MineFriendSearchFragment extends IBaseFragment implements MineFriendSearchContract.View {
    @BindView(R.id.et_add_by_number)
    EditText etAddByNumber;

    private ObservableBoolean empty = new ObservableBoolean(false);

    private MineFriendSearchContract.Presenter presenter;
    private FragmentMineFriendAddByNumBinding friendAddByNumBinding;

    public static MineFriendSearchFragment newInstance() {
        return new MineFriendSearchFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine_friend_add_by_num, container, false);
        friendAddByNumBinding = FragmentMineFriendAddByNumBinding.inflate(inflater, container, false);
        friendAddByNumBinding.setEmpty(empty);
        ButterKnife.bind(this, friendAddByNumBinding.customToolbar);
        initPresenter();
        addOnTouchListener(view);
        return friendAddByNumBinding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewUtils.setChineseExclude(etAddByNumber, 65);
        initKeyListener();
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
                if (TextUtils.isEmpty(getInputNum())
                        || (!JConstant.PHONE_REG.matcher(getInputNum()).matches()
                        && !JConstant.EMAIL_REG.matcher(getInputNum()).matches())) {
                    ToastUtil.showNegativeToast(getString(R.string.ACCOUNT_ERR));
                } else if (getInputNum().equals(account)) {
                    ToastUtil.showNegativeToast(getString(R.string.Tap3_FriendsAdd_NotYourself));
                } else {
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
                empty.set(false);
            }
        });
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
                getActivity().getSupportFragmentManager().popBackStack(MineFriendInformationFragment.class.getSimpleName(), POP_BACK_STACK_INCLUSIVE);
                break;
        }
    }

    public String getInputNum() {
        return etAddByNumber.getText().toString().trim();
    }


    @Override
    public void showLoading(int resId, String... args) {
        LoadingDialog.showLoading(getActivity(), getContext().getString(resId, (Object[]) args), true);
    }

    @Override
    public void hideLoading() {
        LoadingDialog.dismissLoading();
    }

    @Override
    public void onCheckFriendResult(FriendContextItem friendContextItem) {
        empty.set(friendContextItem == null);
        if (empty.get()) {
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putParcelable("friendItem", friendContextItem);
        MineFriendInformationFragment friendInformationFragment = MineFriendInformationFragment.newInstance(bundle);
        ActivityUtils.addFragmentSlideInFromRight(getActivity().getSupportFragmentManager(),
                friendInformationFragment, android.R.id.content, MineFriendInformationFragment.class.getSimpleName());
    }

    /**
     * 网络状态变化
     *
     * @param state
     */
    @Override
    public void onNetStateChanged(int state) {
        if (state == -1) {
            hideLoading();
            ToastUtil.showNegativeToast(getString(R.string.NO_NETWORK_1));
        }
    }
}
