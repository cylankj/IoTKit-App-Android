package com.cylan.jiafeigou.widget;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.utils.DensityUtils;
import com.cylan.jiafeigou.widget.dialog.BaseDialog;

import butterknife.ButterKnife;

/**
 * Created by cylan-hunt on 16-10-27.
 */

public class LoadingDialog extends BaseDialog {

    private static final String KEY_CONTENT = "key_content";
    private static final String KEY_TOUCH = "key_touch";

    public static void showLoading(FragmentManager fragmentManager, String content, boolean dismissTouchOutside) {
        Fragment dialog = fragmentManager.findFragmentByTag("LoadingDialog");
        if (dialog != null && dialog.isVisible())
            ((LoadingDialog) dialog).dismiss();
        LoadingDialog loadingDialog = new LoadingDialog();
        Bundle bundle = new Bundle();
        bundle.putString(KEY_CONTENT, content);
        bundle.putBoolean(KEY_TOUCH, dismissTouchOutside);
        loadingDialog.setArguments(bundle);
        loadingDialog.show(fragmentManager, "LoadingDialog");
    }

    public static boolean isShowing(FragmentManager fragmentManager) {
        Fragment dialog = fragmentManager.findFragmentByTag("LoadingDialog");
        return dialog != null && dialog.isVisible();
    }

    public static void showLoading(FragmentManager fragmentManager, String content) {
        showLoading(fragmentManager, content, false);
    }

    public static void showLoading(FragmentManager fragmentManager) {
        showLoading(fragmentManager, "", false);
    }

    public static void dismissLoading(FragmentManager fragmentManager) {
        Fragment dialog = fragmentManager.findFragmentByTag("LoadingDialog");
        if (dialog != null && dialog.isVisible()) {
            ((LoadingDialog) dialog).dismiss();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.layout_loading_normal, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        TextView textView = (TextView) view.findViewById(R.id.tv_loading_content);
        textView.setText(bundle.getString(KEY_CONTENT, ""));
        getDialog().setCanceledOnTouchOutside(bundle.getBoolean(KEY_TOUCH, false));
    }

    @Override
    protected int getCustomHeight() {
        return WindowManager.LayoutParams.WRAP_CONTENT;
    }

    @Override
    protected int getCustomWidth() {
        return DensityUtils.getScreenWidth() / 3;
    }

}
