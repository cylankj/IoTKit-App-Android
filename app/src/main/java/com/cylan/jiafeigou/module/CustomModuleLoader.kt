package com.cylan.jiafeigou.module

import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.cylan.jiafeigou.support.log.AppLogger
import java.io.InputStream

/**
 * Created by yanzhendong on 2017/11/7.
 */
class CustomModuleLoader(private var loader: ModelLoader<GlideUrl, InputStream>) : ModelLoader<String, InputStream> {
    private val TAG = CustomModuleLoader::class.java.name
    override fun handles(model: String): Boolean {
        val accept = SchemeResolver.accept(model)
        AppLogger.w("CustomModuleLoader:is accept:$accept,for schema:$model")
        return accept;
    }

    override fun buildLoadData(model: String, width: Int, height: Int, options: Options?): ModelLoader.LoadData<InputStream>? {
        val glideUrl = SchemeResolver.build(model)
        return loader.buildLoadData(glideUrl, width, height, options)
    }

    class Factory : ModelLoaderFactory<String, InputStream> {
        override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<String, InputStream> {
            val loader = multiFactory.build(GlideUrl::class.java, InputStream::class.java)
            return CustomModuleLoader(loader)
        }

        override fun teardown() {
            // Do nothing.
        }
    }
}