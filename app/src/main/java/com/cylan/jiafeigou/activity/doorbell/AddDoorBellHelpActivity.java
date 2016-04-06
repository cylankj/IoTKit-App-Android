package com.cylan.jiafeigou.activity.doorbell;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.BaseActivity;

public class AddDoorBellHelpActivity extends BaseActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_doorbell_ledprompt);

		setTitle(R.string.WIFI_HINT);

		setBackBtnImageRes(R.drawable.btn_quicklogin_back_selector);

		setBackBtnOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onBackPressed();

			}
		});

		findViewById(R.id.btn).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(0, R.anim.slide_down_out);
		finish();
	}
}
