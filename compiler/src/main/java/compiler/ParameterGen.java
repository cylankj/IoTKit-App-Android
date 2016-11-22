package compiler;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Modifier;

/**
 * Created by cylan-hunt on 16-11-16.
 */

public class ParameterGen {
    public static void go(ProcessingEnvironment processingEnv, RoundEnvironment roundEnv) {
        TypeSpec msgIdMap =
                TypeSpec.classBuilder("DpQueryHelper")
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .addMethod(createCamQueryMethod().build())
                        .build();
        JavaFile javaFile = JavaFile.builder("com.cylan.jiafeigou.dp", msgIdMap)
                .build();
        try {
            javaFile.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static MethodSpec.Builder createCamQueryMethod() {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("getCamQueryParameters");
//        builder.addModifiers(Modifier.PUBLIC)
//                .addModifiers(Modifier.STATIC)
//                .returns(DpParameters.Builder.class);

        return builder;
    }
}
