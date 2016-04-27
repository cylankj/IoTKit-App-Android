package com.cylan.jiafeigou.activity.video.setting;

import android.app.Dialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.InputType;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.support.DswLog;
import com.cylan.jiafeigou.adapter.MySelectItmAdapter;
import com.cylan.jiafeigou.base.BaseActivity;
import com.cylan.jiafeigou.engine.ClientConstants;
import com.cylan.jiafeigou.entity.SelectItem;
import com.cylan.entity.AlarmInfo;

public class DeviceVoiceActivity extends BaseActivity implements OnClickListener, OnItemClickListener {


    private MySelectItmAdapter mAdapter;
    private LinearLayout mVoiceLengthLayout;
    private TextView mVoiceLengthView;

    private AlarmInfo info = null;

    private static final int DEFAULT_LIST_POSITION = -1;
    private static final int LIST_POSITION0 = 0;
    private static final int LIST_POSITION1 = 1;
    private static final int LIST_POSITION2 = 2;

    private String[] timeschooser;
    private Dialog mTimeDialog;
    private NumberPicker mSecPicker;
    private NumberPicker mPicker1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_device_voice);
        info = getIntent().getParcelableExtra(ClientConstants.ALARMINFO);

        setTitle(R.string.SOUNDS);
        mVoiceLengthLayout = (LinearLayout) findViewById(R.id.layout_warm_voice);
        mVoiceLengthLayout.setOnClickListener(this);
        mVoiceLengthView = (TextView) findViewById(R.id.warm_voice_length);
        ListView mVoiceListView = (ListView) findViewById(R.id.voice_list);

        setBackBtnOnClickListener(this);
        mVoiceListView.setOnItemClickListener(this);
        findViewById(R.id.device_voice_info).setVisibility(View.VISIBLE);

        String[] list = getResources().getStringArray(R.array.device_set_voice_name);
        timeschooser = this.getResources().getString(R.string.warm_voice_length_values).split("\\|");
        mVoiceLengthView.setText(timeschooser[info.sound_long > 0 ? info.sound_long - 1 : 0]);

        mAdapter = new MySelectItmAdapter(this);
        for (int i = 0; i < list.length; i++) {
            SelectItem item = new SelectItem();
            item.value = list[i];
            item.isSelected = (i == info.sound);
            mAdapter.add(item);
        }
        mVoiceListView.setAdapter(mAdapter);
        mAdapter.index = info.sound;
        mAdapter.notifyDataSetChanged();


        if (info.sound != LIST_POSITION0)
            mVoiceLengthLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ico_back:
                onBackPressed();
                break;
            case R.id.layout_warm_voice:
                pickupTime();
                break;
            case R.id.confirm:
                mTimeDialog.dismiss();
                int choose = mSecPicker.getValue();
                info.sound_long = choose + 1;
                mVoiceLengthView.setText(timeschooser[choose]);
                break;
            default:
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        SelectItem item = mAdapter.getItem(position);
        if (!item.isSelected)
            item.isSelected = !item.isSelected;
        if (item.isSelected) {
            mAdapter.index = position;
            info.sound = position;
            MediaPlayer player;
            switch (position) {
                case LIST_POSITION0:
                    mVoiceLengthLayout.setVisibility(View.GONE);
                    break;
                case LIST_POSITION1:
                    player = MediaPlayer.create(DeviceVoiceActivity.this, R.raw.wangwang_voice);
                    mVoiceLengthLayout.setVisibility(View.VISIBLE);
                    player.start();
                    break;
                case LIST_POSITION2:
                    player = MediaPlayer.create(DeviceVoiceActivity.this, R.raw.warm_voice);
                    mVoiceLengthLayout.setVisibility(View.VISIBLE);
                    player.start();
                    break;
                default:
                    break;
            }


        } else {
            mAdapter.index = DEFAULT_LIST_POSITION;
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        Intent intent = getIntent();
        intent.putExtra(ClientConstants.ALARMINFO, info);
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }

    private void pickupTime() {
        try {
            if (mTimeDialog == null) {
                mTimeDialog = new Dialog(this, R.style.func_custom_dialog);
                View content = View.inflate(this, R.layout.dialog_warm_time, null);
                TextView cancel = (TextView) content.findViewById(R.id.cancle);
                mPicker1 = (NumberPicker) content.findViewById(R.id.continuous);
                mSecPicker = (NumberPicker) content.findViewById(R.id.sec);

                cancel.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        mTimeDialog.dismiss();
                    }
                });

                TextView confirm = (TextView) content.findViewById(R.id.confirm);
                confirm.setOnClickListener(this);

                mTimeDialog.setContentView(content);
                mTimeDialog.setOwnerActivity(this);
                mTimeDialog.setCanceledOnTouchOutside(true);
            }

            String[] str = mPicker1.getDisplayedValues();
            if (str == null) {
                str = new String[]{this.getResources().getString(R.string.REPEAT_PLAY)};
                setDatePicker(mPicker1, str, 0, str.length - 1);
                mPicker1.setValue(0);
            }

            String[] times = mSecPicker.getDisplayedValues();
            if (times == null) {
                times = timeschooser;
                setDatePicker(mSecPicker, times, 0, times.length - 1);
            }
            mSecPicker.setValue(info.sound_long > 0 ? info.sound_long - 1 : 0);
            mTimeDialog.show();
            WindowManager windowManager = getWindowManager();
            Display display = windowManager.getDefaultDisplay();
            WindowManager.LayoutParams lp = mTimeDialog.getWindow().getAttributes();
            lp.width = display.getWidth(); // set width
            mTimeDialog.getWindow().setAttributes(lp);
        } catch (Exception e) {
            DswLog.ex(e.toString());
        }

    }

    private void setDatePicker(NumberPicker view, String[] obj, int min, int max) {
        view.setDisplayedValues(obj);
        view.setMinValue(min);
        view.getChildAt(0).setFocusable(false);
        view.setMaxValue(max);
        view.getChildAt(0).setFocusableInTouchMode(false);
        view.getChildAt(0).setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
            }
        });
        ((EditText) view.getChildAt(0)).setInputType(InputType.TYPE_NULL);

    }
}
