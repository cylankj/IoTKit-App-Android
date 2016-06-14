package com.cylan.jiafeigou.n.view.test;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.superlog.SLog;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link TestFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TestFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;



    public TestFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TestFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TestFragment newInstance(String param1, String param2) {
        TestFragment fragment = new TestFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_test, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        getChildFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_down_in,R.anim.slide_down_out,R.anim.slide_down_in,R.anim.slide_down_out)
                .replace(R.id.fLayout_login_container,TestLoginFragment.newInstance("",""),"")
                .commit();
        final TextView btn=    ((TextView)view.findViewById(R.id.tv_login_top_right));
        btn.setText("denglu");
        view.findViewById(R.id.tv_login_top_right).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String  charSet= btn.getText().toString();
                if(TextUtils.equals("denglu",charSet)){
                    btn.setText("zhuce");
                    getChildFragmentManager()
                            .beginTransaction()
                            .setCustomAnimations(R.anim.slide_in_right,R.anim.slide_out_left)
                            .replace(R.id.fLayout_login_container,TestRegisterFragment.newInstance("",""),"")
                            .commit();
                }else if(TextUtils.equals("zhuce",charSet)){
                    btn.setText("denglu");
                    getChildFragmentManager()
                            .beginTransaction()
                            .setCustomAnimations(R.anim.slide_in_left,R.anim.slide_out_right)
                            .replace(R.id.fLayout_login_container,TestLoginFragment.newInstance("",""),"")
                            .commit();
                }

            }
        });
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
