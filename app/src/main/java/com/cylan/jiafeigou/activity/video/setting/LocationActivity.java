package com.cylan.jiafeigou.activity.video.setting;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.adapter.MySelectItmAdapter;
import com.cylan.jiafeigou.base.BaseActivity;
import com.cylan.jiafeigou.engine.ClientConstants;
import com.cylan.jiafeigou.entity.SelectItem;
import com.cylan.jiafeigou.entity.msg.rsp.MsgCidlistRsp;

public class LocationActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private MySelectItmAdapter mAdapter;

    private int mChooseVoiceIndex = 0;

    private static final int DEFAULT_LIST_POSITION = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_voice);
        int scenceID = getIntent().getIntExtra(ClientConstants.SELECT_INDEX, 0);
        ListView mVoiceListView = (ListView) findViewById(R.id.voice_list);
        mVoiceListView.setOnItemClickListener(this);
        setTitle(R.string.LOCATION_SETTING);
        setBackBtnOnClickListener(this);

        mAdapter = new MySelectItmAdapter(this);

        MsgCidlistRsp rsp = MsgCidlistRsp.getInstance();
        if (rsp != null) {
            for (int i = 0; i < rsp.data.size(); i++) {
                SelectItem item = new SelectItem();
                item.value = rsp.data.get(i).scene_name;
                if (rsp.data.get(i).scene_id == scenceID) {
                    item.isSelected = true;
                    mChooseVoiceIndex = i;
                } else {
                    item.isSelected = false;
                }
                mAdapter.add(item);
            }
        }
        mVoiceListView.setAdapter(mAdapter);
        mAdapter.index = mChooseVoiceIndex;
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
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        SelectItem item = mAdapter.getItem(position);
        if (!item.isSelected)
            item.isSelected = !item.isSelected;
        if (item.isSelected) {
            mAdapter.index = position;
            mChooseVoiceIndex = position;
        } else {
            mAdapter.index = DEFAULT_LIST_POSITION;
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        Intent intent = getIntent();
        MsgCidlistRsp rsp = MsgCidlistRsp.getInstance();
        if (rsp != null && rsp.data.size() > mChooseVoiceIndex) {
            intent.putExtra(ClientConstants.SELECT_INDEX, rsp.data.get(mChooseVoiceIndex).scene_id);
        }
        setResult(RESULT_OK, intent);
        super.onBackPressed();

    }


}
