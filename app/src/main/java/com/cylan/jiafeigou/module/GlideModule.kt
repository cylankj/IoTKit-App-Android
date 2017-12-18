package com.cylan.jiafeigou.module

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import java.io.InputStream

/**
 * Created by yanzhendong on 2017/11/10.
 */
@GlideModule
class GlideModule : AppGlideModule() {

    override fun applyOptions(context: Context?, builder: GlideBuilder) {
        //需要在清缓存时把 Glide 里的也删掉,所以放到内部了
//        val cache = File(Environment.getDataDirectory(), "cache")
////        val downloadDirectoryPath = JConstant.ROOT_DIR + File.separator + "cache"
//        val cacheSize = 100 * 1000 * 1000
//        builder.setDiskCache(DiskLruCacheFactory(cache.absolutePath, cacheSize))
    }

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.prepend(String::class.java, InputStream::class.java, CustomModuleLoader.Factory())
    }
}