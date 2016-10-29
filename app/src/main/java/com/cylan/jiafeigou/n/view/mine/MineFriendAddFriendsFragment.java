package com.cylan.jiafeigou.n.view.mine;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineFriendsAddFriendContract;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ViewUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 作者：zsl
 * 创建时间：2016/9/6
 * 描述：
 */
public class MineFriendAddFriendsFragment extends Fragment implements MineFriendsAddFriendContract.View {


    @BindView(R.id.iv_home_mine_relativesandfriends_add_back)
    ImageView ivHomeMineRelativesandfriendsAddBack;
    @BindView(R.id.et_friend_phonenumber)
    EditText etFriendPhonenumber;
    @BindView(R.id.tv_scan_add)
    TextView tvScanAdd;
    @BindView(R.id.tv_add_from_contract)
    TextView tvAddFromContract;

    private MineFriendScanAddFragment scanAddFragment;
    private MineFriendAddFromContactFragment addFromContactFragment;
    private MineFriendAddByNumFragment addByNumFragment;

    public static MineFriendAddFriendsFragment newInstance() {
        return new MineFriendAddFriendsFragment();
    }

    @Override
    public void setPresenter(MineFriendsAddFriendContract.Presenter presenter) {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scanAddFragment = MineFriendScanAddFragment.newInstance();
        addFromContactFragment = MineFriendAddFromContactFragment.newInstance();
        addByNumFragment = MineFriendAddByNumFragment.newInstance();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_mine_relativesandfriends_addfriends, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @OnClick({R.id.iv_home_mine_relativesandfriends_add_back, R.id.tv_scan_add, R.id.tv_add_from_contract, R.id.et_friend_phonenumber})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_home_mine_relativesandfriends_add_back:        //返回
                getFragmentManager().popBackStack();
                break;

            case R.id.tv_scan_add:                                      //扫一扫添加
                if (getView() != null)
                    ViewUtils.deBounceClick(getView().findViewById(R.id.tv_scan_add));
                    AppLogger.e("tv_scan_add");
                    jump2ScanAddFragment();
                break;

            case R.id.tv_add_from_contract:                             //从通讯录添加
                if (getView() != null)
                    ViewUtils.deBounceClick(getView().findViewById(R.id.tv_add_from_contract));
                    AppLogger.e("tv_add_from_contract");
                    jump2AddFromContactFragment();
                break;

            case R.id.et_friend_phonenumber:                            //根据对方账号添加
                if (getView() != null)
                    ViewUtils.deBounceClick(getView().findViewById(R.id.et_friend_phonenumber));
                AppLogger.e("et_friend_phonenumber");
                jump2AddByNumberFragment();
                break;
        }
    }

    private void jump2AddByNumberFragment() {
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(android.R.id.content, addByNumFragment, "addByNumFragment")
                .addToBackStack("mineHelpFragment")
                .commit();
    }

    private void jump2AddFromContactFragment() {
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(android.R.id.content, addFromContactFragment, "addFromContactFragment")
                .addToBackStack("mineHelpFragment")
                .commit();
    }

    private void jump2ScanAddFragment() {
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(android.R.id.content, scanAddFragment, "scanAddFragment")
                .addToBackStack("mineHelpFragment")
                .commit();
    }
}
