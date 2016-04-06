package com.cylan.jiafeigou.activity.doorbell.addDoorbell;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.activity.doorbell.AddDoorBellHelpActivity;
import com.cylan.jiafeigou.activity.doorbell.detail.DoorBellDetailActivity;
import com.cylan.jiafeigou.activity.video.addDevice.AddVideoActivity;
import com.cylan.jiafeigou.activity.video.addDevice.ChooseDeviceFragment;
import com.cylan.jiafeigou.activity.video.addDevice.UpgradeFragement;
import com.cylan.jiafeigou.entity.MyScanResult;
import com.cylan.jiafeigou.utils.PreferenceUtil;

import java.util.ArrayList;


public class AddDoorBellActivity extends AddVideoActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        int flag = intent.getFlags();
        if (flag == DoorBellDetailActivity.TO_SET_WIFI) {
            setTitle(R.string.DEVICES_TITLE_3);
        }

        mHelpView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), AddDoorBellHelpActivity.class));
                overridePendingTransition(R.anim.slide_down_in, 0);
            }
        });
    }

    @Override
    protected void showGuide() {
        if (PreferenceUtil.getIsFirstAddDoorbell(this)) {
            showUseGuideView(R.drawable.add_doorbell_guide, true);
            PreferenceUtil.setIsFirstAddDoorbell(this, false);
        }

    }


    @Override
    protected String subStringCid(String str) {
        return str.substring(7);
    }

    @Override
    protected boolean isMyDeviceByWifi(String ssid) {
        return (ssid.replaceAll("\"", "").startsWith("DOG-ML-") && ssid.replaceAll("\"", "").length() == 13);
    }


    @Override
    protected void setTryAgain() {
        startActivity(new Intent(AddDoorBellActivity.this, AddDoorBellActivity.class));
        finish();
    }

    protected void setViewVisibly(int index, Object... obj) {
        if (titles == null)
            titles = getResources().getStringArray(R.array.addvideo_title);
        setTitle(titles[index]);
        ft = fm.beginTransaction();
        switch (index) {
            case SEARCHPAGE:
                if (mSearchDeviceFragment == null) {
                    mSearchDeviceFragment = SearchDoorBellFragment.newInstance();
                    mSearchDeviceFragment.setOnSearchDeviceListener(this);
                }
                ft.replace(R.id.container_layout, mSearchDeviceFragment, "SEARCHPAGE");
                break;
            case CHOOSEPAGE:
                if (mChooseDeviceFragment == null) {
                    mChooseDeviceFragment = ChooseDeviceFragment.newInstance((ArrayList<MyScanResult>) obj[0]);
                    mChooseDeviceFragment.setOnSelectDeviceListener(this);
                }
                ft.replace(R.id.container_layout, mChooseDeviceFragment, "CHOOSEPAGE");
                break;
            case SETPAGE:
                if (mSubmitWifiInfoFragment == null) {
                    mSubmitWifiInfoFragment = DoorbellSubmitWifiInfoFragment.newInstance((String) obj[0]);
                    mSubmitWifiInfoFragment.setOnSubmitWifiInfoListener(this);
                }
                ft.replace(R.id.container_layout, mSubmitWifiInfoFragment, "SETPAGE");
                break;
            case ADDPAGE:
                if (mBindResultFragment == null) {
                    mBindResultFragment = DoorbellBindResultFragment.newInstance();
                    mBindResultFragment.setOnCompleteButtonClickListener(this);
                }
                ft.replace(R.id.container_layout, mBindResultFragment, "ADDPAGE");
                break;
            case UPGRADE:
                if (mUpgradeFragement == null) {
                    mUpgradeFragement = UpgradeFragement.newInstance();
                    mUpgradeFragement.setOnUpgradeButtonClickListener(this);
                }
                ft.replace(R.id.container_layout, mUpgradeFragement, "UPGRADE");
                break;
        }
        ft.commitAllowingStateLoss();
    }
}