package com.cylan.jiafeigou.n.view.misc;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.FragmentActivity;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.engine.DownloadService;
import com.cylan.jiafeigou.n.mvp.contract.DownloadContract;
import com.cylan.jiafeigou.n.mvp.impl.DownloadContractPresenterImpl;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UpdateActivity extends FragmentActivity implements DownloadContract.View {

    @BindView(R.id.update_progress)
    ProgressBar updateProgress;
    @BindView(R.id.update_progress_text)
    TextView updateProgressText;
    @BindView(R.id.lLayout_update_dialog)
    LinearLayout lLayoutUpdateDialog;
    @BindView(R.id.btn_update_cancel)
    Button btnUpdateCancel;
    private DownloadContract.Presenter presenter;

    private static final String DIALOG_KEY = "dialogFragment";
    private Parcelable parcelableExtra;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_update);
        ButterKnife.bind(this);
        findViewById(R.id.btn_update_cancel)
                .setOnClickListener(v -> {
                    finish();
                    if (presenter != null)
                        presenter.stopDownload();
                    if (presenter != null)
                        presenter.stop();
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
        runOnUiThread(() -> {
            updateProgress.setProgress((int) percent);
        });
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
