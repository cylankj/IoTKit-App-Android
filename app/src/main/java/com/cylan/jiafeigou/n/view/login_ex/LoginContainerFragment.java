package com.cylan.jiafeigou.n.view.login_ex;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.impl.LoginPresenterImpl;
import com.cylan.jiafeigou.n.view.login.LoginFragment;
import com.cylan.jiafeigou.n.view.splash.WelcomePageActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LoginContainerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoginContainerFragment extends Fragment {


    @BindView(R.id.iv_login_top_left)
    ImageView ivLoginTopLeft;
    @BindView(R.id.tv_login_top_right)
    TextView tvLoginTopRight;


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
                .setCustomAnimations(R.anim.slide_up_in, R.anim.slide_down_out
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(R.id.fLayout_login_container, fragment)
                .addToBackStack("LogInFragment")
                .commit();
        new LoginPresenterImpl(fragment);
        return view;
    }

    @OnClick({R.id.iv_login_top_left, R.id.tv_login_top_right})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_login_top_left:
                if (getActivity() != null && getActivity() instanceof WelcomePageActivity) {
                    getActivity().finish();
                }
                break;
            case R.id.tv_login_top_right:
                final String content = tvLoginTopRight.getText().toString();
                if (TextUtils.equals(content, getString(R.string.item_register))) {
                    //register
                } else if (TextUtils.equals(content, getString(R.string.SignIn))) {

                }
                break;
        }
    }

    private void update() {

    }
}
