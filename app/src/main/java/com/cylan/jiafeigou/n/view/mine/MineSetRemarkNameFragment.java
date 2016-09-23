package com.cylan.jiafeigou.n.view.mine;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineSetRemarkNameContract;

/**
 * 作者：zsl
 * 创建时间：2016/9/23
 * 描述：
 */
public class MineSetRemarkNameFragment extends Fragment implements MineSetRemarkNameContract.View {

    public static MineSetRemarkNameFragment newInstance(Bundle bundle){
        MineSetRemarkNameFragment fragment = new MineSetRemarkNameFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //View view = inflater.inflate(R.layout.fragment_set)
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void setPresenter(MineSetRemarkNameContract.Presenter presenter) {

    }
}
