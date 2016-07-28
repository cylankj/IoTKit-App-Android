package com.cylan.jiafeigou.n.view.cam;

import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.utils.IMEUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 创建者     谢坤
 * 创建时间   2016/7/13 17:30
 * 描述：在点击设备名称之后，弹出的fragment对话框
 */
public class DeviceNameDialogFragment extends DialogFragment {

    private static final String TAG = "DeviceNameDialogFragment";
    private EditText mEtEditName;
    private Button mBtnEnsure;
    private Button mBtnCancel;
    private String mEditName;

    protected OnDataChangeListener mListener;
    private TextView mShowState;

    public void setListener(OnDataChangeListener mListener) {
        this.mListener = mListener;
    }

    /**
     * 接口回调，用来刷新UI
     */
    public interface OnDataChangeListener{
        void dataChangeListener(String content);
    }


    public static DeviceNameDialogFragment newInstance(Bundle bundle) {
        DeviceNameDialogFragment fragment = new DeviceNameDialogFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        View view = inflater.inflate(R.layout.fragment_edit_name,container);
        mBtnEnsure = (Button) view.findViewById(R.id.btn_information_ensure);
        mBtnCancel = (Button) view.findViewById(R.id.btn_information_cancel);
        mShowState = (TextView) view.findViewById(R.id.tv_information_show_state);
        mEtEditName = (EditText) view.findViewById(R.id.et_information_edit_name);


        String ininName = PreferencesUtils.getString(getActivity(),"editName","客厅摄像头");
        mEtEditName.setText(ininName);
        mEtEditName.setTextColor(Color.parseColor("#666666"));
        mEtEditName.setSelection(ininName.length());

        mBtnEnsure.setFocusable(false);
        mBtnEnsure.setEnabled(false);
        mBtnEnsure.setClickable(false);
        mBtnEnsure.setBackgroundColor(Color.parseColor("#cecece"));
        //用过butterKnife来找ID
        ButterKnife.bind(this,view);
        return view;
    }

    /**
     * 用来监听editText的各种情况
     */
    private void initListener() {

        mEtEditName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                //当输入不为空，并且与原来输入不一致时的时候，按钮颜色变回原来，并且可以点击
                mEditName = mEtEditName.getText().toString();
                if(mEditName.equals("")){
                    mBtnEnsure.setFocusable(false);
                    mBtnEnsure.setEnabled(false);
                    mBtnEnsure.setClickable(false);
                    mBtnEnsure.setBackgroundColor(Color.parseColor("#cecece"));
                    mShowState.setVisibility(View.VISIBLE);
                    mShowState.setText("名称不能为空");
                }else if(mEditName!=null){
                    mBtnEnsure.setFocusable(true);
                    mBtnEnsure.setEnabled(true);
                    mBtnEnsure.setClickable(true);
                    mBtnEnsure.setBackgroundColor(Color.parseColor("#C5E6FC"));
                    mShowState.setVisibility(View.INVISIBLE);
                }
            }
        });


        mEtEditName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return (event.getKeyCode()==KeyEvent.KEYCODE_ENTER);
            }
        });
    }

    @OnClick({R.id.et_information_edit_name,R.id.btn_information_ensure,R.id.btn_information_cancel})
    public void OnClick(View view){
        switch (view.getId()){
            //点击editText做相应的逻辑
            case R.id.et_information_edit_name:
                initListener();
                break;
            //点击取消按钮做相应的逻辑
            case R.id.btn_information_cancel:
                hideKeyboard(view);
                dismiss();
                break;
            //点击确认按钮做相应的逻辑
            case R.id.btn_information_ensure:
                    saveEditName();
                    String editName = PreferencesUtils.getString(getActivity(),"editName","客厅摄像头");
                    mEtEditName.setTextColor(Color.parseColor("#666666"));
                    mEtEditName.setText(editName);
                    if(mListener!=null){
                        mListener.dataChangeListener(mEditName);
                    }
                    //点击确认按钮之后，把软键盘进行隐藏
                    hideKeyboard(view);
                    dismiss();
                break;
        }
    }

    /**
     *
     * 点击确认按钮之后，把软键盘进行隐藏
     */
    private void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(),0);
    }

    /**
     * 保存用户输入的设备名称
     */
    private void saveEditName() {
        //TODO DeBug 调式模式下才可以使用pre
        PreferencesUtils.putString(getActivity(),"editName",mEditName);
    }

}
