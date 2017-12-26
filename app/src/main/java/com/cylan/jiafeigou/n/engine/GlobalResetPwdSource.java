package com.cylan.jiafeigou.n.engine;

///**
// * 作者：zsl
// * 创建时间：2017/3/14
// * 描述：
// */
//public class GlobalResetPwdSource {
//    private static GlobalResetPwdSource instance;
//    private CompositeSubscription mSubscription;
//
//    private Activity appCompatActivity;
//
//    public static GlobalResetPwdSource getInstance() {
//        if (instance == null) {
//            instance = new GlobalResetPwdSource();
//        }
//        return instance;
//    }
//
//    public void register() {
//        if (mSubscription == null) {
//            mSubscription = new CompositeSubscription();
//        }
//        long time = System.currentTimeMillis();
//        Subscription subscribe = RxBus.getCacheInstance().toObservable(RxEvent.PwdHasResetEvent.class)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(pwdHasResetEvent -> {
//                    AppLogger.d("收到密码已被修改通知" + BaseApplication.isBackground());
//                    PreferencesUtils.putBoolean(JConstant.AUTO_lOGIN_PWD_ERR, true);
//                    RxBus.getCacheInstance().removeAllStickyEvents();
//                    clearPwd();
//                    if (!BaseApplication.isBackground()) {
//                        PreferencesUtils.putBoolean(JConstant.SHOW_PASSWORD_CHANGED, false);
//                        pwdResetedDialog(pwdHasResetEvent.code);
//                    } else {
//                        PreferencesUtils.putBoolean(JConstant.SHOW_PASSWORD_CHANGED, true);
//                    }
//                }, throwable -> AppLogger.e("err:" + MiscUtils.getErr(throwable)));
//        mSubscription.add(subscribe);
//        Log.d("GlobalResetPwdSource", "GlobalResetPwdSource:" + (System.currentTimeMillis() - time) + ":ms");
//    }
//
//    public void unRegister() {
//        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
//            mSubscription.unsubscribe();
//            mSubscription = null;
//        }
//    }
//
//    public void currentActivity(Activity appCompatActivity) {
//        this.appCompatActivity = appCompatActivity;
//    }
//
//    public void pwdResetedDialog(int code) {
//        if (code == 16008 || code == 1007 || code == 16006) {
//            AppLogger.d("pwdResetedDialog:" + code);
//            if (appCompatActivity != null) {
//                AlertDialog.Builder builder = AlertDialogManager.getInstance().getCustomDialog(appCompatActivity);
//                builder.setTitle(R.string.RET_ELOGIN_ERROR)
//                        .setMessage(R.string.PWD_CHANGED)
//                        .setCancelable(false)
//                        .setPositiveButton(R.string.OK, (dialog1, which) -> {
//                            dialog1.dismiss();
//                            jump2LoginFragment();
//                        });
//                AlertDialogManager.getInstance().showDialog("pwdResetedDialog", appCompatActivity, builder);
//            }
//        }
//    }
//
//    private void jump2LoginFragment() {
//        clearPwd();
//        RxBus.getCacheInstance().postSticky(new RxEvent.ResultLogin(JError.ErrorLoginInvalidPass));
//        Intent intent = new Intent(ContextUtils.getContext(), SmartcallActivity.class);
//        intent.putExtra("from_log_out", true);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        ContextUtils.getContext().getApplicationContext().startActivity(intent);
//    }
//
//    public void clearPwd() {
//        AutoSignIn.getInstance().clearPsw();
//    }
//}
