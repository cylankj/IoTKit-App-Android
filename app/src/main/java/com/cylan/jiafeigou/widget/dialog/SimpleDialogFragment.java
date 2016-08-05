package com.cylan.jiafeigou.widget.dialog;


import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cylan.jiafeigou.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 */
public class SimpleDialogFragment extends BaseDialog {

    private Object cache;
    public static final int ACTION_POSITIVE = 1;
    public static final int ACTION_NEGATIVE = -1;

    public static final String KEY_VALUE = "key_value";
    @BindView(R.id.tv_dialog_title)
    TextView tvDialogTitle;
    @BindView(R.id.tv_dialog_btn_positive)
    TextView tvDialogBtnPositive;
    @BindView(R.id.tv_dialog_btn_negative)
    TextView tvDialogBtnNegative;

    public static SimpleDialogFragment newInstance(Bundle bundle) {
        SimpleDialogFragment fragment = new SimpleDialogFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected int getCustomHeight() {
        return (int) (Resources.getSystem().getDisplayMetrics().density * 119 + 0.5f);
    }

    public SimpleDialogFragment() {
        // Required empty public constructor
    }

    public void setValue(Object cache) {
        this.cache = cache;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_simple_dialog, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @OnClick({R.id.tv_dialog_btn_positive, R.id.tv_dialog_btn_negative})
    public void onClick(View view) {
        dismiss();
        switch (view.getId()) {
            case R.id.tv_dialog_btn_positive:
                if (action != null) action.onDialogAction(ACTION_POSITIVE, cache);
                break;
            case R.id.tv_dialog_btn_negative:
                if (action != null) action.onDialogAction(ACTION_NEGATIVE, cache);
                break;
        }
    }

    public void setAction(SimpleDialogAction action) {
        this.action = action;
    }

    public void setupTitle(CharSequence charSequence) {
        if (tvDialogTitle != null && charSequence != null) tvDialogTitle.setText(charSequence);
    }

    private SimpleDialogAction action;

    public interface SimpleDialogAction {
        void onDialogAction(int id, Object value);
    }


}
