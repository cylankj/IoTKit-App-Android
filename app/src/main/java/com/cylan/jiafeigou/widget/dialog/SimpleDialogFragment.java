package com.cylan.jiafeigou.widget.dialog;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
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

    @BindView(R.id.tv_dialog_content)
    TextView tvDialogContent;
    private Object cache;

    public static final String KEY_LEFT_CONTENT = "key_left";
    public static final String KEY_RIGHT_CONTENT = "key_right";
    public static final String KEY_CONTENT_CONTENT = "key_content";

    @BindView(R.id.tv_dialog_title)
    TextView tvDialogTitle;
    @BindView(R.id.tv_dialog_btn_left)
    TextView tvDialogBtnLeft;
    @BindView(R.id.tv_dialog_btn_right)
    TextView tvDialogBtnRight;

    public static SimpleDialogFragment newInstance(Bundle bundle) {
        SimpleDialogFragment fragment = new SimpleDialogFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected int getCustomHeight() {
        return ViewGroup.LayoutParams.WRAP_CONTENT;
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
        View view = inflater.inflate(R.layout.fragment_simple_dialog, container, true);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        final boolean ishow_cancle_btn = bundle.getBoolean("ishow_cancle_btn");
        final String title = bundle.getString(KEY_TITLE);
        final String lContent = bundle.getString(KEY_LEFT_CONTENT);
        final String rContent = bundle.getString(KEY_RIGHT_CONTENT);
        final String content = bundle.getString(KEY_CONTENT_CONTENT);

        if (ishow_cancle_btn) {
            tvDialogBtnRight.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(title)) {
            tvDialogTitle.setVisibility(View.VISIBLE);
            tvDialogTitle.setText(title);
        }
        if (!TextUtils.isEmpty(lContent))
            tvDialogBtnLeft.setText(lContent);
        if (TextUtils.isEmpty(rContent)) {
            tvDialogBtnRight.setVisibility(View.GONE);
        } else tvDialogBtnRight.setText(rContent);
        if (TextUtils.isEmpty(content)) {
            tvDialogContent.setVisibility(View.GONE);
        } else tvDialogContent.setText(content);
        getDialog().setCanceledOnTouchOutside(bundle.getBoolean(KEY_TOUCH_OUT_SIDE_DISMISS, false));
    }

    @OnClick({R.id.tv_dialog_btn_left, R.id.tv_dialog_btn_right})
    public void onClick(View view) {
        dismiss();
        switch (view.getId()) {
            case R.id.tv_dialog_btn_left:
                if (action != null) action.onDialogAction(R.id.tv_dialog_btn_left, cache);
                break;
            case R.id.tv_dialog_btn_right:
                if (action != null) action.onDialogAction(R.id.tv_dialog_btn_right, cache);
                break;
        }
    }


}
