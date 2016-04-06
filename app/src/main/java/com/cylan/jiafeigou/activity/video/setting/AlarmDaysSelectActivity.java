package com.cylan.jiafeigou.activity.video.setting;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.adapter.MySelectItmAdapter;
import com.cylan.jiafeigou.base.BaseActivity;
import com.cylan.jiafeigou.engine.ClientConstants;
import com.cylan.jiafeigou.entity.SelectItem;
import com.cylan.utils.entity.AlarmInfo;

public class AlarmDaysSelectActivity extends BaseActivity implements OnClickListener, OnItemClickListener {

	private MySelectItmAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_device_voice);
		setTitle(R.string.REPEAT);
		setBackBtnOnClickListener(this);
		int days = getIntent().getIntExtra(ClientConstants.ALARM_WEEKS, 0);

		ListView mDaysListView = (ListView) findViewById(R.id.voice_list);
		mAdapter = new MySelectItmAdapter(this);
		mAdapter.setIsRepeatSelect(true);
		String[] list = getResources().getStringArray(R.array.weeks);
		for (int i = 0; i < list.length; i++) {
			SelectItem item = new SelectItem();
			item.value = list[i];
			item.isSelected = AlarmInfo.isSelectedDay(days, i);
			mAdapter.add(item);
		}
		mDaysListView.setAdapter(mAdapter);
		mAdapter.notifyDataSetChanged();
		mDaysListView.setOnItemClickListener(this);

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
		StringBuffer daysVal = new StringBuffer();
		for (int i = 0; i < mAdapter.getCount(); i++) {
			daysVal.append(mAdapter.getItem(i).isSelected ? 1 : 0);
		}
		int days = Integer.parseInt(daysVal.toString(), 2);
		setResult(RESULT_OK, getIntent().putExtra(ClientConstants.ALARM_WEEKS, days));
		super.onBackPressed();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		SelectItem item = mAdapter.getItem(position);
		item.isSelected = !item.isSelected;

		mAdapter.notifyDataSetChanged();

	}
}