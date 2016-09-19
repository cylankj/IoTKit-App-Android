package com.cylan.jiafeigou.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.Uri;
import android.provider.ContactsContract;
import android.text.TextUtils;

;import com.cylan.jiafeigou.support.log.AppLogger;


/**
 * Created by hebin on 2015/8/17.
 * edit by hds 2016-03-24
 */
public class PermissionChecker {
    private static final String AUDIO_RECORD_PERMISSION = "android.permission.RECORD_AUDIO";
    private static final String READ_CONTACT_PERMISSION = "android.permission.READ_CONTACTS";

    /**
     * direct method for check audio_record_permission
     */
//    public static boolean isAudioRecordPermissionGrant(Context context) {
//        return context.checkCallingOrSelfPermission(AUDIO_RECORD_PERMISSION) == PackageManager.PERMISSION_GRANTED;
//    }
    public static boolean isAudioRecordPermissionGrant() {
        int bufferSize = AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        AppLogger.i("PermissionChecker get bufferSize:" + bufferSize);
        AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                8000, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, bufferSize);
        int recordState = -1;
        try {
            audioRecord.startRecording();
            if (audioRecord.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING)
                recordState = -1;
            else recordState = 0;
        } catch (Exception e) {
            recordState = -1;
            AppLogger.e("recordState: " + e.toString());
        } finally {
            audioRecord.release();
            return recordState == 0;
        }
    }

    public static boolean isCameraPermissionGrant(final int cameraIndex) {
        boolean enable = false;
        android.hardware.Camera camera = null;
        try {
            camera = android.hardware.Camera.open(cameraIndex);
            enable = true;
        } catch (Exception e) {
            enable = false;
        } finally {
            if (camera != null) camera.release();
            return enable;
        }
    }

    /**
     * read contact
     *
     * @param context
     * @return
     */
//    public static boolean isCanReadContactPermissionGrant(Context context) {
//        return context.checkCallingOrSelfPermission(READ_CONTACT_PERMISSION) == PackageManager.PERMISSION_GRANTED;
//    }

    /**
     * @param context    :context
     * @param permission :the special permission to check
     * @return true if the permission is grant ,else false.
     */
    public static boolean checkPermission(Context context, String permission) {
        PackageManager pm = context.getPackageManager();
        int hasPerm = pm.checkPermission(
                permission,
                context.getPackageName());
        return hasPerm != PackageManager.PERMISSION_GRANTED;
    }

    public static boolean tryLoadContact(Context context) throws Exception {
        Uri uri = Uri.parse("content://com.android.contacts/contacts");
        //获得一个ContentResolver数据共享的对象
        ContentResolver resolver = context.getContentResolver();
        //取得联系人中开始的游标，通过content://com.android.contacts/contacts这个路径获得
        Cursor cursor = resolver.query(uri, null, null, null, null);
        //上边的所有代码可以由这句话代替：Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        //Uri.parse("content://com.android.contacts/contacts") == ContactsContract.Contacts.CONTENT_URI
        boolean findResults = false;
        while (cursor != null && cursor.moveToNext()) {
            //获得联系人ID
            String id = cursor.getString(cursor.getColumnIndex(android.provider.ContactsContract.Contacts._ID));
            //获得联系人姓名
            String name = cursor.getString(cursor.getColumnIndex(android.provider.ContactsContract.Contacts.DISPLAY_NAME));
            //获得联系人手机号码
            Cursor phoneCursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + id, null, null);
            if (!TextUtils.isEmpty(name)) {
                findResults = true;
                break;
            }
            while (phoneCursor != null && phoneCursor.moveToNext()) { //取得电话号码(可能存在多个号码)
                int phoneFieldColumnIndex = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                String phoneNumber = phoneCursor.getString(phoneFieldColumnIndex);
                if (!TextUtils.isEmpty(phoneNumber)) {
                    findResults = true;
                    break;
                }
            }
            //建立一个Log，使得可以在LogCat视图查看结果
        }
        if (cursor != null) cursor.close();
        return findResults;
    }
}
