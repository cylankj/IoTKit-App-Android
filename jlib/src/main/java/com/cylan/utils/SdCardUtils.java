package com.cylan.utils;

import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by hunt on 16-4-6.
 * 从@{@link  android.os.Environment}中提取处#UserEnvironment
 */
public class SdCardUtils {
    static UserEnvironment userEnvironment;

    static {
        userEnvironment = new UserEnvironment();
    }

    /**
     * @return; 对于一般手机, 最多有两个sdcard路径 {sdcard0,sdcard1}
     * 如果没有sdcard1,去sdcard0,否则取sdcard1.
     */
    public static String getExternalSdcardPath() {
        File[] files = userEnvironment.getExternalDirsForApp();
        if (files.length == 1)
            return files[0].getAbsolutePath();

        if (files[1].length() == 0)
            return files[0].getAbsolutePath();
        return files[1].getAbsolutePath();
    }

    private static class UserEnvironment {
        private static final String ENV_EXTERNAL_STORAGE = "EXTERNAL_STORAGE";
        private static final String ENV_EMULATED_STORAGE_SOURCE = "EMULATED_STORAGE_SOURCE";
        private static final String ENV_EMULATED_STORAGE_TARGET = "EMULATED_STORAGE_TARGET";
        private static final String ENV_MEDIA_STORAGE = "MEDIA_STORAGE";
        public static final String DIR_ANDROID = "Android";
        private static final String DIR_DATA = "data";
        private static final String DIR_MEDIA = "media";
        private static final String DIR_OBB = "obb";
        private static final String DIR_FILES = "files";
        private static final String DIR_CACHE = "cache";
        private static final String ENV_SECONDARY_STORAGE = "SECONDARY_STORAGE";
        // TODO: generalize further to create package-specific environment

        /**
         * External storage dirs, as visible to vold
         */
        private final File[] mExternalDirsForVold;
        /**
         * External storage dirs, as visible to apps
         */
        private final File[] mExternalDirsForApp;
        /**
         * Primary emulated storage dir for direct access
         */
        private final File mEmulatedDirForDirect;

        private UserEnvironment() {
            //userId should be 0
            int userId = 0;
            // See storage config details at http://source.android.com/tech/storage/
            String rawExternalStorage = System.getenv(ENV_EXTERNAL_STORAGE);
            String rawEmulatedSource = System.getenv(ENV_EMULATED_STORAGE_SOURCE);
            String rawEmulatedTarget = System.getenv(ENV_EMULATED_STORAGE_TARGET);

            String rawMediaStorage = System.getenv(ENV_MEDIA_STORAGE);
            if (TextUtils.isEmpty(rawMediaStorage)) {
                rawMediaStorage = "/data/media";
            }

            ArrayList<File> externalForVold = new ArrayList<>();
            ArrayList<File> externalForApp = new ArrayList<>();

            if (!TextUtils.isEmpty(rawEmulatedTarget)) {
                // Device has emulated storage; external storage paths should have
                // userId burned into them.
                final String rawUserId = Integer.toString(0);//
                final File emulatedSourceBase = new File(rawEmulatedSource);
                final File emulatedTargetBase = new File(rawEmulatedTarget);
                final File mediaBase = new File(rawMediaStorage);

                // /storage/emulated/0
                externalForVold.add(buildPath(emulatedSourceBase, rawUserId));
                externalForApp.add(buildPath(emulatedTargetBase, rawUserId));
                // /data/media/0
                mEmulatedDirForDirect = buildPath(mediaBase, rawUserId);

            } else {
                // Device has physical external storage; use plain paths.
                if (TextUtils.isEmpty(rawExternalStorage)) {
                    Log.w("UserEnvironment", "EXTERNAL_STORAGE undefined; falling back to default");
                    rawExternalStorage = "/storage/sdcard0";
                }

                // /storage/sdcard0
                externalForVold.add(new File(rawExternalStorage));
                externalForApp.add(new File(rawExternalStorage));
                // /data/media
                mEmulatedDirForDirect = new File(rawMediaStorage);
            }

            // Splice in any secondary storage paths, but only for owner
            final String rawSecondaryStorage = System.getenv(ENV_SECONDARY_STORAGE);
            if (!TextUtils.isEmpty(rawSecondaryStorage) && userId == 0) {
                for (String secondaryPath : rawSecondaryStorage.split(":")) {
                    externalForVold.add(new File(secondaryPath));
                    externalForApp.add(new File(secondaryPath));
                }
            }

            mExternalDirsForVold = externalForVold.toArray(new File[externalForVold.size()]);
            mExternalDirsForApp = externalForApp.toArray(new File[externalForApp.size()]);
        }


        public File[] getExternalDirsForVold() {
            return mExternalDirsForVold;
        }

        public File[] getExternalDirsForApp() {
            return mExternalDirsForApp;
        }

        public File getMediaDir() {
            return mEmulatedDirForDirect;
        }

        public File[] buildExternalStoragePublicDirs(String type) {
            return buildPaths(mExternalDirsForApp, type);
        }

        public File[] buildExternalStorageAndroidDataDirs() {
            return buildPaths(mExternalDirsForApp, DIR_ANDROID, DIR_DATA);
        }

        public File[] buildExternalStorageAndroidObbDirs() {
            return buildPaths(mExternalDirsForApp, DIR_ANDROID, DIR_OBB);
        }

        public File[] buildExternalStorageAppDataDirs(String packageName) {
            return buildPaths(mExternalDirsForApp, DIR_ANDROID, DIR_DATA, packageName);
        }

        public File[] buildExternalStorageAppDataDirsForVold(String packageName) {
            return buildPaths(mExternalDirsForVold, DIR_ANDROID, DIR_DATA, packageName);
        }

        public File[] buildExternalStorageAppMediaDirs(String packageName) {
            return buildPaths(mExternalDirsForApp, DIR_ANDROID, DIR_MEDIA, packageName);
        }

        public File[] buildExternalStorageAppMediaDirsForVold(String packageName) {
            return buildPaths(mExternalDirsForVold, DIR_ANDROID, DIR_MEDIA, packageName);
        }

        public File[] buildExternalStorageAppObbDirs(String packageName) {
            return buildPaths(mExternalDirsForApp, DIR_ANDROID, DIR_OBB, packageName);
        }

        public File[] buildExternalStorageAppObbDirsForVold(String packageName) {
            return buildPaths(mExternalDirsForVold, DIR_ANDROID, DIR_OBB, packageName);
        }

        public File[] buildExternalStorageAppFilesDirs(String packageName) {
            return buildPaths(mExternalDirsForApp, DIR_ANDROID, DIR_DATA, packageName, DIR_FILES);
        }

        public File[] buildExternalStorageAppCacheDirs(String packageName) {
            return buildPaths(mExternalDirsForApp, DIR_ANDROID, DIR_DATA, packageName, DIR_CACHE);
        }

        /**
         * Append path segments to each given base path, returning result.
         */
        public static File[] buildPaths(File[] base, String... segments) {
            File[] result = new File[base.length];
            for (int i = 0; i < base.length; i++) {
                result[i] = buildPath(base[i], segments);
            }
            return result;
        }

        /**
         * Append path segments to given base path, returning result.
         */
        public static File buildPath(File base, String... segments) {
            File cur = base;
            for (String segment : segments) {
                if (cur == null) {
                    cur = new File(segment);
                } else {
                    cur = new File(cur, segment);
                }
            }
            return cur;
        }
    }
}
