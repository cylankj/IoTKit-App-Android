package com.cylan.jiafeigou.module

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.cache.DiskLruCacheFactory
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions
import com.cylan.jiafeigou.misc.JConstant
import java.io.File
import java.io.InputStream

/**
 * Created by yanzhendong on 2017/11/10.
 */
@GlideModule
class GlideModule : AppGlideModule() {

    override fun applyOptions(context: Context?, builder: GlideBuilder) {
        builder.setDefaultRequestOptions(RequestOptions.formatOf(DecodeFormat.PREFER_ARGB_8888))
        val downloadDirectoryPath = JConstant.ROOT_DIR + File.separator + "cache"
        val cacheSize = 100 * 1000 * 1000
        builder.setDiskCache(DiskLruCacheFactory(downloadDirectoryPath, cacheSize))
    }

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.prepend(String::class.java, InputStream::class.java, CustomModuleLoader.Factory())
    }
}