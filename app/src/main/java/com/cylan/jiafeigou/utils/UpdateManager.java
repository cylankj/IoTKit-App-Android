package com.cylan.jiafeigou.utils;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cylan.jiafeigou.R;
import com.cylan.publicApi.CheckUpdateUrl;
import com.cylan.publicApi.Constants;
import com.cylan.publicApi.DswLog;
import com.cylan.jiafeigou.entity.Update;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.HttpHandler;

import java.io.File;
import java.text.DecimalFormat;

public class UpdateManager {

    private String UTF_8 = "UTF-8";

    private static final int DOWN_NOSDCARD = 0;
    private static final int DOWN_UPDATE = 1;
    private static final int DOWN_OVER = 2;
    private static final int DOWN_NOSPACE = 3;

    private static final int DIALOG_TYPE_LATEST = 0;
    private static final int DIALOG_TYPE_FAIL = 1;

    private Context mContext;
    // 通知对话框
    private Dialog noticeDialog;
    // 下载对话框
    private Dialog downloadDialog;
    // '已经是最新' 或者 '无法获取最新版本' 的对话框
    private Dialog latestOrFailDialog;
    // 进度条
    private ProgressBar mProgress;
    // 显示下载数值
    private TextView mProgressText;
    // 查询动画
    private ProgressDialog mProDialog;
    // 进度值
    private int progress;
    // 下载取消按钮
    private Button cancel;
    // 提示语
    private String updateMsg = "";
    // 返回的安装包url
    private String apkUrl = "";
    // 下载包保存路径
    private String savePath = "";
    // apk保存完整路径
    private String apkFilePath = "";
    // 临时下载文件路径
    private String tmpFilePath = "";
    private String tmpTxtPath = "";
    // 下载文件大小
    private String apkFileSize;
    // 已下载文件大小
    private String tmpFileSize;
    private File apkfile;
    private HttpHandler<File> handler;

    private String curVersionName = "";
    private int curVersionCode;
    private Update mUpdate;

    private long totalCount;

