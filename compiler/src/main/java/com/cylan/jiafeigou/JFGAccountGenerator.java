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
 * 用来生成JFGAccount
 */

public class JFGAccountGenerator extends JFGDeviceInstanceGenerator {
    @Override
    protected TypeSpec.Builder getClassBuilder(String clazzName, String parentName) {
        TypeSpec.Builder builder = super.getClassBuilder(clazzName, parentName);
        ClassName account = ClassName.get("com.cylan.entity.jniCall", "JFGAccount");
        ClassName string = ClassName.get(String.class);
        builder.addField(FieldSpec.builder(account, "mAccount", Modifier.PRIVATE).build())
                .addMethod(MethodSpec.methodBuilder("setAccount").addModifiers(Modifier.FINAL)
                        .addParameter(ParameterSpec.builder(account, "account").build())
                        .returns(ClassName.get(MODULE_PACKAGE, clazzName))
                        .addCode("$L", "" +
                                "this.mAccount = account;\n" +
                                "this.id = 888080;\n" +
                                "return this;\n").build())
                .addMethod(MethodSpec.methodBuilder("getPhone").addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .returns(string)
                        .addCode("$L", " return this.mAccount.getPhone();\n").build())
                .addMethod(MethodSpec.methodBuilder("getToken").addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .returns(string)
                        .addCode("$L", "return this.mAccount.getToken();\n").build())
                .addMethod(MethodSpec.methodBuilder("getAlias").addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .returns(string)
                        .addCode("$L", "return this.mAccount.getAlias();\n").build())
                .addMethod(MethodSpec.methodBuilder("isEnablePush").addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .returns(TypeName.BOOLEAN)
                        .addCode("$L", "return this.mAccount.isEnablePush();\n").build())
                .addMethod(MethodSpec.methodBuilder("isEnableSound").addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .returns(TypeName.BOOLEAN)
                        .addCode("$L", "return this.mAccount.isEnableSound();\n").build())
                .addMethod(MethodSpec.methodBuilder("getEmail").addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .returns(string)
                        .addCode("$L", "return this.mAccount.getEmail();\n").build())
                .addMethod(MethodSpec.methodBuilder("isEnableVibrate").addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .returns(TypeName.BOOLEAN)
                        .addCode("$L", "return this.mAccount.isEnableVibrate();\n").build())
                .addMethod(MethodSpec.methodBuilder("getPhotoUrl").addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .returns(string)
                        .addCode("$L", "return this.mAccount.getPhotoUrl();\n").build())
                .addMethod(MethodSpec.methodBuilder("getAccount").addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .returns(string).addCode("$L", "return this.mAccount.getAccount();\n").build());
        return builder;
    }
}
