package compiler;

import com.cylan.annotation.Device;
import com.cylan.annotation.DeviceBase;
import com.cylan.annotation.DpAnnotation;
import com.cylan.annotation.DpBase;
import com.cylan.annotation.ForDevice;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;

/**
 * Created by cylan-hunt on 16-11-16.
 */

public class BeanInfoGen {

    private Device device;
    private List<String> fieldList = new ArrayList<>();
    private Map<Device, String> deviceBeanMap = new HashMap<>();
    private Map<Integer, String> idNameMap = new HashMap<>();
    private Map<Integer, TypeName> devClazzMap = new HashMap<>();
    private Map<String, TypeName> nameClassMap = new HashMap<>();
    private TypeMirror dpTypeName = null;
    private TypeMirror deviceTypeName = null;

    public void go(Device device, ProcessingEnvironment processingEnv, RoundEnvironment roundEnv) {
        reset();
        this.device = device;
        collect(processingEnv, roundEnv);
    }

    private void reset() {
        devClazzMap.clear();
        idNameMap.clear();
        devClazzMap.clear();
        fieldList.clear();
        nameClassMap.clear();
    }

    private void collect(ProcessingEnvironment processingEnv, RoundEnvironment roundEnv) {
        Set<? extends Element> deviceSet = roundEnv.getElementsAnnotatedWith(ForDevice.class);
        Set<? extends Element> dpSet = roundEnv.getElementsAnnotatedWith(DpAnnotation.class);
        List<String> devList = new ArrayList<>();
        for (Element e : deviceSet) {
            ForDevice forDevice = e.getAnnotation(ForDevice.class);
            List<Device> forDevicesType = Arrays.asList(forDevice.device());
            List<String> beanList = Arrays.asList(forDevice.targetBeanName());

            if (forDevicesType.contains(device)) {
                devList.add(e.getSimpleName().toString());
                int index = forDevicesType.indexOf(device);
                deviceBeanMap.put(device, beanList.get(index));
            }
        }
        for (Element e : dpSet) {
            DpAnnotation test = e.getAnnotation(DpAnnotation.class);
            final String name = (e.getSimpleName().toString());
            idNameMap.put(test.msgId(), name);
            TypeMirror clazzType = null;
            try {
                test.clazz();
            } catch (MirroredTypeException mte) {
                clazzType = mte.getTypeMirror();
            }
            TypeName typeName = ParameterizedTypeName.get(clazzType);
            if (devList.contains(name)) {
                devClazzMap.put(test.msgId(), typeName);
                nameClassMap.put(handlerString(name), typeName);
            }
        }
        Set<? extends Element> dpBase = roundEnv.getElementsAnnotatedWith(DpBase.class);
        Set<? extends Element> deviceBase = roundEnv.getElementsAnnotatedWith(DeviceBase.class);
        int annotationCount = 0;

        for (Element e : dpBase) {
            dpTypeName = e.asType();
            annotationCount++;
            if (annotationCount > 1) {
                throw new RuntimeException("只能能注解一个类:DpBase--->" + dpTypeName);
            }
        }
        annotationCount = 0;
        for (Element e : deviceBase) {
            deviceTypeName = e.asType();
            annotationCount++;
            if (annotationCount > 1) {
                throw new RuntimeException("只能能注解一个类:DeviceBase--->" + deviceTypeName);
            }
        }

        String devBeanName = deviceBeanMap.get(device);
        TypeSpec.Builder typeSpec =
                TypeSpec.classBuilder(devBeanName)
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .addSuperinterface(ClassName.get("android.os", "Parcelable"));
        addDevField(typeSpec);
        addDevMethod(typeSpec);
        addParcelableImplements(typeSpec);
        addGetByteMethod(typeSpec);
        addGetObjectMethod(typeSpec);
        generate(typeSpec, processingEnv);
    }

