package com.cylan.jiafeigou.n.engine;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.model.GenericLoaderFactory;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.module.GlideModule;
import com.cylan.jiafeigou.n.mvp.model.MediaBean;

import java.io.InputStream;

/**
 * Created by yzd on 16-12-13.
 */

public class GlideWonderLoader implements GlideModule {
    @Override
    public void applyOptions(Context context, GlideBuilder builder) {

    }

    @Override
    public void registerComponents(Context context, Glide glide) {
//           glide.register(MediaBean.class, InputStream.class,);
    }

    public static class GlideWonderLoaderFactory implements ModelLoaderFactory<MediaBean,InputStream>{

        @Override
        public ModelLoader build(Context context, GenericLoaderFactory factories) {
            return null;
        }

        @Override
        public void teardown() {

        }
    }
}
