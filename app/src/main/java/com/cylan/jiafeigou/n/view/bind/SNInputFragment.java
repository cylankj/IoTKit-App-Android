package com.cylan.jiafeigou.n.view.bind;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.CustomToolbar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import butterknife.Unbinder;

/**
 * A simple {@link Fragment} subclass.
 */
public class SNInputFragment extends IBaseFragment {


    @BindView(R.id.custom_toolbar)
    CustomToolbar customToolbar;
    Unbinder unbinder;
    @BindView(R.id.et_input_box)
    EditText etInputBox;
    @BindView(R.id.iv_clear)
    ImageView ivClear;
    @BindView(R.id.tv_submit)
    TextView tvSubmit;

    public SNInputFragment() {
        // Required empty public constructor
    }

    public static SNInputFragment newInstance() {
        // Required empty public constructor
        return new SNInputFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sninput, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        customToolbar.setBackAction(v -> getActivity().getSupportFragmentManager().popBackStack());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnTextChanged(R.id.et_input_box)
    public void onPwdChange(CharSequence s, int start, int before, int count) {
        boolean flag = TextUtils.isEmpty(s);
        if (flag || s.length() < 6) {
            tvSubmit.setEnabled(false);
        } else if (!TextUtils.isEmpty(ViewUtils.getTextViewContent(etInputBox))) {
            tvSubmit.setEnabled(true);
        }
    }

    @OnClick({R.id.et_input_box, R.id.iv_clear, R.id.tv_submit})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_clear:
                etInputBox.setText("");
                break;
            case R.id.tv_submit:
                final String content = etInputBox.getText().toString().trim();

                break;
        }
    }
}
