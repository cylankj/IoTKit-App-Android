package com.cylan.jiafeigou.n.view.bell;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.widget.dialog.BaseDialog;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by cylan-hunt on 16-8-8.
 * 门铃低电量提示框
 */
public class LBatteryWarnDialog extends BaseDialog {
    @BindView(R.id.tv_low_battery_content)
    TextView tvLowBatteryContent;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SetSensitivityDialogFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LBatteryWarnDialog newInstance(Bundle bundle) {
        LBatteryWarnDialog fragment = new LBatteryWarnDialog();
        fragment.setArguments(bundle);
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
        View view = inflater.inflate(R.layout.layout_dialog_low_battery, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        highLight();
    }

    private void highLight() {
        final String needHighLight = getString(R.string.item_green);
        final int len = needHighLight.length();
        final String tmpContent = getString(R.string.s_bell_low_battery_content);
        String fullContent = String.format(Locale.getDefault(), tmpContent, needHighLight);
        final int startIndex = fullContent.indexOf(needHighLight);
        if (startIndex < 0 || startIndex + len > fullContent.length()) {
            AppLogger.d("err");
            return;
        }
        fullContent = fullContent.replaceAll(needHighLight, "<font color='green'>" + needHighLight + "</font>");
        tvLowBatteryContent.setText(Html.fromHtml(fullContent));
    }

    @Override
    protected int getCustomHeight() {
        return WindowManager.LayoutParams.WRAP_CONTENT;
    }

    @OnClick(R.id.tv_low_battery_dismiss)
    public void onClick() {
        dismiss();
    }
}
