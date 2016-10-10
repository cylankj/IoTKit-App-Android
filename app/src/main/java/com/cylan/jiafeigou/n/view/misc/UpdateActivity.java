package com.cylan.jiafeigou.n.view.misc;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Process;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.Window;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.engine.DownloadService;
import com.cylan.jiafeigou.n.mvp.contract.DownloadContract;
import com.cylan.jiafeigou.n.mvp.impl.DownloadContractPresenterImpl;

public class UpdateActivity extends FragmentActivity
        implements DownloadContract.View {

    private DownloadContract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_update);
        findViewById(R.id.btn_update_cancel)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                        if (presenter != null)
                            presenter.stopDownload();
                        if (presenter != null)
                            presenter.stop();
                        Process.killProcess(Process.myPid());
                    }
                });
        updateDialog();
        presenter = new DownloadContractPresenterImpl(this);
        presenter.startDownload(getIntent().getParcelableExtra(DownloadService.KEY_PARCELABLE));
    }

    private void updateDialog() {
        setFinishOnTouchOutside(false);
        int w = (int) (Resources.getSystem().getDisplayMetrics().widthPixels * 0.75f);
        int h = (int) (Resources.getSystem().getDisplayMetrics().heightPixels * 0.25f);
        getWindow().setLayout(w, h);
    }

    @Override
    public void onBackPressed() {
        //do nothing but you click cancel btn
    }

    @Override
    public void onDownloadStart() {

    }

    @Override
    public void onDownloadFinish() {

    }

    @Override
    public void onDownloading(double percent, long downloadedLength) {

    }

    @Override
    public void onDownloadErr(int reason) {

    }

    @Override
    public void setPresenter(DownloadContract.Presenter presenter) {

    }

    @Override
    public Context getContext() {
        return this;
    }
}
