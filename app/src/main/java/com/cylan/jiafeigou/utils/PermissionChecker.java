package com.cylan.jiafeigou.utils;

import android.content.Context;
import android.content.pm.PackageManager;

/**
 * Created by hebin on 2015/8/17.
 * edit by hds 2016-03-24
 */
public class PermissionChecker {
    private static final String AUDIO_RECORD_PERMISSION = "android.permission.RECORD_AUDIO";
    private static final String READ_CONTACT_PERMISSION = "android.permission.READ_CONTACTS";

    /**
     * direct method for check audio_record_permission
     *
     * @param context
     * @return
     */
    public static boolean isAudioRecordPermissionGrant(Context context) {
        return context.checkCallingOrSelfPermission(AUDIO_RECORD_PERMISSION) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * read contact
     *
     * @param context
     * @return
     */
    public static boolean isCanReadContactPermissionGrant(Context context) {
        return context.checkCallingOrSelfPermission(READ_CONTACT_PERMISSION) == PackageManager.PERMISSION_GRANTED;
    }

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
}
