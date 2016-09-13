package com.cylan.jiafeigou.n.view.cam;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ViewUtils;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SafeProtectionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SafeProtectionFragment extends Fragment {

    @BindView(R.id.imgV_top_bar_center)
    TextView imgVTopBarCenter;
    @BindView(R.id.fLayout_top_bar_container)
    FrameLayout fLayoutTopBarContainer;
    @BindView(R.id.tv_protection_sensitivity)
    TextView tvProtectionSensitivity;
    @BindView(R.id.tv_protection_notification)
    TextView tvProtectionNotification;
    @BindView(R.id.tv_protection_start_time)
    TextView tvProtectionStartTime;
    @BindView(R.id.tv_protection_end_time)
    TextView tvProtectionEndTime;
    @BindView(R.id.tv_protection_repeat_period)
    TextView tvProtectionRepeatPeriod;


    WeakReference<SetSensitivityDialogFragment> setSensitivityFragmentWeakReference;
    WeakReference<WarnEffectFragment> warnEffectFragmentWeakReference;
    WeakReference<CapturePeriodDialogFragment> capturePeriodDialogFragmentWeakReference;

    public SafeProtectionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param args Parameter 2.
     * @return A new instance of fragment SafeProtectionFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SafeProtectionFragment newInstance(Bundle args) {
        SafeProtectionFragment fragment = new SafeProtectionFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_safe_protection, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        imgVTopBarCenter.setText("安全防护");
        ViewUtils.setViewPaddingStatusBar(fLayoutTopBarContainer);
    }


    @OnClick({R.id.imgV_top_bar_center,
            R.id.fLayout_protection_sensitivity,
            R.id.fLayout_protection_warn_effect,
            R.id.fLayout_protection_start_time,
            R.id.fLayout_protection_end_time,
            R.id.fLayout_protection_repeat_period})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imgV_top_bar_center:
                getActivity().getSupportFragmentManager().popBackStack();
                break;
            case R.id.fLayout_protection_sensitivity:
                initSensitivityFragment();
                showFragment(setSensitivityFragmentWeakReference.get());
                break;
            case R.id.fLayout_protection_warn_effect:
                initWarnEffectFragment();
                loadFragment(android.R.id.content, warnEffectFragmentWeakReference.get());
                break;
            case R.id.fLayout_protection_start_time:
                break;
            case R.id.fLayout_protection_end_time:
                break;
            case R.id.fLayout_protection_repeat_period:
                initCapturePeriodFragment();
                showFragment(capturePeriodDialogFragmentWeakReference.get());
                break;
        }
    }

    private void initSensitivityFragment() {
        if (setSensitivityFragmentWeakReference == null
                || setSensitivityFragmentWeakReference.get() == null) {
            setSensitivityFragmentWeakReference = new WeakReference<>(SetSensitivityDialogFragment.newInstance(new Bundle()));
        }
    }

    private void initWarnEffectFragment() {
        if (warnEffectFragmentWeakReference == null
                || warnEffectFragmentWeakReference.get() == null) {
            warnEffectFragmentWeakReference = new WeakReference<>(WarnEffectFragment.newInstance(new Bundle()));
        }
    }

    private void initCapturePeriodFragment() {
        if (capturePeriodDialogFragmentWeakReference == null
                || capturePeriodDialogFragmentWeakReference.get() == null) {
            capturePeriodDialogFragmentWeakReference = new WeakReference<>(CapturePeriodDialogFragment.newInstance(new Bundle()));
        }
    }

    private void showFragment(DialogFragment fragment) {
        if (fragment != null
                && !fragment.isResumed()
                && getActivity() != null)
            fragment.show(getActivity().getSupportFragmentManager(), fragment.getClass().getSimpleName());
    }

    /**
     * 用来加载fragment的方法。
     */
    private void loadFragment(int id, Fragment fragment) {
        Fragment f = getActivity().getSupportFragmentManager().findFragmentByTag(fragment.getClass().getSimpleName());
        if (f != null) {
            AppLogger.d("fragment is already added: " + f.getClass().getSimpleName());
            return;
        }
        getActivity().getSupportFragmentManager().beginTransaction()
                //如果需要动画，可以把动画添加进来
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(id, fragment, fragment.getClass().getSimpleName())
                .addToBackStack(fragment.getClass().getSimpleName())
                .commit();
    }
}
