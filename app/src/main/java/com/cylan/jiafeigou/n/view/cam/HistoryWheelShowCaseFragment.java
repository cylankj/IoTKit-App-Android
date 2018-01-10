package com.cylan.jiafeigou.n.view.cam;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.utils.PreferencesUtils;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by yanzhendong on 2017/7/11.
 */

public class HistoryWheelShowCaseFragment extends Fragment {
    private View anchor;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.view_camera_wheel_case, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        relayout();
    }

    @OnClick(R.id.btn_ok)
    public void ok() {
        PreferencesUtils.putBoolean(JConstant.KEY_SHOW_HISTORY_WHEEL_CASE, false);
        getFragmentManager().popBackStack(getClass().getSimpleName(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    public void setAnchor(View handAnchor) {
        this.anchor = handAnchor;
        relayout();
    }

    private void relayout() {
        View rootView = getView();
        if (rootView != null) {
            rootView.post(new Runnable() {
                @Override
                public void run() {
                    View guideline = rootView.findViewById(R.id.guideline);
                    ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) guideline.getLayoutParams();
                    if (anchor != null) {
                        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
                        int deviceHeight = displayMetrics.heightPixels;
                        int[] position = new int[2];
                        anchor.getLocationInWindow(position);
                        float percent = (position[1] + anchor.getMeasuredHeight() / 2) * 1.0f / deviceHeight;
                        layoutParams.guidePercent = percent;
                        guideline.setLayoutParams(layoutParams);
                    }
                }
            });
        }
    }
}
