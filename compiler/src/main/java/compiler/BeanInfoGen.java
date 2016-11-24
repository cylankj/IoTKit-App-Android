package compiler;

import com.cylan.annotation.Device;
import com.cylan.annotation.DeviceBase;
import com.cylan.annotation.DpAnnotation;
import com.cylan.annotation.DpBase;
import com.cylan.annotation.ForDevice;
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

    private static Map<Device, String> deviceBeanMap = new HashMap<>();
    private static Map<Integer, String> idNameMap = new HashMap<>();
    private static TypeMirror dpTypeName = null;
    private static TypeMirror deviceTypeName = null;
    private static Map<Integer, String> idFieldNameMap = new HashMap<>();
    private static Device device;

    public static void go(Device device, ProcessingEnvironment processingEnv, RoundEnvironment roundEnv) {
        BeanInfoGen.device = device;
        collect(processingEnv, roundEnv);
    }

    private static void collect(ProcessingEnvironment processingEnv, RoundEnvironment roundEnv) {
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
        Map<Integer, TypeName> devClazzList = new HashMap<>();
        for (Element e : dpSet) {
            DpAnnotation test = e.getAnnotation(DpAnnotation.class);
            final String name = e.getSimpleName().toString();
            idNameMap.put(test.msgId(), e.getSimpleName().toString());
            TypeMirror clazzType = null;
            try {
                test.clazz();

            } catch (MirroredTypeException mte) {
                clazzType = mte.getTypeMirror();
            }
            TypeName typeName = ParameterizedTypeName.get(clazzType);
            if (devList.contains(name)) {
                devClazzList.put(test.msgId(), typeName);
            }
        }
        Set<? extends Element> dpBase = roundEnv.getElementsAnnotatedWith(DpBase.class);
        Set<? extends Element> deviceBase = roundEnv.getElementsAnnotatedWith(DeviceBase.class);
        int annotationCount = 0;

        for (Element e : dpBase) {
            dpTypeName = e.asType();
            annotationCount++;
            if (annotationCount != 1) {
                throw new RuntimeException("只能能注解一个类:DpBase--->" + dpTypeName);
            }
        }
        annotationCount = 0;
        for (Element e : deviceBase) {
            deviceTypeName = e.asType();
            annotationCount++;
            if (annotationCount != 1) {
                throw new RuntimeException("只能能注解一个类:DeviceBase--->" + deviceTypeName);
            }
        }


        String devBeanName = deviceBeanMap.get(device);
        TypeSpec.Builder typeSpec =
                TypeSpec.classBuilder(devBeanName)
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        addDevField(typeSpec, devClazzList);
        addDevMethod(devClazzList, typeSpec, roundEnv);
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

    private static TypeSpec.Builder addDevField(TypeSpec.Builder typeSpec, Map<Integer, TypeName> map) {
        FieldSpec.Builder baseDeviceFiled = FieldSpec.builder(TypeName.get(deviceTypeName),
                "deviceBase", Modifier.PUBLIC);
        typeSpec.addField(baseDeviceFiled.build());
        Iterator<Integer> iterator = map.keySet().iterator();
        while (iterator.hasNext()) {
            int id = iterator.next();
            TypeName typeName = map.get(id);
            String fieldName = handlerString(idNameMap.get(id));
            idFieldNameMap.put(id, fieldName);
            FieldSpec.Builder builder = FieldSpec.builder(typeName,
                    fieldName, Modifier.PUBLIC);
            typeSpec.addField(builder.build());
        }
        return typeSpec;
    }

    private static void addDevMethod(Map<Integer, TypeName> map, TypeSpec.Builder typeSpec, RoundEnvironment roundEnv) {
        MethodSpec.Builder builder0 = MethodSpec.methodBuilder("convert")
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.VOID)
                .addParameter(TypeName.get(deviceTypeName), "deviceBase")
                .addParameter(ParameterizedTypeName.get(ClassName.get(List.class),
                        TypeName.get(dpTypeName)), "listDp");
        addStatement(builder0, map);
        typeSpec.addMethod(builder0.build());
    }

    private static void addStatement(MethodSpec.Builder builder, Map<Integer, TypeName> map) {
        builder.addStatement("this.deviceBase = deviceBase");
        builder.addStatement("int count = listDp.size()");
        builder.beginControlFlow("for (int i = 0; i < count; i++)");
        builder.addStatement("DpMsgDefine.DpMsg dpMsg = listDp.get(i)");
        Iterator<Integer> iterator = map.keySet().iterator();
        while (iterator.hasNext()) {
            int keyId = iterator.next();
            String name = handlerString(idNameMap.get(keyId));
            builder.beginControlFlow("if(dpMsg.msgId==$L)", keyId);
            builder.addStatement("this.$L = ($T) dpMsg.o;", name, map.get(keyId));
            builder.endControlFlow();
        }
        builder.endControlFlow();
    }

    private static String handlerString(String string) {
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