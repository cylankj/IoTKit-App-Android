package com.cylan.jiafeigou.n.view.cloud;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.cloud.CloudVideoChatConettionContract;

import butterknife.ButterKnife;

/**
 * 作者：zsl
 * 创建时间：2016/9/26
 * 描述：
 */
public class CloudVideoChatConnetionFragment extends Fragment implements CloudVideoChatConettionContract.View {

    public static CloudVideoChatConnetionFragment newInstance(Bundle bundle){
        CloudVideoChatConnetionFragment fragment = new CloudVideoChatConnetionFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cloud_live_videochat_connetion,container,false);
        ButterKnife.bind(this,view);
        return view;
    }

    @Override
    public void setPresenter(CloudVideoChatConettionContract.Presenter presenter) {

    }
}
