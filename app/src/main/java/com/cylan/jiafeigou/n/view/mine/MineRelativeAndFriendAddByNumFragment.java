package com.cylan.jiafeigou.n.view.mine;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineRelativeAndFriendAddByNumContract;
import com.cylan.jiafeigou.utils.ViewUtils;

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

    public static MineRelativeAndFriendAddByNumFragment newInstance() {
        return new MineRelativeAndFriendAddByNumFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine_relativeandfriend_add_by_num, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void setPresenter(MineRelativeAndFriendAddByNumContract.Presenter presenter) {

    }

    @OnClick(R.id.iv_home_mine_relativesandfriends_add_by_num_back)
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.iv_home_mine_relativesandfriends_add_by_num_back:
                getFragmentManager().popBackStack();
                break;
        }
    }
}
