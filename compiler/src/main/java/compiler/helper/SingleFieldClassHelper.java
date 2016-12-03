package compiler.helper;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;

/**
 * Created by cylan-hunt on 16-12-2.
 */

public class SingleFieldClassHelper {
    public static void go(ProcessingEnvironment processingEnv, Class<?> clazz, String clazzName) {
        TypeSpec.Builder typeSpec =
                TypeSpec.classBuilder(clazzName)
                        .addAnnotation(ClassName.get("org.msgpack.annotation", "Message"))
                        .superclass(ClassName.get("com.cylan.jiafeigou.dp", "BaseDataPoint"))
                        .addModifiers(Modifier.PUBLIC);
        MethodSpec.Builder builder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(clazz, "value")
                .addStatement("this.value = value");
        FieldSpec.Builder fBuilder = FieldSpec.builder(clazz, "value")
                .addAnnotation(AnnotationSpec.builder(ClassName.get("org.msgpack.annotation", "Index"))
                        .addMember("value", "0").build())
                .addModifiers(Modifier.PUBLIC);
        typeSpec.addMethod(builder.build());
        typeSpec.addField(fBuilder.build());
        generate(typeSpec, processingEnv);
    }

    private static void generate(TypeSpec.Builder typeSpec, ProcessingEnvironment processingEnv) {
        JavaFile javaFile = JavaFile.builder("com.cylan.jiafeigou.n.mvp.model", typeSpec.build())
                .addFileComment("自动生成文件,请勿修改!!!")
                .build();
        try {
            javaFile.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
