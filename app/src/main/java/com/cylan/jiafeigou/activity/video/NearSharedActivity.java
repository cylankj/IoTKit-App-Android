package com.cylan.jiafeigou.activity.video;

import android.app.Dialog;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.publicApi.Constants;
import com.cylan.support.DswLog;
import com.cylan.publicApi.MsgpackMsg;
import com.cylan.jiafeigou.adapter.ShareAdapter;
import com.cylan.jiafeigou.base.BaseActivity;
import com.cylan.jiafeigou.base.MyApp;
import com.cylan.jiafeigou.engine.ClientConstants;
import com.cylan.jiafeigou.entity.msg.MsgCidData;
import com.cylan.jiafeigou.entity.msg.RspMsgHeader;
import com.cylan.jiafeigou.entity.msg.req.MsgShareListReq;
import com.cylan.jiafeigou.entity.msg.req.MsgShareReq;
import com.cylan.jiafeigou.entity.msg.req.MsgUnshareReq;
import com.cylan.jiafeigou.entity.msg.rsp.MsgShareListRsp;
import com.cylan.jiafeigou.entity.msg.rsp.MsgShareRsp;
import com.cylan.jiafeigou.entity.msg.rsp.MsgUnshareRsp;
import com.cylan.jiafeigou.utils.NotifyDialog;
import com.cylan.jiafeigou.utils.PermissionChecker;
import com.cylan.jiafeigou.utils.PreferenceUtil;
import com.cylan.jiafeigou.utils.StringUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.Utils;
import com.tencent.stat.StatService;

public class NearSharedActivity extends BaseActivity implements OnClickListener, OnItemLongClickListener {

    private static final int MAX_SHARE_COUNT = 5;

    private static final int RESULT_CHOOSE_CONTACTS = 0x01;

    private static final int TCP_SHARELIST = 0x01;
    private static final int TCP_UNSHARE = 0x02;
    private static final int TCP_SHARE = 0x03;

    private EditText mAccountView;
    private TextView mAddView;
    private ImageView mSearchView;
    private LinearLayout mSharedLayout;
    private ListView mSharedListView;
    private MsgCidData mData;

    private ShareAdapter mShareAdapter;

