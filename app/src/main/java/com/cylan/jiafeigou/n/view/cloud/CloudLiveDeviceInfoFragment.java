package com.cylan.jiafeigou.n.view.cloud;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.cloud.CloudLiveDeviceInfoContract;

import butterknife.ButterKnife;

/**
 * 作者：zsl
 * 创建时间：2016/9/26
 * 描述：
 */
public class CloudLiveDeviceInfoFragment extends Fragment implements CloudLiveDeviceInfoContract.View {

    public static CloudLiveDeviceInfoFragment newInstance(Bundle bundle){
        CloudLiveDeviceInfoFragment fragment = new CloudLiveDeviceInfoFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cloud_live_device_info,container,false);
        ButterKnife.bind(this,view);
        return view;
    }

    @Override
    public void setPresenter(CloudLiveDeviceInfoContract.Presenter presenter) {

    }
}
