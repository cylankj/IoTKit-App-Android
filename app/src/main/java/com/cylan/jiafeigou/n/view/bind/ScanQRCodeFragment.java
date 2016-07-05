package com.cylan.jiafeigou.n.view.bind;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.view.BaseTitleFragment;

/**
 */
public class ScanQRCodeFragment extends BaseTitleFragment {

    public ScanQRCodeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param bundle Parameter 2.
     * @return A new instance of fragment ScanQRCodeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ScanQRCodeFragment newInstance(Bundle bundle) {
        ScanQRCodeFragment fragment = new ScanQRCodeFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected View getSubContentView(LayoutInflater inflater) {
        return inflater.inflate(R.layout.fragment_scan_qrcode, null, false);
    }


}
