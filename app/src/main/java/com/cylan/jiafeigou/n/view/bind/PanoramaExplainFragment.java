package com.cylan.jiafeigou.n.view.bind;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.utils.ViewUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by yanzhendong on 2017/3/7.
 */

public class PanoramaExplainFragment extends Fragment {

    @BindView(R.id.fragment_panorama_explain_close)
    View close;

    public static PanoramaExplainFragment newInstance(Bundle bundle) {
        PanoramaExplainFragment fragment = new PanoramaExplainFragment();
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_panorama_explain, container, false);


        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
                close();
            }
            return true;
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        ViewUtils.setViewMarginStatusBar(close);
    }

    @OnClick(R.id.fragment_panorama_explain_close)
    public void close() {
        getActivity().getSupportFragmentManager().popBackStack();
    }
}