    private void addGetObjectMethod(TypeSpec.Builder typeSpec) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("getObject");
        builder.addModifiers(Modifier.PUBLIC)
                .addParameter(int.class, "id")
                .returns(Object.class);
        for (int i = 1; i < fieldList.size(); i++) {
            String fieldName = fieldList.get(i);
            Iterator<Integer> it = idNameMap.keySet().iterator();
            while (it.hasNext()) {
                int id = it.next();
                String name = handlerString(idNameMap.get(id));
                if (fieldName.equals(name)) {
                    builder.beginControlFlow("if($L == id) ", id);
                    builder.addStatement("return $L", name);
                    builder.endControlFlow();
                }
            }
        }
        builder.addStatement("return null");
        typeSpec.addMethod(builder.build());
    }

    /**
     * 生成 getByte()函数
     *
     * @param typeSpec
     */
    private void addGetByteMethod(TypeSpec.Builder typeSpec) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("getByte");
        builder.addModifiers(Modifier.PUBLIC)
                .addParameter(int.class, "id")
                .returns(ArrayTypeName.get(byte[].class));
        int getInfoCount = 0;

        for (int i = 1; i < fieldList.size(); i++) {
            String fieldName = fieldList.get(i);
            Iterator<Integer> it = idNameMap.keySet().iterator();
            while (it.hasNext()) {
                int id = it.next();
                String name = handlerString(idNameMap.get(id));
                TypeName typeName = devClazzMap.get(id);
                //处理一些简单的类{String boolean int float}
                //继承BaseDataPoint的类,直接toBytes();
                if (fieldName.equals(name)) {
                    if (handleSimpleObject(builder, name, id, typeName)) {
                        //简单类
                        getInfoCount++;
                        break;
                    } else if (handlerNormalType(builder, id, name)) {
                        //正常类
                        getInfoCount++;
                        break;
                    } else {
                        System.out.println("hunt: " + typeName + " ");
                    }
                }

            }
        }
        builder.addStatement("return null");
        typeSpec.addMethod(builder.build());

        System.out.println("hunt addGetMethod: " + device);
        System.out.println("hunt: " + device + " " + getInfoCount + " ");
        if (getInfoCount != fieldList.size() - 1) {
            throw new IllegalArgumentException("还没完全实现方法");
        }
    }

    private boolean handlerNormalType(MethodSpec.Builder builder, int id, String name) {
        builder.beginControlFlow("if($L == id) ", id);
        builder.addStatement("return $L.toBytes()", name);
        builder.endControlFlow();
        return true;
    }

    private boolean handleSimpleObject(MethodSpec.Builder builder, String name, int id, TypeName typeName) {
        if (typeName != null && (typeName.toString().equals("boolean")
                || typeName.toString().equals("int")
                || typeName.toString().equals("float")
                || typeName.toString().equals("java.lang.String"))) {
            builder.beginControlFlow("if($L == id) ", id);
            builder.addStatement("return com.cylan.jiafeigou.dp.DpUtils.pack($L)", name);
            builder.endControlFlow();
            return true;
        }
        return false;
    }

    private void generate(TypeSpec.Builder typeSpec, ProcessingEnvironment processingEnv) {
        JavaFile javaFile = JavaFile.builder("com.cylan.jiafeigou.n.mvp.model", typeSpec.build())
                .addFileComment("自动生成文件,请勿修改!!!")
                .build();
        try {
            javaFile.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private TypeSpec.Builder addDevField(TypeSpec.Builder typeSpec) {
        FieldSpec.Builder baseDeviceFiled = FieldSpec.builder(TypeName.get(deviceTypeName),
                "deviceBase", Modifier.PUBLIC);
        typeSpec.addField(baseDeviceFiled.build());
        fieldList.add("deviceBase");
        nameClassMap.put("deviceBase", TypeName.get(deviceTypeName));
        Iterator<Integer> iterator = devClazzMap.keySet().iterator();
        while (iterator.hasNext()) {
            int id = iterator.next();
            TypeName typeName = devClazzMap.get(id);
            String fieldName = handlerString(idNameMap.get(id));
            fieldList.add(fieldName);
            FieldSpec.Builder builder = FieldSpec.builder(typeName,
                    fieldName, Modifier.PUBLIC);
            typeSpec.addField(builder.build());
        }
        return typeSpec;
    }

    private void addParcelableImplements(TypeSpec.Builder typeBuilder) {
        //describeContents函数
        MethodSpec.Builder descBuilder = MethodSpec.methodBuilder("describeContents")
                .addAnnotation(AnnotationSpec.builder(Override.class).build())
                .addModifiers(Modifier.PUBLIC)
                .returns(int.class)
                .addStatement("return 0");
        typeBuilder.addMethod(descBuilder.build());
        //空构造函数
        MethodSpec.Builder emptyConstructorBuilder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC);
        typeBuilder.addMethod(emptyConstructorBuilder.build());
        //带Parcel构造函数
        addStatement2Constructor(typeBuilder);
        //CREATOR变量
        FieldSpec.Builder creatorField = FieldSpec.builder(ClassName.get("", "android.os.Parcelable.Creator"),
                "CREATOR", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL);
        creatorField.initializer("new Creator<$L>() {\n" +
                        "            @Override\n" +
                        "            public $L createFromParcel(android.os.Parcel source) {\n" +
                        "                return new $L(source);\n" +
                        "            }\n" +
                        "\n" +
                        "            @Override\n" +
                        "            public $L[] newArray(int size) {\n" +
                        "                return new $L[size];\n" +
                        "            }\n" +
                        "        }",
                deviceBeanMap.get(device),
                deviceBeanMap.get(device),
                deviceBeanMap.get(device),
                deviceBeanMap.get(device),
                deviceBeanMap.get(device));
        typeBuilder.addField(creatorField.build());
        //创建writeToParcel函数
        addStatement2writeToParcel(typeBuilder);
    }

    /**
     * 填充构造函数
     */
    private void addStatement2Constructor(TypeSpec.Builder typeBuilder) {
        MethodSpec.Builder withParamConstructor = MethodSpec.constructorBuilder()
                .addParameter(ClassName.get("", "android.os.Parcel"), "in")
                .addModifiers(Modifier.PUBLIC);
        int count = fieldList.size();
        for (int i = 0; i < count; i++) {
            String fieldName = fieldList.get(i);
            TypeName typeName = nameClassMap.get(fieldName);
            if (typeName == TypeName.BOOLEAN) {
                withParamConstructor.addStatement("this.$L = in.readByte() != 0", fieldName);
            }
            if (typeName.equals(TypeName.get(String.class))) {
                withParamConstructor.addStatement("this.$L = in.readString()", fieldName);
            }
            if (typeName == TypeName.INT) {
                withParamConstructor.addStatement("this.$L = in.readInt()", fieldName);
            }
            if (typeName.toString().contains("DpMsgDefine")) {
                withParamConstructor.addStatement("this.$L = in.readParcelable($L)", fieldName, typeName + ".class.getClassLoader()");
            }
        }
        typeBuilder.addMethod(withParamConstructor.build());
    }

    /**
     * 创建writeToParcel函数
     *
     * @param typeBuilder
     */
    private void addStatement2writeToParcel(TypeSpec.Builder typeBuilder) {
        MethodSpec.Builder writeToParcel = MethodSpec.methodBuilder("writeToParcel")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(ClassName.get("", "android.os.Parcel"), "dest")
                .addParameter(int.class, "flags");
        int count = fieldList.size();
        for (int i = 0; i < count; i++) {
            String fieldName = fieldList.get(i);
            TypeName typeName = nameClassMap.get(fieldName);
            if (typeName.equals(TypeName.BOOLEAN)) {
                writeToParcel.addStatement("dest.writeByte($L?(byte) 1 : (byte) 0)", "this." + fieldName);
            }
            if (typeName.equals(TypeName.get(String.class))) {
                writeToParcel.addStatement("dest.writeString($L)", "this." + fieldName);
            }
            if (typeName.equals(TypeName.INT)) {
                writeToParcel.addStatement("dest.writeInt($L)", "this." + fieldName);
            }
            if (typeName.toString().contains("DpMsgDefine")) {
                writeToParcel.addStatement("dest.writeParcelable(this.$L, $L)", fieldName, "flags");
            }
        }
        typeBuilder.addMethod(writeToParcel.build());
    }

    private void addDevMethod(TypeSpec.Builder typeSpec) {
        MethodSpec.Builder builder0 = MethodSpec.methodBuilder("convert")
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.VOID)
                .addParameter(TypeName.get(deviceTypeName), "deviceBase")
                .addParameter(ParameterizedTypeName.get(ClassName.get(List.class),
                        TypeName.get(dpTypeName)), "listDp");
        addStatement(builder0, devClazzMap);
        typeSpec.addMethod(builder0.build());
    }

    private void addStatement(MethodSpec.Builder builder, Map<Integer, TypeName> map) {
        builder.addStatement("this.deviceBase = deviceBase");
        builder.addStatement("int count = listDp == null ? 0 : listDp.size()");
        builder.beginControlFlow("for (int i = 0; i < count; i++)");
        builder.addStatement("DpMsgDefine.DpMsg dpMsg = listDp.get(i)");
        Iterator<Integer> iterator = map.keySet().iterator();
        while (iterator.hasNext()) {
            int keyId = iterator.next();
            String name = handlerString(idNameMap.get(keyId));
            builder.beginControlFlow("if(dpMsg.msgId==$L)", keyId);
            builder.addStatement("this.$L = ($T) dpMsg.o", name, map.get(keyId));
            builder.endControlFlow();
        }
        builder.endControlFlow();
    }

    private String handlerString(String string) {
        if (string.contains("_")) {
            String[] split = string.split("_");
            StringBuilder builder = new StringBuilder();
            builder.append(split[0].toLowerCase());
            for (int i = 1; i < split.length; i++) {
                String s = split[i].substring(0, 1).toUpperCase() + split[i].substring(1).toLowerCase();
                builder.append(s);
            }
            return builder.toString();
        }
        return string.toLowerCase();
    }
}