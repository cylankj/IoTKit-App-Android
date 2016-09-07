package com.cylan.jiafeigou.n.view.mine;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineAddFromContactContract;

import butterknife.ButterKnife;

/**
 * 作者：zsl
 * 创建时间：2016/9/7
 * 描述：
 */
public class MineAddFromContactFragment extends Fragment implements MineAddFromContactContract.View {


    public static MineAddFromContactFragment newInstance(){
        return new MineAddFromContactFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine_add_from_contact,container,false);
        ButterKnife.bind(this,view);
        return view;
    }

    @Override
    public void setPresenter(MineAddFromContactContract.Presenter presenter) {

    }
}
