package com.cylan.jiafeigou.n.view.bind;


import android.net.wifi.ScanResult;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.anim.FlipAnimation;
import com.cylan.jiafeigou.n.mvp.contract.bind.BindDeviceContract;
import com.cylan.jiafeigou.n.view.BaseTitleFragment;
import com.cylan.jiafeigou.utils.AnimatorUtils;
import com.cylan.utils.ListUtils;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorSet;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BindDoorBellFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BindDoorBellFragment extends BaseTitleFragment implements BindDeviceContract.View {
    public static final String KEY_SUB_FRAGMENT_ID = "sub_key_id";
    public static final String KEY_DEVICE_LIST = "key_device_list";

    @BindView(R.id.fLayout_flip_before)
    FrameLayout fLayoutFlipBefore;
    @BindView(R.id.imgV_wifi_light_flash)
    ImageView imgVWifiLightFlash;
    @BindView(R.id.fLayout_flip_after)
    FrameLayout fLayoutFlipAfter;
    @BindView(R.id.fLayout_flip_layout)
    FrameLayout fLayoutFlipLayout;
    @BindView(R.id.imgV_hand_left)
    ImageView imgVHandLeft;
    @BindView(R.id.imgV_hand_right)
    ImageView imgVHandRight;
    @BindView(R.id.tv_bind_doorbell_tip)
    TextView tvBindDoorbellTip;
    @BindView(R.id.imgV_wifi_light_red_dot_left)
    ImageView imgVWifiLightRedDotLeft;
    @BindView(R.id.imgV_wifi_light_red_dot_right)
    ImageView imgVWifiLightRedDotRight;

    private BindDeviceContract.Presenter presenter;

    public BindDoorBellFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param bundle Parameter 2.
     * @return A new instance of fragment BindCameraFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BindDoorBellFragment newInstance(Bundle bundle) {
        BindDoorBellFragment fragment = new BindDoorBellFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO: inflate a fragment view
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getView() != null) getView().postDelayed(new Runnable() {
            @Override
            public void run() {
                initBeforeFlipAnimation();
            }
        }, 500);
    }

    @Override
    protected int getSubContentViewId() {
        return R.layout.fragment_bind_doorbell;
    }

    AnimatorSet setHandLeft;
    AnimatorSet setHandRight;
    AnimatorSet setRedDotLeft;
    AnimatorSet setRedDotRight;

    private void initBeforeFlipAnimation() {
        setHandLeft = AnimatorUtils.onHand2Left(imgVHandLeft, new AnimatorUtils.SimpleAnimationListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                imgVHandLeft.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                setRedDotLeft.start();
                imgVHandLeft.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        imgVHandLeft.setVisibility(View.INVISIBLE);
                    }
                }, 500);
            }
        });
        setHandRight = AnimatorUtils.onHand2Right(imgVHandRight, new AnimatorUtils.SimpleAnimationListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                imgVHandRight.setVisibility(View.VISIBLE);
                imgVWifiLightRedDotLeft.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                setRedDotRight.start();
                imgVHandRight.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        imgVHandRight.setVisibility(View.INVISIBLE);
                    }
                }, 1000);
            }
        });
        setRedDotLeft = AnimatorUtils.scale(imgVWifiLightRedDotLeft, new AnimatorUtils.SimpleAnimationListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                imgVWifiLightRedDotLeft.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                setHandRight.start();
            }
        });
        setRedDotRight = AnimatorUtils.scale(imgVWifiLightRedDotRight, new AnimatorUtils.SimpleAnimationListener() {

            @Override
            public void onAnimationStart(Animator animator) {
                imgVWifiLightRedDotRight.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                initAnimation();
            }
        });
        setHandLeft.start();
    }

    FlipAnimation flipAnimation;

    private void initAnimation() {
        flipAnimation = new FlipAnimation(fLayoutFlipBefore, fLayoutFlipAfter);
        fLayoutFlipLayout.startAnimation(flipAnimation);
        flipAnimation.setStartOffset(1000);
    }

    WeakReference<BindDeviceListFragment> listFragmentWeakReference;

    private void initDeviceListFragment() {
        if (listFragmentWeakReference == null || listFragmentWeakReference.get() == null)
            listFragmentWeakReference = new WeakReference<>(BindDeviceListFragment.newInstance(getArguments()));
    }

    @Override
    public void onDevicesRsp(List<ScanResult> resultList) {
        final int count = ListUtils.getSize(resultList);
        if (count == 0) {
            Toast.makeText(getContext(), "没发现设备", Toast.LENGTH_SHORT).show();
            return;
        }
        initDeviceListFragment();

        if (listFragmentWeakReference.get().isResumed()) {
            listFragmentWeakReference.get().updateList((ArrayList<ScanResult>) resultList);
            Log.d("simple", "what the hell.....");
            return;
        }
        Bundle bundle = getArguments();
        if (bundle == null) {
            bundle = new Bundle();
        }
        Log.d("simple", "what the hell");
        bundle.putInt(KEY_SUB_FRAGMENT_ID, R.id.fLayout_bind_device_list_fragment_container);
        bundle.putParcelableArrayList(KEY_DEVICE_LIST, (ArrayList<? extends Parcelable>) resultList);
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(android.R.id.content, listFragmentWeakReference.get(), "BindDeviceListFragment")
                .addToBackStack("BindDeviceListFragment")
                .commit();
        cancelAnimation();
    }

    private void cancelAnimation() {
        if (setHandLeft != null && setHandLeft.isRunning())
            setHandLeft.cancel();
        if (setHandRight != null && setHandRight.isRunning())
            setHandRight.cancel();
        if (setRedDotLeft != null && setRedDotLeft.isRunning())
            setRedDotLeft.cancel();
        if (setRedDotRight != null && setRedDotRight.isRunning())
            setRedDotRight.cancel();
    }

    @Override
    public void onNoListError() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Toast.makeText(getContext(), "请你打开定位", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "没有wifi列表", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onNoJFGDevices() {
        Toast.makeText(getContext(), "找不到设备啊", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setPresenter(BindDeviceContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @OnClick(R.id.tv_bind_doorbell_tip)
    public void onClick() {
        Toast.makeText(getContext(), "startScan", Toast.LENGTH_SHORT).show();
        if (presenter != null) presenter.scanDevices();
    }
}
