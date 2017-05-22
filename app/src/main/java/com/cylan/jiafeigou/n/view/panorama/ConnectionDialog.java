package com.cylan.jiafeigou.n.view.panorama;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.view.activity.BindPanoramaCamActivity;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.widget.dialog.BaseDialog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by yanzhendong on 2017/3/13.
 */

public class ConnectionDialog extends BaseDialog {
    @BindView(R.id.dialog_connection_close)
    ImageView close;
    @BindView(R.id.dialog_connection_way_family)
    TextView connectionWayFamily;
    @BindView(R.id.dialog_connection_way_out_door)
    TextView connectionWayOutdoor;
    private String uuid;

    public static ConnectionDialog newInstance(String uuid) {
        ConnectionDialog dialog = new ConnectionDialog();
        Bundle bundle = new Bundle();
        bundle.putString(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
        dialog.setArguments(bundle);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_panorama_connection_way, container, false);
        ButterKnife.bind(this, view);
        this.uuid = getArguments().getString(JConstant.KEY_DEVICE_ITEM_UUID);
        return view;
    }

    @OnClick(R.id.dialog_connection_close)
    public void close() {
        dismiss();
    }

    @OnClick(R.id.dialog_connection_way_family_hand)
    public void showFamilyWayConfigure() {
        AppLogger.d("将配置家居模式");
        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
        if (device == null) {
            dismiss();
            return;
        }
        Intent intent = new Intent(getContext(), BindPanoramaCamActivity.class);
        intent.putExtra("PanoramaConfigure", "Family");
        intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
        startActivity(intent);
        dismiss();
    }

    @OnClick(R.id.dialog_connection_way_out_door_hand)
    public void showOutDoorWayConfigure() {
        AppLogger.d("将配置户外模式");
        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
        if (device == null) {
            dismiss();
            return;
        }
        Intent intent = new Intent(getContext(), BindPanoramaCamActivity.class);
        intent.putExtra("PanoramaConfigure", "OutDoor");
        intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
        startActivity(intent);
        dismiss();
    }

}
