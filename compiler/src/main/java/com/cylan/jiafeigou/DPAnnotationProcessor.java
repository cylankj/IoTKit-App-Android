package com.cylan.jiafeigou;

import com.cylan.annotation.DPInterface;
import com.cylan.annotation.DPMessage;
import com.cylan.annotation.DPTarget;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

/*
 *  @项目名：  APT 
 *  @包名：    com.compiler
 *  @文件名:   DPAnnotationProcessor
 *  @创建者:   yanzhendong
 *  @创建时间:  2017/1/13 22:03
 *  @描述：    TODO
 */
@AutoService(Processor.class)
public class DPAnnotationProcessor extends AbstractProcessor {
    private Map<DPTarget, List<Element>> mResultMap = new HashMap<>();
    private List<DPTarget> mRequiredTarget = new ArrayList<>();

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(DPMessage.class.getCanonicalName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "开始执行DP注解处理程序!!!!");
        resolve();
        collected(roundEnv);
        generator();
        repair();
        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "DP注解处理程序执行完毕!!!!");
        return true;
    }


    private void collected(RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(DPMessage.class);
        List<Element> prop;
        for (Element element : elements) {
            DPMessage message = element.getAnnotation(DPMessage.class);
            DPTarget[] target = message.target();

            for (DPTarget dpTarget : target) {
                prop = mResultMap.get(dpTarget);
                if (prop == null) {
                    prop = new ArrayList<>();
                    mResultMap.put(dpTarget, prop);
                }
                prop.add(element);
            }
        }
    }

    private void resolve() {
        for (DPTarget target : DPTarget.values()) {
            try {
                DPTarget parent = target.getClass().getField(target.name()).getAnnotation(DPInterface.class).parent();
                if (parent != DPTarget.DATAPOINT)
                    mRequiredTarget.add(parent);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
    }

    private void generator() {
        Generator generator;
        JavaFile javaFile;
        for (Map.Entry<DPTarget, List<Element>> entry : mResultMap.entrySet()) {
            generator = GeneratorFactory.getGeneratorInstance(entry.getKey());
            try {
                javaFile = generator.generator(entry.getValue());
                if (javaFile == null) continue;
                javaFile.writeTo(processingEnv.getFiler());
                mRequiredTarget.remove(entry.getKey());
            } catch (Exception e) {
//                e.printStackTrace();
            }
        }
    }


    private void repair() {
        mRequiredTarget.remove(DPTarget.DATAPOINT);
        for (DPTarget target : mRequiredTarget) {
            try {
                JavaFile javaFile = GeneratorFactory.getGeneratorInstance(target).generator(null);
                if (javaFile == null) continue;
                javaFile.writeTo(processingEnv.getFiler());
            } catch (Exception e) {
//                e.printStackTrace();
            }
        }
    }
}
