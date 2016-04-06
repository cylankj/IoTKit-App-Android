package com.cylan.jiafeigou.activity.efamily;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.publicApi.Constants;
import com.cylan.publicApi.MsgpackMsg;
import com.cylan.jiafeigou.base.BaseActivity;
import com.cylan.jiafeigou.base.MyApp;
import com.cylan.jiafeigou.engine.ClientConstants;
import com.cylan.jiafeigou.entity.RelatedbellBean;
import com.cylan.jiafeigou.entity.msg.MsgCidData;
import com.cylan.jiafeigou.entity.msg.RspMsgHeader;
import com.cylan.jiafeigou.entity.msg.req.MsgClientEfamlGetBellsReq;
import com.cylan.jiafeigou.entity.msg.req.MsgClientEfamlSetBellReq;
import com.cylan.jiafeigou.entity.msg.rsp.MsgCidlistRsp;
import com.cylan.jiafeigou.entity.msg.rsp.MsgClientEfamlGetBellsRsp;
import com.cylan.jiafeigou.utils.NotifyDialog;
import com.cylan.jiafeigou.utils.ProgressDialogUtil;

import java.util.ArrayList;
import java.util.List;

public class EfamilyRelatedBellActivity extends BaseActivity {

    private ListView mListView;
    private ChooseBellAdapter mAdapter;
    private MsgCidData mIHomeInfo;
    private ProgressDialogUtil mProgress;
    private boolean isChange;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_voice);
        setTitle(R.string.EFAMILY_RELEVANCE_DOOR);
        setBackBtnOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mIHomeInfo = (MsgCidData) getIntent().getSerializableExtra(ClientConstants.CIDINFO);
        mListView = (ListView) findViewById(R.id.voice_list);

        mAdapter = new ChooseBellAdapter(this);
        mAdapter.addAll(MsgCidlistRsp.getInstance().getDoorbellList());
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position <= mAdapter.getCount() - 1) {
                    if (mAdapter.getItem(position).isChoose()) {
                        showUpgradeDialog(position);
                    } else {
                        mAdapter.getItem(position).setChoose(true);
                        mAdapter.notifyDataSetChanged();
                        isChange = true;
                    }
                }
            }
        });

        View emptyView = LayoutInflater.from(this).inflate(R.layout.layout_no_record, null);

        emptyView.setLayoutParams(
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        ((ViewGroup) mListView.getParent()).addView(emptyView);
        emptyView.setVisibility(View.GONE);
        mListView.setEmptyView(emptyView);

        mProgress = new ProgressDialogUtil(this);

        getData();

    }

    private void getData() {
        MsgClientEfamlGetBellsReq  req = new MsgClientEfamlGetBellsReq(mIHomeInfo.cid);
        MyApp.wsRequest(req.toBytes());
    }

    private class ChooseBellAdapter extends ArrayAdapter<RelatedbellBean> {

        public ChooseBellAdapter(Context context) {
            super(context, 0);
        }

        public void setSelect(int pos, boolean boo) {
            getItem(pos).setChoose(boo);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(getContext(), R.layout.single_text_list_item, null);
            }
            RelatedbellBean item = getItem(position);
            ((TextView) convertView).setText(item.getInfo().mName());

            if (item.isChoose()) {
                ((TextView) convertView).setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ico_wifi_selected, 0);
            } else {
                ((TextView) convertView).setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            }
            return convertView;
        }
    }


    private void showUpgradeDialog(final int pos) {
        final NotifyDialog mDialog = new NotifyDialog(this);
        mDialog.setButtonText(R.string.OK, R.string.CANCEL);
        mDialog.show(String.format(getString(R.string.EFAMILY_RELEVANCE_ALARE), mAdapter.getItem(pos).getInfo().mName(), mIHomeInfo.mName()), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
                mAdapter.setSelect(pos, !mAdapter.getItem(pos).isChoose());
                mAdapter.notifyDataSetChanged();
                isChange = true;
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });
    }

    @Override
    public void handleMsgpackMsg(int msg, MsgpackMsg.MsgHeader msgpackMsg) {
        if (MsgpackMsg.CLIENT_EFAML_SET_BELL_RSP == msgpackMsg.msgId) {
        } else if (MsgpackMsg.CLIENT_EFAML_GET_BELLS_RSP == msgpackMsg.msgId) {
            RspMsgHeader rsp = (RspMsgHeader) msgpackMsg;
            if (rsp.caller.equals(mIHomeInfo.cid)) {
//                mProgress.dismissProgressDlg();
                if (rsp.ret == Constants.RETOK) {
                    initRelated(((MsgClientEfamlGetBellsRsp) rsp).array);
                }
            }
        }
    }

    private void setRelated() {
        MsgClientEfamlSetBellReq req = new MsgClientEfamlSetBellReq(mIHomeInfo.cid);
        List<String> list = new ArrayList<>();
        for (int i = 0, count = mAdapter.getCount(); i < count; i++) {
            if (mAdapter.getItem(i).isChoose()) {
                list.add(mAdapter.getItem(i).getInfo().cid);
            }
        }
        req.list = list;
        MyApp.wsRequest(req.toBytes());
    }

    private void initRelated(List<String> list) {
        if (list == null || list.isEmpty())
            return;
        for (int i = 0, count = mAdapter.getCount(); i < count; i++) {
            for (String cid : list) {
                if (mAdapter.getItem(i).getInfo().cid.equals(cid)) {
                    mAdapter.getItem(i).setChoose(true);
                }
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        if (isChange)
            setRelated();
        super.onBackPressed();
    }
}
