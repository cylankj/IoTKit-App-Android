package com.cylan.jiafeigou.n.view.login;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.mvp.model.RequestResetPwdBean;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.widget.dialog.SimpleDialogFragment;
import com.cylan.utils.NetUtils;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RegisterPwdFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RegisterPwdFragment extends SetupPwdFragment
        implements SimpleDialogFragment.SimpleDialogAction {
    private static final String DIALOG_KEY = "dialogFragment";

    public RegisterPwdFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create activity_cloud_live_mesg_video_talk_item new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SetupPwdFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RegisterPwdFragment newInstance(Bundle args) {
        RegisterPwdFragment fragment = new RegisterPwdFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void doAction(String account, String pwd, String code) {
        if (NetUtils.getJfgNetType(getContext()) == 0) {
            Toast.makeText(getContext(), "bad network", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(getContext(), "注册中", Toast.LENGTH_SHORT).show();
        boolean validPhoneNum = JConstant.PHONE_REG.matcher(account).find();
        AppLogger.i("account:" + account + ",pwd:" + pwd + ",code:" + code);
        boolean isPhone =
                (validPhoneNum && !TextUtils.isEmpty(code) && code.length() == JConstant.VALID_VERIFICATION_CODE_LEN);
        pwdPresenter.register(account,
                pwd,
                isPhone ? JConstant.TYPE_PHONE : JConstant.TYPE_EMAIL,
                isPhone ? PreferencesUtils.getString(JConstant.KEY_REGISTER_SMS_TOKEN) : "");
    }

    @Override
    protected void initNavigateBack() {
        ivLoginTopLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString(SimpleDialogFragment.KEY_TITLE, "是否确认退出？");
                bundle.putString(SimpleDialogFragment.KEY_LEFT_CONTENT, "否");
                bundle.putString(SimpleDialogFragment.KEY_RIGHT_CONTENT, "是");
                SimpleDialogFragment dialogFragment = SimpleDialogFragment.newInstance(bundle);
                dialogFragment.setAction(RegisterPwdFragment.this);
                dialogFragment.show(getActivity().getSupportFragmentManager(), DIALOG_KEY);
            }
        });
    }

    @Override
    public void submitResult(RequestResetPwdBean bean) {
    }

    @Override
    public void onDialogAction(int id, Object value) {
        Fragment f = getActivity()
                .getSupportFragmentManager()
                .findFragmentByTag(DIALOG_KEY);
        if (f != null && f.isVisible()) {
            ((SimpleDialogFragment) f).dismiss();
        }
        if (id == SimpleDialogFragment.ACTION_LEFT) {
        } else {
//            Toast.makeText(getContext(), "去登录", Toast.LENGTH_SHORT).show();
            //dismiss the dialog
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }

}
