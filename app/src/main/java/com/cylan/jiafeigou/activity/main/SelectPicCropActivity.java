package com.cylan.jiafeigou.activity.main;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.cylan.jiafeigou.R;
import cylan.log.DswLog;
import com.cylan.jiafeigou.listener.SaveCompleteListener;
import com.cylan.jiafeigou.utils.NotifyDialog;
import com.cylan.jiafeigou.utils.PathGetter;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.regex.Pattern;

public class SelectPicCropActivity extends Activity implements View.OnClickListener {

    public static final String CROP_WIDTH = "CROP_WIDTH";
    public static final String CROP_HEIGHT = "CROP_HEIGHT";

    // data
    private static final String TAG = "MainActivity";
    private static final int TAKE_BIG_PICTURE = 1;
    private static final int CROP_BIG_PICTURE = 2;
    private static final int CHOOSE_BIG_PICTURE = 3;
    private Uri imageUri;// to store the big bitmap
    private String filePath;
    private int width;
    private int height;
    private Uri TakePhotoUri = null;
    private String fileType = Bitmap.CompressFormat.JPEG.toString();
    private String tmpFilePath = null;
    private static SaveCompleteListener scl;

    private Cursor cursor = null;

    private void cropImageUri(Uri uri, int outputX, int outputY, int requestCode) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", width);
        intent.putExtra("aspectY", height);
        intent.putExtra("outputX", outputX);
        intent.putExtra("outputY", outputY);
        intent.putExtra("scale", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(filePath)));
        intent.putExtra("return-data", false);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true); // no face detection
        intent.putExtra("scaleUpIfNeeded", true);
        startActivityForResult(intent, requestCode);
    }

    private Bitmap decodeUriAsBitmap(Uri uri) {
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
        } catch (FileNotFoundException e) {
            DswLog.ex(e.toString());
            return null;
        }
        return bitmap;
    }

    /**
     * this uri must be a new rui different from the taken pic uri
     *
     * @return
     */
    private Uri getPicUri() {
        return Uri.fromFile(CreateTempPic());
    }

    private File CreateTempFile(String filePath) {
        if (filePath == null)
            return null;
        File f = new File(filePath + "/tmp");
        if (!f.exists())
            f.mkdir();
        tmpFilePath = filePath + "/tmp/tmp.jpg";
        f = new File(tmpFilePath);
        try {
            f.createNewFile();
        } catch (IOException e) {
            // TODO Auto-generated catch block
        }
        return f;
    }

    private File CreateTempPic() {
        File f = CreateTempFile(PathGetter.getScreenShotPath());
        return f;
    }


    /**
     * Activity life cycle
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_pic_crop_layout);
        findViewById(R.id.btn_cancel).setOnClickListener(this);
        findViewById(R.id.btn_take_photo).setOnClickListener(this);
        findViewById(R.id.btn_pick_photo).setOnClickListener(this);
        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        WindowManager.LayoutParams lp = this.getWindow().getAttributes();
        lp.height = display.getHeight(); // set width
        this.getWindow().setAttributes(lp);

        width = getIntent().getIntExtra(CROP_WIDTH, 96);
        height = getIntent().getIntExtra(CROP_HEIGHT, 96);
        filePath = PathGetter.getSpecialPicPath(this);
        String s = Pattern.compile("[@]").matcher(filePath).replaceAll("").trim();
        filePath = s;
        imageUri = Uri.parse("file:///" + filePath);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {// result is not correct
            Log.d(TAG, "requestCode = " + requestCode);
            Log.d(TAG, "resultCode = " + resultCode);
            Log.d(TAG, "data = " + data);
            return;
        } else {

            Intent lastIntent = getIntent();
            switch (requestCode) {
                case TAKE_BIG_PICTURE:
                    Log.d(TAG, "TAKE_BIG_PICTURE: data = " + data);
                    if (TakePhotoUri == null) {
                        File f = new File(filePath);
                        if (f.exists() && f.length() == 0) {
                            f.delete();
                            ToastUtil.showFailToast(getApplicationContext(), getString(R.string.set_failed));
                        }
                        TakePhotoUri = Uri.fromFile(f);
                    }
                    try {
                        cropImageUri(TakePhotoUri, width, height, CROP_BIG_PICTURE);
                    }catch (ActivityNotFoundException e){
                        showErrorDialog();
                    }

                    break;
                case CHOOSE_BIG_PICTURE:
                    Log.d(TAG, "CHOOSE_BIG_PICTURE: data = " + data);// it seems to
                    doPhoto(data);
                    break;
                case CROP_BIG_PICTURE:
                    Log.d("file", "time--->" + System.currentTimeMillis());
                    Log.d("file", this.getClass().getSimpleName() + "onActivityResult  CROP_BIG_PICTURE--file exasit-->" + new File(filePath).exists());
                    lastIntent.putExtra(SetCoverActivity.PIC_POSITION, filePath);
                    lastIntent.putExtra(SetCoverActivity.IS_SDCARD_PIC, true);
//                    setResult(Activity.RESULT_OK, lastIntent);
                    if (scl != null)
                        scl.complete(lastIntent);
                    finish();
                    DswLog.i("scl != null" + (scl != null));
                    DswLog.i("SelectPicCropActivity---finish");
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        Log.d("file", this.getClass().getSimpleName() + " onDestroy");
        DswLog.i("SelectPicCropActivity---onDestroy");
        if (cursor != null)
            cursor.close();
        super.onDestroy();
    }


    public static void setSaveCompleteListener(SaveCompleteListener listener) {
        scl = listener;
    }

    private void doPhoto(Intent data) {
        if (data == null) {
            ToastUtil.showFailToast(getApplicationContext(), getString(R.string.set_failed));
            return;
        }
        Uri photoUri = data.getData();
        if (photoUri == null) {
            ToastUtil.showFailToast(getApplicationContext(), getString(R.string.set_failed));
            return;
        }

        String[] pojo = {MediaStore.Images.Media.DATA};
        cursor = managedQuery(photoUri, pojo, null, null, null);
        String path = null;

        if (cursor != null) {
            int columnIndex = cursor.getColumnIndexOrThrow(pojo[0]);
            cursor.moveToFirst();
            path = cursor.getString(columnIndex);

        }
        if (path != null
                && (path.endsWith(".png") || path.endsWith(".PNG")
                || path.endsWith(".jpg") || path.endsWith(".JPG"))) {
            TakePhotoUri = Uri.fromFile(new File(path));
            cropImageUri(TakePhotoUri, width, height, CROP_BIG_PICTURE);
        } else {
            ToastUtil.showFailToast(getApplicationContext(), getString(R.string.set_failed));
        }

    }


    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.btn_take_photo:

                String SDState = Environment.getExternalStorageState();
                if (SDState.equals(Environment.MEDIA_MOUNTED)) {
                    try {
                        intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        File f = new File(filePath);
                        f.createNewFile();
                        TakePhotoUri = Uri.fromFile(f);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, TakePhotoUri);
                        intent.putExtra("return-data", false);
                        startActivityForResult(intent, TAKE_BIG_PICTURE);
                    } catch (IOException e) {
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(this, getString(R.string.not_found_the_album_application), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(this, getString(R.string.the_memory_card_is_not_present), Toast.LENGTH_LONG).show();
                }
                break;

            case R.id.btn_pick_photo:
                intent = new Intent(Intent.ACTION_PICK, null);
                intent.setType("image/*");
                intent.putExtra("aspectX", width);
                intent.putExtra("aspectY", height);
                intent.putExtra("outputX", width);
                intent.putExtra("outputY", height);
                intent.putExtra("scale", true);
                intent.putExtra("return-data", false);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
                intent.putExtra("noFaceDetection", false); // no face detection
                intent.putExtra("scaleUpIfNeeded", true);
                startActivityForResult(intent, CHOOSE_BIG_PICTURE);

                break;
            case R.id.btn_cancel:
                finish();
                break;
            default:
                break;
        }
    }

    private void showErrorDialog() {
        final NotifyDialog mDialog = new NotifyDialog(this);
        mDialog.hideNegButton();
        mDialog.setButtonText(R.string.WELL_OK, R.string.CANCEL);
        mDialog.show(String.format(getString(R.string.permission_auth),
                Utils.getApplicationName(SelectPicCropActivity.this),
                getString(R.string.photo)), new View.OnClickListener() {
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
