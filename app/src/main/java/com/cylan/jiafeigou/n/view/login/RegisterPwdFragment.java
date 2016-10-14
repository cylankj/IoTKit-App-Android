package com.cylan.jiafeigou.n.view.login;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
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
public class RegisterPwdFragment extends SetupPwdFragment implements SimpleDialogFragment.SimpleDialogAction {

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
    public void submitResult(RequestResetPwdBean bean) {
//        if (bean.ret == 0) {
//            Toast.makeText(getActivity().getApplicationContext(), "注册成功", Toast.LENGTH_SHORT).show();
//            //这样启动Activity耦合严重，应该使用路由的方式。
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//                Bundle bundle = new Bundle();
//                bundle.putInt(NewHomeActivity.KEY_ENTER_ANIM_ID, R.anim.slide_down_out);
//                bundle.putInt(NewHomeActivity.KEY_EXIT_ANIM_ID, R.anim.slide_down_in);
//                getActivity().startActivity(new Intent(getActivity(), NewHomeActivity.class)
//                                .putExtras(bundle),
//                        ActivityOptionsCompat.makeCustomAnimation(getContext(), R.anim.slide_down_in,
//                                R.anim.slide_down_out).toBundle());
//            } else {
//                getActivity().startActivity(new Intent(getActivity(), NewHomeActivity.class));
//            }
//            getActivity().finish();
//        } else if (bean.ret == 183) {
//            Toast.makeText(getActivity().getApplicationContext(), "账号已存在", Toast.LENGTH_SHORT).show();
//            Fragment f = getActivity().getSupportFragmentManager().findFragmentByTag("dialogFragment");
//            if (f != null && f.isVisible()) {
//                AppLogger.i("fragment is added");
//                return;
//            }
//            Bundle bundle = new Bundle();
//            bundle.putString(SimpleDialogFragment.KEY_TITLE, "账号已经存在，请直接登陆");
//            bundle.putString(SimpleDialogFragment.KEY_LEFT_CONTENT, "取消");
//            bundle.putString(SimpleDialogFragment.KEY_RIGHT_CONTENT, "去登陆");
//            SimpleDialogFragment dialogFragment = SimpleDialogFragment.newInstance(bundle);
//            dialogFragment.setAction(this);
//            dialogFragment.show(getActivity().getSupportFragmentManager(), "dialogFragment");
//        } else {
//            Toast.makeText(getActivity(), "注册失败", Toast.LENGTH_SHORT).show();
//        }
    }

    @Override
    public void onDialogAction(int id, Object value) {
//        if (id == SimpleDialogFragment.ACTION_LEFT) {
//            Fragment f = getActivity().getSupportFragmentManager().findFragmentByTag("dialogFragment");
//            if (f != null && f.isVisible()) {
//                ((SimpleDialogFragment) f).dismiss();
//            }
//        } else {
//            Toast.makeText(getContext(), "去登录", Toast.LENGTH_SHORT).show();
//            getActivity().getSupportFragmentManager().popBackStack();
//            RxBus.getInstance().send(new RxEvent.SwitchBox());
//        }
    }
}
