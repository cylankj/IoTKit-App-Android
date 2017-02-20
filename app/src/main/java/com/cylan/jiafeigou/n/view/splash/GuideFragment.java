package com.cylan.jiafeigou.n.view.splash;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.widget.GreatDragView;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by hunt on 16-5-14.
 */
public class GuideFragment extends Fragment implements GreatDragView.ViewDisappearListener {

    @BindView(R.id.v_great_drag)
    GreatDragView vGreatDrag;
//    @BindView(R.id.v_guide_indicator)
//    GuideIndicatorLayout vGuideIndicator;

    public static GuideFragment newInstance() {
        return new GuideFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_welcome_guide_view, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        vGreatDrag.setViewDisappearListener(this);
    }

    @Override
    public void onViewDisappear(View view, int index) {
//        vGuideIndicator.setFocusedIndex(index);
        Log.d("vGuideIndicator", "vGuideIndicator: " + index);
        if (index == 3) {
            //进入登陆页 login page//这里要用replace
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(android.R.id.content, BeforeLoginFragment.newInstance(null))
                    .commitAllowingStateLoss();
        }
    }
}



