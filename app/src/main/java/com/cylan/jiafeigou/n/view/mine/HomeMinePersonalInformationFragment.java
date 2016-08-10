package com.cylan.jiafeigou.n.view.mine;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.utils.ToastUtil;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 创建者     谢坤
 * 创建时间   2016/8/9 10:02
 * 描述	      ${TODO}
 * <p/>
 * 更新者     $Author$
 * 更新时间   $Date$
 * 更新描述   ${TODO}
 */
public class HomeMinePersonalInformationFragment extends Fragment {

    public static HomeMinePersonalInformationFragment newInstance(Bundle bundle) {
        HomeMinePersonalInformationFragment fragment = new HomeMinePersonalInformationFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_mine_personal_information,container,false);
        ButterKnife.bind(this,view);
        return view;
    }

    @OnClick({R.id.iv_home_mine_personal_back,R.id.btn_home_mine_personal_information})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.iv_home_mine_personal_back:
                getFragmentManager().popBackStack();
              break;
            case R.id.btn_home_mine_personal_information:
                ToastUtil.showToast(getActivity(),"1111");
                break;
        }
    }

}
