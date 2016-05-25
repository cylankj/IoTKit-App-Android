package com.cylan.jiafeigou.n.view.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.utils.ActivityUtils;

/**
 * Created by hunt on 16-5-25.
 */

public class AccountInfoFragment extends Fragment {
    public static Fragment getInstance() {
        return new AccountInfoFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_fragment_account_info, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        view.findViewById(R.id.rLayout_account_alias_item).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtils.addFragmentToActivity(getChildFragmentManager(),
                        FragmentAccountMyAccount.getInstance(), R.id.rLayout_account_info, 0);
            }
        });
    }

    public static class FragmentAccountMyAccount extends Fragment {
        public static Fragment getInstance() {
            return new FragmentAccountMyAccount();
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.layout_fragment_account_info, container, false);
            view.setBackgroundColor(getResources().getColor(R.color.back_color));
            return view;
        }
    }
}
