package com.cylan.jiafeigou.n.view.login_ex;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.impl.LoginPresenterImpl;
import com.cylan.jiafeigou.n.view.login.LoginFragment;

import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LoginContainerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoginContainerFragment extends Fragment {

    public static final String KEY_ACTIVITY_FRAGMENT_CONTAINER_ID = "activityFragmentContainerId";


    public LoginContainerFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment LoginHolderFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LoginContainerFragment newInstance(Bundle bundle) {
        LoginContainerFragment fragment = new LoginContainerFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login_model, container, false);
        ButterKnife.bind(this, view);
        LoginFragment fragment = LoginFragment.newInstance(getArguments());
        getChildFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_up_in, R.anim.slide_down_out)
                .add(R.id.fLayout_login_container, fragment)
                .commit();
        new LoginPresenterImpl(fragment);
        return view;
    }

    public interface UpdateTitleListener {
        void update(View.OnClickListener listenerX, CharSequence charCenter, CharSequence charRight, View.OnClickListener listenerRight);
    }
}
