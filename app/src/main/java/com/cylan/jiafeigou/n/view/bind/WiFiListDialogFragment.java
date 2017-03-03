package com.cylan.jiafeigou.n.view.bind;

import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.model.BeanWifiList;
import com.cylan.jiafeigou.n.view.adapter.PickWifiAdapter;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.AnimatorUtils;
import com.cylan.jiafeigou.utils.DensityUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ValueAnimator;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by cylan-hunt on 16-7-7.
 */
public class WiFiListDialogFragment extends DialogFragment implements
        PickWifiAdapter.ItemClickListener {

    @BindView(R.id.rv_wifi_list)
    RecyclerView rvWifiList;
    @BindView(R.id.cLayout_dialog_wifi_list)
    CardView lLayoutDialogWifiList;
    private int currentHeight;
    private int maxHeight = 0;
    private int maxWidth;
    private int minHeight;
    private int itemHeight = 0;
    private static final float MIN_HEIGHT = 0.17F;
    private static final float MAX_HEIGHT = 0.475F;
    private ValueAnimator layoutHeightAnimation;
    private PickWifiAdapter adapter;
    private List<BeanWifiList> resultList;

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
        getDialog().getWindow()
                .setLayout(maxWidth, currentHeight);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_fragment_dialog_wifi_list, container, false);
        ButterKnife.bind(this, view);
        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        adapter = new PickWifiAdapter(getContext());
        adapter.setOnItemClickListener(this);
        rvWifiList.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        rvWifiList.setLayoutManager(layoutManager);
        if (resultList != null)
            adapter.addAll(resultList);
    }

    @Override
    public void onPause() {
        super.onPause();
        cancelAnimation();
    }

    private void initParams() {
        final int count = resultList != null ? resultList.size() : 0;
        final int targetHeight = count * itemHeight;
        currentHeight = Math.min(targetHeight, maxHeight);
        currentHeight = Math.max(currentHeight, minHeight);
    }

    private void cancelAnimation() {
        if (layoutHeightAnimation != null && layoutHeightAnimation.isRunning())
            layoutHeightAnimation.cancel();
    }

    private void prepareAnimation(final int count) {
        int targetHeight = count * itemHeight;
        targetHeight = Math.min(maxHeight, targetHeight);
        targetHeight = Math.max(targetHeight, minHeight);
        if (Math.abs(targetHeight - currentHeight) < itemHeight / 2)
            return;
        cancelAnimation();
        layoutHeightAnimation = ValueAnimator.ofInt(currentHeight, targetHeight);
        layoutHeightAnimation.setDuration(400);
        layoutHeightAnimation.setInterpolator(new DecelerateInterpolator());
        layoutHeightAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final int height = (int) animation.getAnimatedValue();
                currentHeight = height;
                FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) lLayoutDialogWifiList.getLayoutParams();
                lp.height = height;
                lLayoutDialogWifiList.setLayoutParams(lp);
            }
        });
        layoutHeightAnimation.addListener(new AnimatorUtils.SimpleAnimationListener() {
            @Override
            public void onAnimationEnd(Animator animator) {
                if (getDialog() != null && getDialog().getWindow() != null)
                    getDialog().getWindow()
                            .setLayout(maxWidth, currentHeight);
            }
        });
        layoutHeightAnimation.start();
    }

    public void updateList(List<ScanResult> list, Object checkedResult) {
        this.resultList = convertList(list, checkedResult);
        if (isResumed()) {
            prepareAnimation(list != null ? list.size() : 0);
            adapter.clear();
            adapter.addAll(resultList);
        }
    }

    /**
     * 根据当前选中的 ScanResult,在列表中高亮
     *
     * @param list
     * @param checkedResult
     * @return
     */
    private List<BeanWifiList> convertList(List<ScanResult> list, Object checkedResult) {
        List<BeanWifiList> rList = new ArrayList<>();
        boolean notNull = checkedResult != null && (checkedResult instanceof BeanWifiList);
        final ScanResult result = notNull ? ((BeanWifiList) checkedResult).result : null;
        int cacheSecurity = notNull ? NetUtils.getSecurity(result) : -1;
        final String ssid = result == null ? "" : result.SSID;
        final int count = list == null ? 0 : list.size();
        for (int i = 0; i < count; i++) {
            BeanWifiList bean = new BeanWifiList();
            bean.result = list.get(i);
            bean.checked = NetUtils.getSecurity(list.get(i)) == cacheSecurity
                    && TextUtils.equals(list.get(i).SSID, ssid);
            rList.add(bean);
        }
        return rList;
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onClick(View v) {
        final int position = ViewUtils.getParentAdapterPosition(rvWifiList, v, R.id.lLayout_device_item);
        if (position < 0 || position > adapter.getCount()) {
            AppLogger.i("position is invalid: " + position);
            return;
        }
        ScanResult result = adapter.getItem(position).result;
        adapter.setCheckedResult(result);
        if (clickCallBack != null)
            clickCallBack.onDismiss(result);
        dismiss();
    }

    public void setClickCallBack(ClickCallBack clickCallBack) {
        this.clickCallBack = clickCallBack;
    }

    private ClickCallBack clickCallBack;

    public interface ClickCallBack {
        void onDismiss(ScanResult scanResult);
    }
}
