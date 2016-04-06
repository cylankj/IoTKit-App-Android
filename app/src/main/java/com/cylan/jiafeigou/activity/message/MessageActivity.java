package com.cylan.jiafeigou.activity.message;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import com.cylan.jiafeigou.R;
import com.cylan.publicApi.Constants;
import com.cylan.publicApi.DswLog;
import com.cylan.publicApi.JniPlay;
import com.cylan.publicApi.MsgpackMsg;
import com.cylan.jiafeigou.adapter.MessageAdapter;
import com.cylan.jiafeigou.base.BaseActivity;
import com.cylan.jiafeigou.base.MyApp;
import com.cylan.jiafeigou.entity.msg.MsgData;
import com.cylan.jiafeigou.entity.msg.RspMsgHeader;
import com.cylan.jiafeigou.entity.msg.req.MsgMsgClearReq;
import com.cylan.jiafeigou.entity.msg.req.MsgMsgCountReq;
import com.cylan.jiafeigou.entity.msg.req.MsgMsgIgnoreReq;
import com.cylan.jiafeigou.entity.msg.rsp.MsgMsgClearRsp;
import com.cylan.jiafeigou.entity.msg.rsp.MsgMsgCountRsp;
import com.cylan.jiafeigou.utils.CacheUtil;
import com.cylan.jiafeigou.utils.NotifyDialog;
import com.cylan.jiafeigou.utils.StringUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.RefreshListView;
import com.cylan.jiafeigou.widget.RefreshListView.OnRefreshListener;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;


public class MessageActivity extends BaseActivity implements OnClickListener, OnItemClickListener, OnRefreshListener, OnItemLongClickListener {

    private static final int RESULT_TO_DEL_MSG = 0x01;

    private RefreshListView mMsgListView;

    private NotifyDialog notifyDlg;

    void showNotify(String msg, int error) {
        if (notifyDlg == null) {
            notifyDlg = new NotifyDialog(this);
            notifyDlg.hideNegButton();
        }
        if (error == 22) {
            notifyDlg.setCancelable(false);
        } else {
            notifyDlg.setCancelable(true);
        }
        if (!notifyDlg.isShowing())
            notifyDlg.show(msg, error);
    }

    private MessageAdapter mAdapter;

    private int clickPos = -1;

    private MessageDelDialog mDialog;

