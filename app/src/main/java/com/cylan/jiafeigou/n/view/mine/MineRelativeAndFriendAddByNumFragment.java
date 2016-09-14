package com.cylan.jiafeigou.n.view.mine;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineRelativeAndFriendAddByNumContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.MineRelativeAndFriendAddByNumPresenterImp;
import com.cylan.jiafeigou.n.mvp.model.UserInfoBean;
import com.cylan.jiafeigou.widget.roundedimageview.RoundedImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 作者：zsl
 * 创建时间：2016/9/7
 * 描述：
 */
public class MineRelativeAndFriendAddByNumFragment extends Fragment implements MineRelativeAndFriendAddByNumContract.View {

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
    @BindView(R.id.rl_find_load_progress)
    RelativeLayout rlFindLoadProgress;
    @BindView(R.id.fl_display_find_result)
    FrameLayout flDisplayFindResult;

    private MineRelativeAndFriendAddByNumContract.Presenter presenter;

    public static MineRelativeAndFriendAddByNumFragment newInstance() {
        return new MineRelativeAndFriendAddByNumFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine_relativeandfriend_add_by_num, container, false);
        ButterKnife.bind(this, view);
        initPresenter();
        initKeyListener();
        return view;
    }

    private void initPresenter() {
        presenter = new MineRelativeAndFriendAddByNumPresenterImp(this);
    }

    private void initKeyListener() {
        etAddByNumber.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (KeyEvent.KEYCODE_ENTER == keyCode && KeyEvent.ACTION_DOWN == event.getAction()) {
                    showFindLoad();
                    presenter.findUserFromServer(getInputNum());
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void setPresenter(MineRelativeAndFriendAddByNumContract.Presenter presenter) {

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
    public void showFindResult(UserInfoBean bean) {
        rlFindLoadProgress.setVisibility(View.GONE);
        if (bean == null) {
            rlRelativeAndFriendContainer.setVisibility(View.GONE);
            TextView noneResult = new TextView(getContext());
            noneResult.setText("无结果");
            flDisplayFindResult.addView(noneResult);
        } else {
            rlRelativeAndFriendContainer.setVisibility(View.VISIBLE);
            //TODO 设置显示查询结果
        }

    }

    @Override
    public void showFindLoad() {
        rlFindLoadProgress.setVisibility(View.VISIBLE);
        rlRelativeAndFriendContainer.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (presenter != null) {
            presenter.stop();
        }
    }
}
