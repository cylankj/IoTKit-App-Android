package com.cylan.jiafeigou.activity.video.addDevice;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.cylan.jiafeigou.R;


public class BindResultFragment extends Fragment implements View.OnClickListener {

    private OnCompleteButtonClickListener mListener;

    public static BindResultFragment newInstance() {
        return new BindResultFragment();
    }


    private ImageView mConnView;
    private ImageView mConnStateView;
    private TextView mStateTextView;
    private Button mCompleteBtn;
    protected ImageView mDeviceView;
    private AnimationDrawable mConnAni;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_add_result_layout, container, false);
        mConnView = (ImageView) view.findViewById(R.id.pd_connecting);
        mConnStateView = (ImageView) view.findViewById(R.id.connecting_state);
        mStateTextView = (TextView) view.findViewById(R.id.tv_connecting_state);
        mCompleteBtn = (Button) view.findViewById(R.id.btn_complete);
        mCompleteBtn.setOnClickListener(this);
        mDeviceView = (ImageView) view.findViewById(R.id.ico_addvideo_device);
        mConnAni = (AnimationDrawable) (mConnView.getDrawable());
        mConnAni.start();
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnCompleteButtonClickListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mConnAni.stop();
        mConnAni = null;
        mListener = null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_complete:
                if (mListener != null)
                    mListener.OnCompleteButtonClick();
                break;
        }
    }

    public void setOnCompleteButtonClickListener(OnCompleteButtonClickListener listener) {
        mListener = listener;
    }

    public interface OnCompleteButtonClickListener {
        void OnCompleteButtonClick();
    }

    public void setStateTextViewText(int res) {
        if (mStateTextView != null)
            mStateTextView.setText(res);
    }

    public void setStateTextViewText(String res) {
        if (mStateTextView != null)
            mStateTextView.setText(res);
    }

    public void setCompleteBtnVisiblty(int state) {
        if (mCompleteBtn != null)
            mCompleteBtn.setVisibility(state);
    }

    public void stopAnimation() {
        if (mConnAni != null && mConnAni.isRunning())
            mConnAni.stop();
    }

    public void setConnViewResourse(int res) {
        if (mConnView != null)
            mConnView.setImageResource(res);
    }


    public void setConnViewVisiblty(int res) {
        if (mConnView != null)
            mConnView.setVisibility(res);
    }

    public void setConnStateViewResours(int res) {
        if (mConnStateView != null)
            mConnStateView.setImageResource(res);
    }

    public void setCompleteBtnText(int res) {
        if (mCompleteBtn != null)
            mCompleteBtn.setText(res);
    }
}
