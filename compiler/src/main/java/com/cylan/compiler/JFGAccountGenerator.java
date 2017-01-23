package com.cylan.compiler;

import com.squareup.javapoet.ClassName;
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
        builder.addField(string, "phone", Modifier.PUBLIC)
                .addField(string, "token", Modifier.PUBLIC)
                .addField(string, "alias", Modifier.PUBLIC)
                .addField(TypeName.BOOLEAN, "enablePush", Modifier.PUBLIC)
                .addField(TypeName.BOOLEAN, "enableSound", Modifier.PUBLIC)
                .addField(string, "email", Modifier.PUBLIC)
                .addField(TypeName.BOOLEAN, "enableVibrate", Modifier.PUBLIC)
                .addField(string, "photoUrl", Modifier.PUBLIC)
                .addField(string, "account", Modifier.PUBLIC)
                .addMethod(MethodSpec.methodBuilder("setAccount").addModifiers(Modifier.FINAL)
                        .addParameter(ParameterSpec.builder(account, "account").build())
                        .returns(ClassName.get(MODULE_PACKAGE, clazzName))
                        .addCode("$L", "" +
                                "this.id = 888080;\n" +
                                "this.phone= account.getPhone();\n" +
                                "this.token=account.getToken();\n" +
                                "this.alias=account.getAlias();\n" +
                                "this.enablePush=account.isEnablePush();\n" +
                                "this.enableSound=account.isEnableSound();\n" +
                                "this.email=account.getEmail();\n" +
                                "this.enableVibrate=account.isEnableVibrate();\n" +
                                "this.photoUrl=account.getPhotoUrl();\n" +
                                "this.account=account.getAccount();\n" +
                                "return this;\n").build())
                .build();
        return builder;
    }
}
