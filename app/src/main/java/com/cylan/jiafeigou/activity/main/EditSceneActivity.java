package com.cylan.jiafeigou.activity.main;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.publicApi.Constants;
import cylan.log.DswLog;
import com.cylan.publicApi.JniPlay;
import com.cylan.publicApi.MsgpackMsg;
import com.cylan.jiafeigou.base.BaseActivity;
import com.cylan.jiafeigou.base.MyApp;
import com.cylan.jiafeigou.engine.ClientConstants;
import com.cylan.jiafeigou.entity.msg.HttpResult;
import com.cylan.jiafeigou.entity.msg.MsgSceneData;
import com.cylan.jiafeigou.entity.msg.req.MsgCidlistReq;
import com.cylan.jiafeigou.entity.msg.req.MsgDeleteSceneReq;
import com.cylan.jiafeigou.listener.SaveCompleteListener;
import com.cylan.jiafeigou.utils.AppManager;
import com.cylan.jiafeigou.utils.MyImageLoader;
import com.cylan.jiafeigou.utils.NotifyDialog;
import com.cylan.jiafeigou.utils.PreferenceUtil;
import com.cylan.jiafeigou.utils.StringUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.EditDelText;
import com.google.gson.Gson;
import cylan.uil.core.DisplayImageOptions;
import cylan.uil.core.ImageLoader;
import cylan.uil.core.download.ImageDownloader;
import com.tencent.stat.StatService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class EditSceneActivity extends BaseActivity implements OnClickListener, SaveCompleteListener {
    private static final String MTATAG = "EditScencActivity";
    protected static final int TO_SET_COVER = 0x00;
    private EditDelText mNameView;
    private LinearLayout mChooseView;
    // private Button mDeleteBtn;
    private ImageView mCoverView;

    private LinearLayout mMode1Layout;
    private LinearLayout mMode2Layout;
    private LinearLayout mMode3Layout;
    private List<ImageView> mImageViewList;
    private ImageView mMode1;
    private ImageView mMode2;
    private ImageView mMode3;
    private Button mDeleteScene;

    private MsgSceneData mSceneInfo;

    public static String FLAG_MODIFY = "flag_modify";//
    public static String FLAG_ADD = "flag_add";//

    private String flag = null;
    private int count;
    private int mCurrentTheme;

    boolean isSdcard = false;
    String picPosition = "1";

    private NotifyDialog mDeleteDialog;
    public static final int TCP_GET_LIST = 0x01;
    public static final int TCP_DELETE_SCENC = 0x02;

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editscenc);

        flag = getIntent().getStringExtra("flag");
        count = getIntent().getIntExtra("scenc_count", 0);
        mCurrentTheme = getIntent().getIntExtra("CurrentTheme", -1);

        setRightBtn(R.string.SAVE, this);

        mNameView = (EditDelText) findViewById(R.id.name);
        mChooseView = (LinearLayout) findViewById(R.id.choose_pic);
        mChooseView.setOnClickListener(this);

        mCoverView = (ImageView) findViewById(R.id.current_pic);
        String[] strs = getResources().getStringArray(R.array.modes);
        mMode1Layout = (LinearLayout) findViewById(R.id.layout_mode_standard);
        ((TextView) mMode1Layout.getChildAt(0)).setText(strs[0]);
        mMode1Layout.setOnClickListener(this);
        mMode2Layout = (LinearLayout) findViewById(R.id.layout_mode_home_in);
        ((TextView) mMode2Layout.getChildAt(0)).setText(strs[1]);
        mMode2Layout.setOnClickListener(this);
        mMode3Layout = (LinearLayout) findViewById(R.id.layout_mode_home_out);
        ((TextView) mMode3Layout.getChildAt(0)).setText(strs[2]);
        mMode3Layout.setOnClickListener(this);
        mMode1 = (ImageView) findViewById(R.id.select_mode1);
        mMode2 = (ImageView) findViewById(R.id.select_mode2);
        mMode3 = (ImageView) findViewById(R.id.select_mode3);

        mDeleteScene = (Button) findViewById(R.id.btn_delete_sence);
        mDeleteScene.setOnClickListener(this);
        mImageViewList = new ArrayList<>();
        mImageViewList.add(mMode1);
        mImageViewList.add(mMode2);
        mImageViewList.add(mMode3);

        if (flag.equals(FLAG_MODIFY)) {
            //不是最后一个场景，显示删除按钮
            if (count != 2) {
                mDeleteScene.setVisibility(View.VISIBLE);
            }
            mSceneInfo = (MsgSceneData) getIntent().getSerializableExtra(ClientConstants.SCENCINFO);

            int cover = PreferenceUtil.getHomeCover(this);
            if (cover != -1) {
                if (cover != 0) {
                    mCoverView.setImageResource(ClientConstants.covers[cover - 1]);
                } else {
                    String url = "http://" + Constants.WEB_ADDR + ":" + Constants.WEB_PORT + "/index.php?sessid=" + PreferenceUtil.getSessionId(this)
                            + "&mod=client&act=get_scene_image&scene_id=" + mSceneInfo.scene_id;

                    MyImageLoader.loadImageFromNet(url, mCoverView);
                }
            }

            setModeStype(mSceneInfo.mode);
            setTitle(R.string.EDIT_THEME);
            mNameView.setText(mSceneInfo.scene_name);
            picPosition = String.valueOf(mSceneInfo.image_id);
            LinearLayout layout = (LinearLayout) findViewById(R.id.layout_mode);
            layout.setVisibility(View.VISIBLE);

        } else if (flag.equals(FLAG_ADD)) {

            mCoverView.setImageResource(ClientConstants.covers[StringUtils.toInt(picPosition) - 1]);

            setModeStype(0);
            setTitle(R.string.NEW_LOCATION);

        }

        SelectPicCropActivity.setSaveCompleteListener(this);
    }

    private void setModeStype(int type) {

        for (int i = 0; i < mImageViewList.size(); i++) {
            if (type == i) {
                mImageViewList.get(i).setVisibility(View.VISIBLE);
            } else {
                mImageViewList.get(i).setVisibility(View.GONE);
            }
        }

    }

    private int getModeStype() {

        for (int i = 0; i < mImageViewList.size(); i++) {
            if (mImageViewList.get(i).getVisibility() == View.VISIBLE) {
                return i;
            }
        }
        return 0;

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.choose_pic:

                startActivityForResult(new Intent(EditSceneActivity.this, SetCoverActivity.class).putExtra("picposition", picPosition), TO_SET_COVER);

                break;
            case R.id.ico_back:
                finish();
                break;

            case R.id.right_btn:
                try {
                    String name = mNameView.getText().toString().trim();
                    if (StringUtils.isEmptyOrNull(name)) {
                        ToastUtil.showFailToast(EditSceneActivity.this, getString(R.string.LOCATION_NAME_ERROR));
                    } else {

                        if (flag.equals(FLAG_MODIFY)) {

                            if (name.equals(mSceneInfo.scene_name)
                                    && (!isSdcard && picPosition.equals(String.valueOf(mSceneInfo.image_id)) && (getModeStype() == mSceneInfo.mode))) {
                                setResult(Activity.RESULT_OK);
                                finish();
                                return;
                            }

                            mProgressDialog.showDialog(R.string.SETTING);
                            int imageId = isSdcard ? 0 : StringUtils.toInt(picPosition);
                            String url = "http://" + Constants.WEB_ADDR + ":" + Constants.WEB_PORT + "/index.php?sessid="
                                    + PreferenceUtil.getSessionId(EditSceneActivity.this) + "&mod=client&act=edit_scene&mode=" + getModeStype() + "&scene_id="
                                    + mSceneInfo.scene_id + "&name=" + URLEncoder.encode(name, "UTF-8") + "&image_id=" + imageId;
                            if (isSdcard) {
                                JniPlay.HttpPostFile(Constants.WEB_ADDR, Constants.WEB_PORT, url, picPosition);
                            } else {
                                JniPlay.HttpGet(Constants.WEB_ADDR, Constants.WEB_PORT, url);
                            }

                        } else if (flag.equals(FLAG_ADD)) {
                            mProgressDialog.showDialog(R.string.is_creating);
                            int imageId = isSdcard ? 0 : StringUtils.toInt(picPosition);
                            String url = "http://" + Constants.WEB_ADDR + ":" + Constants.WEB_PORT + "/index.php?sessid="
                                    + PreferenceUtil.getSessionId(EditSceneActivity.this) + "&mod=client&act=add_scene&mode=" + getModeStype() + "&name="
                                    + URLEncoder.encode(name, "UTF-8") + "&image_id=" + imageId;
                            if (isSdcard) {
                                JniPlay.HttpPostFile(Constants.WEB_ADDR, Constants.WEB_PORT, url, picPosition);
                            } else {
                                JniPlay.HttpGet(Constants.WEB_ADDR, Constants.WEB_PORT, url);
                            }

                        }

                    }
                } catch (Exception e) {
                    DswLog.ex(e.toString());
                }
                break;
            case R.id.layout_mode_standard:
                setModeStype(0);
                break;
            case R.id.layout_mode_home_in:
                setModeStype(1);
                break;
            case R.id.layout_mode_home_out:
                setModeStype(2);
                break;
            case R.id.btn_delete_sence:
                if (mCurrentTheme != -1) {
                    toDealDelete(mSceneInfo);
                }
                break;
            default:
                break;
        }
    }

    private void toDealDelete(MsgSceneData info) {
        if (info.data.size() > 0) {
            final NotifyDialog dialog = new NotifyDialog(this);
            dialog.setButtonText(R.string.OK, R.string.CANCEL);
            dialog.hideNegButton();
            dialog.show(R.string.DELETE_EQUIPMENT, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (v.getId()) {

                        case R.id.confirm:
                            dialog.dismiss();
                            break;
                    }

                }
            }, null);

        } else {
            mDeleteDialog = new NotifyDialog(this);
            mDeleteDialog.setButtonText(R.string.DELETE, R.string.CANCEL);
            mDeleteDialog.setPosRedTheme(R.drawable.bg_dialogdel_selector, getResources().getColor(R.color.mycount_not_set));
            mDeleteDialog.show(String.format(getString(R.string.DELETE_THEME_XX), info.scene_name), new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    switch (v.getId()) {
                        case R.id.cancel:
                            mDeleteDialog.dismiss();
                            break;
                        case R.id.confirm:
                            mDeleteDialog.dismiss();
                            mProgressDialog.showDialog(R.string.DELETEING);
                            Request(TCP_DELETE_SCENC);
                            break;
                    }

                }
            }, null);

        }

    }

    private void Request(int flag) {
        byte[] map = null;
        switch (flag) {
            case TCP_GET_LIST:
                MsgCidlistReq mMsgCidlistReq = new MsgCidlistReq(PreferenceUtil.getBindingPhone(this));
                map = mMsgCidlistReq.toBytes();
                DswLog.i("send MsgCidlistReq msg-->" + mMsgCidlistReq.toString());
                break;
            case TCP_DELETE_SCENC:
                MsgDeleteSceneReq mMsgDeleteSceneReq = new MsgDeleteSceneReq(PreferenceUtil.getBindingPhone(this));
                mMsgDeleteSceneReq.scene_id = mSceneInfo.scene_id;
                map = mMsgDeleteSceneReq.toBytes();
                DswLog.i("send mMsgDeleteSceneReq msg-->" + mMsgDeleteSceneReq.toString());
                break;
        }
        MyApp.wsRequest(map);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        if (requestCode == TO_SET_COVER) {
            Log.d("file", this.getClass().getSimpleName() + "\tonActivityResult");
            if (data != null) {
                isSdcard = data.getBooleanExtra(SetCoverActivity.IS_SDCARD_PIC, false);
                picPosition = data.getStringExtra(SetCoverActivity.PIC_POSITION);
                if (!isSdcard) {
                    mCoverView.setImageResource(ClientConstants.home_covers[StringUtils.toInt(picPosition) - 1]);
                } else {
                    File file = new File(picPosition);
                    if (file != null && file.exists()) {
                        DisplayImageOptions options = new DisplayImageOptions.Builder()
                                .bitmapConfig(Bitmap.Config.RGB_565)
                                .build();
                        String imageUrl = ImageDownloader.Scheme.FILE.wrap(picPosition);
                        ImageLoader.getInstance().displayImage(imageUrl, mCoverView, options);

                    }
                }
            }

        }

    }

    private void saveScenePic() {

        if (flag.equals(FLAG_MODIFY)) {
            String url = "http://" + Constants.WEB_ADDR + ":" + Constants.WEB_PORT +
                    "/index.php?sessid=" + PreferenceUtil.getSessionId(this)
                    + "&mod=client&act=get_scene_image&scene_id=" + mSceneInfo.scene_id;
            MyImageLoader.removeFromCache(url);
        }

    }

    public void handleJsonMsg(String act, int ret, int httpRet) {
        String[] strs = getResources().getStringArray(R.array.error_msg);
        if (httpRet == Constants.HTTP_RETOK) {
            if (("add_scene_rsp".equals(act) || "edit_scene_rsp".equals(act))) {
                if (Constants.RETOK == ret) {
                    StatService.trackCustomEvent(this, MTATAG, getString(R.string.EDIT_THEME));
                    if (isSdcard) {
                        saveScenePic();
                    }
                    setResult(Activity.RESULT_OK);
                    Intent intent = getIntent();
                    if (flag.equals(FLAG_MODIFY) && (getModeStype() == MsgSceneData.MODE_HOME_OUT || getModeStype() == MsgSceneData.MODE_STANDARD)) {
                        intent.putExtra("auto", "auto");
                    }
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                } else {
                    ToastUtil.showFailToast(EditSceneActivity.this, strs[ret + 1]);
                }
            }
        } else {
            if (flag.equals(FLAG_MODIFY)) {
                ToastUtil.showFailToast(EditSceneActivity.this, getString(R.string.set_failed));
            } else {
                ToastUtil.showFailToast(EditSceneActivity.this, getString(R.string.ADD_FAILED));
            }
        }

    }


    @Override
    public void handleMsgpackMsg(int msg, MsgpackMsg.MsgHeader msgpackMsg) {
        if (msgpackMsg.msgId == MsgpackMsg.CLIENT_DELETESCENE_RSP) {
            mProgressDialog.dismissDialog();
            ToastUtil.showSuccessToast(this, String.format(getString(R.string.THEME_DELETED),
                    mSceneInfo.scene_name));
            finish();
        }
    }

    @Override
    public void httpDone(HttpResult mResult) {
        mProgressDialog.dismissDialog();
        JSONObject jsonobject = null;
        try {
            Gson gson = new Gson();
            DswLog.i("httpDone--->" + gson.toJson(mResult));
            int ret = 0;
            String act = "";
            if (!StringUtils.isEmptyOrNull(mResult.result)) {
                jsonobject = new JSONObject(mResult.result);
                act = jsonobject.has(Constants.ACT) ? jsonobject.getString(Constants.ACT) : "";
                ret = jsonobject.has(Constants.RET) ? jsonobject.getInt(Constants.RET) : 0;
            }
            handleJsonMsg(act, ret, mResult.ret);
        } catch (JSONException e) {
            DswLog.ex(e.toString());
        }

    }


    @Override
    public void complete(Intent inteng) {
        isSdcard = inteng.getBooleanExtra(SetCoverActivity.IS_SDCARD_PIC, false);
        picPosition = inteng.getStringExtra(SetCoverActivity.PIC_POSITION);
        File file = new File(picPosition);
        if (file != null && file.exists()) {
            DisplayImageOptions options = new DisplayImageOptions.Builder()
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .build();
            String imageUrl = ImageDownloader.Scheme.FILE.wrap(picPosition);
            ImageLoader.getInstance().displayImage(imageUrl, mCoverView, options);
        }
        AppManager.getAppManager().finishActivity(SetCoverActivity.class);
        AppManager.getAppManager().finishActivity(SelectPicCropActivity.class);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SelectPicCropActivity.setSaveCompleteListener(null);
    }
}