package com.cylan.jiafeigou.n.view.panorama;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.widget.dialog.BaseDialog;

import butterknife.ButterKnife;

/**
 * Created by yanzhendong on 2017/3/13.
 */

public class ConnectionDialog extends BaseDialog {

    public static ConnectionDialog newInstance() {
        ConnectionDialog dialog = new ConnectionDialog();
        Bundle bundle = new Bundle();
        dialog.setArguments(bundle);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_panorama_connection_way, container, false);
        ButterKnife.bind(this, view);
        return view;
    }


}
