package com.cylan.jiafeigou.activity.video.setting;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.adapter.TimeZoneAdapter;
import com.cylan.jiafeigou.base.BaseActivity;
import com.cylan.jiafeigou.engine.ClientConstants;
import com.cylan.jiafeigou.entity.TimeZoneBean;

import java.util.List;

public class TimeZoneActivity extends BaseActivity implements OnItemClickListener, OnClickListener {

    private TimeZoneAdapter mAdapter;

    private int pos = -1;

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timezone);
        setTitle(R.string.TIMEZONE_CHOOSE);
        setBackBtnOnClickListener(this);
        List<TimeZoneBean> mList = (List<TimeZoneBean>) getIntent().getSerializableExtra(ClientConstants.TIMEZONE_DATA);
        pos = getIntent().getIntExtra(ClientConstants.TIMEZONE_SET_POS, -1);
        ListView mListView = (ListView) findViewById(R.id.timezone_lsit);
        mListView.setOnItemClickListener(this);

        mAdapter = new TimeZoneAdapter(this, mList);
        mListView.setAdapter(mAdapter);

        if (pos != -1 && mAdapter.getCount() > pos) {
            mAdapter.getItem(pos).setIsChecked(true);
            mAdapter.pos = pos;
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        TimeZoneBean item = mAdapter.getItem(position);
        if (!item.getIsChecked())
            item.setIsChecked(!item.getIsChecked());
        if (item.getIsChecked()) {
            mAdapter.pos = position;
        } else {
            mAdapter.pos = -1;
        }
        pos = mAdapter.pos;
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ico_back:
                onBackPressed();
                break;

            default:
                break;
        }

    }

    @Override
    public void onBackPressed() {

        Intent intent = getIntent();
        intent.putExtra(ClientConstants.K_TIMEZONE, pos);
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }
}