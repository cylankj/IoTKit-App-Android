package com.cylan.jiafeigou.n.view.cam;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.wrapper.BaseFragment;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ViewUtils;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by yanzhendong on 2018/1/26.
 */

public class RegisterFaceSuccessFragment extends BaseFragment {

    @BindView(R.id.root_view)
    View rootView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register_face_success, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewUtils.setViewPaddingStatusBar(rootView);
    }

    @OnClick(R.id.finish)
    void finish() {
        AppLogger.w("finish");
        getActivity().finish();
    }

    @OnClick(R.id.continue_register)
    void registerContinue() {
        AppLogger.w("registerContinue");
        if (callBack != null) {
            callBack.callBack(null);
        }
        getFragmentManager().popBackStack();
    }

    @Override
    public boolean useDaggerInject() {
        return false;
    }

    public static RegisterFaceSuccessFragment newInstance(String uuid) {
        RegisterFaceSuccessFragment fragment = new RegisterFaceSuccessFragment();
        Bundle argument = new Bundle();
        argument.putString(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
        fragment.setArguments(argument);
        return fragment;
    }

    @Override
    public boolean beforeInterceptBackEvent() {
        finish();
        return true;
    }
}
