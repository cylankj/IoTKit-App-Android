package com.cylan.jiafeigou.activity.main;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import cylan.log.DswLog;
import com.cylan.jiafeigou.adapter.MyGridAdapter;
import com.cylan.jiafeigou.base.BaseActivity;
import com.cylan.jiafeigou.engine.ClientConstants;
import com.cylan.jiafeigou.utils.DensityUtil;
import com.cylan.jiafeigou.utils.StringUtils;

/**
 * 选择封面界面
 */
public class SetCoverActivity extends BaseActivity implements OnClickListener, OnItemClickListener {
    private static final int TO_SELECT_PHOTO = 0x01;
    // onActivityResult
    private static final int SAVE_COVER = 0x02;
    private static final int SET_BACKGROUND = 0x03;
    private LinearLayout mRootLayout;
    private TextView mGetCoverFromNative;
    private GridView mGridView;

    int pos = -1;
    private String img = "0";

    private int picposition;
    private String name;
    private MyGridAdapter mAdapter;
    Bitmap bm = null;
    public static String IS_SDCARD_PIC = "isSdcardPic";
    private Boolean isSdcard = false;
    public static String PIC_POSITION = "picPosition";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_cover);

        picposition = StringUtils.toInt(getIntent().getStringExtra("picposition"));
        name = getIntent().getStringExtra("name");

        mRootLayout = (LinearLayout) findViewById(R.id.root);
        setTitle(R.string.CHOOSE_COVER);
        mGetCoverFromNative = (TextView) findViewById(R.id.choose_pic_from_native);
        mGetCoverFromNative.setOnClickListener(this);
        mGridView = (GridView) findViewById(R.id.cover_grid);
        mGridView.setOnItemClickListener(this);
        if (picposition >= MyVideos.DEFAULT_MIN_PIC_INDEX && picposition <= MyVideos.DEFAULT_MAX_PIC_INDEX) {
            pos = picposition;
        }
        mAdapter = new MyGridAdapter(this, ClientConstants.covers);
        mAdapter.setPosition(pos);
        mGridView.setAdapter(mAdapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ico_back:
                finish();
                break;
            case R.id.choose_pic_from_native:
                Intent intent = new Intent(SetCoverActivity.this, SelectPicCropActivity.class);
                intent.putExtra(SelectPicCropActivity.CROP_WIDTH, DensityUtil.getScreenWidth(this));
                intent.putExtra(SelectPicCropActivity.CROP_HEIGHT, DensityUtil.dip2px(this, 187));
                startActivityForResult(intent, TO_SELECT_PHOTO);
                break;
            default:
                break;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            if (resultCode == Activity.RESULT_OK) {
                Log.d("file", this.getClass().getSimpleName() + "-----onActivityResult");
                if (requestCode == TO_SELECT_PHOTO) {
                    isSdcard = true;
                    String picPath = data.getStringExtra(PIC_POSITION);
                    Intent intent = getIntent();
                    intent.putExtra(IS_SDCARD_PIC, isSdcard);
                    intent.putExtra(PIC_POSITION, picPath);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
            super.onActivityResult(requestCode, resultCode, data);
        } catch (Exception e) {
            DswLog.ex(e.toString());
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        mAdapter.setPosition(position + 1);
        Intent intent = getIntent();
        intent.putExtra(IS_SDCARD_PIC, isSdcard);
        intent.putExtra(PIC_POSITION, String.valueOf(mAdapter.getPosition()));
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        Log.d("file", this.getClass().getSimpleName() + "onDestroy");
        super.onDestroy();
    }

}