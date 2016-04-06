package com.cylan.jiafeigou.activity.doorbell.setwifi;


import android.app.Dialog;
import android.app.Fragment;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.publicApi.DswLog;
import com.cylan.jiafeigou.activity.video.addDevice.SubmitWifiInfoFragment;
import com.cylan.jiafeigou.entity.MyScanResult;
import com.cylan.jiafeigou.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class DoorBellSetWifiInfoFragment extends SubmitWifiInfoFragment {


    public static DoorBellSetWifiInfoFragment newInstance() {
        return new DoorBellSetWifiInfoFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mWifiName.setText(((SetDoorBellWifi) getActivity()).mDate.name);
        getActivity().findViewById(R.id.device_name_layout).setVisibility(View.GONE);
        getActivity().findViewById(R.id.search_delver1).setVisibility(View.GONE);
        getActivity().findViewById(R.id.search_delver2).setVisibility(View.GONE);
        TextView searchDesc = (TextView)  getActivity().findViewById(R.id.search_desc);
        searchDesc.setText("");
        mStartBtn.setText(R.string.DOOR_CONNECT);
    }


    protected void showChooseWifiDialog() {
        if (mChooseWifiDialog == null) {
            mChooseWifiDialog = new Dialog(getActivity(), R.style.func_dialog);
            View content = View.inflate(getActivity(), R.layout.dialog_selectwif, null);
            ImageView cancel = (ImageView) content.findViewById(R.id.cancle);
            cancel.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    mChooseWifiDialog.dismiss();
                    mArrowView.setImageResource(R.drawable.btn_wiflist_unexpand);
                }
            });
            List<MyScanResult> scanlist = new ArrayList<>();
            List<ScanResult> mScanList = null;
            List<ScanResult> mMyScanList = ((SetDoorBellWifi) getActivity()).results;
            if (mMyScanList != null && mMyScanList.size() > 0) {
                mScanList = mMyScanList;
            } else {
                ((SetDoorBellWifi) getActivity()).wm.startScan();
                mScanList = ((SetDoorBellWifi) getActivity()).wm.getScanResults();
            }
            int size = mScanList.size();
            for (int i = 0; i < size; i++) {
                MyScanResult mr = new MyScanResult();
                mr.scanResult = mScanList.get(i);
                if (!scanlist.contains(mr) && !Utils.is5G(mr.scanResult.frequency))
                    scanlist.add(mr);
            }

            ListView mListView = (ListView) content.findViewById(R.id.wifi_list);
            final List<Map<String, String>> list = new ArrayList<Map<String, String>>();
            for (int i = 0; i < scanlist.size(); i++) {
                if (!(scanlist.get(i).scanResult.SSID.replaceAll("\"", "").equals("") || scanlist.get(i).scanResult.SSID.isEmpty() || scanlist.get(i).scanResult.SSID.compareTo("0x") == 0
                        || scanlist.get(i).scanResult.SSID.compareTo("<unknown ssid>") == 0 || scanlist.get(i).scanResult.SSID.equals("NVRAM WARNING: Err = 0x10"))) {
                    Map<String, String> m = new HashMap<>();
                    m.put("Text", scanlist.get(i).scanResult.SSID);
                    list.add(m);
                }
            }
            SimpleAdapter adapter = new SimpleAdapter(getActivity(), list, R.layout.layout_wifilist_item, new String[]{"Text"}, new int[]{R.id.wifi_name});
            mListView.setAdapter(adapter);
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    mWifiName.setText(list.get(position).get("Text"));
                    mChooseWifiDialog.dismiss();
                    mArrowView.setImageResource(R.drawable.btn_wiflist_unexpand);

                }
            });
            content.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    mChooseWifiDialog.dismiss();
                    mArrowView.setImageResource(R.drawable.btn_wiflist_unexpand);

                }
            });
            mChooseWifiDialog.setContentView(content);
            mChooseWifiDialog.setCanceledOnTouchOutside(true);

        }
        try {
            mChooseWifiDialog.show();
        } catch (Exception e) {
            DswLog.ex(e.toString());
        }
    }
}
