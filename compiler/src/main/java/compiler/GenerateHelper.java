package compiler;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Modifier;

/**
 * Created by cylan-hunt on 16-11-16.
 */

public class GenerateHelper {

    public static void blewHelper(ProcessingEnvironment processingEnv, RoundEnvironment roundEnv) {
        TypeSpec msgIdMap =
                TypeSpec.classBuilder("DpHelper")
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .addMethod(createMethod().build())
                        .build();
        JavaFile javaFile = JavaFile.builder("com.cylan.jiafeigou.dp", msgIdMap)
                .build();
        try {
            javaFile.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static MethodSpec.Builder createMethod() {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("fillData");
        ParameterSpec.Builder p0 = ParameterSpec.builder(String.class, "vice");
        ParameterSpec.Builder p1 = ParameterSpec.builder(String.class, "device");
        builder.addParameter(p0.build());
        builder.addParameter(p1.build());
        builder.addModifiers(Modifier.PUBLIC)
                .addModifiers(Modifier.STATIC);
        return builder;
    }
}
