package com.cylan.jiafeigou.utils;

import android.content.Context;

import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.GenericLoaderFactory;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;

import java.io.InputStream;


public class AvatarLoader implements ModelLoader<AvatarRequest, InputStream> {
    private final Context context;

    public AvatarLoader(Context context) {
        this.context = context;
    }

    @Override
    public DataFetcher<InputStream> getResourceFetcher(AvatarRequest model, int width, int height) {
        return new AvatarFetcher(context, model, width, height);
    }

    public static class Factory implements ModelLoaderFactory<AvatarRequest, InputStream> {

        @Override
        public ModelLoader<AvatarRequest, InputStream> build(Context context, GenericLoaderFactory factories) {
            return new AvatarLoader(context);
        }

        @Override
        public void teardown() {

        }
    }
}