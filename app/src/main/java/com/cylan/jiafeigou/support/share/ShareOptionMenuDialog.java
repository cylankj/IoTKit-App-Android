package com.cylan.jiafeigou.support.share;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.databinding.DialogShareOptionMenuBinding;


/**
 * Created by yanzhendong on 2017/6/1.
 */

public class ShareOptionMenuDialog extends DialogFragment {
    private DialogShareOptionMenuBinding shareBinding;
    private ShareOptionClickListener listener;
    private DialogInterface.OnCancelListener dismiss;

    public static ShareOptionMenuDialog newInstance(ShareOptionClickListener listener, DialogInterface.OnCancelListener dismiss) {
        ShareOptionMenuDialog dialog = new ShareOptionMenuDialog();
        dialog.listener = listener;
        dialog.dismiss = dismiss;
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        shareBinding = DialogShareOptionMenuBinding.inflate(inflater);
        if (listener != null) {
            shareBinding.setOptionListener(listener);
        }
        return shareBinding.getRoot();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.l_style_dialog);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (dismiss != null) {
            dismiss.onCancel(dialog);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (shareBinding != null) {
            shareBinding.setOptionListener(null);
            shareBinding.unbind();
        }
        listener = null;
    }


    public interface ShareOptionClickListener {
        void onShareOptionClick(int shareItemType);
    }
}
