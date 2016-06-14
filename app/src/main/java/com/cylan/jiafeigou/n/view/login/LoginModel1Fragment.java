package com.cylan.jiafeigou.n.view.login;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cylan.jiafeigou.R;

/**
 *
 */
public class LoginModel1Fragment extends Fragment {

    public LoginModel1Fragment() {
        // Required empty public constructor
    }


    public static LoginModel1Fragment newInstance(Bundle bundle) {
        LoginModel1Fragment fragment = new LoginModel1Fragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
          ///
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login_model1, container, false);
    }


}
