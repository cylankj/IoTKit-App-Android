package com.cylan.jiafeigou.n.view.cam;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.utils.ToastUtil;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 创建者     谢坤
 * 创建时间   2016/7/13 17:30
 * 描述：在点击设备名称之后，弹出的fragment对话框
 */
public class DeviceNameDialogFragment extends DialogFragment {

    private EditText mEtEditName;
    private Button mBtnEnsure;
    private Button mBtnCancel;
    private String mEditName;

    protected OnDataChangeListener mListener;

    public void setListener(OnDataChangeListener mListener) {
        this.mListener = mListener;
    }

    /**
     * 接口回调，用来刷新UI
     */
    public interface OnDataChangeListener{
        void dataChangeListener(String content);
    }

    public DeviceNameDialogFragment() {

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
        getDialog().getWindow();
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        View view = inflater.inflate(R.layout.fragment_edit_name,container);
        mBtnEnsure = (Button) view.findViewById(R.id.btn_information_ensure);
        mBtnCancel = (Button) view.findViewById(R.id.btn_information_cancel);
        //设置光标位置在最后边
        mEtEditName = (EditText) view.findViewById(R.id.et_information_edit_name);
        //用过butterKnife来找ID
        ButterKnife.bind(this,view);
        return view;
    }

    @OnClick({R.id.et_information_edit_name,R.id.btn_information_ensure,R.id.btn_information_cancel})
    public void OnClick(View view){
        switch (view.getId()){
            //点击editText做相应的逻辑
            case R.id.et_information_edit_name:
                break;
            //点击取消按钮做相应的逻辑
            case R.id.btn_information_cancel:
                hideKeyboard(view);
                dismiss();
                break;
            //点击确认按钮做相应的逻辑
            case R.id.btn_information_ensure:
                    mEditName = mEtEditName.getText().toString();
                    saveEditName();
                    if(mListener!=null){
                        mListener.dataChangeListener(mEditName);
                    }
                    hideKeyboard(view);
                //TODO 确认之前，要判断用户是否输入了字符串，如果没有输入不允许确认
                //TODO 用户没有改变设备名称，确认按钮会变成灰色，当用户改变了之后，确认按钮才可以点击
                    dismiss();
                break;
        }
    }


    /**
     * 保存用户输入的设备名称
     */
    private void saveEditName() {
        SharedPreferences sp = getActivity().getSharedPreferences("config",0);
        SharedPreferences.Editor edit = sp.edit();
        edit.putString("editName", mEditName);
        edit.commit();
    }

/*    *//**
     *  设置hint的变化
     *//*
    private void setHintText() {
        SpannableString ss = new SpannableString(mEditName);//定义hint的值
        AbsoluteSizeSpan ass = new AbsoluteSizeSpan(14,true);//设置字体大小 true表示单位是sp
        ss.setSpan(ass, 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mEtEditName.setHint(new SpannedString(ss));
    }*/

    /**
     *
     * 点击确认按钮之后，把软键盘进行隐藏
     */
    private void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(),0);
    }

}
