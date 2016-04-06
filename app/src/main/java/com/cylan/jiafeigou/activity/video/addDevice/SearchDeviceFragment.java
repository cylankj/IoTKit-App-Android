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


public class SearchDeviceFragment extends Fragment {

    private OnSearchDeviceListener mListener;

    protected ImageView imageView;
    protected AnimationDrawable mAni;
    protected Button mNextBtn;
    protected TextView mSearchPromptView;

    public static SearchDeviceFragment newInstance() {
        return new SearchDeviceFragment();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_pb, container, false);
        imageView = (ImageView) view.findViewById(R.id.seach_pb);
        mNextBtn = (Button) view.findViewById(R.id.btn_next);
        mSearchPromptView = (TextView) view.findViewById(R.id.search_prompt);
        mNextBtn.setText(getString(R.string.BLINKING));
        mNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null)
                    mListener.OnSearchDevice();
            }
        });
        mAni = (AnimationDrawable) (imageView.getDrawable());
        mAni.start();
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
            mListener = (OnSearchDeviceListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        mAni.stop();
        mAni = null;

    }


    public void setOnSearchDeviceListener(OnSearchDeviceListener listener) {
        this.mListener = listener;
    }

    public interface OnSearchDeviceListener {
        void OnSearchDevice();
    }

}
