package com.cylan.jiafeigou.n.view.login_ex;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.utils.ActivityUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.cylan.jiafeigou.n.view.login.ForgetPwdFragment.KEY_ACCOUNT_TO_SEND_EMAIL;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ForgetPwdByEmailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ForgetPwdByEmailFragment extends Fragment {


    @BindView(R.id.tv_email_confirm)
    TextView tvEmailConfirm;

    public ForgetPwdByEmailFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ForgetPwdByEmailFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ForgetPwdByEmailFragment newInstance(Bundle bundle) {
        ForgetPwdByEmailFragment fragment = new ForgetPwdByEmailFragment();
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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_forget_pwd_by_email, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    private String getContent() {
        return String.format(getString(R.string.send_email_tip_content),
                getArguments().getString(KEY_ACCOUNT_TO_SEND_EMAIL));
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        initContent();
    }

    private void initContent() {
        TextView tv = (TextView) getView().findViewById(R.id.tv_send_email_content);
        tv.setText(getContent());
        tvEmailConfirm.setEnabled(true);
    }


    @OnClick(R.id.tv_email_confirm)
    public void onClick() {
        ActivityUtils.justPop(getActivity());
    }
}
