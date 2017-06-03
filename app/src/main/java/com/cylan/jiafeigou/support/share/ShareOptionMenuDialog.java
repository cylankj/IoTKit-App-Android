package com.cylan.jiafeigou.support.share;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cylan.jiafeigou.databinding.DialogShareOptionMenuBinding;


/**
 * Created by yanzhendong on 2017/6/1.
 */

public class ShareOptionMenuDialog extends DialogFragment {
    private DialogShareOptionMenuBinding shareBinding;
    private ShareOptionClickListener listener;
    private DialogInterface.OnCancelListener cancelListener;

    public static ShareOptionMenuDialog newInstance(DialogInterface.OnCancelListener cancelListener) {
        ShareOptionMenuDialog dialog = new ShareOptionMenuDialog();
        dialog.cancelListener = cancelListener;
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
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ShareOptionClickListener) {
            this.listener = (ShareOptionClickListener) context;
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

    @Override
    public Dialog getDialog() {
        return super.getDialog();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        if (cancelListener != null) {
            cancelListener.onCancel(dialog);
        }
    }

    public interface ShareOptionClickListener {
        void onShareOptionClick(int shareItemType);
    }
}
