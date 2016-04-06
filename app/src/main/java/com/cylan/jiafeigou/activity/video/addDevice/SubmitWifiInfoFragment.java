package com.cylan.jiafeigou.activity.video.addDevice;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.activity.doorbell.setwifi.SetDoorBellWifi;
import com.cylan.jiafeigou.base.MyApp;
import com.cylan.jiafeigou.engine.ClientConstants;
import com.cylan.jiafeigou.entity.MyScanResult;
import com.cylan.jiafeigou.utils.StringUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.Utils;
import com.cylan.jiafeigou.widget.EditDelText;
import com.cylan.publicApi.Function;
import com.cylan.publicApi.NetUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cylan.log.DswLog;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SubmitWifiInfoFragment.OnSubmitWifiInfoListener} interface
 * to handle interaction events.
 * Use the {@link SubmitWifiInfoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SubmitWifiInfoFragment extends Fragment implements View.OnClickListener {

    private String mBindCid;

    private OnSubmitWifiInfoListener mListener;

    public SubmitWifiInfoFragment() {
        // Required empty public constructor
    }

    protected LinearLayout mWifiNameLayout;
    protected TextView mWifiName;
    protected EditDelText mWifiPwd;
    protected EditDelText mDeviceName;
    protected ImageView mArrowView;
    protected Button mStartBtn;

    protected Dialog mChooseWifiDialog;

    WifiManager wifiManager = null;

    List<ScanResult> mScanList = null;

    public static SubmitWifiInfoFragment newInstance(String cid) {
        SubmitWifiInfoFragment fragment = new SubmitWifiInfoFragment();
        Bundle args = new Bundle();
        args.putString(ClientConstants.PARAM_BIND_CID, cid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mBindCid = getArguments().getString(ClientConstants.PARAM_BIND_CID);
        }
        wifiManager = NetUtils.getWifiManager(getActivity());
        
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_search_edit_pwd, container, false);
        mWifiNameLayout = (LinearLayout) view.findViewById(R.id.layout_wifi_name);
        mWifiNameLayout.setOnClickListener(this);
        mWifiName = (TextView) view.findViewById(R.id.wifi_name);
        mWifiPwd = (EditDelText) view.findViewById(R.id.wifi_pwd);
        mDeviceName = (EditDelText) view.findViewById(R.id.device_name);
        mStartBtn = (Button) view.findViewById(R.id.start);
        mStartBtn.setOnClickListener(this);
        mArrowView = ((ImageView) view.findViewById(R.id.wifi_name_arrow));

        setHintNick();

        if (mScanList == null) {
            List<ScanResult> mMyScanList = null;
            if (getActivity() instanceof SetDoorBellWifi) {
                mMyScanList = ((SetDoorBellWifi) getActivity()).results;
            } else {
                mMyScanList = ((AddVideoActivity) getActivity()).results;
            }
            if (mMyScanList != null && mMyScanList.size() > 0) {
                mScanList = mMyScanList;
            } else {
                if (getActivity() instanceof SetDoorBellWifi) {
                    wifiManager.startScan();
                    mScanList = wifiManager.getScanResults();
                } else {
                    wifiManager.startScan();
                    mScanList = wifiManager.getScanResults();
                }
            }
        }
        setWifiname();
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void setWifiname() {
        if (MyApp.getConnectNet() == null)
            return;
        String ssid = MyApp.getConnectNet().getSSID().replaceAll("\"", "");
        if (ssid.equals("")
                || ssid.isEmpty()
                || ssid.compareTo("0x") == 0
                || ssid.compareTo("<unknown ssid>") == 0
                || ssid.equals("NVRAM WARNING: Err = 0x10")
                ) {
        } else {
            boolean is5G = false;
            ScanResult scan = null;
            if (mScanList != null && mScanList.size() > 0) {
                for (int i = 0, size = mScanList.size(); i < size; i++) {
                    scan = mScanList.get(i);
                    if (scan.SSID.equals(ssid)) {
                        is5G = Utils.is5G(scan.frequency);
                        break;
                    }
                }
            }
            if (!is5G) {
                mWifiName.setText(MyApp.getConnectNet().getSSID().replaceAll("\"", ""));
            }
        }
    }


    protected void setHintNick() {
        mDeviceName.setHint(mBindCid);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnSubmitWifiInfoListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_wifi_name:
                mArrowView.setImageResource(R.drawable.btn_wiflist_expand);
                showChooseWifiDialog();
                break;
            case R.id.start:
                String name = mWifiName.getText().toString();
                String pwd = mWifiPwd.getText().toString().trim();
                String alias = StringUtils.isEmptyOrNull(mDeviceName.getText().toString()) ?
                        (mDeviceName.getHint() == null ? mBindCid : mDeviceName.getHint().toString().trim())
                        : mDeviceName.getText().toString().trim();
                if (StringUtils.isEmptyOrNull(name)) {
                    ToastUtil.showFailToast(getActivity(), getString(R.string.please_choose_wifi));
                    return;
                }
                final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(getActivity().getApplicationContext().INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mWifiPwd.getWindowToken(), 0);
                ScanResult info = null;
                short type = Function.JFG_WIFI_OPEN;
                for (ScanResult sr : mScanList) {
                    if (sr.SSID.equals(name)) {
                        info = sr;
                        break;
                    }
                }
                if (info == null) {
                    ToastUtil.showToast(getActivity(), getString(R.string.wifi_hasnot));
                    return;
                }
                if (info.capabilities.contains("WEP")) {
                    type = Function.JFG_WIFI_WEP;
                } else if (info.capabilities.contains("WPA2")) {
                    type = Function.JFG_WIFI_WPA2;
                } else if (info.capabilities.contains("WPA")) {
                    type = Function.JFG_WIFI_WPA;
                }
                mWifiPwd.clearFocus();
                if (mListener != null) {
                    List<String> list = new ArrayList<>();
                    list.add(info.SSID.replaceAll("\"", ""));
                    list.add(pwd);
                    list.add(String.valueOf(type));
                    list.add(String.valueOf(info.frequency));
                    list.add("");
                    mListener.submitWifiInfo(type, info.SSID.replaceAll("\"", ""), pwd, alias, list);
                }
                break;
        }
    }


    public void setOnSubmitWifiInfoListener(OnSubmitWifiInfoListener listener) {
        this.mListener = listener;
    }

    public interface OnSubmitWifiInfoListener {
        void submitWifiInfo(short type, String ssid, String pwd, String alias, List<String> list);
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
