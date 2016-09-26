package com.cylan.jiafeigou.n.view.cloud;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.cloud.CloudLiveSettingContract;

import butterknife.ButterKnife;

/**
 * 作者：zsl
 * 创建时间：2016/9/26
 * 描述：
 */
public class CloudLiveSettingFragment extends Fragment implements CloudLiveSettingContract.View {

    public static CloudLiveSettingFragment newInstance(Bundle bundle){
        CloudLiveSettingFragment fragment = new CloudLiveSettingFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cloud_live_setting,container,false);
        ButterKnife.bind(this,view);
        return view;
    }

    @Override
    public void setPresenter(CloudLiveSettingContract.Presenter presenter) {

    }
}