    private Dialog mDelDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_near_share);
        setTitle(R.string.SHARE);
        setBackBtnOnClickListener(this);
        mData = (MsgCidData) getIntent().getSerializableExtra(ClientConstants.CIDINFO);
        initView();

    }

    @Override
    protected void onResume() {
        super.onResume();
        TcpSend(TCP_SHARELIST, "");
        StatService.trackBeginPage(this, "NearShared");
    }

    @Override
    protected void onPause() {
        super.onPause();
        StatService.trackEndPage(this, "NearShared");
    }

    private void initView() {
        mAccountView = (EditText) findViewById(R.id.edit);
        mAccountView.addTextChangedListener(new EditTextWatcher());
        mAddView = (TextView) findViewById(R.id.add_share);
        mAddView.setOnClickListener(this);
        mSearchView = (ImageView) findViewById(R.id.search_address_list);
        mSearchView.setOnClickListener(this);
        mSharedLayout = (LinearLayout) findViewById(R.id.shared_layout);
        mSharedListView = (ListView) findViewById(R.id.shared_list);
        mSharedListView.setOnItemLongClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ico_back:
                onBackPressed();
                break;
            case R.id.add_share:
                String account = mAccountView.getText().toString().replaceAll(" ", "");
                if (!StringUtils.isEmail(account)) {
                    account = account.replaceAll("-", "");
                }
                if (!StringUtils.isEmptyOrNull(account)) {
                    if (mShareAdapter != null && mShareAdapter.getCount() == MAX_SHARE_COUNT) {
                        ToastUtil.showFailToast(this, getString(R.string.SHARE_ERR));
                        return;
                    }
                    if (mShareAdapter != null) {
                        for (String sb : mShareAdapter.mList) {
                            if (sb.equals(account)) {
                                ToastUtil.showFailToast(this, getString(R.string.RET_ESHARE_REPEAT));
                                return;
                            }
                        }
                    }

                    if (account.equals(PreferenceUtil.getBindingPhone(this))) {
                        ToastUtil.showFailToast(this, getString(R.string.RET_ESHARE_NOT_YOURSELF));
                        return;
                    }

                    TcpSend(TCP_SHARE, account);
                }
                break;

            case R.id.search_address_list:
                try {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setData(ContactsContract.Contacts.CONTENT_URI);
                    startActivityForResult(intent, RESULT_CHOOSE_CONTACTS);
                } catch (Exception e) {
                    showErrorDialog();
                }
                DswLog.e("can read contact: " + PermissionChecker.isCanReadContactPermissionGrant(getApplicationContext()));
                break;
            default:
                break;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case RESULT_CHOOSE_CONTACTS:
                if (data == null) {
                    return;
                }
                try {
                    Uri contactData = data.getData();
                    Cursor cursor = managedQuery(contactData, null, null, null, null);
                    cursor.moveToFirst();
                    String num = this.getContactPhone(cursor);

                    mAccountView.setText(num);// + name
                } catch (Exception e) {
                    showErrorDialog();
                }
                break;

            default:
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    // 获取联系人电话
    private String getContactPhone(Cursor cursor) {
        int phoneColumn = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER);
        String phoneResult = "";
        if (phoneColumn > 0) {
            int phoneNum = cursor.getInt(phoneColumn);
            // 获得联系人的ID号
            int idColumn = cursor.getColumnIndex(ContactsContract.Contacts._ID);
            String contactId = cursor.getString(idColumn);
            Log.d("NearShared", "contactID -->" + contactId);
            if (phoneNum > 0) {
                // 获得联系人的电话号码的cursor;
                Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);
                // int phoneCount = phones.getCount();
                // allPhoneNum = new ArrayList<String>(phoneCount);
                if (phones != null && phones.moveToFirst()) {
                    // 遍历所有的电话号码
                    for (; !phones.isAfterLast(); phones.moveToNext()) {
                        int index = phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
//                        int typeindex = phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE);
//                        int phone_type = phones.getInt(typeindex);
                        phoneResult = phones.getString(index);
//                        switch (phone_type) {
//                            case 2:
//                                phoneResult = phoneNumber;
//                                break;
//                        }
                        // allPhoneNum.add(phoneNumber);
                    }
                    if (!phones.isClosed()) {
                        phones.close();
                    }
                }
            } else {
                Cursor email = getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                        ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + contactId, null, null);
                if (email.moveToFirst()) {
                    for (; !email.isAfterLast(); email.moveToNext()) {
                        int index = email.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS);
//                        int typeIndex = email.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA);
//                        int email_type = email.getInt(typeIndex);
                        phoneResult = email.getString(index);
//                        switch (email_type) {
//                            case 0:
//                                phoneResult = emailAddress;
//                                break;
//                        }
                    }
                    if (!email.isClosed())
                        email.close();
                }
            }
        }
        return phoneResult;
    }

    private void TcpSend(int flag, String account) {
        if (MyApp.getIsLogin()) {
            byte[] datas = null;
            switch (flag) {
                case TCP_SHARELIST:
                    MsgShareListReq mMsgShareListReq = new MsgShareListReq();
                    mMsgShareListReq.cid = mData.cid;
                    datas = mMsgShareListReq.toBytes();

                    DswLog.i("send MsgShareListReq msg-->" + mMsgShareListReq.toString());
                    mProgressDialog.showDialog(getString(R.string.getting));
                    break;
                case TCP_UNSHARE:
                    MsgUnshareReq mMsgUnshareReq = new MsgUnshareReq();
                    mMsgUnshareReq.cid = mData.cid;
                    mMsgUnshareReq.account = account;
                    datas = mMsgUnshareReq.toBytes();
                    DswLog.i("send MsgUnshareReq msg-->" + mMsgUnshareReq.toString());
                    mProgressDialog.showDialog(getString(R.string.upload));
                    break;
                case TCP_SHARE:
                    MsgShareReq mMsgShareReq = new MsgShareReq();
                    mMsgShareReq.cid = mData.cid;
                    mMsgShareReq.account = account;
                    datas = mMsgShareReq.toBytes();
                    DswLog.i("send MsgShareReq msg-->" + mMsgShareReq.toString());
                    mProgressDialog.showDialog(getString(R.string.upload));
                    break;
                default:
                    break;
            }
            MyApp.wsRequest(datas);
        } else {
            ToastUtil.showFailToast(NearSharedActivity.this, "(-" + MyApp.getMsgID() + ")" + getString(R.string.GLOBAL_NO_NETWORK));
        }
    }

    @Override
    public void handleMsgpackMsg(int msg, MsgpackMsg.MsgHeader msgpackMsg) {
        if (msgpackMsg.msgId == MsgpackMsg.CLIENT_SHARELIST_RSP) {
            RspMsgHeader rspMsgHeader = (RspMsgHeader) msgpackMsg;
            mProgressDialog.dismissDialog();
            if (rspMsgHeader.ret == Constants.RETOK) {
                MsgShareListRsp mMsgShareListRsp = (MsgShareListRsp) msgpackMsg;
                if (mMsgShareListRsp.cid.equals(mData.cid)) {
                    if (mShareAdapter != null && mShareAdapter.getCount() > 0) {
                        mShareAdapter.mList.clear();
                        mShareAdapter.notifyDataSetChanged();
                    }
                    mShareAdapter = new ShareAdapter(this, mMsgShareListRsp.data);
                    mSharedListView.setAdapter(mShareAdapter);
                    if (mMsgShareListRsp.data.size() > 0) {
                        if (mSharedLayout.getVisibility() == View.GONE)
                            mSharedLayout.setVisibility(View.VISIBLE);
                    } else if (mMsgShareListRsp.data.size() == 0) {
                        if (mSharedLayout.getVisibility() == View.VISIBLE)
                            mSharedLayout.setVisibility(View.GONE);
                    }
                }
            } else {
                ToastUtil.showFailToast(this, rspMsgHeader.msg);
            }
        } else if (msgpackMsg.msgId == MsgpackMsg.CLIENT_SHARE_RSP) {
            mProgressDialog.dismissDialog();
            RspMsgHeader rspMsgHeader = (RspMsgHeader) msgpackMsg;
            if (rspMsgHeader.ret == Constants.RETOK) {
                MsgShareRsp mMsgShareRsp = (MsgShareRsp) msgpackMsg;
                if (mMsgShareRsp.cid.equals(mData.cid)) {
                    mAccountView.setText("");
//                    ToastUtil.showSuccessToast(this, getString(R.string.has_share));

                    if (mShareAdapter != null) {
                        if (!mShareAdapter.mList.contains(mMsgShareRsp.account)) {
                            mShareAdapter.mList.add(mMsgShareRsp.account);
                            mShareAdapter.notifyDataSetChanged();
                        }
                        if (mShareAdapter.getCount() > 0) {
                            if (mSharedLayout.getVisibility() == View.GONE)
                                mSharedLayout.setVisibility(View.VISIBLE);
                        }
                    }
                }
            } else {
                ToastUtil.showFailToast(this, rspMsgHeader.msg);
            }
        } else if (msgpackMsg.msgId == MsgpackMsg.CLIENT_UNSHARE_RSP) {
            mProgressDialog.dismissDialog();
            RspMsgHeader rspMsgHeader = (RspMsgHeader) msgpackMsg;
            if (rspMsgHeader.ret == Constants.RETOK) {
                MsgUnshareRsp mMsgUnshareRsp = (MsgUnshareRsp) msgpackMsg;
                if (mMsgUnshareRsp.cid.equals(mData.cid)) {
                    if (mShareAdapter != null) {
                        for (String sb : mShareAdapter.mList) {
                            if (sb.equals(mMsgUnshareRsp.account)) {
                                mShareAdapter.mList.remove(sb);
                                mShareAdapter.notifyDataSetChanged();
                                if (mShareAdapter.getCount() == 0) {
                                    if (mSharedLayout.getVisibility() == View.VISIBLE)
                                        mSharedLayout.setVisibility(View.GONE);
                                }
                            }
                        }

                    }
                }
            } else {
                ToastUtil.showFailToast(this, rspMsgHeader.msg);
            }
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        final InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mAccountView.getWindowToken(), 0);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        showCancleDialog(mShareAdapter.getItem(position));
        Vibrator vib = (Vibrator) this.getSystemService(Service.VIBRATOR_SERVICE);
        vib.vibrate(50);// 只震动一秒，一次
        return false;
    }

    private void showCancleDialog(final String acc) {
        // if (mDelDialog == null) {
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
        btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mDelDialog.dismiss();
                TcpSend(TCP_UNSHARE, acc);
            }
        });
        mDelDialog.setContentView(content);
        mDelDialog.setCanceledOnTouchOutside(true);

        // }
        try {
            mDelDialog.show();
        } catch (Exception e) {
            DswLog.ex(e.toString());
        }
    }

    private String backContact(Intent intent) {
        String PhoneNumber = "";
        Uri uri = intent.getData();
        // 得到ContentResolver对象
        ContentResolver cr = getContentResolver();
        // 取得电话本中开始一项的光标
        Cursor cursor = cr.query(uri, null, null, null, null);
        // 向下移动光标
        while (cursor.moveToNext()) {
            // 取得联系人名字
            int nameFieldColumnIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
            String contact = cursor.getString(nameFieldColumnIndex);
            // 取得电话号码
            String ContactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            Cursor phone = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "="
                    + ContactId, null, null);
            // 不只一个电话号码
            while (phone.moveToNext()) {
                PhoneNumber = phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            }
            phone.close();
        }
        cursor.close();
        // 更新主界面
        return PhoneNumber;
    }

    @Override
    public void disconnectServer() {
        if (mProgressDialog.isShow()) {
            mProgressDialog.dismissDialog();
            ToastUtil.showFailToast(NearSharedActivity.this, "(-" + MyApp.getMsgID() + ")" + getString(R.string.GLOBAL_NO_NETWORK));
        }


    }


    private class EditTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // TODO Auto-generated method stub

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (!StringUtils.isEmptyOrNull(s.toString())) {
                mAddView.setEnabled(true);
                mAddView.setBackgroundResource(R.drawable.bg_add_share_selector);
                mAddView.setTextColor(getResources().getColorStateList(R.color.add_scenc_save_color));
            } else {
                mAddView.setEnabled(false);
                mAddView.setTextColor(getResources().getColor(R.color.enable_share));
                mAddView.setBackgroundResource(R.drawable.bg_addshare_blue);
            }

        }

    }

    private void showErrorDialog() {
        final NotifyDialog mDialog = new NotifyDialog(this);
        mDialog.hideNegButton();
        mDialog.setButtonText(R.string.WELL_OK, R.string.CANCEL);
        mDialog.show(String.format(getString(R.string.permission_auth), Utils.getApplicationName(NearSharedActivity.this), getString(R.string.contacts_auth)), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();

            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });
    }
}
