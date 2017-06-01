package com.cylan.jiafeigou.support.share;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cylan.jiafeigou.databinding.DialogShareBinding;

/**
 * Created by yanzhendong on 2017/6/1.
 */

public class ShareDialogFragment extends DialogFragment {

    private DialogShareBinding shareBinding;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        shareBinding = DialogShareBinding.inflate(inflater);
        return shareBinding.getRoot();
    }
}
