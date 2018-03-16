package com.cylan.jiafeigou.n.view.cam;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnticipateOvershootInterpolator;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.widget.pick.AbstractWheel;
import com.cylan.jiafeigou.widget.pick.OnWheelChangedListener;
import com.cylan.jiafeigou.widget.pick.WheelVerticalView;
import com.cylan.jiafeigou.widget.pick.adapters.AbstractWheelTextAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by yanzhendong on 2018/3/16.
 */

public class FaceDetectionSizeChooseFragment extends DialogFragment implements OnWheelChangedListener {

    @BindView(R.id.detection_size_choose)
    WheelVerticalView detectionSizeChoose;
    private int index = 63;
    private ChooseCallback chooseCallback;

    public static FaceDetectionSizeChooseFragment newInstance(String uuid, int size) {
        FaceDetectionSizeChooseFragment fragment = new FaceDetectionSizeChooseFragment();
        Bundle arguments = new Bundle();
        arguments.putString(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
        arguments.putInt("size", size);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.l_style_dialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_face_detection_size, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        initDetectionSizeChoose();
    }

    private void initDetectionSizeChoose() {
        Bundle arguments = getArguments();
        if (arguments != null) {
            int size = arguments.getInt("size", 120);
            index = Math.max(size / 10 - 1, 0);
        }
        AbstractWheelTextAdapter adapter = new AbstractWheelTextAdapter(getContext()) {
            @Override
            public int getItemsCount() {
                return 63;
            }

            @Override
            protected CharSequence getItemText(int index) {
                return String.format("%spx", index * 10 + 10);
            }
        };
        adapter.setTextColor(getContext().getResources().getColor(R.color.color_4b9fd5));
        adapter.setItemResource(R.layout.item_text_view);
        detectionSizeChoose.setViewAdapter(adapter);
        detectionSizeChoose.setCurrentItem(index);
        detectionSizeChoose.addChangingListener(this);
        detectionSizeChoose.setInterpolator(new AnticipateOvershootInterpolator());
        detectionSizeChoose.setVisibleItems(3);
        detectionSizeChoose.setViewAdapter(adapter);
    }

    @OnClick(R.id.tv_dialog_btn_right)
    void onSureClicked() {
        if (chooseCallback != null) {
            chooseCallback.onChoose((index + 1) * 10);
        }
        dismiss();
    }

    @OnClick(R.id.tv_dialog_btn_left)
    void onCancelClicked() {
        dismiss();
    }

    public void setChooseCallback(ChooseCallback chooseCallback) {
        this.chooseCallback = chooseCallback;
    }

    public interface ChooseCallback {

        void onChoose(int size);
    }

    @Override
    public void onChanged(AbstractWheel wheel, int oldValue, int newValue) {
        index = newValue;
    }
}
