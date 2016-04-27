package com.cylan.jiafeigou.activity.message;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.publicApi.Constants;
import com.cylan.publicApi.MsgpackMsg;
import com.cylan.jiafeigou.adapter.MessageDetailAdapter;
import com.cylan.jiafeigou.base.BaseActivity;
import com.cylan.jiafeigou.base.MyApp;
import com.cylan.jiafeigou.engine.ClientConstants;
import com.cylan.jiafeigou.entity.msg.MsgData;
import com.cylan.jiafeigou.entity.msg.MsgSystemData;
import com.cylan.jiafeigou.entity.msg.RspMsgHeader;
import com.cylan.jiafeigou.entity.msg.req.MsgMsgClearReq;
import com.cylan.jiafeigou.entity.msg.req.MsgMsgDeleteReq;
import com.cylan.jiafeigou.entity.msg.req.MsgMsgListReq;
import com.cylan.jiafeigou.entity.msg.req.MsgMsgSystemReq;
import com.cylan.jiafeigou.entity.msg.rsp.MsgEfamilyMsgSafeListRsp;
import com.cylan.jiafeigou.entity.msg.rsp.MsgMsgListRsp;
import com.cylan.jiafeigou.entity.msg.rsp.MsgMsgSystemRsp;
import com.cylan.jiafeigou.utils.CacheUtil;
import com.cylan.jiafeigou.utils.MyImageLoader;
import com.cylan.jiafeigou.utils.NotifyDialog;
import com.cylan.jiafeigou.utils.StringUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import support.uil.core.ImageLoader;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MessageDetailActivity extends BaseActivity implements OnClickListener, OnScrollListener, MessageDetailAdapter.ClickdelWarmListener, MessageDetailAdapter.SelectChangeListener, AdapterView.OnItemClickListener {

    public static final int DEL_PIC_MSG = 1;

    private ListView mListView;
    private LinearLayout mDelLayout;
    private Button mDelView, mDelAllView;
    private MessageDetailAdapter mAdapter;
    private String cid = null;
    private Boolean isNoMsg = false;
    private int os = 0;
    private long time = 0;
    private String key;
    private static final int CACHE_LENGTH = 100;

    private Dialog mDelDialog;
    private boolean isDeleteAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_msg_detail);
        String alias = getIntent().getStringExtra("alias");
        os = getIntent().getIntExtra("os", 0);
        cid = getIntent().getStringExtra("cid");
        setTitle(alias);
        if (os != Constants.OS_SERVER) {
            setRightBtn(R.string.DELETE, this);
        }

        initView();
        initData();

    }

    private void initView() {
        mListView = (ListView) findViewById(R.id.msg_detail_list);
        mListView.setOnItemClickListener(this);
        mListView.setOnScrollListener(this);
        mListView.setRecyclerListener(new AbsListView.RecyclerListener() {
            @Override
            public void onMovedToScrapHeap(View view) {
                ImageView img = (ImageView) view.findViewById(R.id.item_msgdetail_content_pic);
                ImageView img1 = (ImageView) view.findViewById(R.id.item_msgdetail_content_pic1);
                ImageView img2 = (ImageView) view.findViewById(R.id.item_msgdetail_content_pic2);
                if (img != null) {
                    img.setVisibility(View.INVISIBLE);
                    img.setImageDrawable(null);
                    img.setOnClickListener(null);
                }
                if (img1 != null) {
                    img1.setVisibility(View.INVISIBLE);
                    img1.setImageDrawable(null);
                    img1.setOnClickListener(null);
                }
                if (img2 != null) {
                    img2.setVisibility(View.INVISIBLE);
                    img2.setImageDrawable(null);
                    img2.setOnClickListener(null);
                }

            }
        });
        mDelLayout = (LinearLayout) findViewById(R.id.del_bar);
        mDelView = (Button) findViewById(R.id.del);
        mDelView.setOnClickListener(this);
        mDelAllView = (Button) findViewById(R.id.del_all_msg);
        mDelAllView.setOnClickListener(this);
    }


    @SuppressWarnings("unchecked")
    private void initData() {
        key = CacheUtil.getMSG_DETAIL_KEY(cid);

        List<MsgData> str = (List<MsgData>) CacheUtil.readObject(key);
        if (str != null) {
            setAdapter(str);
        }
        if (!MyApp.getIsLogin()) {
            ToastUtil.showFailToast(MessageDetailActivity.this, "(-" + MyApp.getMsgID() + ")" + getString(R.string.GLOBAL_NO_NETWORK));
        } else {
            mProgressDialog.showDialog(getString(R.string.LOADING));
            requestMessage(time);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        ImageLoader.getInstance().resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveCache();
    }

    @Override
    protected void onStop() {
        super.onStop();
        ImageLoader.getInstance().stop();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ico_back:
                finish();
                break;
            case R.id.right_btn:
                if (mAdapter == null)
                    return;
                mAdapter.setShowCheckbox(!mAdapter.isShowCheckbox());
                mAdapter.notifyDataSetChanged();
                if (mAdapter.isShowCheckbox()) {
                    mAdapter.resetSelectList();
                    mDelAllView.setTextColor(getResources().getColor(R.color.del_all_message_unenable));
                    mDelAllView.setBackgroundResource(R.drawable.bg_message_del_all_unenable);
                    setDelViewText(0);
                    setRightBtn(R.string.CANCEL);
//                    showClearNotify();
                    if (mDelLayout.getVisibility() == View.GONE) {
                        showDelBar();
                    }

                } else {
                    setRightBtn(R.string.DELETE);
                    if (mDelLayout.getVisibility() == View.VISIBLE) {
                        dismissDelBar();
                    }
                }
                break;
            case R.id.del:
                showDeleteDialog(isDeleteAll);
                isDeleteAll = false;
                break;
            case R.id.del_all_msg:
                if (mAdapter == null)
                    return;
                if (mAdapter.isAllChecked()) {
                    isDeleteAll = false;
                    mDelAllView.setTextColor(getResources().getColor(R.color.del_all_message_unenable));
                    mDelAllView.setBackgroundResource(R.drawable.bg_message_del_all_unenable);
                    mAdapter.resetSelectList();
                    setDelViewText(0);
                    mAdapter.notifyDataSetChanged();
                } else {
                    isDeleteAll = true;
                    mDelAllView.setTextColor(getResources().getColor(R.color.white));
                    mDelAllView.setBackgroundResource(R.drawable.bg_message_del_all_selector);
                    mAdapter.setSelectAll();
                    setDelViewText(mAdapter.getCheckedCount());
                    mAdapter.notifyDataSetChanged();
                }
                break;
            default:
                break;
        }

    }

    private void requestMessage(long time) {
        if (os == Constants.OS_SERVER) {
            MsgMsgSystemReq msgMsgSystemReq = new MsgMsgSystemReq();
            MyApp.wsRequest(msgMsgSystemReq.toBytes());
        } else {
            MsgMsgListReq msgMsgListReq = new MsgMsgListReq();
            msgMsgListReq.cid = cid;
            msgMsgListReq.time = time;
            MyApp.wsRequest(msgMsgListReq.toBytes());
        }
    }

    @Override
    public void handleMsgpackMsg(int msgId, MsgpackMsg.MsgHeader msgHeader) {
        mProgressDialog.dismissDialog();
        if (MsgpackMsg.CLIENT_MSGLIST_RSP == msgHeader.msgId) {
            RspMsgHeader rspMsgHeader = (RspMsgHeader) msgHeader;
            if (Constants.RETOK == rspMsgHeader.ret) {
                MsgMsgListRsp msgMsgListRsp = (MsgMsgListRsp) msgHeader;
                if (!StringUtils.isEmptyOrNull(msgMsgListRsp.cid) && cid.equals(msgMsgListRsp.cid)) {
                    if (msgMsgListRsp.data != null) {
                        List<MsgData> array = msgMsgListRsp.data;
                        long mTime = msgMsgListRsp.time;
                        if (mTime == time) {
                            if (time == 0 && mAdapter != null && mAdapter.getCount() > 0) {
                                mAdapter.clear();
                            }
                            if (array.size() > 0) {
                                time = array.get(array.size() - 1).time;
                            }
                            if (array.size() == 0) {
                                isNoMsg = true;
                            }
                            setAdapter(array);
                        }
                    }
                }
            } else {
                onError(rspMsgHeader.msg, rspMsgHeader.ret);
            }
        } else if (MsgpackMsg.CLIENT_EFAML_MSG_SAFE_LIST_RSP == msgHeader.msgId) {
            RspMsgHeader rspMsgHeader = (RspMsgHeader) msgHeader;
            if (Constants.RETOK == rspMsgHeader.ret) {
                MsgEfamilyMsgSafeListRsp msgEfamilyMsgSafeListRsp = (MsgEfamilyMsgSafeListRsp) msgHeader;
                if (!StringUtils.isEmptyOrNull(msgEfamilyMsgSafeListRsp.cid) && cid.equals(msgEfamilyMsgSafeListRsp.cid)) {
                    if (msgEfamilyMsgSafeListRsp.data != null) {
                        List<MsgData> array = msgEfamilyMsgSafeListRsp.data;
                        long mTime = msgEfamilyMsgSafeListRsp.time;
                        if (time != 0 && mTime != time) {
                            return;
                        }
                        if (time == 0 && mAdapter != null && mAdapter.getCount() > 0) {
                            mAdapter.clear();
                        }
                        if (array.size() > 0) {
                            time = array.get(array.size() - 1).time;
                        }
                        if (array.size() == 0) {
                            isNoMsg = true;
                        }
                        setAdapter(array);
                    }

                }
            } else {
                onError(rspMsgHeader.msg, rspMsgHeader.ret);
            }

        } else if (MsgpackMsg.CLIENT_MSGSYSTEM_RSP == msgHeader.msgId) {
            MsgMsgSystemRsp msgMsgSystemRsp = (MsgMsgSystemRsp) msgHeader;
            List<MsgSystemData> array = msgMsgSystemRsp.data;
            if (mAdapter != null && mAdapter.getCount() > 0) {
                mAdapter.clear();
            }
            List<MsgData> list = MsgData.parseMsgSysData(array);
            setAdapter(list);

        } else if (MsgpackMsg.CLIENT_MSGDELETE_RSP == msgHeader.msgId) {
            RspMsgHeader rspMsgHeader = (RspMsgHeader) msgHeader;
            if (Constants.RETOK == rspMsgHeader.ret) {
                if (mAdapter.getCheckedList() == null)
                    return;
                for (int i = mAdapter.getCheckedList().size() - 1; i >= 0; i--) {
                    if (mAdapter.getCheckedList().get(i)) {
                        if (mAdapter.getItem(i).push_type == ClientConstants.PUSH_TYPE_WARN) {
                            for (String str : mAdapter.getItem(i).urllist) {
                                if (!StringUtils.isEmptyOrNull(str))
                                    MyImageLoader.removeFromCache(str);
                            }
                        }
                        mAdapter.remove(mAdapter.getItem(i));
                    }
                }
                mAdapter.resetSelectList();
                mAdapter.setShowCheckbox(!mAdapter.isShowCheckbox());
                mAdapter.notifyDataSetChanged();
//                requestMessage(time);

                setRightBtn(R.string.DELETE);
                if (mDelLayout.getVisibility() == View.VISIBLE) {
                    dismissDelBar();
                }
                setResult(RESULT_OK);
            } else {
                onError(rspMsgHeader.msg, rspMsgHeader.ret);
            }
        } else if (MsgpackMsg.CLIENT_MSGCLEAR_RSP == msgHeader.msgId) {
            setResult(RESULT_OK);
            finish();
        }

    }

    //is being scrolled
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
            //
            if (view.getLastVisiblePosition() == view.getCount() - 1) {
                if (!isNoMsg)
                    if (os != Constants.OS_SERVER) {
                        mProgressDialog.showDialog(getString(R.string.LOADING));
                        requestMessage(time);
                    }
            }
        }
    }

    //has been scrolled
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }

    public void onError(String msg, int ret) {
        mProgressDialog.dismissDialog();
        // isRequesting = false;
        if (ret == 22) {
            NotifyDialog dialog = new NotifyDialog(this);
            dialog.hideNegButton();
            dialog.show(msg, ret);
            dialog.setCancelable(false);
        } else {
            ToastUtil.showFailToast(this, msg);

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        if (requestCode == DEL_PIC_MSG) {
            if (mAdapter != null) {
                for (int i = 0; i < mAdapter.getCount(); i++) {
                    MsgData info = mAdapter.getItem(i);
                    if (String.valueOf(info.time).equals(data.getStringExtra("time"))) {
                        mAdapter.remove(info);

                        break;
                    }
                }
                mAdapter.notifyDataSetChanged();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void saveCache() {
        if (mAdapter == null)
            return;
        List<MsgData> l = new ArrayList<>();
        for (int i = 0; i < (mAdapter.getCount() >= CACHE_LENGTH ? CACHE_LENGTH : mAdapter.getCount()); i++) {
            l.add(mAdapter.getItem(i));
        }
        CacheUtil.saveObject((Serializable) l, key);
    }

    private void setAdapter(List<MsgData> str) {
        if (mAdapter == null) {
            //解决了数据不对的情况，不然list的长度会增加一倍，导致全部没选的时候，删除按钮的状态还不变回不能点击
            mAdapter = new MessageDetailAdapter(MessageDetailActivity.this, str);
            mAdapter.addDelListener(this);
            mAdapter.setOnSelectChangeListener(this);
            mAdapter.setCid(cid);
            mListView.setAdapter(mAdapter);
        } else {
            mAdapter.addAll(str, isDeleteAll);
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void delPic(int pos, int index) {
        startActivityForResult(
                new Intent(this, MessagePicturesActivity.class).putExtra(MessagePicturesActivity.TIME, mAdapter.getItem(pos)).putExtra("index", index), DEL_PIC_MSG);

    }

    private void showDelBar() {
        mDelLayout.setVisibility(View.VISIBLE);
        ObjectAnimator anim = ObjectAnimator.ofFloat(mDelLayout, "translationY", mDelLayout.getHeight(), 0).setDuration(100);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
            }
        });
        anim.start();
    }

    private void dismissDelBar() {
        ObjectAnimator anim = ObjectAnimator.ofFloat(mDelLayout, "translationY", 0, mDelLayout.getHeight()).setDuration(100);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mDelLayout.setVisibility(View.GONE);
            }
        });
        anim.start();
    }


    @Override
    public void selectChange(int num) {
        setDelViewText(num);
        if (!mAdapter.isAllChecked()) {
            mDelAllView.setBackgroundResource(R.drawable.bg_message_del_all_unenable);
            mDelAllView.setTextColor(getResources().getColor(R.color.del_all_message_unenable));
        } else {
            mDelAllView.setBackgroundResource(R.drawable.bg_message_del_all_selector);
            mDelAllView.setTextColor(getResources().getColor(R.color.white));
        }
    }

    private void setDelViewText(int num) {
        mDelView.setText(getString(R.string.DELETE));
        if (num == 0) {
            mDelView.setEnabled(false);
        } else {
            mDelView.setEnabled(true);
        }

    }

    private void showDeleteDialog(final boolean isDeleteAll) {
        mDelDialog = new Dialog(this, R.style.func_dialog);
        View content = View.inflate(this, R.layout.dialog_deldevice, null);
        TextView cancel = (TextView) content.findViewById(R.id.btn_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDelDialog.dismiss();
            }
        });
        Button btn = (Button) content.findViewById(R.id.del);
        btn.setText(R.string.DELETE);
        //只能删除加载出来的所有信息，不能彻底清空这个设备的所有信息,所以还是clearMsg
        if (isDeleteAll) {
            btn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDelDialog.dismiss();
                    if (mAdapter.isAllChecked()) {
                        mProgressDialog.showDialog(getString(R.string.DELETEING));
                        MsgMsgClearReq msgMsgClearReq = new MsgMsgClearReq();
                        msgMsgClearReq.cid = cid;
                        MyApp.wsRequest(msgMsgClearReq.toBytes());
                    } else {
                        if (mAdapter.getCheckedList() == null)
                            return;
                        if (mAdapter.getCount() == 0)
                            return;
                        ArrayList<Long> al = new ArrayList<>();
                        for (int i = 0; i < mAdapter.getCount(); i++) {
                            if (!mAdapter.getCheckedList().get(i)) {
                                al.add(mAdapter.getItem(i).time);
                            }
                        }
                        mProgressDialog.showDialog(getString(R.string.DELETEING));
                        MsgMsgDeleteReq msgMsgDeleteReq = new MsgMsgDeleteReq();
                        msgMsgDeleteReq.cid = cid;
                        msgMsgDeleteReq.timelist = al;
                        msgMsgDeleteReq.delete = -1;    //反向删除
                        MyApp.wsRequest(msgMsgDeleteReq.toBytes());
                    }
                }
            });
        } else {
            btn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDelDialog.dismiss();
                    if (mAdapter.getCheckedList() == null)
                        return;
                    if (mAdapter.getCount() == 0)
                        return;
                    ArrayList<Long> al = new ArrayList<>();
                    for (int i = 0; i < mAdapter.getCheckedList().size(); i++) {
                        if (mAdapter.getCheckedList().get(i)) {
                            al.add(mAdapter.getItem(i).time);
                        }
                    }
                    mProgressDialog.showDialog(getString(R.string.DELETEING));
                    MsgMsgDeleteReq msgMsgDeleteReq = new MsgMsgDeleteReq();
                    msgMsgDeleteReq.cid = cid;
                    msgMsgDeleteReq.timelist = al;
                    msgMsgDeleteReq.delete = 1;
                    MyApp.wsRequest(msgMsgDeleteReq.toBytes());

                }
            });
        }

        mDelDialog.setContentView(content);
        mDelDialog.setCanceledOnTouchOutside(true);
        mDelDialog.show();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mAdapter.isShowCheckbox()) {
            ((CheckBox) view.findViewById(R.id.select)).setChecked(!mAdapter.getCheckedList().get(position));
            mAdapter.getCheckedList().set(position, ((CheckBox) view.findViewById(R.id.select)).isChecked());
            selectChange(mAdapter.getCheckedCount());
        }
    }

    @Override
    public void disconnectServer() {
        if (mProgressDialog.isShow()) {
            mProgressDialog.dismissDialog();
            ToastUtil.showFailToast(MessageDetailActivity.this, "(-" + MyApp.getMsgID() + ")" + getString(R.string.GLOBAL_NO_NETWORK));
        }
    }
}
