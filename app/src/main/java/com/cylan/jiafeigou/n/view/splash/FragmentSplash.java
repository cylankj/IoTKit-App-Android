package com.cylan.jiafeigou.n.view.splash;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cylan.jiafeigou.R;

import butterknife.ButterKnife;

/**
 * Created by hunt on 16-5-14.
 */
public class FragmentSplash extends Fragment {

    public static FragmentSplash newInstance(final int index) {
        FragmentSplash fragment = new FragmentSplash();
        Bundle bundle = new Bundle();
        bundle.putInt("key", index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_splash_layout, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle bundle = getArguments();
        final int index = bundle == null ? 0 : bundle.getInt("key");
        TextView textView = (TextView) view.findViewById(R.id.tv_splash);
        textView.setText(index + "");
        textView.setTextSize(50);
    }

}



