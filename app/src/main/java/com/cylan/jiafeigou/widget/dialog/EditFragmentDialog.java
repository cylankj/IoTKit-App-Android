package com.cylan.jiafeigou.widget.dialog;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.cylan.jiafeigou.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 创建者     谢坤
 * 创建时间   2016/7/13 17:30
 * 描述：在点击设备名称之后，弹出的fragment对话框
 */
public class EditFragmentDialog extends BaseDialog {


    @BindView(R.id.tv_confirm)
    TextView tvConfirm;
    @BindView(R.id.tv_cancel)
    TextView tvCancel;
    @BindView(R.id.tv_dialog_title)
    TextView tvDialogTitle;
    public static final String KEY_TITLE = "key_title";
    public static final String KEY_LEFT_CONTENT = "key_left";
    public static final String KEY_RIGHT_CONTENT = "key_right";
    public static final String KEY_SHOW_EDIT = "key_show_edit";
    public static final String KEY_TOUCH_OUT_SIDE_DISMISS = "key_touch_outside";
    @BindView(R.id.et_input_box)
    EditText etInputBox;

    public static EditFragmentDialog newInstance(Bundle bundle) {
        EditFragmentDialog fragment = new EditFragmentDialog();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getCustomHeight() {
        return ViewGroup.LayoutParams.WRAP_CONTENT;
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dialog_edit_name, container);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        final String title = bundle.getString(KEY_TITLE);
        final String lContent = bundle.getString(KEY_LEFT_CONTENT);
        final String rContent = bundle.getString(KEY_RIGHT_CONTENT);
        if (!TextUtils.isEmpty(title))
            tvDialogTitle.setText(title);
        if (!TextUtils.isEmpty(lContent))
            tvConfirm.setText(lContent);
        if (!TextUtils.isEmpty(rContent))
            tvCancel.setText(rContent);
        if (!bundle.getBoolean(KEY_SHOW_EDIT, false)) {
            view.findViewById(R.id.lLayout_input_box).setVisibility(View.GONE);
        }
        getDialog().setCanceledOnTouchOutside(bundle.getBoolean(KEY_TOUCH_OUT_SIDE_DISMISS, false));
    }

    /**
     * 点击确认按钮之后，把软键盘进行隐藏
     */
    private void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @OnClick({R.id.tv_confirm, R.id.tv_cancel})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_confirm:
                //点击确认按钮之后，把软键盘进行隐藏
                hideKeyboard(view);
                dismiss();
                if (action != null) {
                    action.onDialogAction(R.id.tv_confirm, etInputBox.getText().toString());
                }
                break;
            case R.id.tv_cancel:
                hideKeyboard(view);
                dismiss();
//                if (action != null) {
//                    action.onDialogAction(R.id.tv_cancel, null);
//                }
                break;
        }
    }

    public <T> void setAction(DialogAction<T> action) {
        this.action = action;
    }

    private DialogAction action;

    public interface DialogAction<T> {
        void onDialogAction(int id, T value);
    }
}
