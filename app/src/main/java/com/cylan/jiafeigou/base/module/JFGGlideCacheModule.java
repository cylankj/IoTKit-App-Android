//package com.cylan.jiafeigou.base.module;
//
//import android.content.Context;
//
//import com.bumptech.glide.Glide;
//import com.bumptech.glide.GlideBuilder;
//import com.bumptech.glide.load.DecodeFormat;
//import com.bumptech.glide.load.engine.cache.DiskLruCacheFactory;
//import com.bumptech.glide.module.GlideModule;
//import com.cylan.jiafeigou.misc.JConstant;
//import com.cylan.jiafeigou.utils.AvatarLoader;
//import com.cylan.jiafeigou.utils.AvatarRequest;
//
//import java.io.File;
//import java.io.InputStream;
//
///**
// * Created by yanzhendong on 2017/5/27.
// */
//
//public class JFGGlideCacheModule implements GlideModule {
//    @Override
//    public void applyOptions(Context context, GlideBuilder builder) {
//        //设置图片的显示格式ARGB_8888(指图片大小为32bit)
//        builder.setDecodeFormat(DecodeFormat.PREFER_ARGB_8888);
//        //设置磁盘缓存目录（和创建的缓存目录相同）
//        String downloadDirectoryPath = JConstant.ROOT_DIR + File.separator + "cache";
//        //设置缓存的大小为100M
//        int cacheSize = 100 * 1000 * 1000;
//        builder.setDiskCache(new DiskLruCacheFactory(downloadDirectoryPath, cacheSize));
//    }
//
//    @Override
//    public void registerComponents(Context context, Glide glide) {
//        glide.register(AvatarRequest.class, InputStream.class, new AvatarLoader.Factory());
//    }
//}
