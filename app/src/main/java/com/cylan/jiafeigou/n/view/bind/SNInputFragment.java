package com.cylan.jiafeigou.n.view.bind;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.bind.SnContract;
import com.cylan.jiafeigou.n.mvp.impl.bind.SnPresenter;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
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
public class SNInputFragment extends IBaseFragment<SnContract.Presenter> implements SnContract.View {


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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        basePresenter = new SnPresenter(this);
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
        InputFilter filter = (source, start, end, dest, dstart, dend) -> source.toString().trim();
        etInputBox.setFilters(new InputFilter[]{filter, new InputFilter.LengthFilter(20)});
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnTextChanged(R.id.et_input_box)
    public void onPwdChange(CharSequence s, int start, int before, int count) {
        boolean flag = TextUtils.isEmpty(s);
        if (flag || s.length() < 13) {
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
                if (NetUtils.getJfgNetType() == 0) {
                    ToastUtil.showToast(getString(R.string.NoNetworkTips));
                    return;
                }
                basePresenter.getPid(etInputBox.getText().toString().trim());
                break;
        }
    }

    @Override
    public void getPidRsp(int pid) {
        if (!isAdded()) return;
        getActivity().getSupportFragmentManager().beginTransaction().remove(this)
                .commit();
        if (JFGRules.isConsumerCam(pid)) {
            getActivity().findViewById(R.id.v_to_bind_consumer_cam)
                    .performClick();
        } else if (JFGRules.isCloudCam(pid)) {
            getActivity().findViewById(R.id.v_to_bind_camera_cloud)
                    .performClick();
        } else if (JFGRules.isPanoramicCam(pid)) {
            getActivity().findViewById(R.id.v_to_bind_panorama_camera)
                    .performClick();
        } else if (JFGRules.isCamera(pid)) {
            getActivity().findViewById(R.id.v_to_bind_camera)
                    .performClick();
        } else if (JFGRules.isCatEeyBell(pid)) {
            getActivity().findViewById(R.id.v_to_bind_cat_eye_cam)
                    .performClick();
        } else if (JFGRules.isNoPowerBell(pid)) {
            getActivity().findViewById(R.id.v_to_bind_bell_no_battery)
                    .performClick();
        } else if (JFGRules.isBell(pid)) {
            getActivity().findViewById(R.id.v_to_bind_bell_battery)
                    .performClick();
        }
    }
}
