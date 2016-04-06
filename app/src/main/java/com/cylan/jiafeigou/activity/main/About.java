package com.cylan.jiafeigou.activity.main;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import cylan.log.DswLog;
import com.cylan.jiafeigou.base.BaseActivity;
import com.cylan.jiafeigou.utils.OEMConf;
import com.cylan.jiafeigou.utils.PreferenceUtil;
import com.cylan.jiafeigou.utils.UpdateManager;
import com.cylan.jiafeigou.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class About extends BaseActivity implements View.OnClickListener {

    private UpdateManager mUpdateManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ly_about);
        setTitle(R.string.ABOUT);
        TextView version = (TextView) findViewById(R.id.version);
        version.setText(getString(R.string.version_hint, Utils.getVersion(this)));//+ "-" + getSmallVision()
        TextView mAgreement = (TextView) findViewById(R.id.agreement);
        LinearLayout mJFGWeb = (LinearLayout) findViewById(R.id.net);
        ImageView mVersionView = (ImageView) findViewById(R.id.new_version_remind);
        if (PreferenceUtil.getIsNeedUpgrade(this)) {
            mVersionView.setVisibility(View.VISIBLE);
        }
        mAgreement.setOnClickListener(this);
        mJFGWeb.setOnClickListener(this);
        findViewById(R.id.check_updata).setOnClickListener(this);
        findViewById(R.id.layout_help).setOnClickListener(this);
        findViewById(R.id.service_hotline).setOnClickListener(this);
        if (!OEMConf.showCopyright()) {
            findViewById(R.id.copyrightEn).setVisibility(View.GONE);
        }
        if (!OEMConf.showWeb()) {
            findViewById(R.id.net).setVisibility(View.GONE);
            findViewById(R.id.net_line).setVisibility(View.GONE);
        }
        if (!OEMConf.showServelTel()) {
            findViewById(R.id.hotline_line).setVisibility(View.GONE);
            findViewById(R.id.service_hotline).setVisibility(View.GONE);
        }
        mUpdateManager = new UpdateManager(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.agreement:
                startActivity(new Intent(About.this, WebViewActivity.class).putExtra(WebViewActivity.URL, OEMConf.showTreayUrl()).putExtra(WebViewActivity.TITLE,
                        getString(R.string.TERM_OF_USE)));
                break;
            case R.id.net:
                startActivity(new Intent(About.this, WebViewActivity.class).putExtra(WebViewActivity.URL, getString(R.string.web)).putExtra(WebViewActivity.TITLE,
                        getString(R.string.WEB)));
                break;
            case R.id.check_updata:
                if (Utils.isNetworkConnected(this))
                    mUpdateManager.checkAppUpdate(true);
                break;
            case R.id.layout_help:
                startActivity(new Intent(this, Help.class));
                break;

            case R.id.service_hotline:
                try {
                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + getString(R.string.service_phonenum)));
                    startActivity(intent);
                } catch (Exception e) {
                }
                break;

        }
    }
    private String getSmallVision() {
        try {
            ApplicationInfo ai = getPackageManager().getApplicationInfo(getPackageName(), 0);
            ZipFile zf = new ZipFile(ai.sourceDir);
            ZipEntry ze = zf.getEntry("classes.dex");
            long time = ze.getTime();
            SimpleDateFormat dateFm = new SimpleDateFormat("yyyyMMdd");
            return dateFm.format(new java.util.Date(time));
        } catch (Exception e) {
            DswLog.ex(e.toString());
        }
        return "";
    }


}