    private final static int isForceUpdate = 1;// 是否强制升级 1是

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DOWN_UPDATE:
                    mProgress.setProgress(msg.arg1);
                    mProgressText.setText(msg.obj.toString());
                    break;
                case DOWN_OVER:
                    if (downloadDialog != null && downloadDialog.isShowing())
                        downloadDialog.dismiss();
                    installApk();
                    break;
                case DOWN_NOSDCARD:
                    if (downloadDialog != null && downloadDialog.isShowing())
                        downloadDialog.dismiss();
                    ToastUtil.showToast(mContext, mContext.getString(R.string.the_memory_card_is_not_present), Gravity.CENTER, Toast.LENGTH_SHORT);
                    break;
                case DOWN_NOSPACE:
                    ToastUtil.showToast(mContext, mContext.getString(R.string.isStorageLow), Gravity.CENTER, Toast.LENGTH_SHORT);
                    break;
            }
        }

    };

    public UpdateManager(Context context) {
        this.mContext = context;
    }

    /**
     * 检查App更新
     *
     * @param isShowMsg 是否显示提示消息
     */
    public void checkAppUpdate(final boolean isShowMsg) {
        getCurrentVersion();
        if (isShowMsg) {
            if (mProDialog == null)
                mProDialog = ProgressDialog.show(mContext, null, mContext.getString(R.string.checking), true, false);
            else if (mProDialog.isShowing() || (latestOrFailDialog != null && latestOrFailDialog.isShowing()))
                return;
        }
        final Handler handler = new Handler() {
            public void handleMessage(Message msg) {
                // 进度条对话框不显示 - 检测结果也不显示
                if (mProDialog != null && !mProDialog.isShowing()) {
                    return;
                }
                // 关闭并释放释放进度条对话框
                if (isShowMsg && mProDialog != null) {
                    if (mProDialog.isShowing())
                        mProDialog.dismiss();
                    mProDialog = null;
                }
                // 显示检测结果
                if (msg.what == 1) {
                    mUpdate = (Update) msg.obj;
                    if (mUpdate != null) {
                        if (mUpdate.getRet() == Constants.RETOK) {
                            if (Utils.getAppVersionName(mContext).equals("888"))
                                return;
                            if (Utils.isNoSurrortVersion(mUpdate.getVersion(), Utils.getAppVersionName(mContext))) {
                                PreferenceUtil.setIsNeedUpgrade(mContext, true);
                                apkUrl = mUpdate.getUrl();
                                updateMsg = mUpdate.getDesc();
                                showNoticeDialog(isShowMsg);
                            } else {
                                PreferenceUtil.setIsNeedUpgrade(mContext, false);
                                if (isShowMsg) {
                                    showLatestOrFailDialog(DIALOG_TYPE_LATEST);
                                    ToastUtil.showToast(mContext, mContext.getString(R.string.NEW_VERSION), Gravity.CENTER, Toast.LENGTH_SHORT);
                                }
                            }
                        } else if ((mUpdate.getRet() == -4 || mUpdate.getRet() == -5)) {
                            PreferenceUtil.setIsNeedUpgrade(mContext, false);
                            showLatestOrFailDialog(DIALOG_TYPE_LATEST);
                            if (isShowMsg)
                                ToastUtil.showToast(mContext, mContext.getString(R.string.NEW_VERSION), Gravity.CENTER, Toast.LENGTH_SHORT);
                        } else {
                            PreferenceUtil.setIsNeedUpgrade(mContext, false);
                            if (isShowMsg)
                                ToastUtil.showToast(mContext,
                                        StringUtils.isEmptyOrNull(mUpdate.getMsg()) ? mContext.getString(R.string.NEW_VERSION) : mUpdate.getMsg(),
                                        Gravity.CENTER, Toast.LENGTH_SHORT);
                        }
                    } else {
                        if (isShowMsg)
                            ToastUtil.showToast(mContext, mContext.getString(R.string.NEW_VERSION), Gravity.CENTER, Toast.LENGTH_SHORT);
                    }
                } else if (isShowMsg) {
                    showLatestOrFailDialog(DIALOG_TYPE_FAIL);
                }
            }
        };

        new Thread() {
            public void run() {
                Message msg = new Message();
                try {
                    if (!PreferenceUtil.getCheckVersion(mContext).equals("0") && !PreferenceUtil.getCheckVersionUrl(mContext).equals("")) {
                        String url = CheckUpdateUrl.getCheckUpdateUrl(PreferenceUtil.getCheckVersion(mContext), PreferenceUtil.getCheckVersionUrl(mContext),
                                CheckUpdateUrl.ANDROIDPHONE, Utils.getAppPackageName(mContext));
                        Update update = new ApiClient().checkVersion(mContext, url);
                        msg.what = 1;
                        msg.obj = update;
                    } else {
                        msg.what = 1;
                        msg.obj = null;
                    }

                } catch (Exception e) {
                    DswLog.ex(e.toString());
                } catch (Throwable e) {
                    DswLog.ex(e.toString());
                }
                handler.sendMessage(msg);
            }
        }.start();
    }

    /**
     * 显示'已经是最新'或者'无法获取版本信息'对话框
     */
    private void showLatestOrFailDialog(int dialogType) {
        if (latestOrFailDialog != null) {
            // 关闭并释放之前的对话框
            latestOrFailDialog.dismiss();
            latestOrFailDialog = null;
        }

    }

    /**
     * 获取当前客户端版本信息
     */
    private void getCurrentVersion() {
        try {
            PackageInfo info = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            curVersionName = info.versionName;
            curVersionCode = info.versionCode;
        } catch (NameNotFoundException e) {
            DswLog.ex(e.toString());
        }
    }

    /**
     * 显示版本更新通知对话框
     */
    private void showNoticeDialog(final boolean is) {

        final NotifyDialog dialog = new NotifyDialog(mContext);
        dialog.setButtonText(R.string.UPGRADE_NOW, R.string.NEXT_TIME);

        dialog.showUpdateTheme(mContext.getString(R.string.find_new_version));

        if (mUpdate != null && !StringUtils.isEmptyOrNull(mUpdate.getDesc())) {
            dialog.setContent(mUpdate.getDesc());
        }
        if (!is && PreferenceUtil.getIsUpgrade(mContext) == isForceUpdate) {
            dialog.hideNegButton();
            dialog.setCancelable(false);
        }
        dialog.show(R.string.upgrade_hint, new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.cancel:
                        dialog.dismiss();
                        break;
                    case R.id.confirm: {
                        showDownloadDialog();
                        if (!is && PreferenceUtil.getIsUpgrade(mContext) == isForceUpdate)
                            return;
                        dialog.dismiss();

                    }
                    break;
                }

            }
        }, null);

    }

    /**
     * 显示下载对话框
     */
    private void showDownloadDialog() {

        try {
            downloadDialog = new Dialog(mContext, R.style.func_dialog);
            View v = View.inflate(mContext, R.layout.update_progress, null);
            mProgress = (ProgressBar) v.findViewById(R.id.update_progress);
            mProgressText = (TextView) v.findViewById(R.id.update_progress_text);
            cancel = (Button) v.findViewById(R.id.cancel);
            downloadDialog.setContentView(v);
            cancel.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    downloadDialog.dismiss();
                    if (handler != null)
                        handler.stop();
                }
            });
            downloadDialog.setOnCancelListener(new OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    dialog.dismiss();
                    if (handler != null)
                        handler.stop();
                }
            });
            downloadDialog.setCanceledOnTouchOutside(false);
            downloadDialog.show();

            downloadApk();
        } catch (Exception e) {
            DswLog.ex(e.toString());
        }
    }

    /**
     * 下载apk
     */
    private void downloadApk() {
        String apkName = "jiafeigou_" + mUpdate.getVersion() + ".apk";
        String tmpApk = "CylanApp_" + mUpdate.getVersion() + ".tmp";
        // 判断是否挂载了SD卡
        String storageState = Environment.getExternalStorageState();
        if (storageState.equals(Environment.MEDIA_MOUNTED)) {
            savePath = PathGetter.getUpgradePath();
            File file = new File(savePath);
            if (!file.exists()) {
                file.mkdirs();
            }
            apkFilePath = savePath + apkName;
            tmpFilePath = savePath + tmpApk;
        }

        // 没有挂载SD卡，无法下载文件
        if (StringUtils.isEmptyOrNull(apkFilePath)) {
            mHandler.sendEmptyMessage(DOWN_NOSDCARD);
            return;
        }

        apkfile = new File(apkFilePath);
        // 是否已下载更新文件
        if (apkfile.exists()) {
            if (downloadDialog != null && downloadDialog.isShowing())
                downloadDialog.dismiss();
            installApk();
            return;
        }
        FinalHttp fh = new FinalHttp();
        if (handler != null) {
            handler.stop();
        }
        handler = fh.download(apkUrl,
                tmpFilePath,
                true,
                new AjaxCallBack<File>() {
                    @Override
                    public void onLoading(long count, long current) {
                        DswLog.i("onLoading count-->" + count + "---current-->" + current);
                        DecimalFormat df = new DecimalFormat("0.00");
                        String apkFileSize = df.format((float) count / 1024 / 1024) + "MB";
                        String tmpFileSize = df.format((float) current / 1024 / 1024) + "MB";
                        progress = (int) (((float) current / count) * 100);
                        Message msg = mHandler.obtainMessage();
                        msg.what = DOWN_UPDATE;
                        msg.arg1 = progress;
                        msg.obj = tmpFileSize + "/" + apkFileSize;
                        mHandler.sendMessage(msg);
                    }

                    @Override
                    public void onSuccess(File t) {
                        DswLog.i("onSuccess");
                        // 通知安装
                        if (progress != 100) {
                            return;
                        }
                        if (t.renameTo(apkfile)) {
                            mHandler.sendEmptyMessage(DOWN_OVER);
                        }


                    }

                }

        );
    }


    /**
     * 安装apk
     */
    private void installApk() {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setDataAndType(Uri.parse("file://" + apkfile.toString()), "application/vnd.android.package-archive");
        mContext.startActivity(i);
    }

}