    @Override
    @SuppressWarnings("unchecked")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_msg);
        setTitle(R.string.MESSAGE);
        setRightBtn(R.string.IGNORE, this);
        mMsgListView = (RefreshListView) findViewById(R.id.msg_list);
        mMsgListView.setOnItemClickListener(this);
        mMsgListView.setOnItemLongClickListener(this);
        mMsgListView.setOnRefreshListener(this);

        try {
            List<MsgData> str = (List<MsgData>) CacheUtil.readObject(CacheUtil.getMSG_CENTER_KEY());
            if (str != null) {
                setAdapter(str);
            }
            if (!MyApp.getIsLogin()) {
                ToastUtil.showFailToast(MessageActivity.this, "(-" + MyApp.getMsgID() + ")" + getString(R.string.GLOBAL_NO_NETWORK));
                mMsgListView.setRefreshEnabled(false);
            } else {
                mProgressDialog.showDialog(R.string.LOADING);
                mMsgListView.setRefreshEnabled(true);
                // mMsgListView.prepareForRefresh();

                toStringList();
            }
        } catch (Exception e) {
            DswLog.ex(e.toString());
        }

    }


    @Override
    protected void onPause() {
        super.onPause();
        if (mAdapter != null && mAdapter.mList != null)
            CacheUtil.saveObject((Serializable) mAdapter.mList, CacheUtil.getMSG_CENTER_KEY());
    }

    void toStringList() {
        if (!MyApp.getIsLogin()) {
            ToastUtil.showFailToast(MessageActivity.this, "(-" + MyApp.getMsgID() + ")" + getString(R.string.GLOBAL_NO_NETWORK));
            return;
        }
        MsgMsgCountReq msgMsgCountReq = new MsgMsgCountReq();
        JniPlay.SendBytes(msgMsgCountReq.toBytes());
        DswLog.i("send MsgMsgCountReq-->" + msgMsgCountReq.toString());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.right_btn:
                if (MyApp.getIsLogin()) {
                    mProgressDialog.showDialog(getString(R.string.pb_request));
                    final MsgMsgIgnoreReq msgMsgIgnoreReq = new MsgMsgIgnoreReq();
                    MyApp.wsRequest(msgMsgIgnoreReq.toBytes());
                    DswLog.i("send MsgMsgIgnoreReq msg-->" + msgMsgIgnoreReq.toString());
                }
                break;
            default:
                break;
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        toStringList();
    }

    @Override
    public void handleMsgpackMsg(int msg, MsgpackMsg.MsgHeader msgpackMsg) {
        mProgressDialog.dismissDialog();
        if (MsgpackMsg.CLIENT_MSGCOUNT_RSP == msgpackMsg.msgId) {
            RspMsgHeader rspMsgHeader = (RspMsgHeader) msgpackMsg;
            if (rspMsgHeader.ret == Constants.RETOK) {
                MsgMsgCountRsp msgMsgCountRsp = (MsgMsgCountRsp) msgpackMsg;
                onSuc(msgMsgCountRsp);
            } else {
                showNotify(rspMsgHeader.msg, rspMsgHeader.ret);
            }
        } else if (MsgpackMsg.CLIENT_MSGIGNORE_RSP == msgpackMsg.msgId) {
            RspMsgHeader rspMsgHeader = (RspMsgHeader) msgpackMsg;
            if (rspMsgHeader.ret == Constants.RETOK) {
                clearHasReader();
            } else {
                showNotify(rspMsgHeader.msg, rspMsgHeader.ret);
            }
        } else if (MsgpackMsg.CLIENT_MSGCLEAR_RSP == msgpackMsg.msgId) {
            RspMsgHeader rspMsgHeader = (RspMsgHeader) msgpackMsg;
            if (Constants.RETOK == rspMsgHeader.ret) {
                MsgMsgClearRsp msgMsgClearRsp = (MsgMsgClearRsp) msgpackMsg;
                String cid = msgMsgClearRsp.cid;
                if (mAdapter != null) {
                    for (int i = 0; i < mAdapter.getCount(); i++) {
                        if (mAdapter.mList.get(i).cid.equals(cid)) {
                            mAdapter.mList.remove(i);
                            mAdapter.notifyDataSetChanged();
                            break;
                        }
                    }
                }
                CacheUtil.remove(CacheUtil.getMSG_DETAIL_KEY(cid));
                if (mAdapter.getCount() == 0) {
                    findViewById(R.id.msg_info).setVisibility(View.VISIBLE);
                    mMsgListView.setVisibility(View.GONE);
                }
            } else {
                showNotify(rspMsgHeader.msg, rspMsgHeader.ret);
            }
        } else if (MsgpackMsg.CLIENT_MSGDELETE_RSP == msgpackMsg.msgId) {
            RspMsgHeader rspMsgHeader = (RspMsgHeader) msgpackMsg;
            if (Constants.RETOK == rspMsgHeader.ret) {
                toStringList();
                if (mAdapter.getCount() == 0) {
                    findViewById(R.id.msg_info).setVisibility(View.VISIBLE);
                    mMsgListView.setVisibility(View.GONE);
                }
            }
        }
    }

    private void clearHasReader() {
        if (mAdapter != null) {
            for (int i = 0; i < mAdapter.getCount(); i++) {
                mAdapter.getItem(i).count = 0;
            }
            mAdapter.notifyDataSetChanged();
        }
    }

    private void onSuc(MsgMsgCountRsp msgpackMsg) {
        mMsgListView.onRefreshComplete();
        if (mAdapter != null) {
            mAdapter.mList.clear();
            mAdapter = null;
        }
//            JSONArray json = obj.getJSONArray("list");
//            List<MsgInfo> list = MsgInfo.parseJson(this, json.toString());
        List<MsgData> data = msgpackMsg.data;
        setAdapter(data);
        if (mAdapter.getCount() == 0) {
            findViewById(R.id.msg_info).setVisibility(View.VISIBLE);
            mMsgListView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position == 0)
            return;
        if (mAdapter != null) {
            mAdapter.getItem(position - 1).count = 0;
            mAdapter.notifyDataSetChanged();
            clickPos = position - 1;
            startActivityForResult(new Intent(MessageActivity.this, MessageDetailActivity.class).putExtra("cid", mAdapter.getItem(position - 1).cid)
                    .putExtra("alias", StringUtils.isEmptyOrNull(mAdapter.getItem(position - 1).alias) ? mAdapter.getItem(position - 1).cid : mAdapter.getItem(position - 1).alias).putExtra("os", mAdapter.getItem(position - 1).os), RESULT_TO_DEL_MSG);

        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

        if (mAdapter != null) {
            if (mAdapter.getItem(position - 1).os == Constants.OS_SERVER) {
                return true;
            }
            if (mDialog == null) {
                mDialog = new MessageDelDialog(this);
            }
            mDialog.setListenter(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDialog.dismiss();
                    showDelMessageDialog(position - 1);
                }
            });
            mDialog.show();
            Vibrator vib = (Vibrator) this.getSystemService(Service.VIBRATOR_SERVICE);
            vib.vibrate(50);//

        }
        // must be true,or after onItemLongClick , onItemClick will still run .
        return true;
    }

    @Override
    public void onRefresh() {
        if (!MyApp.getIsLogin()) {
            ToastUtil.showFailToast(MessageActivity.this, "(-" + MyApp.getMsgID() + ")" + getString(R.string.GLOBAL_NO_NETWORK));
            mMsgListView.onRefreshComplete();
        } else {
            toStringList();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        if (requestCode == RESULT_TO_DEL_MSG) {
            if (clickPos == -1 || mAdapter == null || mAdapter.mList.size() == 0)
                return;
            toStringList();

            if (mAdapter.getCount() == 0) {
                findViewById(R.id.msg_info).setVisibility(View.VISIBLE);
                mMsgListView.setVisibility(View.GONE);
            }
        }
    }

    private void setAdapter(List<MsgData> list) {
        Collections.sort(list);
        mAdapter = new MessageAdapter(this, list);
        mMsgListView.setAdapter(mAdapter);
    }

    private void showDelMessageDialog(final int pos) {
        final NotifyDialog dialog = new NotifyDialog(this);
        dialog.setButtonText(R.string.DELETE, R.string.CANCEL);
        dialog.setPosRedTheme(R.drawable.bg_dialogdel_selector, getResources().getColor(R.color.mycount_not_set));
        dialog.show(R.string.SURE_DELETE, new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                switch (v.getId()) {

                    case R.id.confirm:
                        dialog.dismiss();
                        clear(pos);
                        break;
                    case R.id.cancel:
                        dialog.dismiss();
                        break;
                }

            }
        }, null);

    }

    private void clear(int pos) {
        mProgressDialog.showDialog(R.string.DELETEING);
        MsgMsgClearReq msgMsgClearReq = new MsgMsgClearReq();
        msgMsgClearReq.cid = mAdapter.getItem(pos).cid;
        MyApp.wsRequest(msgMsgClearReq.toBytes());
        DswLog.i("send MsgMsgClearReq msg-->" + msgMsgClearReq.toString());
    }


    @Override
    public void disconnectServer() {
        if (mProgressDialog.isShow()) {
            mProgressDialog.dismissDialog();
            ToastUtil.showFailToast(MessageActivity.this, "(-" + MyApp.getMsgID() + ")" + getString(R.string.GLOBAL_NO_NETWORK));
        }

    }
}