package com.cylan.jiafeigou.widget.dialog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.view.bind.WiFiListDialogFragment;
import com.cylan.jiafeigou.support.superadapter.SuperAdapter;
import com.cylan.jiafeigou.utils.AnimatorUtils;
import com.cylan.jiafeigou.utils.DensityUtils;
import com.cylan.jiafeigou.utils.ListUtils;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ValueAnimator;

import java.util.List;

import butterknife.ButterKnife;

/**
 * Created by hds on 17-6-7.
 */

public abstract class BaseListDialog<T> extends DialogFragment {

    RecyclerView rvWifiList;
    ViewGroup parentContainer;
    private int currentHeight;
    private int maxHeight = 0;
    private int maxWidth;
    private int minHeight;
    private int itemHeight = 0;
    private static final float MIN_HEIGHT = 0.17F;
    private static final float MAX_HEIGHT = 0.475F;
    private ValueAnimator layoutHeightAnimation;
    protected SuperAdapter<T> baseAdapter;

    private List<T> finalList;

    public static WiFiListDialogFragment newInstance(Bundle bundle) {
        WiFiListDialogFragment fragment = new WiFiListDialogFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.l_style_dialog);
        setCancelable(true);
        maxWidth = (int) (DensityUtils.getScreenWidth() * 0.78f);
        maxHeight = (int) (DensityUtils.getScreenHeight() * MAX_HEIGHT);
        minHeight = (int) Math.max(DensityUtils.getScreenHeight() * MIN_HEIGHT,
                DensityUtils.dip2px(54 * 2));
        itemHeight = (int) (minHeight / 3.5f);
    }

    @Override
    public void onResume() {
        super.onResume();
        initParams();
        getDialog().getWindow().setLayout(maxWidth, currentHeight);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(getViewId(), container, false);
        ButterKnife.bind(this, view);
        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        baseAdapter = getAdapter();
        rvWifiList = getRvWifiList((ViewGroup) view);
        parentContainer = getParentContainer((ViewGroup) view);
        rvWifiList.setAdapter(baseAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        rvWifiList.setLayoutManager(layoutManager);
        if (!ListUtils.isEmpty(finalList)) {
            baseAdapter.addAll(finalList);
        }
    }

    protected abstract ViewGroup getParentContainer(ViewGroup container);

    protected abstract RecyclerView getRvWifiList(ViewGroup container);

    protected abstract int getViewId();

    protected abstract SuperAdapter<T> getAdapter();

    public void updateDataList(List<T> list) {
        this.finalList = list;
        if (isResumed() && ListUtils.getSize(list) > 0) {
            prepareAnimation(list != null ? list.size() : 0);
            baseAdapter.clear();
            baseAdapter.addAll(list);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        cancelAnimation();
    }

    private void initParams() {
        final int count = baseAdapter.getCount();
        final int targetHeight = count * itemHeight;
        currentHeight = Math.min(targetHeight, maxHeight);
        currentHeight = Math.max(currentHeight, minHeight);
    }

    private void cancelAnimation() {
        if (layoutHeightAnimation != null && layoutHeightAnimation.isRunning()) {
            layoutHeightAnimation.cancel();
        }
    }

    private void prepareAnimation(final int count) {
        int targetHeight = count * itemHeight;
        targetHeight = Math.min(maxHeight, targetHeight);
        targetHeight = Math.max(targetHeight, minHeight);
        if (Math.abs(targetHeight - currentHeight) < itemHeight / 2) {
            return;
        }
        cancelAnimation();
        layoutHeightAnimation = ValueAnimator.ofInt(currentHeight, targetHeight);
        layoutHeightAnimation.setDuration(400);
        layoutHeightAnimation.setInterpolator(new DecelerateInterpolator());
        layoutHeightAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final int height = (int) animation.getAnimatedValue();
                currentHeight = height;
                FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) parentContainer.getLayoutParams();
                lp.height = height;
                parentContainer.setLayoutParams(lp);
            }
        });
        layoutHeightAnimation.addListener(new AnimatorUtils.SimpleAnimationListener() {
            @Override
            public void onAnimationEnd(Animator animator) {
                if (getDialog() != null && getDialog().getWindow() != null) {
                    getDialog().getWindow()
                            .setLayout(maxWidth, currentHeight);
                }
            }
        });
        layoutHeightAnimation.start();
    }


    @Override
    public void onStop() {
        super.onStop();
    }


}
