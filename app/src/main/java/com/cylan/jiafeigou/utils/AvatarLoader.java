package com.cylan.jiafeigou.utils;

//public class AvatarLoader implements ModelLoader<AvatarRequest, InputStream> {
//    private final Context context;
//
//    public AvatarLoader(Context context) {
//        this.context = context;
//    }
//
//    @Override
//    public DataFetcher<InputStream> getResourceFetcher(AvatarRequest model, int width, int height) {
//        return new AvatarFetcher(context, model, width, height);
//    }
//
//    public static class Factory implements ModelLoaderFactory<AvatarRequest, InputStream> {
//
//        @Override
//        public ModelLoader<AvatarRequest, InputStream> build(Context context, GenericLoaderFactory factories) {
//            return new AvatarLoader(context);
//        }
//
//        @Override
//        public void teardown() {
//
//        }
//    }
//}