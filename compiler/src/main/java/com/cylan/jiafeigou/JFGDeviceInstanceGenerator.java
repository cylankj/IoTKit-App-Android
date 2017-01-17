package com.cylan.jiafeigou;

import com.cylan.annotation.DPInterface;
import com.cylan.annotation.DPMessage;
import com.cylan.annotation.DPProperty;
import com.cylan.annotation.DPTarget;
import com.cylan.annotation.DPType;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;

/**
 * Created by yzd on 17-1-14.
 * <p>
 * 用来生成具体的Device
 */
public class JFGDeviceInstanceGenerator implements Generator {
    protected DPTarget mTarget;
    protected DPTarget mParentTarget;


    @Override
    public JavaFile generator(List<Element> fields) throws Exception {
        DPInterface childType = mTarget.getClass().getField(mTarget.name()).getAnnotation(DPInterface.class);
        String clazzName = childType.name();
        mParentTarget = childType.parent();
        DPInterface parentType = mParentTarget.getClass().getField(mParentTarget.name()).getAnnotation(DPInterface.class);
        String parentName = parentType.name();
        TypeSpec.Builder classBuilder = getClassBuilder(clazzName, parentName);
        classBuilder.addMethod(MethodSpec.methodBuilder("$")
                .addAnnotation(Override.class)
                .returns(ClassName.get("", clazzName))
                .addModifiers(Modifier.PUBLIC)
                .addCode("return ($L)super.$L();\n", clazzName, "$").build());
        if (fields != null) {
            DPType dpType;
            TypeName fieldType;
            DPMessage dpMessage;
            TypeMirror primaryType = null;
            for (Element property : fields) {
                dpMessage = property.getAnnotation(DPMessage.class);
                dpType = dpMessage.dpType();
                try {
                    dpMessage.primaryType();
                } catch (MirroredTypeException e) {
                    primaryType = e.getTypeMirror();
                }
                fieldType = getFieldType(dpType, primaryType);
                FieldSpec.Builder builder = FieldSpec.builder(fieldType, dpMessage.name(), Modifier.PUBLIC);
                builder.addAnnotation(AnnotationSpec.builder(DPProperty.class).
                        addMember("msgId", "$L", ((VariableElement) property).getConstantValue()).build()).build();
                classBuilder.addField(builder.build());
            }
        }
        return JavaFile.builder(MODULE_PACKAGE, classBuilder.build())
                .addStaticImport(ClassName.get(TYPE_DEFINE_PACKAGE, TYPE_DEFINE_NAME), "*")
                .addFileComment("$L", "APT自动生成的文件,请勿修改!!!!")
                .build();
    }

    @Override
    public void doProcessChain(ProcessChain chain) {


    }

    protected TypeSpec.Builder getClassBuilder(String clazzName, String parentName) {
        String pkgName = "DataPoint".equals(parentName) ? TYPE_DEFINE_PACKAGE : MODULE_PACKAGE;
        return TypeSpec.classBuilder(clazzName)
                .superclass(ClassName.get(pkgName, parentName))
                .addModifiers(Modifier.PUBLIC);
    }


    protected TypeName getFieldType(DPType type, TypeMirror primaryType) {
        TypeName result = null;
        switch (type) {
            case TYPE_FIELD:
                result = ClassName.get(primaryType);
                break;
            case TYPE_PRIMARY:
                result = ParameterizedTypeName.get(ClassName.get("", "DPPrimary"), TypeName.get(primaryType));
                break;
            case TYPE_SET:
                result = ParameterizedTypeName.get(ClassName.get("", "DPSet"), TypeName.get(primaryType));
                break;
        }
        return result;
    }

    @Override
    public void setTarget(DPTarget target) {
        mTarget = target;
    }
}
