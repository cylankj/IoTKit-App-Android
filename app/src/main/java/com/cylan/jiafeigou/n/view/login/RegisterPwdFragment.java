package com.cylan.jiafeigou.n.view.login;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.dialog.BaseDialog;
import com.cylan.jiafeigou.widget.dialog.SimpleDialogFragment;
import com.cylan.utils.NetUtils;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RegisterPwdFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RegisterPwdFragment extends SetupPwdFragment
        implements BaseDialog.BaseDialogAction {
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
                showSimpleDialog("是否确认退出?", "否", "是", false);
            }
        });
    }

    @Override
    public void onDialogAction(int id, Object value) {
        Fragment f = getActivity()
                .getSupportFragmentManager()
                .findFragmentByTag(DIALOG_KEY);
        if (f != null && f.isVisible()) {
            ((SimpleDialogFragment) f).dismiss();
        }
        if (id == R.id.tv_dialog_btn_left) {
        } else {
//            Toast.makeText(getContext(), "去登录", Toast.LENGTH_SHORT).show();
            //dismiss the dialog
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }

    @Override
    public void submitResult(RxEvent.ResultRegister register) {
        switch (register.code) {
            case JError.ErrorAccountAlreadyExist:
                showSimpleDialog("账号已经存在，请直接登陆", "取消", "去登陆", false);
                break;
            case JError.ErrorSMSCodeNotMatch:
                ToastUtil.showToast("验证码不正确");
                break;
            case JError.ErrorOK:
                ToastUtil.showToast("注册成功");
                break;
        }
    }

    /**
     * 弹框，{fragment}
     */
    private void showSimpleDialog(String title,
                                  String lContent,
                                  String rContent,
                                  boolean dismiss) {
        Fragment f = getActivity().getSupportFragmentManager().findFragmentByTag(DIALOG_KEY);
        if (f == null) {
            Bundle bundle = new Bundle();
            bundle.putString(SimpleDialogFragment.KEY_TITLE, title);
            bundle.putString(SimpleDialogFragment.KEY_LEFT_CONTENT, lContent);
            bundle.putString(SimpleDialogFragment.KEY_RIGHT_CONTENT, rContent);
            bundle.putBoolean(SimpleDialogFragment.KEY_TOUCH_OUT_SIDE_DISMISS, dismiss);
            SimpleDialogFragment dialogFragment = SimpleDialogFragment.newInstance(bundle);
            dialogFragment.setAction(this);
            dialogFragment.show(getActivity().getSupportFragmentManager(), DIALOG_KEY);
        }
    }
}
