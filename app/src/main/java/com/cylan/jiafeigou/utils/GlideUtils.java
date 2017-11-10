package com.cylan.jiafeigou.utils;

import com.bumptech.glide.load.model.GlideUrl;

import java.io.File;

/**
 * Created by yanzhendong on 2017/6/2.
 */

public class GlideUtils {
    public interface FilePathReady {
        void onFilePathReady(File file);
    }

    public static void fetchFile(GlideUrl url, FilePathReady file) {
//        Glide.with(ContextUtils.getContext())
//                .load(url)
//                .downloadOnly(new SimpleTarget<File>() {
//                    @Override
//                    public void onResourceReady(File resource, GlideAnimation<? super File> glideAnimation) {
//                        if (file != null) {
//                            file.onFilePathReady(resource);
//                        }
//                        ;
//                    }
//                });
        // TODO: 2017/11/10 GLIDE 

    }
}
