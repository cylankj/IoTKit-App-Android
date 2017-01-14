package com.cylan.jiafeigou;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;

/**
 * Created by yzd on 17-1-14.
 * <p>
 * 用来生成抽象的Device,即具体Device的父类
 */

public class JFGDeviceParentGenerator extends JFGDeviceInstanceGenerator {
    @Override
    protected TypeSpec.Builder getClassBuilder(String clazzName, String parentName) {
        TypeSpec.Builder builder = super.getClassBuilder(clazzName, parentName);
        TypeName string = ClassName.get(String.class);
        MethodSpec setDevice = MethodSpec.methodBuilder("setDevice").addModifiers(Modifier.FINAL)
                .returns(ClassName.get(MODULE_PACKAGE, clazzName))
                .addParameter(ParameterSpec.builder(
                        ClassName.get("com.cylan.entity.jniCall", "JFGDevice"), "device")
                        .addModifiers(Modifier.FINAL)
                        .build())
                .addCode("$L", "" +
                        "this.alias = device.alias;\n" +
                        "this.uuid = device.uuid;\n" +
                        "this.sn = device.sn;\n" +
                        "this.shareAccount = device.shareAccount;\n" +
                        "this.pid = device.pid;\n" +
                        "\n" +
                        "//因为JFGDevice也被当做DataPoint对待的,所以把JFGDevice的pid当做他的id,\n" +
                        "// 把最后对他的修改当做他的version,seq暂时无用\n" +
                        "this.id = device.pid;\n" +
                        "return this;\n"
                ).build();
        builder.addModifiers(Modifier.ABSTRACT)
                .addField(FieldSpec.builder(string, "uuid").addModifiers(Modifier.PUBLIC).build())
                .addField(FieldSpec.builder(string, "sn").addModifiers(Modifier.PUBLIC).build())
                .addField(FieldSpec.builder(string, "alias").addModifiers(Modifier.PUBLIC).build())
                .addField(FieldSpec.builder(string, "shareAccount").addModifiers(Modifier.PUBLIC).build())
                .addField(FieldSpec.builder(TypeName.INT, "pid").addModifiers(Modifier.PUBLIC).build())
                .addMethod(setDevice);

        return builder;
    }
}
