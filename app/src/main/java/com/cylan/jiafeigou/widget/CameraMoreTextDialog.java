package com.cylan.jiafeigou.widget;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.utils.DensityUtils;
import com.cylan.jiafeigou.widget.dialog.BaseDialog;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by yanzhendong on 2017/12/9.
 */

public class CameraMoreTextDialog extends BaseDialog {

    @BindView(R.id.text_content)
    TextView textContent;

    private int maxWidth;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.l_style_dialog);
        setCancelable(false);
        maxWidth = (int) (DensityUtils.getScreenWidth() * 0.78f);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.layout_camera_more_text, container, false);
        ButterKnife.bind(this, inflate);
        return inflate;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String text = getArguments().getString("text");
        textContent.setText(text);
    }

    @Override
    public void onResume() {
        super.onResume();
        View view = getView();
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = Math.min(layoutParams.height, getResources().getDimensionPixelOffset(R.dimen.y232));
        view.setLayoutParams(layoutParams);
        getDialog().getWindow().setLayout(maxWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    public static CameraMoreTextDialog newInstance(String text) {
        CameraMoreTextDialog dialog = new CameraMoreTextDialog();
        Bundle bundle = new Bundle();
        bundle.putString("text", text);
        dialog.setArguments(bundle);
        return dialog;
    }
}
