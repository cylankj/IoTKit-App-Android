package com.cylan.jiafeigou.base.module;

import android.text.TextUtils;

import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.http.Query;

/**
 * Created by yanzhendong on 2017/5/8.
 */

public class ImageFileConverterFactory extends Converter.Factory {
    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        Converter<ResponseBody, ?> converter = null;
        if (type == File.class) {
            String fileName = null;
            if (annotations != null) {
                for (Annotation annotation : annotations) {
                    if (annotation instanceof Query) {
                        Query query = (Query) annotation;
                        if (TextUtils.equals(query.value(), "fileName")
                                || TextUtils.equals(query.value(), "thumb")) {
                            fileName = query.value();
                            break;
                        }
                    }
                }
            }
            if (fileName == null) fileName = "default";
            converter = new ImageFileConverter(fileName);
        }
        return converter;
    }

    public static ImageFileConverterFactory create() {
        return new ImageFileConverterFactory();
    }


    public static class ImageFileConverter implements Converter<ResponseBody, File> {
        private String fileName;

        public ImageFileConverter(String fileName) {
            this.fileName = fileName;
        }

        @Override
        public File convert(ResponseBody value) throws IOException {
            File file = new File(JConstant.MEDIA_PATH, fileName);
            FileUtils.writeFile(file, value.byteStream());
            return file;
        }
    }
}
